package com.klynox.brandy;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by YOBO on 2/21/2018.
 */

// This application class retains offline details for UI user
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
