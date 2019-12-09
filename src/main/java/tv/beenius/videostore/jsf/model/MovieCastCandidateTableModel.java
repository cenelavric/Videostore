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

import tv.beenius.videostore.model.Movie;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@SessionScoped
@Named(value = "movieCastCandidateTableModel")
public class MovieCastCandidateTableModel implements Serializable {
  
  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  private RegisterService register;

  private List<Movie> movies;

  // Facelet Variables.
  private DataModel<Movie> data;
  private long recordCount = 0;
     
  private String emptyListStatus;
  
  /**
   * Constructor.
   */
  public MovieCastCandidateTableModel() {
    super();
  
    this.movies = new ArrayList<>();
    this.data = new ListDataModel<Movie>(movies);
    this.recordCount = 0L;
  }
  
  /**
   * Adds element to DataModel list.
   * 
   * @param movie Movie.
   */
  public void add(Movie movie) {
    
    // Avoid row duplication.
    Iterator<Movie> iterator = movies.iterator();
    
    while (iterator.hasNext()) {
      if (iterator.next().getImdbId().equals(movie.getImdbId())) {
        return;
      }
    }
    
    movies.add(movie);
    this.data = new ListDataModel<Movie>(movies);
    this.recordCount = movies.size();
  }
  
  /**
   * Removes selected element from DataModel list.
   */
  public void removeSelected() {
    
    Iterator<Movie> iterator;

    iterator = movies.iterator();
    
    while (iterator.hasNext()) {
      if (iterator.next().getImdbId().equals(getData().getRowData().getImdbId())) {
        iterator.remove();
        break;
      }
    }

    this.data = new ListDataModel<Movie>(movies);
    this.recordCount = movies.size();
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
    
    Iterator<Movie> iterator;

    iterator = movies.iterator();
    
    try {
      while (iterator.hasNext()) {

        if (!register.findMovieById(iterator.next().getImdbId()).isPresent()) {
          iterator.remove();
        }
      } 
      resetEmptyListStatus();
    } catch (RuntimeException rte) {
      setEmptyListStatus("Table refresh failed. For more details dive into server log");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }
    
    setData(new ListDataModel<Movie>(movies));
    setRecordCount(movies.size());
  }
  
  // Getters/setters.
 
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
    this.emptyListStatus = "No movie cast candidates retrieved.";
  }

}
