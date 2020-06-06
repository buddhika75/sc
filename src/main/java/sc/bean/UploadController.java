package sc.bean;

import sc.entity.Upload;
import sc.util.JsfUtil;
import sc.util.JsfUtil.PersistAction;
import sc.facade.UploadFacade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.Serializable;
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
import org.apache.commons.io.IOUtils;
import org.primefaces.model.UploadedFile;
import sc.entity.Item;
import sc.entity.Product;

@Named
@SessionScoped
public class UploadController implements Serializable {

    @EJB
    private sc.facade.UploadFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private ItemController itemController;

    private List<Upload> items = null;

    private List<Upload> productImages = null;

    private Upload selected;
    private UploadedFile file;
    private Product product;

    public UploadController() {
    }

    public Upload getSelected() {
        return selected;
    }

    public void setSelected(Upload selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    public String toListUploads() {
        String j = "select u from Upload u "
                + " where u.retired<>:ret";
        Map m = new HashMap();
        m.put("ret", true);
        items = getFacade().findByJpql(j, m);
        return "/upload/upload_list";
    }

    public String toUploadsNewSiteImage() {
        selected = new Upload();
        selected.setCreatedAt(new Date());
        selected.setCreater(webUserController.getLoggedUser());
        Item imageType = itemController.findItemByCode("site_image");
        selected.setImageType(imageType);
        return "/upload/upload";
    }

    public String toUploadsNewProductImage() {
        selected = new Upload();
        selected.setCreatedAt(new Date());
        selected.setCreater(webUserController.getLoggedUser());
        selected.setProduct(getProduct());
        Item imageType = itemController.findItemByCode("product_image");
        selected.setImageType(imageType);
        return "/product/upload";
    }

    private UploadFacade getFacade() {
        return ejbFacade;
    }

    public String saveAndUploadProductImage() {
        saveAndUpload();
        return "/product/select";
    }

    public String saveAndUploadSiteImage() {
        saveAndUpload();
        return "/upload/upload_list";
    }

    public String saveAndUpload() {
        if(getSelected()==null) {
            return "";
        }
        if (getSelected().getId() == null) {
            getFacade().create(getSelected());
        } else {
            getFacade().edit(getSelected());
        }
        if (selected.getImageType() == null) {
            Item imageType = itemController.findItemByCode("site_image");
            selected.setImageType(imageType);
        }
        InputStream in;
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an image");
            return "";
        }
        if (selected.getImageType() == null) {
            Item imageType = itemController.findItemByCode("site_image");
            selected.setImageType(imageType);
        }
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Please select an Upload");
            return "";
        }
        if (getSelected().getId() == null) {
            getFacade().create(getSelected());
        } else {
            getFacade().edit(getSelected());
        }

        try {
            in = getFile().getInputstream();
            File f = new File(getSelected().getId().toString() + Math.rint(100) + "");
            FileOutputStream out = new FileOutputStream(f);

            //            OutputStream out = new FileOutputStream(new File(fileName));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            getSelected().setRetireComments(f.getAbsolutePath());
            getSelected().setFileName(file.getFileName());
            getSelected().setFileType(file.getContentType());
            in = file.getInputstream();
            getSelected().setBaImage(IOUtils.toByteArray(in));
            if (getSelected().getId() == null) {
                getFacade().create(getSelected());
            } else {
                getFacade().edit(getSelected());
            }
            return toUploadsNewSiteImage();
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
            return "";
        }

    }

    public Upload prepareCreate() {
        selected = new Upload();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("UploadCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("UploadUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("UploadDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Upload> getItems() {
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

    public Upload getUpload(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Upload> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Upload> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public sc.facade.UploadFacade getEjbFacade() {
        return ejbFacade;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        productImages = null;
    }

    public List<Upload> getProductImages() {
        if (productImages == null) {
            productImages = fillImages(getProduct());
        }
        return productImages;
    }

    public void setProductImages(List<Upload> productImages) {
        this.productImages = productImages;
    }

    private List<Upload> fillImages(Product product) {
        String j = "select u from Upload u "
                + " where u.retired<>:ret "
                + " and u.product=:p ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("p", product);
        return getFacade().findByJpql(j, m);

    }

    @FacesConverter(forClass = Upload.class)
    public static class UploadControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UploadController controller = (UploadController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "uploadController");
            return controller.getUpload(getKey(value));
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
            if (object instanceof Upload) {
                Upload o = (Upload) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Upload.class.getName()});
                return null;
            }
        }

    }

}
