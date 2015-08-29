package net.ghosttrails.www.mydetic.api;

import net.ghosttrails.www.mydetic.exceptions.MyDeticException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Class that represents a single memory record
 */
public class MemoryData {

  private String userId;
  private String memoryText;
  private Date memoryDate;

  public MemoryData() {
    this.userId = null;
    this.memoryDate = null;
    this.memoryText = null;
  }

  public MemoryData(String userId, String memoryText, Date memoryDate) {
    this.userId = userId;
    this.memoryText = memoryText;
    this.memoryDate = (Date) memoryDate.clone();
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

  public Date getMemoryDate() {
    return memoryDate;
  }

  public void setMemoryDate(Date memoryDate) {
    this.memoryDate = memoryDate;
  }

  /**
   * Build a MemoryData object from the JSON wire format.
   *
   * {
   *   "created_at": "2015-11-13T05:14:13.548922",
   *   "memory_date": "2015-11-12",
   *   "memory_text": "Today my favourite TV show was cancelled :(",
   *   "modified_at": "2015-11-13T05:20:52.981991",
   *   "user_id": "mreynolds"
   * }
   *
   * @param jsonObject
   * @return a MemoryData object.
   * @throws MyDeticException on parse errors
   */
  static public MemoryData fromJSON(JSONObject jsonObject) throws MyDeticException {
    MemoryData memoryData = new MemoryData();
    try {
      String userId = jsonObject.getString("user_id");
      memoryData.setUserId(userId);
      memoryData.setMemoryDate(Utils.parseIsoDate(jsonObject.getString("memory_date")));
      memoryData.setMemoryText(jsonObject.getString("memory_text"));
      return memoryData;
    } catch (JSONException | ParseException e) {
      throw new MyDeticException("Error parsing memory JSON", e);
    }
  }

  public JSONObject toJSON() throws MyDeticException {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("user_id", userId);
      jsonObject.put("memory_date", Utils.isoFormat(memoryDate));
      jsonObject.put("memory_text", memoryText);
      return jsonObject;
    } catch (JSONException e) {
      throw new MyDeticException("Error converting MemoryData to JSON", e);
    }
  }

  /**
   * Deep copy
   */
  @Override
  protected Object clone() {
    return new MemoryData(this.userId, this.memoryText, this.memoryDate);
  }
}
