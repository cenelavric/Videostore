package tv.beenius.videostore.util;

public class TextFilter {
  
  // Table filtering variables shared with JSF view..
  private boolean chkFilterApplied;
  private String inputFilterSearchFor;
  private String query;

  public String getQuery() {
    return query;
  }

  public TextFilter(String query) {
    super();
    this.query = query;
  }

  public boolean isChkFilterApplied() {
    return chkFilterApplied;
  }

  /**
   * Sets filter checkBox.
   * Resets filter search string when unchecked.
   *  
   * @param chkFilterApplied Filter check-box indicator.
   */
  public void setChkFilterApplied(boolean chkFilterApplied) {
    
    this.chkFilterApplied = chkFilterApplied;
    
    if (! chkFilterApplied) {
      this.inputFilterSearchFor = null;
    }
  }

  public String getInputFilterSearchFor() {
    return inputFilterSearchFor;
  }

  /**
   * Sets filter search string.
   * Resets check-box when search string is cleared.
   * 
   * @param inputFilterSearchFor Filter search string.
   */
  public void setInputFilterSearchFor(String inputFilterSearchFor) {

    this.inputFilterSearchFor = inputFilterSearchFor;

    this.chkFilterApplied = ! ((inputFilterSearchFor == null) || inputFilterSearchFor.isEmpty());
  }


}
