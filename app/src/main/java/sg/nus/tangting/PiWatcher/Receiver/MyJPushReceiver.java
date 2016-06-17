package sg.nus.tangting.PiWatcher.Receiver;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.orhanobut.logger.Logger;

import cn.jpush.android.api.JPushInterface;
import sg.nus.tangting.PiWatcher.Activity.MainActivity;
import sg.nus.tangting.PiWatcher.R;
import sg.nus.tangting.PiWatcher.Utils;

public class MyJPushReceiver extends BroadcastReceiver {

    public static final int JPUSH_LOCAL_NOTIFY_ID = 0x01;

    public MyJPushReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Logger.d("自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            processCustomMessage(context, bundle);

        }else {
            Logger.d("Unhandled intent - " + intent.getAction());
        }
    }

    //send msg to MainActivity
    private void processCustomMessage(Context context, Bundle bundle) {
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        long timestamp = Long.valueOf(message);
        message = Utils.timestampToDate(timestamp,null);

        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

        if (MainActivity.isForeground) {
            Logger.d("MainActivity is Foreground");

            Intent msgIntent = new Intent(MainActivity.JPUSH_MSG_RECEIVED_ACTION);
            msgIntent.putExtra(MainActivity.KEY_JPUSH_MESSAGE, message);
            if(Utils.isJson(extras)){
                msgIntent.putExtra(MainActivity.KEY_JPUSH_EXTRA, extras);
            }
            context.sendBroadcast(msgIntent);
        }else{
            Logger.d("MainActivity is not Foreground");
            if(Utils.isJson(extras)){
                createNotify(context, message, extras);
            }else{
                createNotify(context, message, null);
            }
        }
    }

    private void createNotify(Context context, String message, String extras){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notifications_raspberry)
                        .setContentTitle("Pi Watcher")
                        .setContentText(message);
        mBuilder.setAutoCancel(true).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS).setShowWhen(false);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(MainActivity.KEY_JPUSH_EXTRA, extras);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(JPUSH_LOCAL_NOTIFY_ID, mBuilder.build());
    }

}
