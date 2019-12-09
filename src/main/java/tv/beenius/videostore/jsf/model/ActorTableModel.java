package tv.beenius.videostore.jsf.model;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import tv.beenius.videostore.exception.EjbConstraintViolationException;
import tv.beenius.videostore.model.Actor;
import tv.beenius.videostore.service.RegisterService;
import tv.beenius.videostore.util.DataListingSupport;
import tv.beenius.videostore.util.TextFilter;

@SuppressWarnings("serial")
@SessionScoped()
@Named(value = "actorTableModel")
public class ActorTableModel extends DataListingSupport<Actor> {

  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  RegisterService register;
  
  // Facelet Variables.
  TextFilter nameFilter;
  
  private String emptyListStatus;
  
  /**
   * Constructor with sorting and filtering initialization.
   */
  public ActorTableModel() {
    setSortField("lastName");
    this.nameFilter = new TextFilter("Filter actors on name by ");
  }
 
  @Override
  protected void refreshDataModel() {
    
    List<Actor> actors = new ArrayList<>();
    long recordCount = 0L;
    
    try {
      if (nameFilter.isChkFilterApplied()) {
        actors = register.findPageOfActorsByName(getStartRowPerPage(), getRowsPerPage(),
            nameFilter.getInputFilterSearchFor());
        recordCount = register.countActorsByName(nameFilter.getInputFilterSearchFor());
      } else {
        actors = register.findPageOfActors(getStartRowPerPage(), getRowsPerPage());
        recordCount = register.countActors();
      } 
      resetEmptyListStatus();
    } catch (EjbConstraintViolationException e) {
      setEmptyListStatus(e.getLocalizedMessage());
      logger.log(Level.WARN, e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      setEmptyListStatus("Table refresh failed. For more details dive into server log");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    setData(new ListDataModel<>(actors));
    setRecordCount(recordCount);
  }
  
  // Getters/setters.

  public TextFilter getNameFilter() {
    return nameFilter;
  }

  public void setNameFilter(TextFilter filter) {
    this.nameFilter = filter;
  } 
  
  public String getEmptyListStatus() {
    return emptyListStatus;
  }

  public void setEmptyListStatus(String emptyListStatus) {
    this.emptyListStatus = emptyListStatus;
  }

  public void resetEmptyListStatus() {
    this.emptyListStatus = "No actors retrieved.";
  }

}
