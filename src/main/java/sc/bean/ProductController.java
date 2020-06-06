package sc.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import sc.entity.Product;
import sc.util.JsfUtil;
import sc.util.JsfUtil.PersistAction;
import sc.facade.ProductFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import sc.entity.Institution;
import sc.entity.Item;
import sc.entity.Person;
import sc.entity.SiComponentItem;

import sc.enums.EncounterType;

import sc.enums.RenderType;
import sc.facade.ComponentFacade;

import sc.pojcs.YearMonthDay;
import java.io.ByteArrayInputStream;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
// </editor-fold>

@Named
@SessionScoped
public class ProductController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private sc.facade.ProductFacade ejbFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    ApplicationController applicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private ItemController itemController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private CommonController commonController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private List<Product> items = null;
    private List<Product> relatedProducts = null;
    private List<Product> selectedProducts = null;
    private Product featuredProduct;
    private Product selected;
    private Long idFrom;
    private Long idTo;
    private Institution institution;
    private String searchingId;
    private Item item;
    private String searchingName;
    private Date from;
    private Date to;
    private Item department;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public ProductController() {
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigation">

    public String toSelectProduct() {
        return "/product/select";
    }

    public String toDepartmentProducts() {
        if (department == null) {
            return "";
        }
        items = listProductsOfDepartment(department);
        if (items == null || items.isEmpty()) {
            return "searched_products_empty";
        }
        return "searched_products";
    }

    public String toSelectProductPublic() {
        return "/products";
    }

    public String toListAllProducts() {
        String j = "select s from Product s "
                + " where s.retired=:ret "
                + " order by s.name";
        Map m = new HashMap();
        m.put("ret", false);
        selectedProducts = getFacade().findByJpql(j, m);
        return "/product/select";
    }

    public String toSearchProducts() {
        selectedProducts = null;
        selected = null;
        return "/product/search_by_name";
    }

    public String toListAllProductsPublic() {
        String j = "select s from Product s "
                + " where s.retired=:ret "
                + " order by s.name";
        Map m = new HashMap();
        m.put("ret", false);
        selectedProducts = getFacade().findByJpql(j, m);
        return "/products";
    }

    public String toEditProduct() {
        return "/product/product";
    }

    public String toProductProfile() {
        return "/product/profile";
    }

    public String toProductProfilePublic() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return "";
        }
        selected.setViewCount(selected.getViewCount() + 1);
        getFacade().edit(selected);
        return "/product";
    }

    public String toAddNewProduct() {
        selected = new Product();
        return "/product/product";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public List<Product> completeProduct(String qry) {
        List<Product> cs = new ArrayList<>();
        if (qry == null) {
            return cs;
        }
        String j = "select c from Product c "
                + " where c.retired=:ret "
                + " and (lower(c.name) like :qry or lower(c.sname) like :qry) "
                + " order by c.name";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("qry", "%" + qry.trim().toLowerCase() + "%");
        cs = getFacade().findByJpql(j, m);
        return cs;
    }

    public String retireSelected() {
        Product c = selected;
        if (c != null) {
            c.setRetired(true);
            c.setRetiredBy(webUserController.getLoggedUser());
            c.setRetiredAt(new Date());
            getFacade().edit(c);
        }
        JsfUtil.addSuccessMessage("Removed");
        return toListAllProducts();
    }

    public String searchByNamePublic() {
        System.out.println("searchByNamePublic");
        System.out.println("searchingName = " + searchingName);
        selectedProducts = listProductsByName(searchingName);
        System.out.println("selectedProducts = " + selectedProducts.size());
        if (selectedProducts == null || selectedProducts.isEmpty()) {
            selected = null;
            return "/searched_products_empty";
        }
        if (selectedProducts.size() == 1) {
            selected = selectedProducts.get(0);
            return "/product";
        }
        selected = null;
        return "/searched_products";
    }

    public void clearSearchItems() {
        searchingName = "";
    }

    public String searchByName() {
        selectedProducts = listProductsByName(searchingName);
        if (selectedProducts == null || selectedProducts.isEmpty()) {
            JsfUtil.addErrorMessage("No Results Found. Try different search criteria.");
            return "";
        }
        if (selectedProducts.size() == 1) {
            selected = selectedProducts.get(0);
            selectedProducts = null;
            clearSearchByName();
            return toProductProfile();
        } else {
            selected = null;
            clearSearchByName();
            return toSelectProduct();
        }
    }

    public void clearSearchByName() {
        searchingId = "";
        searchingName = "";
    }

    public Product prepareCreate() {
        selected = new Product();
        return selected;
    }

    public String saveProduct() {

        saveProduct(selected);
        JsfUtil.addSuccessMessage("Saved.");
        applicationController.fillCategoryData();
        selected.setSiComponentItems(null);
        selected = getFacade().find(selected.getId());
        return toProductProfile();
    }

    public void saveProductSilantly() {
        saveProduct(selected);
    }

    public String saveProduct(Product c) {
        if (c == null) {
            JsfUtil.addErrorMessage("No Product Selected to save.");
            return "";
        }
        if (c.getId() == null) {
            c.setCreatedBy(webUserController.getLoggedUser());
            c.setCreatedAt(new Date());
            getFacade().create(c);
        } else {
            c.setLastEditBy(webUserController.getLoggedUser());
            c.setLastEditeAt(new Date());
            getFacade().edit(c);
        }
        return toProductProfile();
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ClientUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ClientDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public String getSearchingId() {
        return searchingId;
    }

    public void setSearchingId(String searchingId) {
        this.searchingId = searchingId;
    }

    public String getSearchingName() {
        return searchingName;
    }

    public void setSearchingName(String searchingName) {
        this.searchingName = searchingName;
    }

    public ProductFacade getEjbFacade() {
        return ejbFacade;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public Product getSelected() {
        return selected;
    }

    public void setSelected(Product selected) {
        this.selected = selected;
    }

    private ProductFacade getFacade() {
        return ejbFacade;
    }

    public List<Product> getItems() {
        return items;
    }

    public List<Product> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
    }

    public List<Product> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Product> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Product> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Long getIdTo() {
        return idTo;
    }

    public void setIdTo(Long idTo) {
        this.idTo = idTo;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public void setInstitutionController(InstitutionController institutionController) {
        this.institutionController = institutionController;
    }

    public Date getFrom() {
        if (from == null) {
            from = commonController.startOfTheDay();
        }
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        if (to == null) {
            to = commonController.endOfTheDay();
        }
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Product> getFeaturedProducts() {
        return applicationController.getFeaturedProducts();
    }

    public Product getFeaturedProduct() {
        if (getFeaturedProducts().isEmpty()) {
            return null;
        }
        Random rand = new Random();
        featuredProduct = getFeaturedProducts().get(rand.nextInt(getFeaturedProducts().size()));
        return featuredProduct;
    }

    public void setFeaturedProduct(Product featuredProduct) {
        this.featuredProduct = featuredProduct;
    }

    public List<Product> listAllProducts() {
        String j;
        j = "select s from Product s "
                + " where s.retired<>:ret "
                + " order by s.name";
        Map m = new HashMap();
        m.put("ret", true);
        return getFacade().findByJpql(j, m);
    }

    public Product getProduct(Object id) {
        return getFacade().find(id);
    }

    private List<Product> listProductsByName(String searchingName) {
        List<Product> lst;
        String j = "Select p from Product p "
                + " where p.retired<>:ret "
                + " and lower(p.name) like :q "
                + " order by p.name";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("q", "%" + searchingName.toLowerCase() + "%");
        lst = getFacade().findByJpql(j, m);
        if (lst == null) {
            lst = new ArrayList<>();
        }
        return lst;
    }

    private List<Product> listProductsOfDepartment(Item dept) {
        List<Product> lst;
        String j = "Select p from Product p "
                + " where p.retired<>:ret "
                + " and p.department = :q "
                + " order by p.name";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("q", dept);
        lst = getFacade().findByJpql(j, m);
        if (lst == null) {
            lst = new ArrayList<>();
        }
        return lst;
    }

    public Item getDepartment() {
        return department;
    }

    public void setDepartment(Item department) {
        this.department = department;
    }

    public List<Product> getRelatedProducts() {
        if (selected != null && selected.getDepartment() != null) {
            relatedProducts = listProductsOfDepartment(selected.getDepartment());
        }else{
            relatedProducts = new ArrayList<>();
        }
        return relatedProducts;
    }

    public void setRelatedProducts(List<Product> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Inner Classes">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Product.class)
    public static class productControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ProductController controller = (ProductController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "productController");
            return controller.getProduct(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Product) {
                Product o = (Product) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Product.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
