/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sc.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.imageio.stream.FileImageOutputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import sc.util.JsfUtil;
import sc.entity.SiComponentItem;
import sc.entity.Item;
import sc.enums.DataRepresentationType;
import sc.facade.ComponentFacade;
import org.primefaces.event.CaptureEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Buddhika
 */
@Named
@RequestScoped
public class ImageController implements Serializable {

    @Inject
    ItemController itemController;
    @Inject
    ClientEncounterComponentFormSetController clientEncounterComponentFormSetController;
    @EJB
    ComponentFacade componentFacade;

    private List<String> photos = new ArrayList<>();

    private String getRandomImageName() {
        int i = (int) (Math.random() * 10000000);

        return String.valueOf(i);
    }

    public List<String> getPhotos() {
        return photos;
    }

    @Inject
    ProductController productController;

    public ProductController getproductController() {
        return productController;
    }

    public StreamedContent getClientPhoto() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        } else {
            //System.out.println("getPatientController().getSelected() = " + getproductController().getSelected());
            if (getproductController().getSelected() == null) {
                return new DefaultStreamedContent();
            }
            SiComponentItem dp = clientEncounterComponentFormSetController.fillClientValue(getproductController().getSelected(), "client_default_photo");
            if (dp == null) {
                return new DefaultStreamedContent();
            }
            byte[] p = dp.getByteArrayValue();
            if (p == null) {
                return new DefaultStreamedContent();
            }
            return new DefaultStreamedContent(new ByteArrayInputStream(p), dp.getShortTextValue(), dp.getLongTextValue());
        }
    }

    public void oncapturePatientPhoto(CaptureEvent captureEvent) {
        if (getproductController().getSelected() == null || getproductController().getSelected().getId() == null) {
            JsfUtil.addErrorMessage("Product ?");
            return;
        }

        Item defaultPhoto = itemController.findItemByCode("client_default_photo");
        Item photo = itemController.findItemByCode("client_photo");

        List<SiComponentItem> ps = clientEncounterComponentFormSetController.fillClientValues(getproductController().getSelected(), "client_default_photo");
        for (SiComponentItem i : ps) {
            i.setItem(photo);
            componentFacade.edit(i);
        }

        SiComponentItem ip = new SiComponentItem();
        ip.setClient(getproductController().getSelected());
        ip.setClientValue(getproductController().getSelected());
        ip.setItem(defaultPhoto);
        ip.setByteArrayValue(captureEvent.getData());
        ip.setShortTextValue("image/png");
        ip.setLongTextValue("client_image_" + getproductController().getSelected().getId() + ".png");
        ip.setDataRepresentationType(DataRepresentationType.Product);
        componentFacade.create(ip);

        productController.finishCapturingPhotoWithWebCam();
        
        JsfUtil.addSuccessMessage("Photo captured from webcam.");
    }

    public void oncapture(CaptureEvent captureEvent) {
        String photo = getRandomImageName();
        this.photos.add(0, photo);
        byte[] data = captureEvent.getData();

        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String newFileName = servletContext.getRealPath("") + File.separator + "photocam" + File.separator + photo + ".png";

        FileImageOutputStream imageOutput;
        try {
            imageOutput = new FileImageOutputStream(new File(newFileName));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
        } catch (IOException e) {
            throw new FacesException("Error in writing captured image.");
        }
    }

    /**
     * Creates a new instance of PhotoCamBean
     */
    public ImageController() {
    }

}
