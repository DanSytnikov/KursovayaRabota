package com.example.user.kursach;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.kursach.BittrexPackage.Bittrex;
import com.example.user.kursach.BittrexPackage.Buy;
import com.example.user.kursach.BittrexPackage.Sell;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


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
    boolean isToggledValues = false;
    boolean firstRun = true;
    boolean isShownBorders = false;
    int timeSleep = 3;
    ProgressBar pb;
    SeekBar sb;
    HorizontalBarChart brSell;
    public String currentExch = "Bittrex";
    CEXAPI cexAPI;
    String symbol1 = "BTC";
    String symbol2 = "USD";
    HorizontalBarChart mChart;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mChart = findViewById(R.id.sellchart);
        switch (item.getItemId()) {
            case R.id.actionToggleValues: {
                isToggledValues = !isToggledValues;
                break;
            }

            case R.id.actionTogglePinch: {
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;
            }
            case R.id.actionToggleBarBorders: {
                isShownBorders = !isShownBorders;
                break;
            }
            case R.id.animateX: {
                mChart.animateX(3000);
                break;
            }
            case R.id.animateY: {
                mChart.animateY(3000);
                break;
            }
            case R.id.animateXY: {

                mChart.animateXY(3000, 3000);
                break;
            }

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout ll = findViewById(R.id.linearLay);
        sb = findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(seekBarChangeListener);
        pushExchangeSpinner();
        pushBittrexSpinner();
        /***
         * Settings for changing orientation
         */
      /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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
*/

    }

    /***
     * Auto-update time setting
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            timeSleep = seekBar.getProgress();
            TextView tv = findViewById(R.id.seekBarProgress);
            tv.setText("" + seekBar.getProgress());
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

    /***
     * Spinner of exchanges
     */
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
                brSell = findViewById(R.id.sellchart);
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

    /***
     * Spinner of markets
     */
    public void pushBittrexSpinner() {
        dropdown = findViewById(R.id.spinnerLeft);
        ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.query_suggestions));
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("POSITION", String.valueOf(position));
                HorizontalBarChart brSell = findViewById(R.id.sellchart);
                firstRun = true;
                brSell.fitScreen();
                marketType = (String) parent.getItemAtPosition(position);
                switch (marketType) {
                    case "USDT-BTC":
                        symbol1 = "BTC";
                        symbol2 = "USD";
                        break;
                    case "USDT-ETH":
                        symbol1 = "ETH";
                        symbol2 = "USD";
                        break;
                    case "USDT-BCH":
                        symbol1 = "BCH";
                        symbol2 = "USD";
                        break;
                    case "USDT-BTG":
                        symbol1 = "BTG";
                        symbol2 = "USD";
                        break;
                    case "USDT-DASH":
                        symbol1 = "DASH";
                        symbol2 = "USD";
                        break;
                    case "USDT-XMR":
                        firstRun = false;
                        Toast.makeText(getBaseContext(),"Missing market", Toast.LENGTH_SHORT).show();
                        break;
                    case "USDT-LTC":
                        firstRun = false;
                        Toast.makeText(getBaseContext(),"Missing market", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                marketType = "USDT-BTC";
            }
        });
    }

    /***
     * AsyncTask for Bittrex
     */
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
            List<Buy> buyList = data.getResult().getBuy(); // Creating of bids' container
            Collections.sort(buyList); //sorting to avoid rendering crashes
            Collections.max(buyList);
            List<Sell> sellList = data.getResult().getSell();

            float maxQuan = 0;
            float minSell = 999999999;
            float maxBuy = 0;
            List<BarEntry> entries = new ArrayList<>();
            List<BarEntry> entries1 = new ArrayList<>();
            List<BarEntry> entriesEmbit = new ArrayList<>();
            int c = 0;
            for (Buy i : buyList) {
                Float quan = i.getQuantity().floatValue() * 1000;
                Float rate = i.getRate().floatValue();
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate > maxBuy) {
                    maxBuy = rate;
                }
                if (quan < 1000) {
                    entries.add(new BarEntry(rate, quan));
                } else {
                    if (quan < 10000) {
                        entriesEmbit.add(new BarEntry(rate, quan / 1000));
                    }
                }

            }
            Log.e("sadaw", String.valueOf(c));
            for (Sell i : sellList) {
                Float quan = i.getQuantity().floatValue() * 1000;
                Float rate = i.getRate().floatValue();
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate < minSell) {
                    minSell = rate;
                }
                if (quan < 1000) {
                    entries1.add(new BarEntry(rate, quan));
                } else {
                    entriesEmbit.add(new BarEntry(rate, quan / 1000));
                }
            }
            TextView tv = findViewById(R.id.textView);
            tv.setText("MaxBuy:" + maxBuy + "  MinSell:" + minSell + "  MaxQuan:" + maxQuan);
            Log.d("MAXQUAN", String.valueOf(maxQuan));
            Log.d("MINSELL", String.valueOf(minSell));
            Log.d("MAXBUY", String.valueOf(maxBuy));

            BarDataSet dataSetSell = new BarDataSet(entries1, "Sell");
            BarDataSet dataSetBuy = new BarDataSet(entries, "Buy");
            BarDataSet dataSetEmbit = new BarDataSet(entriesEmbit, "Embit");
            dataSetEmbit.setColor(Color.BLACK);
            dataSetSell.setColor(Color.GREEN);
            dataSetSell.setBarBorderColor(getResources().getColor(R.color.Black));
            dataSetBuy.setColors(Color.BLACK);
            dataSetBuy.setColor(Color.RED);
            BarDataSet[] barDataSets = new BarDataSet[3];
            barDataSets[0] = dataSetBuy;
            barDataSets[1] = dataSetSell;
            barDataSets[2] = dataSetEmbit;


            BarData ld = new BarData(barDataSets);
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            chartSell.setData(ld);
            chartsSettings();
            if (firstRun) {
                chartSell.animateXY(3000, 3000);
                chartSell.invalidate();
                firstRun = false;
            }
            toggleValues(isToggledValues);
            toggleBorders(isShownBorders);
            chartSell.invalidate();
        }

        public void chartsSettings() {
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            chartSell.setDescription(null);

            chartSell.getAxisRight().setAxisMinimum(0);
            chartSell.getAxisLeft().setAxisMinimum(0);

            chartSell.getAxisRight().setEnabled(false);

            chartSell.getLegend().setEnabled(false);
        }

        public void toggleValues(boolean isToggled) {
            mChart = findViewById(R.id.sellchart);
            if (isToggled) {
                List<IBarDataSet> sets = mChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    IBarDataSet set = (BarDataSet) iSet;
                    set.setDrawValues(true);
                }

            } else {
                List<IBarDataSet> sets = mChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    IBarDataSet set = (BarDataSet) iSet;
                    set.setDrawValues(false);
                }
            }
        }

        public void toggleBorders(boolean isToggled) {
            if (isToggled) {
                for (IBarDataSet set : mChart.getData().getDataSets())
                    ((BarDataSet) set).setBarBorderWidth(1.f);
            } else {
                for (IBarDataSet set : mChart.getData().getDataSets())
                    ((BarDataSet) set).setBarBorderWidth(0.f);
            }
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
            List<BarEntry> entriesEmbit = new ArrayList<>();


            for (int i = 0; i < 35; i++) {
                Float quan = buyList.get(i).get(1).floatValue();
                Float rate = buyList.get(i).get(0).floatValue();
                if (quan > maxQuan) {
                    maxQuan = quan;
                }
                if (rate > maxBuy) {
                    maxBuy = rate;
                }

                if (quan < 1000) {
                    entriesBuy.add(new BarEntry(rate, quan));
                } else {
                    entriesEmbit.add(new BarEntry(rate, quan / 1000));
                }

                Float quanS = sellList.get(i).get(1).floatValue();
                Float rateS = sellList.get(i).get(0).floatValue();
                if (quanS > maxQuan) {
                    maxQuan = quanS;
                }
                if (rateS < minSell) {
                    minSell = rateS;
                }
                if (quanS < 1000) {
                    entriesSell.add(new BarEntry(rateS, quanS));
                } else {
                    entriesEmbit.add(new BarEntry(rate, quanS / 1000));
                }
            }
            TextView tv = findViewById(R.id.textView);
            tv.setText("MaxBuy:" + maxBuy + "  MinSell:" + minSell + "  MaxQuan:" + maxQuan);
            Log.d("MAXQUAN", String.valueOf(maxQuan));
            Log.d("MINSELL", String.valueOf(minSell));
            Log.d("MAXBUY", String.valueOf(maxBuy));

            BarDataSet dataSetSell = new BarDataSet(entriesSell, "Sell");
            BarDataSet dataSetBuy = new BarDataSet(entriesBuy, "Buy");
            BarDataSet dataSetEmbit = new BarDataSet(entriesEmbit, "Embit");
            BarDataSet[] barDataSets = new BarDataSet[3];
            dataSetSell.setColor(Color.GREEN);
            dataSetBuy.setColor(Color.RED);
            dataSetEmbit.setColor(Color.BLACK);
            barDataSets[0] = dataSetBuy;
            barDataSets[1] = dataSetSell;
            barDataSets[2] = dataSetEmbit;
            BarData ld = new BarData(barDataSets);

            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            chartSell.setData(ld);
            chartsSettings();
            if (firstRun) {
                chartSell.animateXY(3000, 3000);
                chartSell.invalidate();
                firstRun = false;
            }
            toggleValues(isToggledValues);
            toggleBorders(isShownBorders);
            chartSell.invalidate();
            chartSell.invalidate();
        }

        public void chartsSettings() {
            HorizontalBarChart chartSell = findViewById(R.id.sellchart);
            chartSell.setDescription(null);

            chartSell.getAxisRight().setAxisMinimum(0);
            chartSell.getAxisLeft().setAxisMinimum(0);

            chartSell.getAxisRight().setEnabled(false);

            chartSell.getLegend().setEnabled(false);
        }

        public void toggleValues(boolean isToggled) {
            mChart = findViewById(R.id.sellchart);
            if (isToggled) {
                List<IBarDataSet> sets = mChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    IBarDataSet set = (BarDataSet) iSet;
                    set.setDrawValues(true);
                }

            } else {
                List<IBarDataSet> sets = mChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    IBarDataSet set = (BarDataSet) iSet;
                    set.setDrawValues(false);
                }
            }
        }

        public void toggleBorders(boolean isToggled) {
            if (isToggled) {
                for (IBarDataSet set : mChart.getData().getDataSets())
                    ((BarDataSet) set).setBarBorderWidth(1.f);
            } else {
                for (IBarDataSet set : mChart.getData().getDataSets())
                    ((BarDataSet) set).setBarBorderWidth(0.f);
            }
        }
    }

    @Override
    protected void onPause() {
        Log.e("PROGRAMM", " on Pause");
        super.onPause();
        setBittrexAPIcanceled();
        setCexAPIcanceled();
    }

    @Override
    protected void onStop() {
        Log.e("PROGRAMM", " on Stop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        setCexAPIcanceled();
        bittrexAPI = new BittrexAPI();
        bittrexAPI.execute();
        super.onRestart();
    }
}
