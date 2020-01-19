package com.example.climaapp.singletons;

public class MainRequestQueue {
    private static final MainRequestQueue ourInstance = new MainRequestQueue();

    public static MainRequestQueue getInstance() {
        return ourInstance;
    }

    private MainRequestQueue() {
    }
}
