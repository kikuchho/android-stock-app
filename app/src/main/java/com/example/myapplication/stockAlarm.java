package com.example.myapplication;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.PendingIntent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class stockAlarm extends AppCompatActivity {
    // page where you can set alarm when price price hit

    private WebSocketClient webSocketClient;

    //for alarm
    boolean isRinging = false;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;


    // there should be only one element in the array
    protected String [] stockArray = { "AAPL" };
    protected double [] prices = {0, 0, 0};

    //temporary storage for alarm price
    String tempAlarmPrice;

    //true -> alarm is on
    boolean isAlarmOn;


    public void goBackOnClick(View view) {
        webSocketClient.close(100,1000, "back");
        //getOnBackPressedDispatcher().onBackPressed();
        //finish();

        //Switching Between Activities Without Closing Them
        //https://stackoverflow.com/questions/55274020/switching-between-activities-without-closing-them
        if(isAlarmOn){
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }else{
            finish();
        }

    }

    //onclick function for add alarm button
    public void updateAlarmOnclick(View view) {

        EditText et_alarm = (EditText) findViewById(R.id.alarmPriceSetter);
        tempAlarmPrice = et_alarm.getText().toString() ;

        //temp code to display set alarm price
        TextView tv_alarm = (TextView) findViewById(R.id.currentAlarmPrice);
        tv_alarm.setText(tempAlarmPrice);

        //TODO IMPLEMENT A FUNCTION WHICH GET THE ENTERED VALUE AND INSERT IT TO THE DATABASE



    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock_alarm);



        //starting websocket connection
        createWebSocketClient();
        //starting  alarm service
        onStart();

        //get stock name and the current price for
        Intent i = getIntent();
        String symbol = i.getStringExtra("symbol");
        double price = i.getDoubleExtra("price",0);

        TextView viewText = (TextView) findViewById(R.id.textSymbol);
        viewText.setText(symbol);




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    //TODO IMPLEMENT FUNCTION FOR WHEN THE SWITCH IS TURNED ON
    public void onAlarmSwitchedOn(View view){
        long currentPrice;


        //when the switch is checked, it will set off alarm
        if( ((Switch) view).isChecked()  ){
            Toast.makeText(stockAlarm.this, "ALARM ON", Toast.LENGTH_SHORT).show();
            isAlarmOn = true;
        }
        else {
            //alarmManager.cancel(pendingIntent);
            Toast.makeText(stockAlarm.this, "ALARM OFF", Toast.LENGTH_SHORT).show();
            isAlarmOn = false;

            //onStop();
        }


    }


    //for ringing alarm sounds
    //https://stackoverflow.com/questions/53377987/making-the-music-play-in-the-background-after-the-app-has-been-closed

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onRing() {
        Intent intent = new Intent(this, BackgroundSoundService.class);
        startService(intent);

        isRinging = true;
    }

    @Override
    public void onStop() {
            super.onStop();
            Intent intent = new Intent(this, BackgroundSoundService.class);
            stopService(intent);

    }

    //end of alarm classes

    @Override
    protected void onRestart() {

        super.onRestart();
        createWebSocketClient();
    }




    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to the finnhub
            uri = new URI("wss://ws.finnhub.io?token=cqofvtpr01qk95832thgcqofvtpr01qk95832ti0");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }


        webSocketClient = new WebSocketClient(uri) {


            @Override
            public void onOpen() {
                Log.i("WebSocket_stockAlarm", "Session is starting");

                //JsonObject value = new JsonObject();

                //value.addProperty("type", "subscribe");
                //value.addProperty("symbol", "AAPL");

                String dataToSend = "{\"type\": \"subscribe\", \"symbol\": \"AAPL\"  }";
                webSocketClient.send(dataToSend);

                Log.d("Websocket_stockAlarm", "subscribed aapl" );



            }



            @Override
            public void onTextReceived(String s) {

                try {
                    JsonObject converted = new JsonParser().parse(s).getAsJsonObject();

                    Log.d("WebSocket_stockAlarm", "json parsing");

                    Log.d("WebSocket_stockAlarm",  converted.get("type").toString() );

                    if(  converted.get("type").toString().contains("trade")  ){
                        Log.d("WebSocketOT", converted.get("data").toString());
                        // extract price from the json
                        JsonArray data = converted.get("data").getAsJsonArray();
                        //Log.d("WebSocketOT", data.get(0).getAsJsonObject().get("p").toString() );

                        //TODO FOREACH LOOP TO UPDATE THE PRICE FOR EACH STOCK, update the strings in string.xml to reflect the update in each activity
                        //loop through data to update the price for each registered symbol
                        int i = 0;
                        for(String Symbol : stockArray ){

                            for (JsonElement smallData : data ) {
                                if(smallData.getAsJsonObject().get("s").toString().contains(Symbol)){
                                    prices[i] = Double.parseDouble( smallData.getAsJsonObject().get("p").toString() ) ;

                                    //break;
                                }
                            }


                            i++;

                        }
                        //update the current price
                        TextView viewText = (TextView) findViewById(R.id.currentPrice);


                        Log.i("onTextReceived", Arrays.toString(prices));

                        //Only the original thread that created a view hierarchy can touch its views. SO USE HANDLER TO EXCUTE THE CODE WITHOUT disturbing the main UI Thread.
                        //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
                        new Handler(Looper.getMainLooper()).post(new Runnable(){
                            @Override
                            public void run() {
                                viewText.setText( data.get(0).getAsJsonObject().get("p").toString() );
                            }
                        });

                        //call callback function if the price hits target price from low to top
                        if(isAlarmOn){

                            if( prices[0] >= Double.parseDouble(tempAlarmPrice)  ){
                                Log.i("Alarm",  "currentP >= alarmP"  );

                                onRing();
                            }
                        }

                        //customBaseAdapter.notifyDataSetChanged();


                    }else{
                        Log.e("WebSocket_stockAlarm", "error: stock conversion failed: type is not trade ");
                    }



                }catch (JsonParseException err){
                    Log.d("Error", err.toString());
                }


            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                Log.e("WebSocket_stockAlarm", e.getMessage());
            }

            @Override
            public void onCloseReceived(int num , String string) {
                Log.i("WebSocket_stockAlarm", "Closed ");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }



}