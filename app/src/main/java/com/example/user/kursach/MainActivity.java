package com.example.user.kursach;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Math.abs;


public class MainActivity extends AppCompatActivity {
    public static String TAG = "MainActivity";
    public GETinterface getInterface;
    public Bittrex data;
    public String marketType = "USDT-BTC";
    public Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner dropdown = (Spinner)findViewById(R.id.spinnerLeft);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://bittrex.com") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        getInterface = retrofit.create(GETinterface.class); //Создаем объект, при помощи которого будем выполнять запросы

BittrexMarket bittrexMarket = new BittrexMarket();
bittrexMarket.execute();
        BittrexAPI bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();
    }


    public void onClick(View view) {
        BittrexAPI bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();
    }


    class BittrexMarket extends AsyncTask<Void, Response<BittrexMarketClass>, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> marketList = new ArrayList();
            Call<BittrexMarketClass> marketCall = getInterface.getBittrexMarket();
            Response<BittrexMarketClass> marketRes = null;
            try {
                marketRes = marketCall.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<BittrexMarketResponse> marketResponses = marketRes.body().getResult();
            for (BittrexMarketResponse i : marketResponses){
                marketList.add(i.getMarketName());
            }
            return marketList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> marketList) {
            super.onPostExecute(marketList);
            ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, marketList);
            Spinner dropdown = findViewById(R.id.spinnerLeft);
            dropdown.setAdapter(adapter);
            /*dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener){
                onIte
            };*/
        }
    }
    class BittrexAPI extends AsyncTask<Void, Response<Bittrex>, Response<Bittrex>> {

        @Override
        protected Response<Bittrex> doInBackground(Void... voids) {
            Log.d("RUN", "Thread started.");
            Response<Bittrex> res = null;
            while (!isCancelled()) {
                try {
                    Call<Bittrex> responseCall = getInterface.getData(marketType, "both");
                    res = responseCall.execute();
                    publishProgress(res);
                    Thread.sleep(3000);
                    Log.d("CYCLE", "+1");
                } catch (IOException | InterruptedException e) {
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
            Collections.sort(buyList);
            Collections.max(buyList);
            List<Sell> sellList = data.getResult().getSell();

            float maxQuan = 0;
            float minSell = 999999999;
            float maxBuy = 0;
            List<BarEntry> entries = new ArrayList<>();
            List<BarEntry> entries1 = new ArrayList<>();
            int c = 0;
            for (Buy i : buyList) {
                Float quan = abs(i.getQuantity());
                Float rate = abs(i.getRate());
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate > maxBuy) {
                    maxBuy = rate;
                }
                entries.add(new BarEntry(rate, quan));
            }
            Log.e("sadaw", String.valueOf(c));
            for (Sell i : sellList) {
                Float quan = i.getQuantity();
                Float rate = i.getRate();
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate < minSell) {
                    minSell = rate;
                }
                entries1.add(new BarEntry(rate, quan));
            }
            TextView tv = findViewById(R.id.textView);
            tv.setText("MaxBuy:" + maxBuy + "  MinSell:" + minSell + "  MaxQuan:" + maxQuan);
            Log.d("MAXQUAN", String.valueOf(maxQuan));
            Log.d("MINSELL", String.valueOf(minSell));
            Log.d("MAXBUY", String.valueOf(maxBuy));

            BarDataSet dataSetSell = new BarDataSet(entries1, "Sell");
            BarDataSet dataSetBuy = new BarDataSet(entries, "Buy");
            dataSetSell.setColor(Color.GREEN);
            dataSetBuy.setColor(Color.RED);
            BarData barDataSell = new BarData(dataSetSell);
            BarData barDataBuy = new BarData(dataSetBuy);
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            HorizontalBarChart chartBuy = findViewById(R.id.buychart);

            chartSell.setDescription(null);
            chartBuy.setDescription(null);
            chartSell.setData(barDataSell);
            chartBuy.setData(barDataBuy);
            chartSell.getAxisLeft().setInverted(true);
            chartBuy.getAxisLeft().setAxisMaximum(maxQuan);
            chartBuy.getAxisLeft().setAxisMinimum(0);
            chartSell.getAxisLeft().setAxisMaximum(maxQuan);
            chartSell.getAxisLeft().setAxisMinimum(0);

            chartBuy.getAxisLeft().setEnabled(false);
            chartSell.getAxisRight().setEnabled(false);
            chartBuy.setFitBars(true);
            barDataBuy.setBarWidth(1f);
            barDataSell.setBarWidth(1f);
            chartBuy.getLegend().setEnabled(true);
            chartSell.getLegend().setEnabled(true);

            chartBuy.setTouchEnabled(true);
            chartBuy.setDragEnabled(true);
            chartBuy.setHighlightPerDragEnabled(true);
            chartBuy.setHighlightPerTapEnabled(true);
            chartBuy.setMaxHighlightDistance(100);


            chartSell.invalidate();
            chartBuy.invalidate(); // refresh
        }

        @Override
        protected void onPostExecute(Response<Bittrex> bittrexResponse) {
            super.onPostExecute(bittrexResponse);

        }
    }
}
