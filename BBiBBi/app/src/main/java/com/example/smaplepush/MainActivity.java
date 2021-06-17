package com.example.smaplepush;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    TextView textView2;
    EditText editText;
    String newToken;
    String mid;
    RequestQueue requestQueue;
    String regId;
    String user1="dVSL77Bp6Zc";
    String user1key=":APA91bHLv3cGTLFu_bnMgm0PTGFkkwwrq1SExEUwxvHZm--wzeGSiWbrNA95QOI9ogca_WhZjUpJKUwIFkC2zTrWRY8FeB31gYIE3IytkEJzVyH6D8hU4RtnRhe3oafWAiwJ2wv2Oswq";
    String user2="fKZn8amunqc";
    String user2key=":APA91bH5q2ZXi5ZmiERvt4In5VbpHl3LAzWs-cnIqYkJpS_hoAisIapn_FBOybaxwng6L9aKwtpnO5CR28cN4wVq9ek5RiPvVEZSzXd3wYGCIL5xaobHUrOb4VVqfBT9V1KT5mZ_fo3j";
    String user3="euNBKCArypU";
    String user3key=":APA91bGesji0AyBCojEBXEeUJXhFYYTSllqp8WBewJR9bRd8O4s7D9sylEUypawdGY5MXvYuQMDEM_J6Tw9_b0AIFWrzzEThOA36JLpN12B7GQ0Ot2NlqFChfPnbwluMXcVG10JtojZg";
    String receivedmess;
    DatabaseHelper dbHelper;
    Button button2;
    int num;
    private BroadcastReceiver mBroadcastReceiver;
    private static String COPYDATA_NAME = "hmmess.db";
    private static String DATABASE_NAME="hmmess.db";
    public static String PACKAGE_DIR="com.example.smaplepush";
    private static String TABLE_NAME = "message";
    private static int DATABASE_VERSION = 1;
    final String AUTH_TOKEN="AAAAk_EU690:APA91bFXGP7BXUu8Qqpty-LU95QbXQ3XCBA3FQi_E42J4hgP7GSUY9jFDT3wIV6lkEX9UebfROJZM-4dSx7u24TrSQ1Lch1Uj_M2zV3JVtVvWxge024kj6VNDUJPTlue09Y-y0Coqhjl";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView2=(TextView)findViewById(R.id.textView2);
        editText=(EditText)findViewById(R.id.editText);
        dbHelper=new DatabaseHelper(this);
        db=dbHelper.getWritableDatabase();
        db.execSQL("create table if not exists message (ID integer PRIMARY KEY autoincrement unique, DATETIME text not null, TEXTMESS text not null)");
        dbinitprocess();
        if(receivedmess!=null){
            textView2.setText(receivedmess);
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult result) {
                newToken = result.getToken();
                Log.d("JJM",newToken);
            }
        });
        regId = newToken;
        button2=findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("전체 메시지 삭제").setMessage("삭제 하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        db.execSQL("delete from message");
                        dbinitprocess();
                        Toast.makeText(getApplicationContext(),"메시지가 전체 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mes=editText.getText().toString();
                if(mes.equals("")){
                    Toast.makeText(getApplicationContext(),"메시지를 입력하세요",Toast.LENGTH_SHORT).show();
                }
                else{
                    mid=FirebaseInstanceId.getInstance().getId();
                    send(mes);
                    Toast.makeText(getApplicationContext(),"전송완료",Toast.LENGTH_SHORT).show();
                    editText.setText("");
                }
            }
        });
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        mBroadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String from = intent.getStringExtra("from");
                if (from == null) {
                    return;
                }

                String contents = intent.getStringExtra("contents");
                String dt=intent.getStringExtra("datetime");
                textView2.append(dt+": " + contents+"\n");
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("info"));

    }
    private void dbinitprocess(){
        Cursor c = db.rawQuery("select * from message",null);
        num=c.getCount();
        textView2.setText("");
        if(num==0){
            return;
        }
        else if(num<11){
            c=db.rawQuery("select * from message order by ID asc",null);
            while(c.moveToNext()){
                String contents = c.getString(c.getColumnIndex("TEXTMESS"));
                String dt=c.getString(c.getColumnIndex("DATETIME"));
                textView2.append(dt+": " + contents+"\n");
            }
        }
        else{
            String ss="select * from message where ID > "+Integer.toString(num-10)+" order by ID asc;";
            c=db.rawQuery(ss,null);
            while(c.moveToNext()){
                String contents = c.getString(c.getColumnIndex("TEXTMESS"));
                String dt=c.getString(c.getColumnIndex("DATETIME"));
                //int n=c.getInt(c.getColumnIndex("ID"));
                textView2.append(dt+": " + contents+"\n");
            }
        }
    }
    public void send(String input) {

        JSONObject requestData = new JSONObject();

        try {
            requestData.put("priority", "high");

            JSONObject dataObj = new JSONObject();
            dataObj.put("body", input);
            if(mid.equals(user1)){
                //현지이면
                dataObj.put("title","현지");
            }
            else if(mid.equals(user2)){
                //종민이면
                dataObj.put("title","종민");
            }
            else if(mid.equals(user3)){
                dataObj.put("title","봇");
            }
            requestData.put("data", dataObj);
            JSONArray idArray = new JSONArray();
            if(mid.equals(user1)){
                //현지이면
                idArray.put(0,user2+user2key);
            }
            else if(mid.equals(user2)){
                //종민이면 테스트용
                idArray.put(0, user1+user1key);
            }
            else if(mid.equals(user3)){
                idArray.put(0, user2+user2key);
            }
            requestData.put("registration_ids", idArray);

        } catch(Exception e) {
            e.printStackTrace();
        }

        sendData(requestData, new SendResponseListener() {
            @Override
            public void onRequestCompleted() {
            }

            @Override
            public void onRequestStarted() {
            }

            @Override
            public void onRequestWithError(VolleyError error) {
                Log.d("JJM",error.toString());
            }
        });
    }
    public interface SendResponseListener {
        public void onRequestStarted();
        public void onRequestCompleted();
        public void onRequestWithError(VolleyError error);
    }


    public void sendData(JSONObject requestData, final SendResponseListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://fcm.googleapis.com/fcm/send",
                requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onRequestCompleted();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onRequestWithError(error);
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Authorization","key=AAAAk_EU690:APA91bFXGP7BXUu8Qqpty-LU95QbXQ3XCBA3FQi_E42J4hgP7GSUY9jFDT3wIV6lkEX9UebfROJZM-4dSx7u24TrSQ1Lch1Uj_M2zV3JVtVvWxge024kj6VNDUJPTlue09Y-y0Coqhjl");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setShouldCache(false);
        listener.onRequestStarted();
        requestQueue.add(request);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            processIntent(intent);
        }
        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        String from = intent.getStringExtra("from");
        if (from == null) {
            return;
        }

        String contents = intent.getStringExtra("contents");
        String dt=intent.getStringExtra("datetime");
        textView2.append(dt+": " + contents+"\n");
        //String dbsql="insert into message (DATETIME,TEXTMESS) values ('"+dt+"', '"+contents+"')";
        //db.execSQL(dbsql);
        receivedmess=contents;
    }
    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context){
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
}
