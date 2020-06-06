package sc.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Product implements Serializable {

    @OneToMany(mappedBy = "product")
    private List<Upload> uploads;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "product")
    @OrderBy("orderNo")
    private List<SiComponentItem> siComponentItems;

// <editor-fold defaultstate="collapsed" desc="Attributes">
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    String strId;

    private static final long serialVersionUID = 1L;

    private String name;

    @Column(length = 45)
    private String sname;
    
    @ManyToOne
    private Item department;


    @Transient
    String productData;
    
    @Lob
    private String intro;

    @Lob
    private String description;
    
    @Lob
    private String information;
    
    private Double retailPrice;
    
    private Double discountPrice;
    
    @ManyToOne
    private Item weightUnit;
    private Double weight;
    
    private String weightString;

    private long viewCount;

    private int reviewCount;

    private double rating = 4.5;

    private boolean featured;

    /*
    Create Properties
     */
    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    private Institution createInstitution;
    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;
    /*
    Retire Reversal Properties
     */
    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;
    /*
    Retire Properties
     */
    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    // </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Overrides">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Product)) {
            return false;
        }
        Product other = (Product) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + '}';
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetiredReversedAt() {
        return retiredReversedAt;
    }

    public void setRetiredReversedAt(Date retiredReversedAt) {
        this.retiredReversedAt = retiredReversedAt;
    }

   

// </editor-fold>
    public List<SiComponentItem> getSiComponentItems() {
        if (siComponentItems == null) {
            siComponentItems = new ArrayList<>();
        }
        return siComponentItems;
    }

    public void setSiComponentItems(List<SiComponentItem> siComponentItems) {
        this.siComponentItems = siComponentItems;
    }

    public Institution getCreateInstitution() {
        return createInstitution;
    }

    public void setCreateInstitution(Institution createInstitution) {
        this.createInstitution = createInstitution;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

   

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

   

    public String getShortNameTmp() {
        if (name == null) {
            return "";
        }
        String tn = name + "                                              ";
        return tn.substring(0, 35);
    }

    public String getStrId() {
        if (id == null) {
            strId = "";
        } else {
            strId = id + "";
        }
        return strId;
    }

    public void setStrId(String strId) {
        if (id == null) {
            strId = "";
        } else {
            strId = id + "";
        }
        this.strId = strId;
    }

    public String getProductData() {
        return productData;
    }

    public String findSlutionData(String code) {
        productData = "";
        for (SiComponentItem sici : getSiComponentItems()) {
            if (sici.getItem().getCode().equals(code) && !sici.isRetired()) {
                productData += sici.getValueAsString() + " ";
            };
        }
        if (productData == null) {
            productData = "";
        }
        productData = productData.trim();
        return productData;
    }

    public String getSname() {
        if (sname == null || sname.trim().equals("")) {
            String tm;
            if (name == null) {
                tm = "                                                                     ";
            } else {
                tm = name + "                                                                     ";
            }
            sname = tm.substring(0, 26);
        }
        if (sname.length() > 26) {
            sname = sname.substring(0, 26);
        }
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public List<Upload> getUploads() {
        return uploads;
    }

    public void setUploads(List<Upload> uploads) {
        this.uploads = uploads;
    }

    
    
    
    public String getUploadIdForImageType(String imageTypeCode) {
        if (uploads == null) {
            return "";
        }
        String tid = "";
        for (Upload u : uploads) {
            String itc = u.getImageType().getCode();

            if (itc.equals(imageTypeCode)) {
                tid = u.getStrId();
            }
        }
        return tid;
    }

    public Double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(Double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public Double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(Double discountPrice) {
        this.discountPrice = discountPrice;
    }
    
    

    public Item getDepartment() {
        return department;
    }

    public void setDepartment(Item department) {
        this.department = department;
    }

    public String getInformation() {
        return information;
    }

    
    
    public void setInformation(String information) {
        this.information = information;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Item getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(Item weightUnit) {
        this.weightUnit = weightUnit;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getWeightString() {
        return weightString;
    }

    public void setWeightString(String weightString) {
        this.weightString = weightString;
    }

    
    
}
