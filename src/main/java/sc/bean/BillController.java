package sc.bean;

import sc.entity.Bill;
import sc.util.JsfUtil;
import sc.util.JsfUtil.PersistAction;
import sc.facade.BillFacade;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import sc.entity.Item;
import sc.facade.BillItemFacade;

@Named
@SessionScoped
public class BillController implements Serializable {

    @EJB
    private BillFacade facade;
    @EJB
    private BillItemFacade billItemFacade;
    
    
    @Inject
    private WebUserController webUserController;
    @Inject private ItemController itemController;
    
    
    private List<Bill> items = null;
    private Bill selected;
    private Bill shoppingCart;
    private Bill favouriteCart;
    
    public Bill createNewShoppingCart(){
        
        Item billCategory = itemController.findItemByCode("shopping_cart",true);
        Item billType = itemController.findItemByCode("billed",true);
        
        Bill sc = new Bill();
        sc.setCreatedAt(new Date());
        sc.setCreatedBy(webUserController.getCurrent());
        sc.setCreatedInstitution(webUserController.getInstitution());
        sc.setBillCategory(billCategory);
        sc.setBillType(billType);
        getFacade().create(sc);
        return sc;
    }
    
    public Bill createNewFavouriteCart(){
        
        Item billCategory = itemController.findItemByCode("favourite_cart",true);
        Item billType = itemController.findItemByCode("billed",true);
        
        Bill sc = new Bill();
        sc.setCreatedAt(new Date());
        sc.setCreatedBy(webUserController.getCurrent());
        sc.setCreatedInstitution(webUserController.getInstitution());
        sc.setBillCategory(billCategory);
        sc.setBillType(billType);
        getFacade().create(sc);
        return sc;
    }

    public BillController() {
    }
    
    

    public Bill getSelected() {
        return selected;
    }

    public void setSelected(Bill selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private BillFacade getFacade() {
        return facade;
    }

    public Bill prepareCreate() {
        selected = new Bill();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("BillCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("BillUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("BillDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Bill> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
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

    public Bill getBill(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Bill> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Bill> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public Bill getShoppingCart() {
        if(shoppingCart==null){
            shoppingCart = createNewShoppingCart();
        }
        return shoppingCart;
    }

    public void setShoppingCart(Bill shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public Bill getFavouriteCart() {
        if(favouriteCart==null){
            
            favouriteCart = createNewFavouriteCart();
        }
        return favouriteCart;
    }

    public void setFavouriteCart(Bill favouriteCart) {
        this.favouriteCart = favouriteCart;
    }
    
    
    

    @FacesConverter(forClass = Bill.class)
    public static class BillControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BillController controller = (BillController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "billController");
            return controller.getBill(getKey(value));
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
            if (object instanceof Bill) {
                Bill o = (Bill) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Bill.class.getName()});
                return null;
            }
        }

    }

}
