package tv.beenius.videostore.jsf.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.exception.EjbValidationException;
import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@Named(value = "actorModel")
@SessionScoped
public class ActorModel implements Serializable {

  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  private FacesContext facesContext;
  
  @Inject
  private RegisterService register;

  // Actor constructed for RegisterService service calls.
  private Actor actor;
  
  // Facelet Variables.
  private Long id;
  private String firstName;
  private String lastName;
  private LocalDate bornDate;
  
  private FacesMessage message;
 
  /**
   * Constructor.
   */
  public ActorModel() {
    super();
    
    actor = new Actor();  
    actorToFaceletVariables();
  }

  /**
   * Refresh facelet data.
   */
  public void refresh() {
    refreshFromDatabase();
    actorToFaceletVariables();
  }
  
  /**
   * Populates facelet variables from database.
   */
  private void refreshFromDatabase() {
    
    if (actor.getId() != null) {
      Optional<Actor> optionalActor;
      try {
        optionalActor = register.findActorByIdLazily(actor.getId());
      } catch (RuntimeException rte) {
        optionalActor = Optional.empty();
        message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " refresh failed!",
            "For more details dive into server log.");
        facesContext.addMessage(null, message);
        logger.log(Level.ERROR, rte.getLocalizedMessage());
      }
      
      if (optionalActor.isPresent()) {
        actor = optionalActor.get();   
      } else {
        clear();
      }
    }
  }
  
  /**
   * Load an actor via 
   * {@link tv.beenius.videostore.service.RegisterService#findActorByIdLazily(Long)}.
   */
  public void load() {
    
    actor.setId(id);

    try {
      register.validateEntityIdentifier(id, true);
      register.validateActorId(id);
      
      Optional<Actor> optionalActor = register.findActorByIdLazily(id);

      if (optionalActor.isPresent()) {
        actor = optionalActor.get();          
        message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Actor retrieved.",
            actor + " has been loaded.");
      } else {
        actor.setFirstName(null);
        actor.setLastName(null);
        actor.setBornDate(null);
        message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Actor not retrieved.",
            "No actor with imdb Id {" + id + "} could be found.");
      }
    } catch (EjbConstraintViolationException e) {
      actor.setFirstName(null);
      actor.setLastName(null);
      actor.setBornDate(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " retrieval failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " retrieval failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }

  /**
   * Register an actor via 
   * {@link tv.beenius.videostore.service.RegisterService#registerActor(Actor)}.
   */
  public void registerActor() {
    
    actorFromFaceletVariables();
    actor.setId(null);

    try {
      final Actor registerdActor = register.registerActor(actor);

      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Actor registered.",
          registerdActor + " has been registered.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " registering failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " registering failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Update actor via 
   * {@link tv.beenius.videostore.service.RegisterService#updateActor(Actor)}.
   * 
   * @throws Exception in case of constraint violation or validation errors.
   */
  public void updateActor() {
    
    actorFromFaceletVariables();;

    try {
      register.updateActor(actor);
      
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Actor updated.",
          actor + " has been updated.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " update failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " update failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    } 

    facesContext.addMessage(null, message);
  }
  
  /**
   * Remove actor from actor-list view.
   * 
   * @param id Actor identifier.
   */
  public void removeSelectedActor(Long id) {
    setId(id);
    removeActor();
  }

  /**
   * Un-register actor via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterActor(Long)}.
   */
  public void removeActor() {
  
    try {
      register.unRegisterActor(id);

      clear();
      
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Actor removed.",
          actor + " has been removed.");

    } catch (EjbConstraintViolationException cve) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " removal failed!",
          cve.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, actor + " removal failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Clears input form.
   */
  public void clear() {
    actor = new Actor();
    actorToFaceletVariables();
  }

  // Utilities.
  
  private void actorToFaceletVariables() {
    setId(actor.getId());
    setFirstName(actor.getFirstName());
    setLastName(actor.getLastName().orElse(""));
    setBornDate(actor.getBornDate());
  }
  
  private void actorFromFaceletVariables() {
    actor.setId(id);
    actor.setFirstName(firstName);
    actor.setLastName(lastName);
    actor.setBornDate(bornDate);
  }
  
  // Getters/setters.
  
  public Actor getActor() {
    return actor;
  }
  
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = (id != null) && id.equals(0L) ? null : id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = (firstName != null) && firstName.isBlank() ? null : firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = (lastName != null) && lastName.isBlank() ? null : lastName;
  }

  public LocalDate getBornDate() {
    return bornDate;
  }

  public void setBornDate(LocalDate bornDate) {
    this.bornDate = bornDate;
  }
 
}
