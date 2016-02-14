package view;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import ljbm.modelo.Modelo;


@Named
@Stateful
@ConversationScoped
public class ModeloBean implements Serializable {
	private static final Logger LOG = Logger.getLogger(ModeloBean.class); 
	private static final long serialVersionUID = -2371629251349891646L;

    // ======================================
    // =             Attributes             =
    // ======================================

    private Long id;
    private Modelo modelo;

    // Support searching Modelo entities with pagination
    private int page;
    private long count;
    private List<Modelo> pageItems;
    private Modelo example = new Modelo();

    // Support adding children to bidirectional, one-to-many tables
    private Modelo add = new Modelo();

    @Inject
    private Conversation conversation;

    private Client client = ClientBuilder.newClient();
    private WebTarget target;
 
    // ======================================
    // =         Lifecycle Methods          =
    // ======================================

    @PostConstruct
    private void setWebTarget() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		String restEndointURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
//				+ request.getContextPath()
				+ "/lab"
				+ "/modelos/";
		target = client.target(restEndointURL);
    }
 
    // ======================================
    // =          Business Methods          =
    // ======================================

    public String create() {

        conversation.begin();
        return "create?faces-redirect=true";
    }

    public void retrieve() {

        if (FacesContext.getCurrentInstance().isPostback()) {
            return;
        }

        if (conversation.isTransient()) {
            conversation.begin();
        }

        if (id == null) {
            modelo = example;
        } else {
            modelo = findById(getId());
        }
    }

    public Modelo findById(Long id) {
 
    	
    	Response response = target.path("{id}").resolveTemplate("id", id).request(MediaType.APPLICATION_XML).get();
    	if (response.getStatus() != 200) {
    		// TODO sinalizar erro
//    		return new ArrayList<Modelo>(0);
    	}    	
    	String modelo = response.readEntity(String.class);
		response.close();
    	LOG.info(modelo);
    	JAXBContext context;
        StringReader writer = new StringReader(modelo);
        try {
			context = JAXBContext.newInstance(Modelo.class);
	        Unmarshaller m = context.createUnmarshaller();
//	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        return (Modelo) m.unmarshal(writer);    	
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
 
    public String update() {
        conversation.end();
 
        try {
            if (id == null) {
                target.request().post(Entity.entity(modelo, MediaType.APPLICATION_XML));
                return "search?faces-redirect=true";
            } else {
                target.request().put(Entity.entity(modelo, MediaType.APPLICATION_XML));
                return "view?faces-redirect=true&id=" + modelo.getNumero();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
            return null;
        }
    }
 
    public String delete() {
        conversation.end();
 
        try {
 
            target.path("{id}").resolveTemplate("id", modelo.getNumero()).request(MediaType.APPLICATION_XML).delete();
 
            return "search?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
            return null;
        }
    }
    
    public void search() {
        page = 0;
    }

    public String paginate() {

//        CriteriaBuilder builder = em.getCriteriaBuilder();
//
//        // Populate count
        count = 14;//TODO target.path("count").path("{example}").resolveTemplate("example", example).request().get(Long.class);
//
//        // Populate pageItems
//
//        CriteriaQuery<Book> criteria = builder.createQuery(Book.class);
//        Root<Book> root = criteria.from(Book.class);
//        TypedQuery<Book> query = em.createQuery(criteria.select(root).where(getSearchPredicates(root)));
//        query.setFirstResult(page * getPageSize()).setMaxResults(getPageSize());
//        pageItems = query.getResultList();
        pageItems = getAll();
        return null;
    }
    
    public List<Modelo> getAll() {
    	
    	Response response = target.request(MediaType.APPLICATION_XML).get();
//    	Response response = target.request(MediaType.APPLICATION_JSON).get();
    	if (response.getStatus() != 200) {
    		// TODO sinalizar erro
    		return new ArrayList<Modelo>(0);
    	}    	
    	List<Modelo> modelos = response.readEntity(new GenericType<List<Modelo>>() {});
    	LOG.info(modelos);
		response.close(); 
        return modelos;
    }

    
    // ======================================
    // =          Getters & Setters         =
    // ======================================

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Modelo getModelo() {
        return this.modelo;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return 10;
    }

    public Modelo getExample() {
        return this.example;
    }

    public void setExample(Modelo example) {
        this.example = example;
    }

    public List<Modelo> getPageItems() {
        return this.pageItems;
    }

    public long getCount() {
        return this.count;
    }

    public Modelo getAdd() {
        return this.add;
    }

    public Modelo getAdded() {
        Modelo added = this.add;
        this.add = new Modelo();
        return added;
    }
    
    
}