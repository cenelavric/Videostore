package tv.beenius.videostore.jsf.model;

import java.io.Serializable;

import javax.ejb.DependsOn;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.exception.EjbValidationException;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@Named(value = "castModel")
@SessionScoped
@DependsOn({"movieActorTableModel"})
public class CastModel implements Serializable {
   
  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  private FacesContext facesContext;
  
  @Inject
  private RegisterService register;

  private FacesMessage message;
 
  /**
   * Constructor.
   */
  public CastModel() {
    super();
  }
  
  /**
   * Register cast via 
   * {@link tv.beenius.videostore.service.RegisterService#registerCast(String, Long)}.
   * 
   * @param imdbId Movie identifier.
   * @param id Actor identifier.
   */
  public void registerCast(String imdbId, Long id) {

    try {
      register.registerCast(imdbId, id);

      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cast registered.",
          "Cast between Actor with id {" + id + "} and Movie with imdb {" + imdbId + "} "
          + "has been registered.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
          "Cast between Actor with id {" + id + "} and Movie with imdb {" + imdbId + "} "
          + "could not be registered!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
          "Actor with id {" + id + "} and Movie with imdb {" + imdbId + "}" 
          + " cast registering failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Un-register cast via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterCast(String, Long)}.
   * 
   * @param imdbId Movie eidentifier.
   * @param id Actor identifier.
   */
  public void removeCast(String imdbId, Long id) {
  
    try {
      register.unRegisterCast(imdbId, id);
      
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cast unregistered.",
          "Cast between Actor with id {" + id + "} and Movie with imdb {" + imdbId + "} "
          + "has been removed.");

    } catch (EjbConstraintViolationException | EjbValidationException e) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
          "Cast between Actor with id {" + id + "} and Movie with imdb {" + imdbId + "} "
          + "unregistereing failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR,  
          "Actor with id {" + id + "} and Movie with imdb {" + imdbId + "}" 
          + " cast unregistering failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
}
