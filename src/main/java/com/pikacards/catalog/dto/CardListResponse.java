package com.pikacards.catalog.dto;
import java.util.List;
public class CardListResponse {
    private int page; private int pageSize; private long total; private List<CardResponse> results;
    public CardListResponse(int page, int pageSize, long total, List<CardResponse> results) {
        this.page = page; this.pageSize = pageSize; this.total = total; this.results = results;
    }
    public int getPage() { return page; } public int getPageSize() { return pageSize; }
    public long getTotal() { return total; } public List<CardResponse> getResults() { return results; }
}
