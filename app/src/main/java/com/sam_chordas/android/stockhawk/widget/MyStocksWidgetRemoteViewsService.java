package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by russellhicks on 21/05/16.
 */

public class MyStocksWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = MyStocksWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] STOCK_COLUMNS = {
            QuoteDatabase.QUOTES + "." + QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.ISUP
    };
    // these indices must match the projection
    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_BIDPRICE = 2;
    static final int INDEX_STOCK_PERCENT_CHANGE = 3;
    static final int INDEX_STOCK_IS_UP = 4;


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                //nothing to do
                Log.e(LOG_TAG, "OnCreate Called");
            }

            @Override
            public void onDataSetChanged() {

                Log.e(LOG_TAG, "OnDataSetChanged Called ");

                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        STOCK_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
        }

            @Override
            public void onDestroy() {

                Log.e(LOG_TAG, "OnDestroy Called");

                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                Log.e(LOG_TAG, "getCount Called");
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                Log.e(LOG_TAG, "GetViewAt " + position);

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.my_stocks_widget_list_item);

                //get strings for display
                String stockSymbol = data.getString(INDEX_STOCK_SYMBOL);
                String bidPrice = data.getString(INDEX_STOCK_BIDPRICE);
                String stockChange = data.getString(INDEX_STOCK_PERCENT_CHANGE);

                //set in view
                views.setTextViewText(R.id.stock_symbol, stockSymbol);
                views.setTextViewText(R.id.bid_price, bidPrice);
                views.setTextViewText(R.id.change, stockChange);

                //set percent change colour
                if (data.getInt(INDEX_STOCK_IS_UP) == 1){
                    views.setTextColor(R.id.change, getResources().getColor(R.color.material_green_700));
                } else{
                    views.setTextColor(R.id.change, getResources().getColor(R.color.material_red_700));
                }


                //TODO - add intent / onclick to launch activity


                return views;

            }

            @Override
            public RemoteViews getLoadingView() {
                Log.e(LOG_TAG, "getLoadingView Called");
                return new RemoteViews(getPackageName(), R.layout.my_stocks_widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.e(LOG_TAG, "getItemId Called");
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
