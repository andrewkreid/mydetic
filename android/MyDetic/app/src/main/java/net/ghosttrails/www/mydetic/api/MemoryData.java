package net.ghosttrails.www.mydetic.api;

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

}
