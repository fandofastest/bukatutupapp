package net.bukatutup.mynewsapp.app;

import android.app.Application;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by Ashiq on 4/13/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseMessaging.getInstance().subscribeToTopic("wpnewsnotification");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

    }
}
