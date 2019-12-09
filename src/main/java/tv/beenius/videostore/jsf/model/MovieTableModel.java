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
import tv.beenius.videostore.model.Movie;
import tv.beenius.videostore.service.RegisterService;
import tv.beenius.videostore.util.DataListingSupport;
import tv.beenius.videostore.util.TextFilter;

@SuppressWarnings("serial")
@SessionScoped()
@Named(value = "movieTableModel")
public class MovieTableModel extends DataListingSupport<Movie> {

  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  RegisterService register;
  
  // Facelet Variables.
  TextFilter titleFilter;
  
  private String emptyListStatus;
  
  /**
   * Constructor with sorting and filtering initialization.
   */
  public MovieTableModel() {
    setSortField("title");
    this.titleFilter = new TextFilter("Filter movies on title by ");
  }

  @Override
  protected void refreshDataModel() {

    List<Movie> movies = new ArrayList<>();
    long recordCount = 0L;
    
    try {
      if (titleFilter.isChkFilterApplied()) {
        movies = register.findPageOfMoviesByTitle(getStartRowPerPage(), getRowsPerPage(),
            titleFilter.getInputFilterSearchFor());
        recordCount = register.countMoviesByTitle(titleFilter.getInputFilterSearchFor());
      } else {
        movies = register.findPageOfMovies(getStartRowPerPage(), getRowsPerPage());
        recordCount = register.countMovies();
      }     
      resetEmptyListStatus();   
    } catch (EjbConstraintViolationException e) {
      setEmptyListStatus(e.getLocalizedMessage());
      logger.log(Level.WARN, e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      setEmptyListStatus("Table refresh failed. For more details dive into server log");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    setData(new ListDataModel<>(movies));
    setRecordCount(recordCount);
  }
  
  // Getters/setters.

  public TextFilter getTitleFilter() {
    return titleFilter;
  }

  public void setTitleFilter(TextFilter filter) {
    this.titleFilter = filter;
  }
    
  public String getEmptyListStatus() {
    return emptyListStatus;
  }

  public void setEmptyListStatus(String emptyListStatus) {
    this.emptyListStatus = emptyListStatus;
  }

  public void resetEmptyListStatus() {
    this.emptyListStatus = "No movies retrieved.";
  }

}
