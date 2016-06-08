package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private String mStockSymbol;
    private LineChartView mLineGraph;
    private static final int CURSOR_LOADER_DETAIL_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        Intent intent = getIntent();

        mLineGraph = (LineChartView)findViewById(R.id.linechart);

        mStockSymbol = intent.getStringExtra(MyStocksActivity.EXTRA_STOCK_SYMBOL);

        TextView stockSymbolView = (TextView)findViewById(R.id.stock_symbol);
        stockSymbolView.setText(mStockSymbol + "");

        //start loader
        getLoaderManager().initLoader(CURSOR_LOADER_DETAIL_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.CREATED},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{mStockSymbol},
                QuoteColumns.CREATED + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.getCount() > 0){

            //use Arraylists to hold our data points
            ArrayList<String> mLabels = new ArrayList<String>();
            ArrayList<Float> mValues = new ArrayList<Float>();

            float lowestValue = 0;
            float highestValue = 0;
            int pos = 0;
            //limit maximum data points
            int maxDataPoints = 29;

            //flag for first run through
            boolean firstDataPoint = true;

            //show data
            while (data.moveToNext() && pos <= maxDataPoints) {

                //if this is the first item, make sure we are at the start
                //of the cursor
                //(in case of screen rotation)
                if(firstDataPoint){
                    firstDataPoint = false;
                    data.moveToPosition(0);
                }

                String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));

                if(bidPrice != null){

                    mValues.add(Float.parseFloat(bidPrice));

                    String date = data.getString(data.getColumnIndex(QuoteColumns.CREATED));

                    if(date == null || pos % 5 != 0)
                    {
                        //if date is null, add empty label
                        mLabels.add("");
                    } else {
                        //show label every 5 rows
                        //attempt to parse date
                        SimpleDateFormat receivedFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        Date convertedDate = new Date();
                        try {
                            convertedDate = receivedFormat.parse(date);
                            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd");
                            mLabels.add(displayFormat.format(convertedDate));
                        } catch (ParseException e) {
                            //can't parse date - set label as empty string
                            mLabels.add("");
                        }
                    }

                    //set lowest value
                    if((lowestValue == 0) || (Float.parseFloat(bidPrice) < lowestValue)){
                        lowestValue = Float.parseFloat(bidPrice);
                    }
                    //set highest value
                    if((highestValue == 0) || (Float.parseFloat(bidPrice) > highestValue)){
                        highestValue = Float.parseFloat(bidPrice);
                    }

                }
                pos++;
            }

            //convert arraylists to arrays
            float[] mValuesArray = new float[mValues.size()];
            int i = 0;
            for (Float f : mValues) {
                mValuesArray[i++] = (f);
            }

            String[] mLabelsArray = mLabels.toArray(new String[0]);

            LineSet dataset = new LineSet(mLabelsArray, mValuesArray);
            ContextCompat.getColor(this, R.color.material_blue_500);
            dataset.setColor(ContextCompat.getColor(this, R.color.material_blue_500))
                    .setFill(ContextCompat.getColor(this, R.color.line_graph_fill))
                    .setDotsColor(ContextCompat.getColor(this, R.color.material_blue_500))
                    .setThickness(4)
                    .setDashed(new float[]{10f,10f});
            mLineGraph.addData(dataset);

            //set highest and lowest labels on graph
            int intLowest = (int)Math.floor((double) lowestValue);
            int intHighest = (int)Math.ceil((double)highestValue);
            mLineGraph.setAxisBorderValues(intLowest, intHighest);

            mLineGraph.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
