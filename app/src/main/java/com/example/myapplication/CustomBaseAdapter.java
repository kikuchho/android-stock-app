package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomBaseAdapter extends BaseAdapter {

    Context context;
//    String[] stockList;
//    double[] price;

    List<StockModel> stockModels = new ArrayList<>();

    LayoutInflater inflater;


    public CustomBaseAdapter(Context ctx, List<StockModel> stockModels){
        this.context = ctx;
        this.stockModels = stockModels;

        inflater = LayoutInflater.from(ctx);

    }

    @Override
    public int getCount() {
        //return stockList.length;
        return stockModels.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //setting text
        convertView = inflater.inflate(R.layout.activity_stock_list_view, null);

        TextView textview = (TextView) convertView.findViewById(R.id.textView);
        //textview.setText(stockList[position]);
        textview.setText(stockModels.get(position).getSymbol());



        return convertView;
    }
}
