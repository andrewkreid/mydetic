package net.ghosttrails.www.mydetic.api;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

/**
 * A List of Memory dates returned from the API
 */
public class MemoryDataList {

  private String userId;
  private TreeMap<Date, Boolean> dates;

  public MemoryDataList() {
    this.dates = new TreeMap<Date, Boolean>();
  }

  public MemoryDataList(String userId) {
    this.userId = userId;
    this.dates = new TreeMap<Date, Boolean>();
  }

  public MemoryDataList(String userId, TreeMap<Date, Boolean> dates) {
    this.userId = userId;
    this.dates = dates;
  }

  public String getUserID() {
    return userId;
  }

  public void setUserID(String userID) {
    this.userId = userID;
  }

  public void setDate(Date date) {
    dates.put(date, true);
  }

  public boolean hasDate(Date date) {
    return dates.containsKey(date);
  }

  /**
   * @return Set of dates in ascending order
   */
  public Set<Date> getDates() {
    return this.dates.keySet();
  }

  public void clear() {
    this.dates.clear();
  }

  /**
   * Update this memory list's memories from another, adding or
   * overwriting existing memories.
   *
   * @param memories a MemoryDataList to merge from.
   */
  public void mergeFrom(MemoryDataList memories) throws MyDeticException {
    if (!this.userId.equals(memories.getUserID())) {
      throw new MyDeticException(
          "Tried to merge MemoryDataLists with different userIds");
    }
    for (Date d : memories.getDates()) {
      this.setDate(d);
    }
  }

  /**
   * @return a deep copy of this object.
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    MemoryDataList retval = (MemoryDataList)super.clone();
    retval.setUserID(this.userId);
    for (Date d : this.getDates()) {
      retval.setDate(d);
    }
    return retval;
  }
}
