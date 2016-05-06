package ro.expectations.expenses.app;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

import ro.expectations.expenses.DefaultEventBusIndex;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Event Bus.
        EventBus.builder().addIndex(new DefaultEventBusIndex()).installDefaultEventBus();
    }
}
