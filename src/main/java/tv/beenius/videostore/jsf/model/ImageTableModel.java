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
import tv.beenius.videostore.model.Image;
import tv.beenius.videostore.service.RegisterService;

@SuppressWarnings("serial")
@SessionScoped
@Named(value = "imageTableModel")
@DependsOn("movieModel")
public class ImageTableModel implements Serializable {

  Logger logger = Logger.getLogger(getClass());
  
  @Inject
  RegisterService register;

  // Movie Image reference identifier.
  private String imdbId;

  // Facelet Variables.
  private DataModel<Image> data;
  private long recordCount = 0;
     
  private String emptyListStatus;
  
  /**
   * Constructor.
   */
  public ImageTableModel() {
    super();
  
    this.imdbId = null;
    this.data = new ListDataModel<Image>();
    this.recordCount = 0L;
    
    System.out.println("ImageTableModel constructed. " + this.toString());
  }
  
  /**
   * Refresh facelet data.
   * 
   * @param imdbId Movie identifier.
   */
  public void refresh(String imdbId) {   
    setImdbId(imdbId);
    refreshDataModel();
  }
    
  /**
   * Populates facelet variable values from database.
   */
  protected void refreshDataModel() {
    
    if (imdbId == null) {
      setData(new ListDataModel<Image>());
      setRecordCount(0L);
      resetEmptyListStatus();
      return;
    }

    List<Image> images = new ArrayList<Image>();
    long recordCount = 0L;
    
    try {
      images = register.findMovieImages(imdbId);
      recordCount = images.size();
      resetEmptyListStatus();
    } catch (EjbConstraintViolationException | EjbValidationException e) {
      resetEmptyListStatus();
      logger.log(Level.INFO, e.getLocalizedMessage());
    } catch (RuntimeException rte) {
      setEmptyListStatus("Table refresh failed. For more details dive into server log");
      logger.log(Level.ERROR, rte.getLocalizedMessage());
    }

    setData(new ListDataModel<Image>(images));
    setRecordCount(recordCount);
  }

  // Getters/setters.
  
  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }
  
  public long getRecordCount() {
    return recordCount;
  }

  public void setRecordCount(long recordCount) {
    this.recordCount = recordCount;
  }

  public DataModel<Image> getData() {
    return data;
  }

  public void setData(DataModel<Image> data) {
    this.data = data;
  }  
  
  public String getEmptyListStatus() {
    return emptyListStatus;
  }

  public void setEmptyListStatus(String emptyListStatus) {
    this.emptyListStatus = emptyListStatus;
  }

  public void resetEmptyListStatus() {
    this.emptyListStatus = "No images retrieved.";
  }

}
