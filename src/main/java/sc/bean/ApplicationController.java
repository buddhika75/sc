/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sc.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import sc.entity.Institution;
import sc.entity.Item;
import sc.entity.Product;
import sc.facade.InstitutionFacade;
import sc.facade.ItemFacade;
import sc.facade.ProductFacade;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named(value = "applicationController")
@ApplicationScoped
public class ApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private ProductFacade productFacade;
    @EJB
    private ItemFacade itemFacade;

// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private boolean demoSetup = false;
    private String versionNo = "1.1.4";
    Long numberOfProducts = null;
    List<Item> categories;

    private List<Product> featuredProducts = null;
    private List<Product> popularProducts = null;

// </editor-fold>
    public ApplicationController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Functions">
    public List<Product> getFeaturedProducts() {
        if (featuredProducts == null) {
            String j = "select s from Product s "
                    + " where s.retired<>:ret "
                    + " and s.featured=:fet";
            Map m = new HashMap();
            m.put("ret", true);
            m.put("fet", true);
            featuredProducts = getProductFacade().findByJpql(j, m);
            if (featuredProducts == null) {
                featuredProducts = new ArrayList<>();
            }
        }
        return featuredProducts;
    }

    public List<Product> getPopularProducts() {
        if (popularProducts == null) {
            String j = "select s from Product s "
                    + " where s.retired<>:ret "
                    + " order by s.viewCount desc";
            Map m = new HashMap();
            m.put("ret", true);
            popularProducts = getProductFacade().findByJpql(j, m, 8);
            if (popularProducts == null) {
                popularProducts = new ArrayList<>();
            }
        }
        return popularProducts;
    }

    public void fillCategoryData() {
        featuredProducts = null;
        popularProducts = null;
        getFeaturedProducts();
        getPopularProducts();
        String j;
        Map m = new HashMap();

        j = "select count(s) from Product s where s.retired<>:ret";
        m.put("ret", true);

        numberOfProducts = getProductFacade().countByJpql(j, m);

        m = new HashMap();
        j = "select i from Item i where i.retired<>:ret and i.parent.code=:code";
        m.put("code", "product_categories");
        m.put("ret", true);

        categories = getItemFacade().findByJpql(j, m);

        for (Item i : categories) {
            m = new HashMap();
            j = "select count(distinct si) from SiComponentItem si"
                    + " join si.product s "
                    + " where si.retired<>:ret "
                    + " and si.itemValue=:item ";

            m.put("item", i);
            m.put("ret", true);
            Long temLng = getProductFacade().countByJpql(j, m);
            System.out.println("i = " + i.getCode());
            if (temLng == null) {
                temLng = 0l;
            }
            i.setProductCountTemp(temLng);
        }

    }

    public Long productForCategoryCount(Item cat) {

        System.out.println("cat code= " + cat.getCode());
        for (Item i : categories) {

            if (i.getCode().equals(cat.getCode())) {
                return i.getProductCountTemp();
            }
        }
        return 0l;
    }

    public static boolean validateHin(String validatingHin) {
        if (validatingHin == null) {
            return false;
        }
        char checkDigit = validatingHin.charAt(validatingHin.length() - 1);
        String digit = calculateCheckDigit(validatingHin.substring(0, validatingHin.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    public static String calculateCheckDigit(String card) {
        if (card == null) {
            return null;
        }
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2) {
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Enums">

    

    // <editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

// </editor-fold>
    public boolean isDemoSetup() {
        return demoSetup;
    }

    public Long getNumberOfProducts() {
        if (numberOfProducts == null) {
            fillCategoryData();
        }
        return numberOfProducts;
    }

    public List<Item> getCategories() {
        return categories;
    }

    public void setDemoSetup(boolean demoSetup) {
        this.demoSetup = demoSetup;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public ProductFacade getProductFacade() {
        return productFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

}
