package tv.beenius.videostore.jsf.model;

import java.io.Serializable;
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
import tv.beenius.videostore.model.Movie;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@Named(value = "movieModel")
@SessionScoped
public class MovieModel implements Serializable {
  
  Logger logger = Logger.getLogger(getClass());
   
  @Inject
  private FacesContext facesContext;
  
  @Inject
  private RegisterService register;

  // Movie constructed for RegisterService service calls.
  private Movie movie;
  
  // Facelet Variables.
  private String imdbId;
  private String title;
  private Integer year;
  private String description;
  
  private FacesMessage message;
 
  /**
   * Constructor.
   */
  public MovieModel() {
    super();
    
    movie = new Movie();  
    movieToFaceletVariables();
  }

  /**
   * Refresh facelet data.
   */
  public void refresh() { 
    refreshFromDatabase();
    movieToFaceletVariables();
  }
    
  /**
   * Populates facelet variables from database.
   */
  private void refreshFromDatabase() {
    
    if (movie.getImdbId() != null) {
      Optional<Movie> optionalMovie;
      try {
        optionalMovie = register.findMovieByIdLazily(movie.getImdbId());
      } catch (RuntimeException rte) {
        optionalMovie = Optional.empty();
        message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " refresh failed!",
            "For more details dive into server log.");
        facesContext.addMessage(null, message);
        logger.log(Level.ERROR, rte.getLocalizedMessage());
      }
     
      if (optionalMovie.isPresent()) {
        movie = optionalMovie.get();   
      } else {
        clear();
      }
    }
  }
  
  /**
   * Load a movie via 
   * {@link tv.beenius.videostore.service.RegisterService#findMovieByIdLazily(String)}.
   */
  public void load() {
    
    movie.setImdbId(imdbId);

    try {
      register.validateImdbId(imdbId);
      
      Optional<Movie> optionalMovie = register.findMovieByIdLazily(imdbId);

      if (optionalMovie.isPresent()) {
        movie = optionalMovie.get();          
        message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Movie retrieved.",
            movie + " has been loaded.");
      } else {
        movie.setTitle(null);
        movie.setYear(null);
        movie.setDescription(null);
        message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Movie not retrieved.",
            "No movie with imdb Id {" + imdbId + "} could be found.");
      }
    } catch (EjbConstraintViolationException e) {
      movie.setTitle(null);
      movie.setYear(null);
      movie.setDescription(null);
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " retrieval failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " retrieval failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }

  /**
   * Register a movie via 
   * {@link tv.beenius.videostore.service.RegisterService#registerMovie(Movie)}.
   */
  public void registerMovie() {
    
    movieFromFaceletVariables();

    try {
      final Movie registeredMovie = register.registerMovie(movie);

      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Movie registered.",
          registeredMovie + " has been registered.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " registering failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " registering failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Update movie via 
   * {@link tv.beenius.videostore.service.RegisterService#updateMovie(Movie)}.
   */
  public void updateMovie() {
    
    movieFromFaceletVariables();;

    try {
      register.updateMovie(movie);
      
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Movie updated.",
          movie + " has been updated.");
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " update failed!",
          e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " update failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    } 

    facesContext.addMessage(null, message);
  }
  
  /**
   * Remove movie from movie-list view.
   * 
   * @param imdbId Movie identifier.
   */
  public void removeSelectedMovie(String imdbId) {
    setImdbId(imdbId);
    removeMovie();
  }

  /**
   * Un-register movie via 
   * {@link tv.beenius.videostore.service.RegisterService#unRegisterMovie(String)}.
   */
  public void removeMovie() {
  
    try {
      register.unRegisterMovie(imdbId);

      clear();
      
      message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Movie removed.",
          movie + " has been removed.");

    } catch (EjbConstraintViolationException cve) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " removal failed!",
          cve.getLocalizedMessage());
    } catch (RuntimeException rte) {
      message = new FacesMessage(FacesMessage.SEVERITY_ERROR, movie + " removal failed!",
          "For more details dive into server log.");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    facesContext.addMessage(null, message);
  }
  
  /**
   * Clears input form.
   */
  public void clear() {
    movie = new Movie();
    movieToFaceletVariables();
  }

  // Utilities.
  
  private void movieToFaceletVariables() {
    setImdbId(movie.getImdbId());
    setTitle(movie.getTitle());
    setYear(movie.getYear());
    setDescription(movie.getDescription());
  }
  
  private void movieFromFaceletVariables() {
    movie.setImdbId(imdbId);
    movie.setTitle(title);
    movie.setYear(year);
    movie.setDescription(description);
  }
  
  // Getters/setters.
  
  public Movie getMovie() {
    return movie;
  }
  
  public void setMovie(Movie movie) {
    this.movie = movie;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = (imdbId != null) && imdbId.isBlank() ? null : imdbId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = (title != null) && title.isBlank() ? null : title;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = (description != null) && description.isBlank() ? null : description;
  }
  
}
