package com.envyserve.githubreference.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.envyserve.githubreference.R;
import com.envyserve.githubreference.SettingsFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewAnimationUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class Helper {
	
	private static boolean DISPLAY_DEBUG = true;
	
	public static void noConnection(final Context context, String message) {
    	
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
    	   
    	if (isOnline(context, false)){
    		String messageText = "";
        	if (message != null && DISPLAY_DEBUG){
        		messageText = "\n\n" + message;
        	}
        	
    		ab.setMessage(context.getResources().getString(R.string.dialog_connection_description) + messageText);
    	   	ab.setPositiveButton(context.getResources().getString(R.string.ok), null);
    	   	ab.setTitle(context.getResources().getString(R.string.dialog_connection_title));
    	} else {
    		ab.setMessage(context.getResources().getString(R.string.dialog_internet_description));
     	   	ab.setPositiveButton(context.getResources().getString(R.string.ok), null);
     	   	ab.setTitle(context.getResources().getString(R.string.dialog_internet_title));
    	}
    	
    	ab.show();
     }	

    public static void noConnection(final Context context) {
        noConnection(context, null);
     }
    
    public static boolean isOnline(Context c, boolean showDialog) {
    	ConnectivityManager cm = (ConnectivityManager) 
    	c.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo ni = cm.getActiveNetworkInfo();
    	 
    	if (ni != null && ni.isConnected())
    	  return true;
    	else
    	  if (showDialog){
    		  noConnection(c);
    	  }
    	  return false;
    }
    
    public static void admobLoader(Context c, Resources resources, View AdmobView){
    	String adId = resources.getString(R.string.ad_id);
		if (adId != null && !adId.equals("") && !SettingsFragment.getIsPurchased(c)) {
			AdView adView = (AdView) AdmobView;
			adView.setVisibility(View.VISIBLE);
			
			// Look up the AdView as a resource and load a request.
			Builder adRequestBuilder = new AdRequest.Builder();
			adView.loadAd(adRequestBuilder.build());
		}
    }
    
    @SuppressLint("NewApi")
	public static void revealView(View toBeRevealed, View frame){
		try {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				// get the center for the clipping circle
				int cx = (frame.getLeft() + frame.getRight()) / 2;
				int cy = (frame.getTop() + frame.getBottom()) / 2;

				// get the final radius for the clipping circle
				int finalRadius = Math.max(frame.getWidth(), frame.getHeight());

				// create the animator for this view (the start radius is zero)
				Animator anim = ViewAnimationUtils.createCircularReveal(
						toBeRevealed, cx, cy, 0, finalRadius);

				// make the view visible and start the animation
				toBeRevealed.setVisibility(View.VISIBLE);
				anim.start();
			} else {
				toBeRevealed.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}
    
	@SuppressLint("NewApi")
	public static void setStatusBarColor(Activity mActivity, int color){
		try {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				mActivity.getWindow().setStatusBarColor(color); 
			}
		} catch (Exception e){
			Log.printStackTrace(e);
		}
	}
	
	
	//Makes high numbers readable (e.g. 5000 -> 5K)
	public static String formatValue(double value) {
		if (value > 0){
			int power; 
		    String suffix = " kmbt";
		    String formattedNumber = "";

		    NumberFormat formatter = new DecimalFormat("#,###.#");
		    power = (int)StrictMath.log10(value);
		    value = value/(Math.pow(10,(power/3)*3));
		    formattedNumber=formatter.format(value);
		    formattedNumber = formattedNumber + suffix.charAt(power/3);
		    return formattedNumber.length()>4 ?  formattedNumber.replaceAll("\\.[0-9]+", "") : formattedNumber;  
		} else {
			return "0";
		}
	}

    public static String getDataFromUrl(String url){
        // Making HTTP request
        Log.v("INFO", "Requesting: " + url);

        StringBuffer chaine = new StringBuffer("");
        try {
            URL urlCon = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlCon
                    .openConnection();
            connection.setRequestProperty("User-Agent", "Universal/2.0 (Android)");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            Log.printStackTrace(e);
        }

        return chaine.toString();
    }

    //Get JSON from an url and parse it to a JSON Object.
	public static JSONObject getJSONObjectFromUrl(String url) {
		String data = getDataFromUrl(url);

		try {
			return new JSONObject(data);
		} catch (Exception e) {
            Log.e("INFO", "Error parsing JSON. Printing stacktrace now");
			Log.printStackTrace(e);
		}

		return null;
	}

    //Get JSON from an url and parse it to a JSON Array.
    public static JSONArray getJSONArrayFromUrl(String url) {
        String data = getDataFromUrl(url);

        try {
            return new JSONArray(data);
        } catch (Exception e) {
            Log.e("INFO", "Error parsing JSON. Printing stacktrace now");
            Log.printStackTrace(e);
        }

        return null;
    }

}
