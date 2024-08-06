package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.gustavoavila.websocketclient.WebSocketClient;
import stockData.Price;

import org.json.JSONException;
import org.json.JSONObject;




public class MainActivity extends AppCompatActivity {

    private WebSocketClient webSocketClient;

    EditText et_symbol;
    Button mainMenu_btn;


    //TODO GET ALARMON AND ALARMPRICE FROM DATABASE
    //1, call getallstocks to get list of stock
    //2. use foreach loop to update the stockarray and prices
    //3. this will be done in oncreate
    //4. for when add stock is pressed and  , implement same code in the addstock function as well
    protected ArrayList<String> stockArray = new ArrayList<>();
    protected ArrayList<Double>  prices = new ArrayList<>();

    //MainStockModel is used to fill the listview with the array
    List<StockModel> MainStockModel = new ArrayList<>();

    // CAN NOT GET CONTEXT BEFORE ONCREATE
    //protected CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(getApplicationContext(), stockArray, prices );

    //TODO : USE PUTEXTRA() WHEN SWITCHING ACTIVITY TO PREVENT :  WHEN ALARM IS RINGING, IF YOU SWITCH THE ACTIVITY , IT WILL THROW ERROR,
    boolean isRinging = false;



    public void addStock(View view) {

        StockModel stockModel;

        try {
            et_symbol = findViewById(R.id.editTextSymbol);
            //temporary func: get aapl data from finnhub
             stockModel = new StockModel(-1, et_symbol.getText().toString());


        } catch (Exception e) {
            
            Toast.makeText(MainActivity.this, "error creating model instance", Toast.LENGTH_LONG).show();
            stockModel = new StockModel(-1, "error");
        }

        DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);

        boolean success = dataBaseHelper.addOne(stockModel);


        Toast.makeText(MainActivity.this, "success" + success, Toast.LENGTH_LONG).show();



    }


    public void lanuchStockAlarm(View v, int pos) {
        //close the connection
        // WHILE CONNECTING TO WEBSOCKET, CUT CONNECTION WHEN stockalarm connection ACTIVITY IS OPEN
        //https://stackoverflow.com/questions/13220091/how-to-run-code-when-coming-back-to-activity
        webSocketClient.close(1,1000,"new activity opened");
        //launch new activity when row clicked
        Intent intent = new Intent(this, stockAlarm.class);
        //send the
        intent.putExtra("symbol", MainStockModel.get(pos).getSymbol());
        intent.putExtra("price", MainStockModel.get(pos).getPrice());

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //set the title of the label on top when opening app
        //setTitle("stock app");

        et_symbol = findViewById(R.id.editTextSymbol);



        //create array and populate list view
        ListView listView = (ListView) findViewById(R.id.stocksList);

        //fill the array with data from db
        int i = 0;
        DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);
//        for ( StockModel stockModel : dataBaseHelper.GetAllStocks() ) {
//
//            stockArray[i] = stockModel.getSymbol();
//            prices[i] = stockModel.getPrice();
//            i++;
//        }
        MainStockModel = dataBaseHelper.GetAllStocks();


        // initialize custombaseadater to use it
        //TODO : USE THE WEBSOCKET  LISTENER IN ORDER TO INITIALIZE THE INSTANCE OF THE CUSTOM ADAPTER AND USE IT IN WEBSOCKET CLIENT (ONTEXTRECEIVED METHOD)
        CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(getApplicationContext(), MainStockModel );


        //starting websocket connection
        createWebSocketClient();

        //associate this adapter to the actual screen controller
        listView.setAdapter(customBaseAdapter);

        //onclick event listener for stock list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i("stockListView", "Item is clicked at " + position);
                    // calling launchstockalarm

                    lanuchStockAlarm(view, position);
                }
            }
        );

        //TODO : IMPLEMENT BTN_ADDSTOCK.ONCLICKLISTENER AND UPDATE VIEWLIST




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //this will run the code below when coming back to this activity
    //https://stackoverflow.com/questions/13220091/how-to-run-code-when-coming-back-to-activity
    @Override
    protected void onRestart() {

        super.onRestart();
        createWebSocketClient();
    }



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

        isRinging = false;

        super.onStop();
        Intent intent = new Intent(this, BackgroundSoundService.class);
        stopService(intent);

    }

    //end of alarm classes






    //TODO IMPLEMENT : FETCH ALARMPRICE AND CODE ON MESSAGE JUST LIKE THE THE ONE IN STOCKALARM
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
                Log.i("WebSocket_Main", "Session is starting");

                //JsonObject value = new JsonObject();

                //value.addProperty("type", "subscribe");
                //value.addProperty("symbol", "AAPL");

//                String dataToSend = "{\"type\": \"subscribe\", \"symbol\": \"AAPL\"  }";
//                String dataToSend2 = "{\"type\": \"subscribe\", \"symbol\": \"META\"  }";
//                String dataToSend3 = "{\"type\": \"subscribe\", \"symbol\": \"NVDA\"  }";
//                webSocketClient.send(dataToSend);
//                webSocketClient.send(dataToSend2);
//                webSocketClient.send(dataToSend3);

                for ( StockModel stockModel  : MainStockModel ) {
                    String dataToSend = "{\"type\": \"subscribe\", \"symbol\":   \" " + stockModel.getSymbol() + " \"  }";

                    webSocketClient.send(dataToSend);
                }



                Log.d("WebSocket_Main", "subscribed aapl" );



            }



            @Override
            public void onTextReceived(String s) {

                try {
                    JsonObject converted = new JsonParser().parse(s).getAsJsonObject();

                    Log.d("WebSocket_Main", "json parsing");

                    Log.d("WebSocket_Main",  converted.get("type").toString() );

                    if(  converted.get("type").toString().contains("trade")  ){
                        Log.d("WebSocketOT", converted.get("data").toString());
                        // extract price from the json
                        JsonArray data = converted.get("data").getAsJsonArray();
                        //Log.d("WebSocketOT", data.get(0).getAsJsonObject().get("p").toString() );

                        //TODO FOREACH LOOP TO UPDATE THE PRICE FOR EACH STOCK, update the strings in string.xml to reflect the update in each activity

                        //get stock model list from db
                        DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);
                        List<StockModel> stocks =  dataBaseHelper.GetAllStocks();

                        //UPDATE IN CLASS MAINSTOCKMODEL ARRAY ; loop through data to update the prices array that is DEFINED IN THIS CLASS for each registered symbol
                        int i = 0;
                        //for(String Symbol : stockArray ){
                        for( StockModel stockmodel : MainStockModel ){

                            String Symbol = stockmodel.getSymbol();

                            for (JsonElement smallData : data ) {
                                if(smallData.getAsJsonObject().get("s").toString().contains(Symbol)){
                                    //prices[i] = Double.parseDouble( smallData.getAsJsonObject().get("p").toString() ) ;

                                    MainStockModel.get(i).setPrice( Double.parseDouble( smallData.getAsJsonObject().get("p").toString() ) );


                                    //boolean success = dataBaseHelper.UpdateOne();
                                    //break;
                                }
                            }
                            i++;

                        }

                        //UPDATE RT PRICE DATA IN DATABASE : loop through data to update the price ON DATABASE for each registered symbol
                        int temp = 0;
                        for(StockModel stockModel : stocks ){

                            for (JsonElement smallData : data ) {
                                if( smallData.getAsJsonObject().get("s").toString().contains(stockModel.getSymbol()) ){

                                    //store the price temporally and update the current
                                    double price = Double.parseDouble( smallData.getAsJsonObject().get("p").toString() ) ;

                                    int success = dataBaseHelper.UpdateOne( price , stockModel.getId() );

                                    Toast.makeText(MainActivity.this, "number of rows updated" + success, Toast.LENGTH_LONG).show();

                                    //break;
                                }
                            }
                            temp++;

                        }




                        Log.i("onTextReceived", MainStockModel.toString());



                        //TODO FOR EACH LOOP STOCKS TO UPDATE CURRENT PRICES AND CHECK IF ALARMPRICE IS ABOVE / BELOW  CURRENT PRICE


                        // ALARM : call callback function if the price hits target price from low to top
                        for(StockModel stockModel : stocks ){

                            //check if the priceatm is set
                            if(stockModel.getPriceAtm() == -1)
                            {
                                if(stockModel.isAlarmSet()){

                                    if(stockModel.getPriceAtm() > stockModel.getAlarmPrice()){
                                        if( stockModel.getAlarmPrice() >= stockModel.getPrice() ){

                                            Log.i("Alarm",  "currentP >= alarmP"  );

                                            //if not ringing, ring the alarm
                                            if(!isRinging){

                                                onRing();
                                            }

                                        }

                                    }else {
                                        if( stockModel.getAlarmPrice() <= stockModel.getPrice()  ){
                                            //stockModel.getAlarmPrice() <= Double.parseDouble(tempAlarmPrice)
                                            Log.i("Alarm",  "currentP <= alarmP"  );

                                            if(!isRinging){

                                                onRing();
                                            }

                                        }
                                    }



                                }

                            }


                        }

                        //customBaseAdapter.notifyDataSetChanged();


                    }else{
                        Log.e("WebSocket_Main", "error: stock conversion failed: type is not trade ");
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
    }



}



// private void createWebSocketClient() {
//     URI uri;
//     try {
//         // Connect to the finnhub
//         uri = new URI("wss://ws.finnhub.io?token=cp7hldpr01qpb9raopn0cp7hldpr01qpb9raopng");
//     }
//     catch (URISyntaxException e) {
//         e.printStackTrace();
//         return;
//     }
//
//     webSocketClient = new WebSocketClient(uri) {
//
//
//         @Override
//         public void onOpen() {
//             Log.i("WebSocket", "Session is starting");
//
//             //JsonObject value = new JsonObject();
//
//             //value.addProperty("type", "subscribe");
//             //value.addProperty("symbol", "AAPL");
//
//             String dataToSend = "{\"type\": \"subscribe\", \"symbol\": \"AAPL\"  }";
//             String dataToSend2 = "{\"type\": \"subscribe\", \"symbol\": \"META\"  }";
//             String dataToSend3 = "{\"type\": \"subscribe\", \"symbol\": \"NVDA\"  }";
//
//             webSocketClient.send(dataToSend);
//             webSocketClient.send(dataToSend2);
//             webSocketClient.send(dataToSend3);
//
//             Log.d("Websocket", "subscribed aapl" );
//
//
//
//         }
//
//
//
//         @Override
//         public void onTextReceived(String s) {
//             Log.i("WebSocket", "Message received");
//
//             Log.d("WebSocket", s   );
//
//             try {
//                 JsonObject converted = new JsonParser().parse(s).getAsJsonObject();
//
//                 Log.d("WebSocket", "json parsing");
//
//                 Log.d("WebSocket",  converted.get("type").toString() );
//
//                  /*
//                    //equals() not working why? contains() works
//                    if( converted.get("type").toString() == "trade"  ){
//                        Log.d("WebSocket", "true");
//                    }else{
//                        Log.d("WebSocket", "false");
//                    }
//                    */
//
//
//                 if(  converted.get("type").toString().contains("trade")  ){
//                     Log.d("WebSocket", converted.get("data").toString());
//                     // extract price from the json
//                     JsonArray data = converted.get("data").getAsJsonArray();
//                     data.get(0).getAsJsonObject().get("p");
//
//                     //TODO FOREACH LOOP TO UPDATE THE PRICE FOR EACH STOCK, update the strings in string.xml to reflect the update in each activity
//                     int i = 0;
//                     for (JsonElement symbol : data ){
//                         //String target = getResources().getString(R.string.symbol);
//
//                         TextView currentPrice = (TextView) findViewById(R.id.RTPrice);
//                         //update the array and notify the adapter of the change and make it update the value on screen
//                         prices[i] = Double.parseDouble( symbol.getAsJsonObject().get("p").toString() ) ;
//                         i++;
//
//                     }
//                     //customBaseAdapter.notifyDataSetChanged();
//
//
//
//
//                     Log.d("WebSocket", data.get(0).toString() );
//
//
//                 }else{
//                     Log.e("WebSocket", "error: stock conversion failed: type is not trade ");
//                 }
//
//
//
//             }catch (JsonParseException err){
//                 Log.d("Error", err.toString());
//             }
//
//
//         }
//
//        //belows are same
// }
