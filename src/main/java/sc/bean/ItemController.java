package sc.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import sc.util.JsfUtil;
import sc.util.JsfUtil.PersistAction;
import sc.facade.ItemFacade;
import org.apache.commons.lang.WordUtils;

import java.io.Serializable;
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
import javax.persistence.Transient;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import sc.entity.DesignComponentFormSet;
import sc.entity.Item;
import sc.enums.ItemType;
import java.util.ArrayList;
import org.primefaces.model.UploadedFile;
import sc.enums.SelectionDataType;

@Named("itemController")
@SessionScoped
public class ItemController implements Serializable {

    @EJB
    private sc.facade.ItemFacade ejbFacade;

    @Inject
    private WebUserController webUserController;

    private List<Item> items = null;
    private Item selected;
    private Item selectedParent;
    private List<Item> titles;
    private List<Item> ethinicities;
    private List<Item> religions;
    private List<Item> sexes;
    private List<Item> marietalStatus;
    private List<Item> educationalStatus;
    private List<Item> citizenships;
    private List<Item> mimeTypes;
    private List<Item> categories;
    private UploadedFile file;

    private int itemNameColumnNumber;
    private int itemCodeColumnNumber;
    private int parentCodeColumnNumber;
    private int startRow = 1;

    public ItemController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Navigation">
    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void fillDuplicateItemsInAFormSet(DesignComponentFormSet s) {
        String j = "select di.item from DesignComponentFormItem di "
                + "  where di.retired=false "
                + "  and di.parentComponent.parentComponent=:s "
                + "  group by di.item "
                + " having count(*)>1 "
                + "  ";
        Map m = new HashMap();
        m.put("s", s);
        items = getFacade().findByJpql(j, m);
    }

    public void makeAsSelectedParent(Item pi) {
        selectedParent = pi;
    }

    public void makeAsSelectedParentByCode(String strPi) {
        Item pi = findItemByCode(strPi);
        selectedParent = pi;
    }

    public List<String> completeItemCodes(String qry) {
        String j = "select i.code from Item i "
                + " where lower(i.code) like :q "
                + "  and i.retired=false "
                + " order by i.code";
        Map m = new HashMap();
        m.put("q", "%" + qry.trim().toLowerCase() + "%");
        List<String> ss = getFacade().findString(j, m);
        return ss;
    }

    public List<String> listAllItems() {
        String j = "select i from Item i "
                + " where i.retired<>:ret "
                + " order by i.code";
        Map m = new HashMap();
        m.put("ret", true);
        List<String> ss = getFacade().findByJpql(j, m);
        return ss;
    }

    public String importItemsFromExcel() {
        try {
            String strParentCode;
            String strItemName;
            String strItemCode;

            Item parent = null;

            File inputWorkbook;
            Workbook w;
            Cell cell;
            InputStream in;

            sc.facade.util.JsfUtil.addSuccessMessage(file.getFileName());

            try {
                sc.facade.util.JsfUtil.addSuccessMessage(file.getFileName());
                in = file.getInputstream();
                File f;
                f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
                FileOutputStream out = new FileOutputStream(f);
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                in.close();
                out.flush();
                out.close();

                inputWorkbook = new File(f.getAbsolutePath());

                sc.facade.util.JsfUtil.addSuccessMessage("Excel File Opened");
                w = Workbook.getWorkbook(inputWorkbook);
                Sheet sheet = w.getSheet(0);

                for (int i = startRow; i < sheet.getRows(); i++) {

                    Map m = new HashMap();

                    cell = sheet.getCell(parentCodeColumnNumber, i);
                    strParentCode = cell.getContents();

                    parent = findItemByCode(strParentCode);

                    cell = sheet.getCell(itemNameColumnNumber, i);
                    strItemName = cell.getContents();

                    cell = sheet.getCell(itemCodeColumnNumber, i);
                    strItemCode = cell.getContents();
                    strItemCode = strItemCode.trim().toLowerCase().replaceAll(" ", "_");

                    Item item = createItem(parent, strItemName, strItemCode, i);

                    getFacade().edit(item);

                }

                sc.facade.util.JsfUtil.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
                return "";
            } catch (IOException ex) {
                sc.facade.util.JsfUtil.addErrorMessage(ex.getMessage());
                return "";
            } catch (BiffException e) {
                sc.facade.util.JsfUtil.addErrorMessage(e.getMessage());
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public List<Item> completeDictionaryItems(String qry) {
        return findItemList(null, ItemType.Dictionary_Item, qry);
    }

    public List<Item> completeProductData(String qry) {
        return findItemList("product_data", null, qry);
    }

    public List<Item> completeItems(String qry) {
        return findItemList(null, null, qry);
    }

    public List<Item> completeItemsofParent(String qry) {
        //System.out.println("completeItemsofParent");
        //System.out.println("qry = " + qry);
        return findChildrenAndGrandchildrenItemList(selectedParent, qry);
    }

    public List<Item> completeItemsofParentWithFIlter(String qry) {
        FacesContext context = FacesContext.getCurrentInstance();
        String o = (String) UIComponent.getCurrentComponent(context).getAttributes().get("filter");
        Item ti = findItemByCode(o);
        return findChildrenAndGrandchildrenItemList(ti, qry);
    }

    public void generateDisplayNames() {
        List<Item> tis = getFacade().findAll();
        for (Item i : tis) {
            if (i.getDisplayName() == null || i.getDisplayName().trim().equals("")) {
                i.setDisplayName(i.getName());
                getFacade().edit(i);
            }
        }

    }

    public Item createItem(Item parent, String name, String code, int orderNo) {
        Item item;
        Map m = new HashMap();
        String j = "select i from Item i "
                + " where i.retired=false ";
        if (parent != null) {
            j += " and i.parent.code=:p ";
            m.put("p", parent.getCode());
        }
        j += " and i.name=:name "
                + " and i.code=:code "
                + " order by i.id";

        m.put("name", name);
        m.put("code", code);
        item = getFacade().findFirstByJpql(j, m);
        if (item == null) {
            item = new Item();
            item.setName(name);
            item.setDisplayName(name);
            item.setCode(code.trim().toLowerCase());
            item.setParent(parent);
            item.setOrderNo(orderNo);
            item.setCreatedAt(new Date());
            item.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(item);
        }
        return item;
    }

    public void addDiplayAndCode() {
        addDisplayForItem();
        addCodeForItem();
    }

    public void addDisplayForItem() {
        if (selected == null) {
            return;
        }
        if (selected.getName() == null) {
            return;
        }

        if (selected.getDisplayName() != null && !selected.getDisplayName().trim().equals("")) {
            return;
        }
        String before = selected.getName().trim();
        String after = before.trim().replaceAll(" +", " ");
        selected.setDisplayName(after);
    }

    public void addCodeForItem() {
        if (selected == null) {
            return;
        }

        if (selected.getName() == null) {
            return;
        }

        if (selected.getCode() != null && !selected.getCode().trim().equals("")) {
            return;
        }

        String before = selected.getName().trim();
        String after = before.trim().replaceAll(" +", " ");
        after = after.replaceAll(" +", "_");
        after = after.toLowerCase();

        selected.setCode(after);
    }

    public Item findItemByCode(String code) {
        Item item;
        String j;
        Map m = new HashMap();
        if (code != null) {
            j = "select i from Item i "
                    + " where i.retired=false "
                    + " and lower(i.code)=:code "
                    + " order by i.id";
            m = new HashMap();
            m.put("code", code.trim().toLowerCase());
            // //System.out.println("m = " + m);
            // //System.out.println("j = " + j);
            item = getFacade().findFirstByJpql(j, m);
        } else {
            item = null;
        }
        return item;
    }

    public Item findItemByCode(String code, boolean createNew) {
        Item item;
        String j;
        Map m ;
        if (code != null) {
            return null;
        }
        j = "select i from Item i "
                + " where i.retired<>:ret "
                + " and lower(i.code)=:code "
                + " order by i.id";
        m = new HashMap();
        m.put("code", code.trim().toLowerCase());
         m.put("ret", true);
        item = getFacade().findFirstByJpql(j, m);

        if(item==null && createNew){
            item = new Item();
            item.setName(code);
            item.setCode(code);
            item.setDisplayName(code);
            item.setCreatedAt(new Date());
            getFacade().create(item);
        }
        
        return item;
    }

    // </editor-fold>    
    public Item getSelected() {
        return selected;
    }

    public void setSelected(Item selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ItemFacade getFacade() {
        return ejbFacade;
    }

    public Item prepareCreate() {
        selected = new Item();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("ItemCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("ItemUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("ItemDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Item> getItems() {
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

    public String findItemNameByCode(String q) {
        Item i;
        String j = "select i from Item i "
                + " where i.retired<>:ret "
                + " and lower(i.code)=:code "
                + " order by i.id desc";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("code", q.trim().toLowerCase());
        i = getFacade().findFirstByJpql(j, m);
        if (i == null) {
            i = new Item();
            i.setName(q);
            i.setCode(q.trim().toLowerCase());
            i.setDataType(SelectionDataType.Short_Text);
            i.setCreatedAt(new Date());
            getFacade().create(i);
        }
        return i.getName();
    }

    public Item getItem(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Item> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Item> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public List<Item> getTitles() {
        if (titles == null) {
            String j = "select t from Item t where t.retired=false and t.parent.parent=:p order by t.orderNo";
            Map m = new HashMap();
            m.put("p", findItemByCode("title"));
            titles = getFacade().findByJpql(j, m);
        }
        return titles;
    }

    public List<Item> findItemList(String parentCode, ItemType t) {
        return findItemList(parentCode, t, null);
    }

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent) {
        return findChildrenAndGrandchildrenItemList(parent, null);
    }

    public List<Item> findChildrenAndGrandchildrenItemList(Item parent, String qry) {
        Map<Long, Item> mapItems = new HashMap<>();
        findChildrenAndGrandchildrenItemList(parent, qry, mapItems);
        List<Item> lstItems = new ArrayList<>();
        lstItems.addAll(mapItems.values());
        return lstItems;
    }

    public boolean findChildrenAndGrandchildrenItemList(Item parent, String qry, Map<Long, Item> tis) {
        if (tis == null) {
            tis = new HashMap<>();
        }
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p";
        }
        if (qry != null) {
            m.put("n", "%" + qry.trim().toLowerCase() + "%");
            j += " and lower(t.name) like :n ";
        }
        j += " order by t.orderNo";

        List<Item> ttis = getFacade().findByJpql(j, m);

        if (ttis == null) {
            return false;
        }

        for (Item ti : ttis) {
            tis.put(ti.getId(), ti);
        }

        return true;
    }

    public List<Item> findChildrenItemsByParentCode(String code) {
        List<Item> tis;
        if (code == null) {
            tis = new ArrayList<>();
            return tis;
        }
        String j = "select t from Item t where t.retired<>:ret ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("n", code.trim().toLowerCase());
        j += " and lower(t.parent.code) =:n ";
        j += " order by t.orderNo";
        tis = getFacade().findByJpql(j, m);
        if (tis == null) {
            tis = new ArrayList<>();
        }

        return tis;
    }

    public List<Item> findItemListByDisplayName(String parentCode) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        Item parent = findItemByCode(parentCode);
        if (parentCode != null) {
            m.put("p", parentCode.trim().toLowerCase());
            j += " and lower(t.parent.code)=:p ";
        }
        j += " order by t.displayName";
        return getFacade().findByJpql(j, m);
    }

    public List<Item> findItemList(String parentCode, ItemType t, String qry) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        Item parent = findItemByCode(parentCode);
        if (t != null) {
            m.put("t", t);
            j += " and t.itemType=:t  ";
        }
        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p ";
        }
        if (qry != null) {
            m.put("n", "%" + qry.trim().toLowerCase() + "%");
            j += " and (lower(t.name) like :n or lower(t.code) like :n) ";
        }
        j += " order by t.orderNo";
        return getFacade().findByJpql(j, m);
    }

    public List<Item> findItemList(Item parent) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();

        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p ";
        }
        j += " order by t.name";
        return getFacade().findByJpql(j, m);
    }

    public List<Item> findItemListByCode(String parentCode) {
        String j = "select t from Item t where t.retired=false ";
        Map m = new HashMap();
        Item parent = findItemByCode(parentCode);
        if (parent != null) {
            m.put("p", parent);
            j += " and t.parent=:p ";
        }
        j += " order by t.name";
        return getFacade().findByJpql(j, m);
    }

    public void setTitles(List<Item> titles) {
        this.titles = titles;
    }

    public List<Item> getEthinicities() {
        if (ethinicities == null) {
            ethinicities = findItemList("ethnic_group", ItemType.Dictionary_Item);
        }
        return ethinicities;
    }

    public void setEthinicities(List<Item> ethinicities) {
        this.ethinicities = ethinicities;
    }

    public List<Item> getReligions() {
        if (religions == null) {
            religions = findItemList("religion", ItemType.Dictionary_Item);
        }
        return religions;
    }

    public void setReligions(List<Item> religions) {
        this.religions = religions;
    }

    public List<Item> getSexes() {
        if (sexes == null) {
            sexes = findItemList("sex", ItemType.Dictionary_Item);
        }
        return sexes;
    }

    public void setSexes(List<Item> sexes) {
        this.sexes = sexes;
    }

    public List<Item> getMarietalStatus() {
        if (marietalStatus == null) {
            marietalStatus = findItemList("marital_status", ItemType.Dictionary_Item);
        }
        return marietalStatus;
    }

    public void setMarietalStatus(List<Item> marietalStatus) {
        this.marietalStatus = marietalStatus;
    }

    public List<Item> getCitizenships() {
        if (citizenships == null) {
            citizenships = findItemList("citizenship", ItemType.Dictionary_Item);
        }
        return citizenships;
    }

    public void setCitizenships(List<Item> citizenships) {
        this.citizenships = citizenships;
    }

    public List<Item> getMimeTypes() {
        if (mimeTypes == null) {
            mimeTypes = findItemList("mime_type", ItemType.Dictionary_Item);
        }
        return mimeTypes;
    }

    public void setMimeTypes(List<Item> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public List<Item> getCategories() {
        if (categories == null) {
            categories = findItemList(null, ItemType.Dictionary_Category);
        }
        return categories;
    }

    public void setCategories(List<Item> categories) {
        this.categories = categories;
    }

    public sc.facade.ItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public int getItemNameColumnNumber() {
        return itemNameColumnNumber;
    }

    public void setItemNameColumnNumber(int itemNameColumnNumber) {
        this.itemNameColumnNumber = itemNameColumnNumber;
    }

    public int getItemCodeColumnNumber() {
        return itemCodeColumnNumber;
    }

    public void setItemCodeColumnNumber(int itemCodeColumnNumber) {
        this.itemCodeColumnNumber = itemCodeColumnNumber;
    }

    public int getParentCodeColumnNumber() {
        return parentCodeColumnNumber;
    }

    public void setParentCodeColumnNumber(int parentCodeColumnNumber) {
        this.parentCodeColumnNumber = parentCodeColumnNumber;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public Item getSelectedParent() {
        return selectedParent;
    }

    public void setSelectedParent(Item selectedParent) {
        this.selectedParent = selectedParent;
    }

    public List<Item> getEducationalStatus() {
        if (educationalStatus == null) {
            educationalStatus = findItemList("education_levels", ItemType.Dictionary_Item);
        }
        return educationalStatus;
    }

    public void setEducationalStatus(List<Item> educationalStatus) {
        this.educationalStatus = educationalStatus;
    }

    @FacesConverter(forClass = Item.class)
    public static class ItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getItem(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException e) {
                key = 0l;
            }
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
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Item.class.getName()});
                return null;
            }
        }

    }

}
