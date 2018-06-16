package net.ghosttrails.www.mydetic.api;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

/** Class that represents a single memory record */
public class MemoryData implements Cloneable {

  // values for the status column
  public static final int CACHESTATE_DEFAULT = 0; // legacy code.
  public static final int CACHESTATE_PENDING_SAVE = 1; // Saved to cache but not saved to API.
  public static final int CACHESTATE_SAVED = 2; // Saved to API.

  private String userId;
  private String memoryText;
  private LocalDate memoryDate;
  private int revision;
  private int cacheState;

  public MemoryData() {
    this(null, null, null, 1, CACHESTATE_DEFAULT);
  }

  public MemoryData(String userId, String memoryText, LocalDate memoryDate) {
    this(userId, memoryText, memoryDate, 1, CACHESTATE_DEFAULT);
  }

  public MemoryData(
      String userId, String memoryText, LocalDate memoryDate, int revision, int cacheState) {
    this.userId = userId;
    this.memoryText = memoryText;
    this.memoryDate = memoryDate;
    this.revision = revision;
    this.cacheState = cacheState;
  }

  /**
   * Build a MemoryData object from the JSON wire format.
   *
   * <p>{ "created_at": "2015-11-13T05:14:13.548922", "memory_date": "2015-11-12", "memory_text":
   * "Today my favourite TV show was cancelled :(", "modified_at": "2015-11-13T05:20:52.981991",
   * "user_id": "mreynolds" }
   *
   * @param jsonObject the JSON to parse.
   * @return a MemoryData object.
   * @throws MyDeticException on parse errors
   */
  public static MemoryData fromJSON(JSONObject jsonObject) throws MyDeticException {
    MemoryData memoryData = new MemoryData();
    try {
      String userId = jsonObject.getString("user_id");
      memoryData.setUserId(userId);
      memoryData.setMemoryDate(Utils.parseIsoDate(jsonObject.getString("memory_date")));
      memoryData.setMemoryText(jsonObject.getString("memory_text"));
      if (jsonObject.has("revision")) {
        memoryData.setRevision(jsonObject.getInt("revision"));
      }
      return memoryData;
    } catch (JSONException | IllegalArgumentException e) {
      throw new MyDeticException("Error parsing memory JSON", e);
    }
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getMemoryText() {
    return memoryText;
  }

  public void setMemoryText(String memoryText) {
    this.memoryText = memoryText;
  }

  public LocalDate getMemoryDate() {
    return memoryDate;
  }

  public void setMemoryDate(LocalDate memoryDate) {
    this.memoryDate = memoryDate;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getCacheState() {
    return cacheState;
  }

  public void setCacheState(int cacheState) {
    this.cacheState = cacheState;
  }

  public JSONObject toJSON() throws MyDeticException {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("user_id", userId);
      jsonObject.put("memory_date", Utils.isoFormat(memoryDate));
      jsonObject.put("memory_text", memoryText);
      jsonObject.put("revision", revision);
      return jsonObject;
    } catch (JSONException e) {
      throw new MyDeticException("Error converting MemoryData to JSON", e);
    }
  }

  /** Deep copy */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    super.clone();
    return new MemoryData(
        this.userId, this.memoryText, this.memoryDate, this.revision, this.cacheState);
  }
}
