package com.wbohn.recipefinder;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class App extends Application {
    private static Bus eventBus;

    @Override
    public void onCreate() {
        super.onCreate();

        eventBus = new Bus(ThreadEnforcer.ANY);
    }

    public static Bus getEventBus() {
        return eventBus;
    }
}
