package tv.beenius.videostore.jsf.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.exception.EjbValidationException;
import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.service.RegisterService;
import tv.beenius.videostore.util.ImageUtil;

@SuppressWarnings("serial")
@SessionScoped
@Named
public class ImageModel implements Serializable {
  
  Logger logger = Logger.getLogger(getClass());

  @Inject
  private FacesContext facesContext;
  
  @Inject
  RegisterService register;
  
  @Inject
  ImageUtil imageUtil;
  
  // Image constructed for RegisterService service calls.
  private Image image;

  // Facelet Variables.
  private String imdbId;
  private Long id;
  private String description;
  private byte[] content;
  private Part imageFile;
  
  private FacesMessage message;
  
  /**
   * Constructor.
   */
  public ImageModel() {
    super();
    
    image = new Image();
    imageToFaceletVariables();
    this.imageFile = null;
  }

  /**
   * Refresh facelet data.
   */
  public void refresh() { 
    refreshFromDatabase();
    imageToFaceletVariables();
  }
  
  /**
   * Populate facelet variables from database.
   */
  private void refreshFromDatabase() {
    
    if ((imdbId != null) && (image.getId() != null)) {   
      Optional<Image> optionalImage;
      try {
        optionalImage = register.findMovieImageById(imdbId, image.getId());
      } catch (RuntimeException rte) {
        optionalImage = Optional.empty();
        message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " refresh failed!",
            "For more details dive into server log.");
        facesContext.addMessage(null, message);
        logger.log(Level.ERROR, rte.getLocalizedMessage());
      }
      
      if (optionalImage.isPresent()) {
        image = optionalImage.get();   
      } else {
        clear();
      }
    }
  }

  /**
   * Load an image via 
   * {@link tv.beenius.videostore.service.RegisterService#findMovieImageById(String, Long)}.
   */
  public void load() {

    image.setId(id);
    
    try {
      register.validateImdbId(imdbId);
      register.validateEntityIdentifier(id, true);

      Optional<Image> optionalImage = register.findMovieImageById(imdbId, id);
      
      if (optionalImage.isPresent()) {
        image = optionalImage.get();         
        message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Image retrieved.",
            image + " has been loaded.");
      } else {
        image.setDescription(null);
        image.setContent(null);
        message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Image not retrieved.",
            "No image with imdbId {" + imdbId + "} and Id {" + id + "} could be found.");
      }
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      image.setDescription(null);
      image.setContent(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Image retrieval failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " retrieval failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }

  /**
   * Register a movie image via 
   * {@link tv.beenius.videostore.service.RegisterService#registerMovieImage(String, Image) )}.
   */
  public void registerImage() {
  
    try {
      setContent(imageUtil.getImageFromPart(imageFile));

      imageFromFaceletVariables();
      image.setId(null);

      final Image registeredImage = register.registerMovieImage(imdbId, image);

      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Image registered.",
          registeredImage + " has been registered.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      image.setContent(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " registering failed!",
          e.getLocalizedMessage());
    } catch (IOException ioe) {
      image.setContent(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " registering failed!",
          ioe.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " registering failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Update movie image via 
   * {@link tv.beenius.videostore.service.RegisterService#updateImage(Image)}.
   */
  public void updateImage() {

    try {
      setContent(imageUtil.getImageFromPart(imageFile));

      imageFromFaceletVariables();

      register.updateImage(image);
      
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Image has been updated.",
          "Updated selected " + image + "attributes description and/or content.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      image.setContent(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " update failed!",
          e.getLocalizedMessage());
    } catch (IOException ioe) {
      image.setContent(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " update failed!",
          ioe.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " update failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    } 

    facesContext.addMessage(null, message);
  }
  
  /**
   * Remove image from list view.
   * 
   * @param imdbId Movie identifier.
   * @param id Image identifier.
   */
  public void removeSelectedImage(String imdbId, Long id) {
    setImdbId(imdbId);
    setImage(image);
    
    removeImage();
  }
 
  /**
   * Un-register movie image via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterMovieImage(String, Long)}.
   */
  public void removeImage() {

    try {
      register.unRegisterMovieImage(imdbId, id);

      clear();
  
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Image removed.",
          image + " has been removed.");

    } catch (EjbConstraintViolationException | EjbValidationException ve) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " removal failed!",
          ve.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, image + " removal failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Clears input form.
   */
  public void clear() {
    imdbId = null;
    image = new Image();
    imageToFaceletVariables();
  }
    
  // Utilities.
 
  // Converts byte array image to a string to be displayed in facelet.
  public static String convertImage(byte[] content) {
    
    return content == null ? "" : new String(Base64.getEncoder().encode(content));
  }
  
  /**
   * Validate image file attributes.
   * 
   * @param ctx FacesContext.
   * @param comp UIComponent.
   * @param value Object.
   */
  public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
    
    List<FacesMessage> messages = new ArrayList<FacesMessage>();
    
    Part file = (Part) value;
    
    if (file == null) {
      return;
    }
    
    if (file.getSize() > 20971520) {
      messages.add(new FacesMessage(
          FacesMessage.SEVERITY_ERROR, 
          "Image could not be uploaded!", 
          "File is bigger than 20 MB."));
      logger.log(Level.ERROR, "File is bigger than 20 MB.");
    }
    
    if (!"image/jpeg".equals(file.getContentType())) {
      messages.add(new FacesMessage(
          FacesMessage.SEVERITY_ERROR, 
          "Image could not be uploaded!", 
          "Not a image/jpeg file"));
      logger.log(Level.ERROR, "Not a image/jpeg file");
    }
    
    if (!messages.isEmpty()) {
      throw new ValidatorException(messages);
    }
  }
    
  private void imageToFaceletVariables() {
    setId(image.getId());
    setDescription(image.getDescription());
    setContent(image.getContent());
  }
  
  private void imageFromFaceletVariables() {
    image.setId(id);
    image.setDescription(description);
    image.setContent(content);
  }
  
  public void setMovieImage(String imdbId, Image image) {
    setImdbId(imdbId);
    setImage(image);
  }
  
  // Getters/setters.
  
  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Part getImageFile() {
    return imageFile;
  }

  public void setImageFile(Part imageFile) {
    this.imageFile = imageFile;
  }
  
}
