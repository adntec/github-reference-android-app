package com.envyserve.githubreference.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.envyserve.githubreference.MainActivity;
import com.envyserve.githubreference.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import com.envyserve.githubreference.util.Log;
import android.util.Xml;

/**
 * RSS Service Executer
 */

public class RssService extends Service {
	
	public static final int NOTIFICATION_ID = 1;
	public static final String TITLE_ERROR = "Notify Error";
    
    // -- test 
    private URL feedUrl;
	private URLConnection feedURLConnection;
	private InputStream inputStream;
	// -- end

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	   Log.i("LocalService", "Received start id " + startId + ": " + intent);
		
	   Log.v("RSS Service", "started");
       //new Operation().execute();
	   //DoSomething(intent);
	   new ProgressFactory().execute();
	    
	   return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	    Log.v("RSS Service", "stopped");
	}
    
    private class ProgressFactory extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

        		SharedPreferences spUpdate = getSharedPreferences("rssUpdate", 0);
        		String lastTitle = spUpdate.getString("lastTitle", "No feed");
        		String feedLink = spUpdate.getString("feedLink",
        				getString(R.string.rss_push_url));
        		String rssFeed = "";
        		// Initialize Connection
        		try {
        			feedUrl = new URL(feedLink);
        			feedURLConnection = feedUrl.openConnection();
        			inputStream = feedURLConnection.getInputStream();
        		} catch (Exception e) {
        			Log.printStackTrace(e);
        		}
        		String title = "";
        		String summary = "";
        		String feedTitle= "";
        		try {
        			CharSequence[] entry = getEntry(rssFeed);
        			title = (String) entry[0];
        			summary = (String) entry[1];
        			feedTitle=(String) entry[2];
        			if (!lastTitle.equalsIgnoreCase(title) && entry[0] != TITLE_ERROR) {
        				
        				showNotification(title, summary, feedTitle);
        				// store last title so next time will not notify for same rss
        				Editor editor = spUpdate.edit();
        				editor.putString("lastTitle", title);
        				editor.commit();
        			}
        		} catch (XmlPullParserException e) {
        			Log.printStackTrace(e);
        		} catch (Exception e) {
        			Log.printStackTrace(e);
        		}
        		try {
        			inputStream.close();
        		} catch (Exception e) {
        			Log.printStackTrace(e);
        		}

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) { }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

	private CharSequence[] getEntry(String rssFeed)
			throws XmlPullParserException, IOException {
		CharSequence[] title = { TITLE_ERROR, "Turn off notifications", "Turn off notifications" };
		XmlPullParser rssParser = Xml.newPullParser();
		rssParser.setInput(inputStream, null);

		// Parse the XML
		int eventType = -1;
		boolean foundEntry = false;
		boolean firstEntry = false;

		while (eventType != XmlPullParser.END_DOCUMENT && !foundEntry) {
			if (eventType == XmlPullParser.START_TAG) {

				String strName = rssParser.getName();
				if(!firstEntry && strName.equals("title")) {
					title[2] = rssParser.nextText();
				} else if(strName.equals("item") || strName.equals("entry")) {
					firstEntry = true;
				} else if (firstEntry) {
					if (strName.equalsIgnoreCase("title")) {
						title[0] = rssParser.nextText();
					} else if (strName.equalsIgnoreCase("description") || strName.equalsIgnoreCase("summary")) {
						title[1] = rssParser.nextText();
					}
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				String strName = rssParser.getName();
				if (strName.equals("item")|| strName.equals("entry"))
					foundEntry = true;
			}
			eventType = rssParser.next();
		}
		return title;
	}

	private void showNotification(String title, String summary, String feedTitle) {
		Bitmap icon = BitmapFactory.decodeResource(getResources(),  
                R.drawable.ic_launcher);  
		
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher).setLargeIcon(icon)
				.setContentTitle(feedTitle).setContentText(title)
				.setAutoCancel(true)
				.setSound(alarmSound);
		
		NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();  
        bigText.bigText(Html.fromHtml(summary));  
        bigText.setBigContentTitle(Html.fromHtml(title));  
        bigText.setSummaryText(Html.fromHtml(feedTitle));  
        mBuilder.setStyle(bigText);  
         
		// Creates an Intent that shows the title and a description of the feed
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.putExtra("title", title);
		resultIntent.putExtra("summary", summary);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
}