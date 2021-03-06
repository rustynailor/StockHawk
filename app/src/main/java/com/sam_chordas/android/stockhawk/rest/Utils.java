package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        String created = jsonObject.getString("created");
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          //check for a valid quote here
           if(!jsonObject.getString("Change").equals("null")) {
               //basic check for null result
               batchOperations.add(buildBatchOperation(jsonObject, created));
           }
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){

              jsonObject = resultsArray.getJSONObject(i);
                //check for a valid quote here
                if(!jsonObject.getString("Change").equals("null")) {
                    batchOperations.add(buildBatchOperation(jsonObject, created));
                }
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, String created){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
      boolean validData = true;
    try {
      String change = jsonObject.getString("Change");

          builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
          String bid = jsonObject.getString("Bid");
          //check bidprice is not null
          if(bid.equals("null")) {
              validData = false;
          } else {
              builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bid));
              builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                      jsonObject.getString("ChangeinPercent"), true));
              builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
              builder.withValue(QuoteColumns.ISCURRENT, 1);
              builder.withValue(QuoteColumns.CREATED, created);
              if (change.charAt(0) == '-') {
                  builder.withValue(QuoteColumns.ISUP, 0);
              } else {
                  builder.withValue(QuoteColumns.ISUP, 1);
              }

      }
    } catch (JSONException e){
      e.printStackTrace();
    }
      if(validData){
        return builder.build();
      } else {
          return null;
      }
  }

  /**
   * Returns true if the network is available or about to become available.
   * Taken from Advanced Android Development Sunshine App example
   * (https://github.com/udacity/Advanced_Android_Development/blob/86eba0c4f74857e21cb066b66341297fbeb7f934/app/src/main/java/com/example/android/sunshine/app/Utility.java)
   *
   * @param c Context used to get the ConnectivityManager
   * @return
   */
  static public boolean isNetworkAvailable(Context c) {
    ConnectivityManager cm =
            (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting();
  }
}
