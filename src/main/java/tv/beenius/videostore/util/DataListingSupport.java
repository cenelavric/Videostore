package tv.beenius.videostore.util;

import java.io.Serializable;

import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;

/**
 * Supports pagination of dataTable.
 * This bean has to be used together with the Paginator facelet.
 * 
 * <p>This abstract class should be extended with a CDI @Named @SessionScoped bean.
 * Extended class should keep state about rowsPerPage and search filter between HTTP requests.
 * To support the stateful nature of the extended class internal calls to method refresh() 
 * have been banned (commented). Instead, call to refresh method should be performed in 
 * facelet calling the Paginator facelet. The following line should be inserted 
 * just before declaration of Paginator element in the caller code. For example:
 * f:event type="preRenderView" listener="#{movieListing.refresh}" 
 * 
 * <p>Base paginator code and explanation can be found at:
 * https://dwuysan.wordpress.com/2013/11/17/using-jsf-2-2-features-to-develop-ajax-scrollable-lazy-loading-data-table/
 *
 * @param <T> T is the type of the dataTable list element.
 */
@SuppressWarnings("serial")
public abstract class DataListingSupport<T extends Serializable> implements Serializable {
  
  // Selected from Paginator radio button values. 
  protected static int DEFAULT_PAGINATOR_ROWS_PER_PAGE = 10;
  
  // Paginator variables shared with JSF view - implemented.
  private long recordCount = 0;
  private int totalPages = 0;
  private int page = 1;
  private Integer rowsPerPage = DEFAULT_PAGINATOR_ROWS_PER_PAGE;
  private DataModel<T> data;  
  
  // Paginator variables shared with JSF view - unimplemented.
  private boolean ascending = true;
  private String sortField;

  /**
   * Navigates single page forward or backwards.
   * 
   * @param forward Indicates moving page forward (true) or backward (false).
   */
  public void navigatePage(final boolean forward) {
    setPage((forward) ? ++page : --page);
  }

  /**
   * Set sorting of table fields - not implemented.
   * 
   * @param sortField name of the field to be sorted.
   */
  public void sort(final String sortField) {
    setSortField(sortField);
    setAscending(getSortField().equals(sortField) ? !isAscending() : true);
  }

  /**
   * Resets current page property to 1 
   * after row-per-page property has been changed in the Paginator. 
   * 
   * @param event Event not used for processing.
   */
  public void updateRowsPerPage(final AjaxBehaviorEvent event) {
    setPage(1);
  }

  /**
   * Reloads a page of data from database according to filtering and paging criteria.
   * Calculates total number of pages in database.
   */
  public void refresh() {
    
    // Hook to extended class to populate data and record count.
    refreshDataModel();
    
    // Compute total number of pages.
    setTotalPages(countTotalPages(getRecordCount(), getRowsPerPage()));
  }

  /**
   * The concrete implementation of this class must perform data retrieval based
   * on the current information available (accessible via methods such as
   * {@link #getSortField()}, {@link #isAscending()}, etc.
   * 
   * <p>The implementation is responsible in populating the values for
   * {@link #setRecordCount(long)} and
   * {@link #setData(javax.faces.model.DataModel)}
   */
  protected abstract void refreshDataModel();

  /**
   * Calculate number of pages in database respecting paging and filtering criteria.
   * 
   * @param totalRecord All records in database compliant with filtering criteria.
   * @param rowsPerPage Current maximum page size.
   * @return
   */
  protected static int countTotalPages(long totalRecord, int rowsPerPage) {  
    return (int)((totalRecord == 0) ? 0 : ((totalRecord - 1) / rowsPerPage) + 1);
  }

  /**
   * Calculates starting record from the list of all database filtered records.
   * 
   * @return Record number of first record in current page.
   */
  public int getStartRowPerPage() {
    return (getPage() - 1) * getRowsPerPage();
  }
  
  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public boolean isAscending() {
    return ascending;
  }

  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  public Integer getRowsPerPage() {
    return rowsPerPage;
  }

  public void setRowsPerPage(Integer rowsPerPage) {
    this.rowsPerPage = rowsPerPage;
  }

  public DataModel<T> getData() {
    return data;
  }

  public void setData(DataModel<T> data) {
    this.data = data;
  }

  public String getSortField() {
    return sortField;
  }

  public void setSortField(String sortField) {
    this.sortField = sortField;
  }

  public long getRecordCount() {
    return recordCount;
  }

  public void setRecordCount(long recordCount) {
    this.recordCount = recordCount;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  } 

}
