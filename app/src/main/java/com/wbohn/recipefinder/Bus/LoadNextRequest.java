package com.wbohn.recipefinder.Bus;

public class LoadNextRequest {
    private int page;

    public LoadNextRequest(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }
}
