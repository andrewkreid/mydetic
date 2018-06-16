package net.ghosttrails.www.mydetic.api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A List of Memory dates returned from the API */
public class MemoryDataList implements Iterable<LocalDate> {

  private String userId;
  private TreeMap<LocalDate, Boolean> dates;

  // A map of years -> months -> dates
  private TreeMap<Integer, TreeMap<Integer, TreeMap<LocalDate, Boolean>>> datesForYearMonth;

  public MemoryDataList() {
    initMaps();
  }

  public MemoryDataList(String userId) {
    this.userId = userId;
    initMaps();
  }

  /**
   * Build a MemoryDataList from the JSON wire format.
   *
   * <p>{ "memories": [ "2015-12-18", "2015-12-19", "2015-12-20" ], "user_id": "mreynolds" }
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
      for (int i = 0; i < memoryArray.length(); i++) {
        String dateStr = memoryArray.getString(i);
        memoryDataList.setDate(Utils.parseIsoDate(dateStr));
      }

      return memoryDataList;
    } catch (JSONException | IllegalArgumentException e) {
      throw new MyDeticException("Format error in memory list JSON:" + e.toString());
    }
  }

  private void initMaps() {
    this.dates = new TreeMap<>();
    this.datesForYearMonth = new TreeMap<>();
  }

  public String getUserID() {
    return userId;
  }

  public void setUserID(String userID) {
    this.userId = userID;
  }

  public void setDate(LocalDate date) {
    dates.put(date, true);
    int year = date.getYear();
    int month = date.getMonthOfYear();
    if (!datesForYearMonth.containsKey(year)) {
      datesForYearMonth.put(year, new TreeMap<Integer, TreeMap<LocalDate, Boolean>>());
    }
    TreeMap<Integer, TreeMap<LocalDate, Boolean>> monthMap = datesForYearMonth.get(year);
    if (!monthMap.containsKey(month)) {
      monthMap.put(month, new TreeMap<LocalDate, Boolean>());
    }
    monthMap.get(month).put(date, true);
  }

  public boolean hasDate(LocalDate date) {
    return dates.containsKey(date);
  }

  /** @return Set of dates in ascending order */
  public Set<LocalDate> getDates() {
    return this.dates.keySet();
  }

  public Set<Integer> getYears() {
    return datesForYearMonth.keySet();
  }

  public Set<Integer> getMonthsForYear(int year) {
    if (datesForYearMonth.containsKey(year)) {
      return datesForYearMonth.get(year).keySet();
    } else {
      return new HashSet<>();
    }
  }

  public Set<LocalDate> getDatesForMonth(int year, int month) {
    if (datesForYearMonth.containsKey(year)) {
      TreeMap<Integer, TreeMap<LocalDate, Boolean>> monthMap = datesForYearMonth.get(year);
      if (monthMap.containsKey(month)) {
        return monthMap.get(month).keySet();
      }
    }
    return new HashSet<>();
  }

  public void clear() {
    this.dates.clear();
  }

  /**
   * Update this memory list's memories from another, adding or overwriting existing memories.
   *
   * @param memories a MemoryDataList to merge from.
   */
  public void mergeFrom(MemoryDataList memories) throws MyDeticException {
    if (!this.userId.equals(memories.getUserID())) {
      throw new MyDeticException("Tried to merge MemoryDataLists with different userIds");
    }
    for (LocalDate d : memories.getDates()) {
      this.setDate(d);
    }
  }

  public JSONObject toJSON() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    JSONArray memoryArray = new JSONArray();
    for (LocalDate date : dates.keySet()) {
      memoryArray.put(Utils.isoFormat(date));
    }
    jsonObject.put("memories", memoryArray);
    jsonObject.put("user_id", userId);
    return jsonObject;
  }

  /** @return a deep copy of this object. */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    MemoryDataList retval = (MemoryDataList) super.clone();
    retval.setUserID(this.userId);
    for (LocalDate d : this.getDates()) {
      retval.setDate(d);
    }
    return retval;
  }

  @Override
  public Iterator<LocalDate> iterator() {
    return dates.keySet().iterator();
  }
}
