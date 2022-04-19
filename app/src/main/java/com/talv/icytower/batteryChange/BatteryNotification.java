package com.talv.icytower.batteryChange;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.talv.icytower.R;
import com.talv.icytower.activities.GameActivity;

public class BatteryNotification {


    private static final int NOTIFICATION_ID = 0;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;

    public BatteryNotification(Context context) {
        // initialize channel
        String name = "Battery Information";
        String description = "Notifies when the battery is low";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String channelID = "Battery information";
        NotificationChannel channel = new NotificationChannel(channelID, name, importance);
        channel.setDescription(description);
        notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent intent = new Intent(context, GameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Battery low")
                .setContentText("Your battery is low, please connect your phone to a charger")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
    }

    public void showNotification() {
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public String toString() {
        return "BatteryNotification{" +
                "notificationManager=" + notificationManager +
                ", builder=" + builder +
                '}';
    }
}
