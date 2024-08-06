package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String STOCK_TABLE = "STOCK_TABLE";
    public static final String COLUMN_SYMBOL = "SYMBOL";
    public static final String COLUMN_PRICE = "PRICE";
    public static final String COLUMN_ISALARMSET = "ISALARMSET";
    //public static final String COLUMN_ALARMTIME = "ALARMTIME";
    public static final String COLUMN_ALARMPRICE = "ALARMPRICE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_PRICEATM = "PRICEATM";


    public DataBaseHelper(@Nullable Context context) {
        super(context, "Stocks.db", null, 1);
    }

    //this is called the first time the database is accessed
    //THIS WILL BE CALLED AUTOMATICALLY
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + STOCK_TABLE + " (" + COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_SYMBOL + " TEXT, " + COLUMN_PRICE + " DOUBLE, " + COLUMN_ISALARMSET + " BOOL, " +  COLUMN_ALARMPRICE + " DOUBLE ," + COLUMN_PRICEATM + " DOUBLE  )";

        db.execSQL(createTableStatement);
    }

    //this is called when the database version changes , it prevents user app from breaking
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(StockModel stockModel){

        SQLiteDatabase db = this.getWritableDatabase();

        //cv is a associative array like a hashmap
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SYMBOL, stockModel.getSymbol());
        cv.put(COLUMN_PRICE, stockModel.getPrice());
        cv.put(COLUMN_ISALARMSET, stockModel.isAlarmSet());
        //what is alarm time even ?
        //cv.put(COLUMN_ALARMTIME, new SimpleDateFormat("HH:mm:ss").format(new Date())  );
        cv.put(COLUMN_ALARMPRICE, stockModel.getAlarmPrice());
        cv.put(COLUMN_PRICEATM, stockModel.getPriceAtm());


        long insert = db.insert(STOCK_TABLE, null, cv);

        if(insert == -1) {
            return false;
        }
        else{
            return true;
        }
    }

    public boolean DeleteOne(StockModel stockModel){

        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "DELETE FROM " + STOCK_TABLE + " WHERE " + COLUMN_ID + " = " + stockModel.getId() ;

        Cursor cursor = db.rawQuery(queryString, null);

        cursor.close();
        db.close();

        // if not empty, it return true, otherwise false
        return cursor.moveToFirst();

    }

    public int UpdateOne(double price, boolean isAlarmSet, double alarmPrice ,  int id, double priceAtm){


        int flag = (isAlarmSet)? 1 : 0;

        String sql = "UPDATE " + STOCK_TABLE + " SET " + COLUMN_PRICE +  " =? , " + COLUMN_ISALARMSET + " =? , " + COLUMN_ALARMPRICE + " =? ," + COLUMN_PRICEATM + "=?  WHERE " + COLUMN_ID + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();

        SQLiteStatement statement = db.compileStatement(sql);

        //boolean is stored as int 1 = true

        statement.bindDouble(1, price);
        statement.bindLong(2, flag);
        statement.bindDouble(3, alarmPrice);
        statement.bindLong(4, id);
        statement.bindDouble(5, priceAtm );



        //number of rows affected will return
        return statement.executeUpdateDelete();

    }

    public int UpdateOne(double price,  int id){




        String sql = "UPDATE " + STOCK_TABLE + " SET " + COLUMN_PRICE +  " =? WHERE " + COLUMN_ID + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();

        SQLiteStatement statement = db.compileStatement(sql);

        //boolean is stored as int 1 = true

        statement.bindDouble(1, price);
        statement.bindLong(4, id);



        //number of rows affected will return
        return statement.executeUpdateDelete();

    }

    public int UpdateOne( boolean isAlarmSet,  int id){


        int flag = (isAlarmSet)? 1 : 0;

        String sql = "UPDATE " + STOCK_TABLE + " SET " + COLUMN_ISALARMSET + " =?  WHERE " + COLUMN_ID + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();

        SQLiteStatement statement = db.compileStatement(sql);

        //boolean is stored as int 1 = true


        statement.bindLong(2, flag);
        statement.bindLong(4, id);



        //number of rows affected will return
        return statement.executeUpdateDelete();

    }

    public StockModel GetOneById(int id){


        String queryString = "SELECT * FROM " + STOCK_TABLE + " WHERE " + COLUMN_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();

        String[] param = { Integer.toString(id)  };

        Cursor cursor = db.rawQuery(queryString, param );

        int stockID = cursor.getInt(0);
        String symbol = cursor.getString(1);
        double price = cursor.getDouble(2);
        boolean alarmActive = cursor.getInt(3) == 1;
        double alarmPrice = cursor.getDouble(4);
        double priceAtm = cursor.getDouble(5);

        //object to be returned
        StockModel stockModel = new StockModel(stockID, symbol, price, alarmActive, alarmPrice, priceAtm );

        cursor.close();
        db.close();

        return stockModel;

    }


    public List<StockModel> GetAllStocks(){

        List<StockModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + STOCK_TABLE;

        //writable works but  locks the db so no process can access it so use readable
        SQLiteDatabase db = this.getReadableDatabase();



        //execute query
        Cursor cursor = db.rawQuery(queryString, null);

        // move to the first item in cursor (array inside array )
        if(cursor.moveToFirst()){
            // loop through the cursor
            do {

                int stockID = cursor.getInt(0);
                String symbol = cursor.getString(1);
                double price = cursor.getDouble(2);
                boolean alarmActive = cursor.getInt(3) == 1;
                double alarmPrice = cursor.getDouble(4);
                double priceAtm = cursor.getDouble(5);


                //object to be returned
                StockModel stockModel = new StockModel(stockID, symbol, price, alarmActive, alarmPrice, priceAtm );

                returnList.add(stockModel);


            }while (cursor.moveToNext());

        }
        else {
            //failure , leave it emptiy
        }

        //close the connection
        cursor.close();
        db.close();


        return returnList;
    }



}
