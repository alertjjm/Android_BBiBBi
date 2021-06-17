package com.example.smaplepush;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String myid;
    String channelId="channel";
    String channelName="Channel Name";
    private static String COPYDATA_NAME = "hmmess.db";
    private static String DATABASE_NAME="hmmess.db";
    public static String PACKAGE_DIR="com.example.smaplepush";
    private static String TABLE_NAME = "message";
    private static int DATABASE_VERSION = 1;
    private static final String TAG="MyMS";
    String from;
    String Datetime;
    private SQLiteDatabase db;
    MDatabaseHelper dbHelper;
    public MyFirebaseMessagingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myid=FirebaseInstanceId.getInstance().getId();
        dbHelper=new MDatabaseHelper(this);
        db=dbHelper.getWritableDatabase();
        db.execSQL("create table if not exists message (ID integer PRIMARY KEY autoincrement unique, DATETIME text not null, TEXTMESS text not null)");

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"onMessageReceived() 호출됨.");
        from = remoteMessage.getFrom();
        String contents;
        String title;
        if(remoteMessage.getNotification()!=null){
            contents=remoteMessage.getNotification().getBody();
            title=remoteMessage.getNotification().getTitle();
        }
        else{
            Map<String,String> dataset=remoteMessage.getData();
            contents=dataset.get("body");
            title=dataset.get("title");
        }
        Log.d(TAG, "from : " + from + ", contents : " + contents);
        long now=System.currentTimeMillis();
        Date mDate=new Date(now);
        SimpleDateFormat simpleDate=new SimpleDateFormat("MM월 dd일 hh:mm");
        Datetime=simpleDate.format(mDate);
        String cont=contents.replace("\'","\'\'");
        Cursor c = db.rawQuery("select * from message",null);
        int nu=c.getCount();
        nu=nu+1;
        String dbsql="insert into message (ID,DATETIME,TEXTMESS) values ("+Integer.toString(nu)+", "+"'"+Datetime+"', '"+cont+"')";
        db.execSQL(dbsql);
        if(!isAppIsInBackground(getApplicationContext())){
            sendToActivity(getApplicationContext(), from, contents,Datetime);
        }
        mynotification(title,contents);
    }
    private void mynotification(String title,String contents){
        int importance=NotificationManager.IMPORTANCE_HIGH;
        NotificationManager mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel=new NotificationChannel(channelId,channelName,importance);
            mNotificationManager.createNotificationChannel(mChannel);

        }
        Intent notiintent = new Intent(this, MainActivity.class);
        notiintent.putExtra("from",from);
        notiintent.putExtra("contents",contents);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notiintent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),channelId);
        builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.appicon));
        builder.setSmallIcon(R.drawable.appicon);
        builder.setTicker("JJM");
        builder.setContentTitle(title);
        builder.setContentText(contents);
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setNumber(999);
        mNotificationManager.notify(0, builder.build());
    }

    private void sendToActivity(Context context, String from, String contents, String dt) {
        Intent intent = new Intent("info");
        intent.putExtra("from", from);
        intent.putExtra("contents", contents);
        intent.putExtra("datetime",dt);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }
    private class MDatabaseHelper extends SQLiteOpenHelper {
        public MDatabaseHelper(Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                componentInfo = taskInfo.get(0).topActivity;
            }
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
