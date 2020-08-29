package com.example.developerslife;

public class Item {
    private String url;
    private String title;

    public Item(String itemUrl, String itemTitle) {
        url = itemUrl;
        title = itemTitle;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
}
