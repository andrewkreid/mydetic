package net.ghosttrails.www.mydetic.api;

import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

/**
 * A List of Memory dates returned from the API
 */
public class MemoryDataList {

    private String userID;
    private TreeMap<Date, Boolean> dates;

    public MemoryDataList() {
        this.dates = new TreeMap<Date, Boolean>();
    }

    public MemoryDataList(String userID, TreeMap<Date, Boolean> dates) {
        this.userID = userID;
        this.dates = dates;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setDate(Date date) {
        dates.put(date, true);
    }

    public boolean hasDate(Date date) {
        return dates.containsKey(date);
    }

    /**
     *
     * @return Set of dates in ascending order
     */
    public Set<Date> getDates() {
        return this.dates.keySet();
    }

}
