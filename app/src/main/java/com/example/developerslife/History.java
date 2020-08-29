package com.example.developerslife;

import com.android.volley.RequestQueue;

import java.util.ArrayList;

public class History {
    private String apiUrl;
    private ArrayList<Item> history;
    private int currentIndex;

    public History(String feedUrl) {
        apiUrl = feedUrl;
        history = new ArrayList<>();
        currentIndex = 0;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void add(Item item) {
        history.add(item);
    }

    public Item current() {
        return history.get(currentIndex);
    }

    public Item next() {
        currentIndex = Math.min(lastIndex(), currentIndex + 1);
        return history.get(currentIndex);
    }

    public Item prev() {
        currentIndex = Math.max(0, currentIndex - 1);
        return history.get(currentIndex);
    }

    public int size() {
        return history.size();
    }

    public boolean empty() {
        return history.size() == 0;
    }

    public boolean isAtStart() {
        return currentIndex == 0;
    }

    public boolean isAtEnd() {
        return currentIndex == lastIndex();
    }

    private int lastIndex() {
        return history.size() - 1;
    }

}
