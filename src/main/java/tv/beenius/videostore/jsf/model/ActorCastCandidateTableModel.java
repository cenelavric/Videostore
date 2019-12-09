package tv.beenius.videostore.jsf.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@SessionScoped
@Named(value = "actorCastCandidateTableModel")
public class ActorCastCandidateTableModel implements Serializable {

  Logger logger = Logger.getLogger(getClass());  

  @Inject
  private RegisterService register;

  private List<Actor> actors;

  // Facelet Variables.
  private DataModel<Actor> data;
  private long recordCount = 0;
  
  private String emptyListStatus;
       
  /**
   * Constructor.
   */
  public ActorCastCandidateTableModel() {
    super();
  
    this.actors = new ArrayList<>();
    this.data = new ListDataModel<Actor>(actors);
    this.recordCount = 0L;
  }
  
  /**
   * Adds element to DataModel list.
   * 
   * @param actor Actor.
   */
  public void add(Actor actor) {
    
    // Avoid row duplication.
    Iterator<Actor> iterator = actors.iterator();
    
    while (iterator.hasNext()) {
      if (iterator.next().getId().equals(actor.getId())) {
        return;
      }
    }
    
    actors.add(actor);
    this.data = new ListDataModel<Actor>(actors);
    this.recordCount = actors.size();
  }
  
  /**
   * Removes selected element from DataModel list.
   */
  public void removeSelected() {
    
    Iterator<Actor> iterator;

    iterator = actors.iterator();
    
    while (iterator.hasNext()) {
      if (iterator.next().getId().equals(getData().getRowData().getId())) {
        iterator.remove();
        break;
      }
    }

    this.data = new ListDataModel<Actor>(actors);
    this.recordCount = actors.size();
  }
  
  /**
   * Refresh facelet data..
   */
  public void refresh() {
    refreshDataModel();
  }
    
  /**
   * Populates facelet variable values from database.
   */
  protected void refreshDataModel() {
    
    Iterator<Actor> iterator;

    iterator = actors.iterator();
    
    try {
      while (iterator.hasNext()) {
        
        if (! register.findActorByIdLazily(iterator.next().getId()).isPresent()) {
          iterator.remove();
        }
      }
      resetEmptyListStatus();
    } catch (RuntimeException rte) {
      setEmptyListStatus("Table refresh failed. For more details dive into server log");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    setData(new ListDataModel<Actor>(actors));
    setRecordCount(actors.size());
  }
 
  // Getters/setters.
 
  public long getRecordCount() {
    return recordCount;
  }

  public void setRecordCount(long recordCount) {
    this.recordCount = recordCount;
  }

  public DataModel<Actor> getData() {
    return data;
  }

  public void setData(DataModel<Actor> data) {
    this.data = data;
  }  
  
  public String getEmptyListStatus() {
    return emptyListStatus;
  }

  public void setEmptyListStatus(String emptyListStatus) {
    this.emptyListStatus = emptyListStatus;
  }

  public void resetEmptyListStatus() {
    this.emptyListStatus = "No actor cast candidates retrieved.";
  }

}
