package com.example.myapplication;

import java.sql.Time;
import java.util.Optional;

//sqlite model
public class StockModel {

    private int id;
    private String Symbol;
    private double price;
    private boolean isAlarmSet;
    //private Time alarmTIme;
    private double alarmPrice;

    //price when alarm was set
    private double priceAtm;

    //constructor
    public StockModel(int id, String symbol, double price, boolean isAlarmSet,  double alarmPrice , double priceAtm) {
        this.id = id;
        this.Symbol = symbol;
        this.price = price;
        this.isAlarmSet = isAlarmSet;
        //this.alarmTIme = alarmTIme;
        this.alarmPrice = alarmPrice;

        this.priceAtm = priceAtm;

    }

    public StockModel(int id, String symbol) {
        this.id = id;
        this.Symbol = symbol;


        //price atm when alarm set is null so set to -1
        this.priceAtm = -1;


    }

    public StockModel() {
    }

    // toString is necessary for printing the contents

    @Override
    public String toString() {
        return "StockModel{" +
                "id=" + id +
                ", Symbol='" + Symbol + '\'' +
                ", price=" + price +
                ", isAlarmSet=" + isAlarmSet +
                //", alarmTIme=" + alarmTIme +
                ", alarmPrice=" + alarmPrice +
                ", priceAtm " + priceAtm +
                '}';
    }


    // getter and setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAlarmSet() {
        return isAlarmSet;
    }

    public void setAlarmSet(boolean alarmSet) {
        isAlarmSet = alarmSet;
    }

    public double getAlarmPrice() {
        return alarmPrice;
    }

    public void setAlarmPrice(double alarmPrice) {
        this.alarmPrice = alarmPrice;
    }

    public double getPriceAtm() {
        return priceAtm;
    }

    public void setPriceAtm(double priceAtm) {
        this.priceAtm = priceAtm;
    }



}
