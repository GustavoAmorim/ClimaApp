package com.example.climaapp.shared.services;

import android.app.Notification;
import android.util.Log;

import com.example.climaapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inlocomedia.android.engagement.InLocoEngagement;
import com.inlocomedia.android.engagement.PushMessage;
import com.inlocomedia.android.engagement.request.FirebasePushProvider;
import com.inlocomedia.android.engagement.request.PushProvider;

import java.util.Map;

public class FirebaseMsgService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    public FirebaseMsgService() {
    }

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        // OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
        //        .build();
        // WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Getting the Notification Data
        final Map<String, String> data = remoteMessage.getData();

        if (data != null) {
            // Decoding the notification data HashMap
            final PushMessage pushContent = InLocoEngagement.decodeReceivedMessage(this, data);

            if (pushContent != null) {
                // Presenting the notification
                InLocoEngagement.presentNotification(
                        this, // Context
                        pushContent,  // The notification message hash
                        R.drawable.ic_notification, // The notification icon drawable resource to display on the status bar. Put your own icon here. You can also use R.drawable.ic_notification for testing.
                        1111111  // Optional: The notification identifier
                );
            } else {
                // It's your regular message. Do as you used to do.
            }
        }
    }

    @Override
    public void onNewToken(String firebaseToken) {

        if (firebaseToken != null && !firebaseToken.isEmpty()) {

            final PushProvider pushProvider = new FirebasePushProvider.Builder()
                    .setFirebaseToken(firebaseToken)
                    .build();
            InLocoEngagement.setPushProvider(this, pushProvider);
        }
    }
}
