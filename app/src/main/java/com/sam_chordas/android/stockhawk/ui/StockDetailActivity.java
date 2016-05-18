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

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

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
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.getCount() > 0){
            String[] mLabels= new String[data.getCount()];
            float[] mValues = new float[data.getCount()];
            float lowestValue = 0;
            int pos = 0;
            while (data.moveToNext()) {
                String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                String date = data.getString(data.getColumnIndex(QuoteColumns.CREATED));
                mValues[pos] = Float.parseFloat(bidPrice);
                mLabels[pos] = date;
                //set lowest value
                if((lowestValue == 0) || (Float.parseFloat(bidPrice) < lowestValue)){
                    lowestValue = Float.parseFloat(bidPrice);
                }
                //mLabels[pos] = pos + "";
                pos++;
            }
            LineSet dataset = new LineSet(mLabels, mValues);
            ContextCompat.getColor(this, R.color.material_blue_500);
            dataset.setColor(ContextCompat.getColor(this, R.color.material_blue_500))
                    .setFill(ContextCompat.getColor(this, R.color.line_graph_fill))
                    .setDotsColor(ContextCompat.getColor(this, R.color.material_blue_500))
                    .setThickness(4)
                    .setDashed(new float[]{10f,10f})
                    .beginAt((int)lowestValue);
            mLineGraph.addData(dataset);
            
            mLineGraph.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
