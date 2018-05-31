package com.example.user.kursach;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    public static String TAG = "MainActivity";
    public GETinterface getInterface;
    public Bittrex data;

    public Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BarChart chart = (BarChart) findViewById(R.id.chart);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://bittrex.com") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        getInterface = retrofit.create(GETinterface.class); //Создаем объект, при помощи которого будем выполнять запросы

/*
        YourData[] dataObjects = ...;

        List<Entry> entries = new ArrayList<Entry>();

        for (YourData data : dataObjects) {

            // turn your data into Entry objects
            entries.add(new Entry(data.getValueX(), data.getValueY()));
        }*/

    }

    public void onClick(View view) {
        BittrexAPI bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();

    }

    class BittrexAPI extends AsyncTask<Void, Void, Response<Bittrex>> {

        @Override
        protected Response<Bittrex> doInBackground(Void... voids) {
            Log.d("RUN", "Thread started.");

            Call<Bittrex> responseCall = getInterface.getData("USDT-BTC", "both");
            Response<Bittrex> res = null;
            try {
                res = responseCall.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("RUN", "Resp result: " + res.code());
            return res;
        }

        @Override
        protected void onPostExecute(Response<Bittrex> bittrexResponse) {
            super.onPostExecute(bittrexResponse);
            data = bittrexResponse.body();
            List<Buy> buyList = data.getResult().getBuy();
            List<Sell> sellList = data.getResult().getSell();
            List<BarEntry> entries = new ArrayList<BarEntry>();
            for (Buy i : buyList) {
                Float quan = new Float(i.getQuantity());
                Float rate = new Float(i.getRate());
                entries.add( new BarEntry(quan, rate));
            }


            BarDataSet dataSet = new BarDataSet(entries, "Label"); // add entries to dataset

            dataSet.setColor(R.color.colorAccent);
            BarData barData = new BarData(dataSet);

            BarChart chart = findViewById(R.id.chart);

            chart.setData(barData);
            YAxis leftAxis = chart.getAxisLeft();
            YAxis rightAxis = chart.getAxisRight();

            leftAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
            leftAxis.setGranularity(10);
            chart.setDragEnabled(true);
            chart.setHighlightFullBarEnabled(true);
            chart.setScaleXEnabled(true);
            chart.setScaleYEnabled(true);
            chart.setTouchEnabled(true);
            chart.setPinchZoom(true);
            chart.setScaleEnabled(true);
            chart.invalidate(); // refresh
        }
    }
}
