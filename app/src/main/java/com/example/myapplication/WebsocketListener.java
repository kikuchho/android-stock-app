package com.example.myapplication;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.net.URI;
import dev.gustavoavila.websocketclient.WebSocketClient;
import androidx.appcompat.app.AppCompatActivity;


import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import com.example.myapplication.MainActivity.*;


//https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/WebSocketEcho.java












        /*
    webSocketClient = new WebSocketClient(uri) {


        @Override
        public void onOpen() {
            Log.i("WebSocket", "Session is starting");

            //JsonObject value = new JsonObject();

            //value.addProperty("type", "subscribe");
            //value.addProperty("symbol", "AAPL");

            String dataToSend = "{\"type\": \"subscribe\", \"symbol\": \"AAPL\"  }";

            webSocketClient.send(dataToSend);

            Log.d("Websocket", "subscribed aapl" );



        }



        @Override
        public void onTextReceived(String s) {
            Log.i("WebSocket", "Message received");

            Log.d("WebSocket", s   );

            try {
                JsonObject converted = new JsonParser().parse(s).getAsJsonObject();

                Log.d("WebSocket", "json parsing");

                Log.d("WebSocket",  converted.get("type").toString() );




                if(  converted.get("type").toString().contains("trade")  ){
                    Log.d("WebSocket", converted.get("data").toString());
                    // extract price from the json
                    JsonArray data = converted.get("data").getAsJsonArray();
                    data.get(0).getAsJsonObject().get("p");




                    Log.d("WebSocket", data.get(0).toString() );


                }else{
                    Log.e("WebSocket", "error: stock conversion failed: type is not trade ");
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
            Log.e("WebSocket", e.getMessage());
        }

        @Override
        public void onCloseReceived(int num , String string) {
            Log.i("WebSocket", "Closed ");
        }
    };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    */






    public class WebsocketListener {

        WebSocketClient webSocketClient;


        public WebsocketListener(URI uri, String [] stockArray, double [] prices , View view, boolean isAlarmOn, String tempAlarmPrice ) {

            webSocketClient = new WebSocketClient(uri) {


                @Override
                public void onOpen() {
                    Log.i("WebSocket_stockAlarm", "Session is starting");

                    //JsonObject value = new JsonObject();

                    //value.addProperty("type", "subscribe");
                    //value.addProperty("symbol", "AAPL");

                    String dataToSend = "{\"type\": \"subscribe\", \"symbol\": \"AAPL\"  }";
                    webSocketClient.send(dataToSend);

                    Log.d("Websocket_stockAlarm", "subscribed aapl");


                }


                @Override
                public void onTextReceived(String s) {

                    try {
                        JsonObject converted = new JsonParser().parse(s).getAsJsonObject();

                        Log.d("WebSocket_stockAlarm", "json parsing");

                        Log.d("WebSocket_stockAlarm", converted.get("type").toString());

                        if (converted.get("type").toString().contains("trade")) {
                            Log.d("WebSocketOT", converted.get("data").toString());
                            // extract price from the json
                            JsonArray data = converted.get("data").getAsJsonArray();
                            //Log.d("WebSocketOT", data.get(0).getAsJsonObject().get("p").toString() );

                            //TODO FOREACH LOOP TO UPDATE THE PRICE FOR EACH STOCK, update the strings in string.xml to reflect the update in each activity
                            //loop through data to update the price for each registered symbol
                            int i = 0;
                            for (String Symbol : stockArray) {

                                for (JsonElement smallData : data) {
                                    if (smallData.getAsJsonObject().get("s").toString().contains(Symbol)) {
                                        prices[i] = Double.parseDouble(smallData.getAsJsonObject().get("p").toString());

                                        //break;
                                    }
                                }


                                i++;

                            }
                            //update the current price
                            TextView viewText = (TextView) view.findViewById(R.id.currentPrice);




                            Log.i("onTextReceived", Arrays.toString(prices));

                            //Only the original thread that created a view hierarchy can touch its views. SO USE HANDLER TO EXCUTE THE CODE WITHOUT disturbing the main UI Thread.
                            //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    viewText.setText(data.get(0).getAsJsonObject().get("p").toString());
                                }
                            });

                            //call callback function if the price hits target price from low to top
                            if (isAlarmOn) {

                                if (prices[0] >= Double.parseDouble(tempAlarmPrice)) {
                                    Log.i("Alarm", "currentP >= alarmP");

                                    // TODO onRing();
                                }
                            }

                            //customBaseAdapter.notifyDataSetChanged();


                        } else {
                            Log.e("WebSocket_stockAlarm", "error: stock conversion failed: type is not trade ");
                        }


                    } catch (JsonParseException err) {
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
                public void onCloseReceived(int num, String string) {
                    Log.i("WebSocket_stockAlarm", "Closed ");
                }
            };

            webSocketClient.setConnectTimeout(10000);
            webSocketClient.setReadTimeout(60000);
            webSocketClient.enableAutomaticReconnection(5000);
            webSocketClient.connect();
        }
    }
