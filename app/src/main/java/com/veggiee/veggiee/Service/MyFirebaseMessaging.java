package com.veggiee.veggiee.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.veggiee.veggiee.CategoryActivity;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Helper.NotificationHelper;
import com.veggiee.veggiee.OrderStatusActivity;
import com.veggiee.veggiee.R;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "01";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sendNotificationAPI26(remoteMessage);
        else
            sendNotification(remoteMessage);
    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        String title = notification.getTitle();
        String body = notification.getBody();

        Intent intent = new Intent(this, OrderStatusActivity.class);
        intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhoneNumber());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper helper = new NotificationHelper(this);
        Notification.Builder builder = helper.getVeggieeChannelNotification(title, body, pendingIntent, defaultSoundUri);

        helper.getManager().notify(new Random().nextInt(), builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Intent intent = new Intent(this, OrderStatusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0, builder.build());
    }
}
