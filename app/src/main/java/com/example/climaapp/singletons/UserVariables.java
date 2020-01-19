package com.example.climaapp.singletons;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MainRequestQueue {

    private static MainRequestQueue ourInstance;

    private static Context ctx;
    private RequestQueue requestQueue;

    public static synchronized MainRequestQueue getInstance(Context context) {

        if (ourInstance == null) {

            ourInstance = new MainRequestQueue(context);
        }

        return ourInstance;
    }

    private MainRequestQueue(Context context) {

        ctx = context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {

        if (requestQueue == null) {

            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {

        getRequestQueue().add(req);
    }
}
