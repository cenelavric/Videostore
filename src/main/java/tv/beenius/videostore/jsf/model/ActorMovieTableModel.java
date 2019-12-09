package tv.beenius.videostore.jsf.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.DependsOn;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.exception.EjbValidationException;
import tv.beenius.videostore.model.Movie;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@SessionScoped
@Named(value = "actorMovieTableModel")
@DependsOn("actorModel")
public class ActorMovieTableModel implements Serializable {
  
  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  RegisterService register;

  // Referenced Actor identifier.
  private Long id;

  // Facelet Variables.
  private DataModel<Movie> data;
  private long recordCount = 0;
  
  private String emptyListStatus;
  
  /**
   * Constructor.
   */
  public ActorMovieTableModel() {
    super();
  
    this.id = null;
    this.data = new ListDataModel<Movie>();
    this.recordCount = 0L;
    
    System.out.println("ActorMovieTableModel constructed. " + this.toString());
  }
  
  /**
   * Refresh facelet data.
   * 
   * @param id Actor identifier.
   */
  public void refresh(Long id) { 
    setId(id);  
    refreshDataModel();
  }
  
  /**
   * Populates facelet variable values from database.
   */
  protected void refreshDataModel() {
    
    if (id == null) {
      setData(new ListDataModel<Movie>());
      setRecordCount(0L);
      resetEmptyListStatus();
      return;
    }

    List<Movie> movies = new ArrayList<>();
    long recordCount = 0L;
    
    try {
      movies = register.findActorMoviesLazily(id);
      recordCount = movies.size();
      resetEmptyListStatus();
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      resetEmptyListStatus();
      logger.log(Level.INFO, e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      setEmptyListStatus("Table refresh failed. For more details dive into server log");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    setData(new ListDataModel<Movie>(movies));
    setRecordCount(recordCount);
  }
 
  // Getters/setters.
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = (id != null) && id.equals(0L) ? null : id;
  }
  
  public long getRecordCount() {
    return recordCount;
  }

  public void setRecordCount(long recordCount) {
    this.recordCount = recordCount;
  }

  public DataModel<Movie> getData() {
    return data;
  }

  public void setData(DataModel<Movie> data) {
    this.data = data;
  }  
  
  public String getEmptyListStatus() {
    return emptyListStatus;
  }

  public void setEmptyListStatus(String emptyListStatus) {
    this.emptyListStatus = emptyListStatus;
  }

  public void resetEmptyListStatus() {
    this.emptyListStatus = "No actor movies retrieved.";
  }

}
