package com.wa2c.android.medoly.plugin.action.lyricsscraper.search;

import java.util.ArrayList;

/**
 * Search result.
 */
public class Result {
    /** Result info list. */
    private ArrayList<ResultItem> infoList;
    /** Page count. */
    private int pageCount;
    /** Current page. */
    private int currentPage;
    /** Valid. */
    private boolean valid;

    // Getter / Setter

    public ArrayList<ResultItem> getInfoList() {
        return infoList;
    }

    public void setInfoList(ArrayList<ResultItem> infoList) {
        this.infoList = infoList;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
