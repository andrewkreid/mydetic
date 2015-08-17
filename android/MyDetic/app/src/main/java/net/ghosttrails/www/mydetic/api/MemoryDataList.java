package net.ghosttrails.www.mydetic.api;

import android.text.format.DateUtils;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
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
    for (Date d: memories.getDates()) {
      this.setDate(d);
    }
  }

  /**
   * Build a MemoryDataList from the JSON wire format.
   *
   * {
   *   "memories": [
   *    "2015-12-18",
   *    "2015-12-19",
   *    "2015-12-20"
   *   ],
   *   "user_id": "mreynolds"
   * }
   *
   * @param jsonObject JSON returned from the REST API
   * @return a MemoryDataList object.
   * @throws MyDeticException on format errors
   */
  static MemoryDataList fromJSON(JSONObject jsonObject) throws MyDeticException {
    MemoryDataList memoryDataList = new MemoryDataList();

    try {
      String userId = jsonObject.getString("user_id");
      memoryDataList.setUserID(userId);

      JSONArray memoryArray = jsonObject.getJSONArray("memories");
      for(int i = 0; i < memoryArray.length(); i++) {
        String dateStr = memoryArray.getString(i);
        memoryDataList.setDate(Utils.parseIsoDate(dateStr));
      }

      return memoryDataList;
    } catch (JSONException | ParseException e) {
      throw new MyDeticException("Format error in memory list JSON:" + e.toString());
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
