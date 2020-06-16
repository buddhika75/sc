/*
 * The MIT License
 *
 * Copyright 2020 hiu_pdhs_sp.
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

import sc.entity.Upload;
import sc.facade.ProductFacade;
import sc.facade.UploadFacade;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author hiu_pdhs_sp
 */
@Named
@SessionScoped
public class StreamedContentSessionController  implements Serializable {

    @EJB
    private ProductFacade productFacade;
    @EJB
    private UploadFacade uploadFacade;

    private String strId;

    /**
     * Creates a new instance of StreamedContentController
     */
    public StreamedContentSessionController() {
    }

    public String toViewImageByStrId() {
        FacesContext context = FacesContext.getCurrentInstance();
        strId = context.getExternalContext().getRequestParameterMap().get("id");
        return "image";
    }
    
    public StreamedContent getImageByPageUploadId() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        } else {
            String id = strId;
            Long l;
            try {
                l = Long.valueOf(id);
            } catch (NumberFormatException e) {
                l = 0l;
            }
            String j = "select s from Upload s where s.id=:id";
            Map m = new HashMap();
            m.put("id", l);
            Upload temImg = getUploadFacade().findFirstByJpql(j, m);
            if (temImg != null) {
                byte[] imgArr = null;
                try {
                    imgArr = temImg.getBaImage();
                } catch (Exception e) {
                    return new DefaultStreamedContent();
                }
                if (imgArr == null) {
                    return new DefaultStreamedContent();
                }
                StreamedContent str = new DefaultStreamedContent(new ByteArrayInputStream(imgArr), temImg.getFileType());
                return str;
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

   

    public String getStrId() {
        return strId;
    }

    public void setStrId(String strId) {
        this.strId = strId;
    }

    public ProductFacade getProductFacade() {
        return productFacade;
    }

    public void setProductFacade(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public void setUploadFacade(UploadFacade uploadFacade) {
        this.uploadFacade = uploadFacade;
    }
    
    

}
