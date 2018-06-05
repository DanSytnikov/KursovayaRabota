package com.example.user.kursach;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
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
        //HorizontalBarChart chart = (HorizontalBarChart) findViewById(R.id.chart);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://bittrex.com") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        getInterface = retrofit.create(GETinterface.class); //Создаем объект, при помощи которого будем выполнять запросы
        BittrexAPI bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();
    }

    public void onClick(View view) {
        BittrexAPI bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();

    }

    class BittrexAPI extends AsyncTask<Void, Response<Bittrex>, Response<Bittrex>> {

        @Override
        protected Response<Bittrex> doInBackground(Void... voids) {
            Log.d("RUN", "Thread started.");

            Response<Bittrex> res = null;
            while (!isCancelled()) {
                try {
                    Call<Bittrex> responseCall = getInterface.getData("USDT-BTC", "both");
                    res = responseCall.execute();
                    publishProgress(res);
                    Thread.sleep(3000);
                    Log.d("CYCLE", "+1");
                } catch (IOException e) {
                    Log.e("ERROR", e.toString());
                } catch (InterruptedException e) {
                    Log.e("ERROR", e.toString());
                }
            }

            Log.d("RUN", "Resp result: " + res.code());
            return res;
        }

        @Override
        protected void onProgressUpdate(Response<Bittrex>... bittrexResponse) {
            super.onProgressUpdate(bittrexResponse);
            data = bittrexResponse[0].body();
            List<Buy> buyList = data.getResult().getBuy();
            List<Sell> sellList = data.getResult().getSell();
            float maxQuan = 0;


            List<BarEntry> entries = new ArrayList<>();
            List<BarEntry> entries1 = new ArrayList<>();
            for (Buy i : buyList) {
                Float quan = i.getQuantity();
                Float rate = i.getRate();
                if(quan> maxQuan){
                    maxQuan = quan;
                }
                entries.add(new BarEntry(rate, quan));

            }
            for (Sell i : sellList) {
                Float quan = i.getQuantity();
                Float rate = i.getRate();
                if (quan > maxQuan){
                    maxQuan = quan;
                }
                entries1.add(new BarEntry(rate, quan));
            }


            BarDataSet dataSetBuy = new BarDataSet(entries, "Label");
            BarDataSet dataSetSell = new BarDataSet(entries1, "Label");
            dataSetSell.setColor(Color.GREEN);
            BarData barDataSell = new BarData(dataSetSell);
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            dataSetBuy.setColor(Color.RED);

            BarData barDataBuy = new BarData(dataSetBuy);
            dataSetSell.setAxisDependency(YAxis.AxisDependency.RIGHT);
            HorizontalBarChart chartBuy = findViewById(R.id.buychart);
            //chartBuy.getAxisRight().setDrawLabels(false);
            chartBuy.setDescription(null);
            chartSell.setDescription(null);
            chartBuy.setData(barDataBuy);
            chartSell.setData(barDataSell);
            chartSell.getAxisLeft().setInverted(true);
            chartSell.getAxisRight().setInverted(true);
            YAxis yAxisBuy = chartBuy.getAxisLeft();
            chartBuy.getAxisRight().setAxisMaximum(maxQuan);
            chartSell.getAxisLeft().setAxisMaximum(maxQuan);
            yAxisBuy.setAxisMaximum(maxQuan);

            YAxis yAxisSell = chartSell.getAxisRight();
            yAxisSell.setAxisMaximum(maxQuan);
            //chartBuy.getAxisRight().setEnabled(false);
            chartBuy.getAxisLeft().setEnabled(false);
            chartSell.getAxisRight().setEnabled(false);
            chartBuy.getLegend().setEnabled(false);
            chartSell.getLegend().setEnabled(false);

            /*chart.setDragEnabled(true);
            //chart.setHighlightFullLineEnabled(true);
            chart.setScaleXEnabled(true);
            chart.setScaleYEnabled(true);
            chart.setTouchEnabled(true);
            chart.setPinchZoom(true);
            chart.setScaleEnabled(true);*/
            chartBuy.invalidate(); // refresh
            chartSell.invalidate();
        }

        @Override
        protected void onPostExecute(Response<Bittrex> bittrexResponse) {
            super.onPostExecute(bittrexResponse);

        }
    }
}
