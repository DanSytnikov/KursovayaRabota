package com.example.user.kursach;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.user.kursach.BittrexPackage.Bittrex;
import com.example.user.kursach.BittrexPackage.Buy;
import com.example.user.kursach.BittrexPackage.Sell;
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
    public CEX dataCEX;
    public String marketType = "USDT-BTC";
    public Retrofit retrofit;
    BittrexAPI bittrexAPI;
    Spinner dropdown;
    Spinner exc;
    int timeSleep = 3;
    ProgressBar pb;
    SeekBar sb;
    HorizontalBarChart brBuy;
    HorizontalBarChart brSell;
    public String currentExch = "Bittrex";
    CEXAPI cexAPI;
    String symbol1 = "BTC";
    String symbol2 = "USD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout ll = findViewById(R.id.linearLay);
        sb = findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(seekBarChangeListener);
        pushExchangeSpinner();
        pushBittrexSpinner();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayout linearLayout = findViewById(R.id.spinners);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
            lp.height = 100;
            linearLayout.setLayoutParams(lp);
            LinearLayout lLOr = findViewById(R.id.lLOrient);
            lLOr.setOrientation(LinearLayout.VERTICAL);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout linearLayout = findViewById(R.id.spinners);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
            lp.height = 0;
            linearLayout.setLayoutParams(lp);
            LinearLayout lLOr = findViewById(R.id.lLOrient);
            lLOr.setOrientation(LinearLayout.HORIZONTAL);
        }


    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            timeSleep = seekBar.getProgress();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };


    public void setBittrexAPIcanceled() {
        if (bittrexAPI != null) {
            bittrexAPI.cancel(true);
        }
    }

    public void setCexAPIcanceled() {
        if (cexAPI != null) {
            cexAPI.cancel(true);
        }
    }

    public void pushExchangeSpinner() {
        exc = findViewById(R.id.spinnerRight);
        ArrayAdapter adapterExch = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.exchange));
        exc.setAdapter(adapterExch);
        exc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("POSITION", String.valueOf(position));
                setBittrexAPIcanceled();
                setCexAPIcanceled();
                brBuy = findViewById(R.id.buychart);
                brSell = findViewById(R.id.sellchart);
                brBuy.fitScreen();
                brSell.fitScreen();
                currentExch = (String) parent.getItemAtPosition(position);
                switch (currentExch) {
                    case "Bittrex":
                        retrofit = new Retrofit.Builder()
                                .baseUrl("https://bittrex.com") //Базовая часть адреса
                                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                                .build();
                        getInterface = retrofit.create(GETinterface.class); //Создаем объект, при помощи которого будем выполнять запросы
                        bittrexAPI = new BittrexAPI();
                        bittrexAPI.execute();
                        break;
                    case "CEX":
                        retrofit = new Retrofit.Builder()
                                .baseUrl("https://cex.io") //Базовая часть адреса
                                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                                .build();
                        getInterface = retrofit.create(GETinterface.class); //Создаем объект, при помощи которого будем выполнять запросы
                        cexAPI = new CEXAPI();
                        cexAPI.execute();
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void pushBittrexSpinner() {
        dropdown = findViewById(R.id.spinnerLeft);
        ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.query_suggestions));
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("POSITION", String.valueOf(position));
                HorizontalBarChart brBuy = findViewById(R.id.buychart);
                HorizontalBarChart brSell = findViewById(R.id.sellchart);
                brBuy.fitScreen();
                brSell.fitScreen();
                marketType = (String) parent.getItemAtPosition(position);
                switch (marketType) {
                    case "USDT-BTC":
                        symbol1 = "BTC";
                        symbol2 = "USD";
                        break;
                    case "USDT-ETC":
                        symbol1 = "ETH";
                        symbol2 = "USD";
                        break;
                    case "USDT-XRP":
                        symbol1 = "XRP";
                        symbol2 = "USD";
                        break;
                    case "USDT-DASH":
                        symbol1 = "DASH";
                        symbol2 = "USD";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                marketType = "USDT-BTC";
            }
        });
    }


    public void onClick(View view) {
        BittrexAPI bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();
    }


    class BittrexAPI extends AsyncTask<Void, Response<Bittrex>, Response<Bittrex>> {

        @Override
        protected Response<Bittrex> doInBackground(Void... voids) {
            Log.d("RUN", "Thread started.");
            pb = findViewById(R.id.progressBar);
            Response<Bittrex> res = null;
            while (!isCancelled()) {
                try {
                    if (timeSleep == 0) {
                        pb.setMax(0);
                    } else {
                        pb.setMax(100);
                        Call<Bittrex> responseCall = getInterface.getData(marketType, "both");
                        res = responseCall.execute();
                        publishProgress(res);
                    }
                    pb.setProgress(0);
                    pb.setDrawingCacheBackgroundColor(Color.GRAY);
                    int i = 0;
                    while (i < 100) {
                        i++;
                        Thread.sleep(timeSleep * 10);
                        pb.setProgress(i);
                    }
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
                Float quan = i.getQuantity().floatValue();
                Float rate = i.getRate().floatValue();
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
                Float quan = i.getQuantity().floatValue();
                Float rate = i.getRate().floatValue();
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
            chartBuy.getAxisLeft().setAxisMaximum(maxQuan);
            chartSell.getAxisLeft().setAxisMaximum(maxQuan);
            chartSell.setData(barDataSell);
            chartBuy.setData(barDataBuy);
            chartsSettings();

            chartSell.invalidate();
            chartBuy.invalidate(); // refresh
        }

        public void chartsSettings() {
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            HorizontalBarChart chartBuy = findViewById(R.id.buychart);
            chartSell.setDescription(null);
            chartBuy.setDescription(null);

            chartSell.getAxisLeft().setInverted(true);
            chartBuy.getAxisRight().setAxisMinimum(0);
            chartBuy.getAxisLeft().setAxisMinimum(0);   //Charts' settings
            chartSell.getAxisRight().setAxisMinimum(0);
            chartSell.getAxisLeft().setAxisMinimum(0);

            chartBuy.getAxisLeft().setEnabled(false);
            chartSell.getAxisRight().setEnabled(false);

            chartBuy.getLegend().setEnabled(false);
            chartSell.getLegend().setEnabled(false);
        }

        @Override
        protected void onPostExecute(Response<Bittrex> bittrexResponse) {
            super.onPostExecute(bittrexResponse);

        }
    }

    class CEXAPI extends AsyncTask<Void, Response<CEX>, Response<CEX>> {

        @Override
        protected Response<CEX> doInBackground(Void... voids) {
            Log.d("RUN", "Thread started.");
            pb = findViewById(R.id.progressBar);
            Response<CEX> resCEX = null;
            while (!isCancelled()) {
                try {
                    if (timeSleep == 0) {
                        pb.setMax(0);
                    } else {
                        pb.setMax(100);
                        Call<CEX> responseCall = getInterface.getDataCex(symbol1, symbol2);
                        resCEX = responseCall.execute();
                        publishProgress(resCEX);
                    }
                    pb.setProgress(0);
                    int i = 0;
                    while (i < 100) {
                        i++;
                        Thread.sleep(timeSleep * 10);
                        pb.setProgress(i);
                    }
                    Log.d("CYCLE", "+1");
                } catch (IOException | InterruptedException e) {
                    Log.e("ERROR", e.toString());
                }
            }

            Log.d("RUN", "Resp result: " + resCEX.code());
            return resCEX;
        }

        @Override
        protected void onProgressUpdate(Response<CEX>... cexResp) {
            super.onProgressUpdate(cexResp);
            dataCEX = cexResp[0].body();
            float maxQuan = 0;
            float maxBuy = 0;
            float minSell = 999999999;
            List<List<Double>> buyList = dataCEX.getBids();
            List<List<Double>> sellList = dataCEX.getAsks();
            List<BarEntry> entriesBuy = new ArrayList<>();
            List<BarEntry> entriesSell = new ArrayList<>();

            for (List<Double> i : buyList) {
                Float quan = i.get(1).floatValue();
                Float rate = i.get(0).floatValue();
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate > maxBuy) {
                    maxBuy = rate;
                }

                entriesBuy.add(new BarEntry(rate, quan));
            }

            for (List<Double> i : sellList) {
                Float quan = i.get(1).floatValue();
                Float rate = i.get(0).floatValue();
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate < minSell) {
                    minSell = rate;
                }
                entriesSell.add(new BarEntry(rate, quan));
            }
            TextView tv = findViewById(R.id.textView);
            tv.setText("MaxBuy:" + maxBuy + "  MinSell:" + minSell + "  MaxQuan:" + maxQuan);
            Log.d("MAXQUAN", String.valueOf(maxQuan));
            Log.d("MINSELL", String.valueOf(minSell));
            Log.d("MAXBUY", String.valueOf(maxBuy));

            BarDataSet dataSetSell = new BarDataSet(entriesSell, "Sell");
            BarDataSet dataSetBuy = new BarDataSet(entriesBuy, "Buy");
            dataSetSell.setColor(Color.GREEN);
            dataSetBuy.setColor(Color.RED);
            BarData barDataSell = new BarData(dataSetSell);
            BarData barDataBuy = new BarData(dataSetBuy);
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            HorizontalBarChart chartBuy = findViewById(R.id.buychart);
            chartBuy.getAxisLeft().setAxisMaximum(maxQuan);
            chartSell.getAxisLeft().setAxisMaximum(maxQuan);
            chartSell.setData(barDataSell);
            chartBuy.setData(barDataBuy);
            chartsSettings();

            chartSell.invalidate();
            chartBuy.invalidate(); // refresh
        }

        public void chartsSettings() {
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            HorizontalBarChart chartBuy = findViewById(R.id.buychart);
            chartSell.setDescription(null);
            chartBuy.setDescription(null);

            chartSell.getAxisLeft().setInverted(true);
            chartBuy.getAxisRight().setAxisMinimum(0);
            chartBuy.getAxisLeft().setAxisMinimum(0);   //Charts' settings
            chartSell.getAxisRight().setAxisMinimum(0);
            chartSell.getAxisLeft().setAxisMinimum(0);

            chartBuy.getAxisLeft().setEnabled(false);
            chartSell.getAxisRight().setEnabled(false);

            chartBuy.getLegend().setEnabled(false);
            chartSell.getLegend().setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBittrexAPIcanceled();
        setCexAPIcanceled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setBittrexAPIcanceled();
        setCexAPIcanceled();
    }
}
