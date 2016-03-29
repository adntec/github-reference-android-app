package com.envyserve.githubreference.rss;

import com.envyserve.githubreference.R;
import com.envyserve.githubreference.util.Helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import com.envyserve.githubreference.util.Log;

/**
 * RSS Notify Service Manager
 */
public class ServiceStarter extends BroadcastReceiver {
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
    
    static String NOTIFY_ON = "notify_on";
  
    @Override
    public void onReceive(Context context, Intent intent) {   

    	SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(context);
        
        boolean prefson = prefs.getBoolean(NOTIFY_ON, true);
           
        Log.v("INFO", "Status = " + prefson);
        
    	if (Helper.isOnline(context, false) && prefson){
    		Intent service = new Intent(context, RssService.class);
        
    		// Start the service, keeping the device awake while it is launching.
    		context.startService(service);
    		// END_INCLUDE(alarm_onreceive)
    	}
    }

    // BEGIN_INCLUDE(set_alarm)
    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServiceStarter.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        
        int frequency= context.getResources().getInteger(R.integer.frequency);
        
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                 AlarmManager.ELAPSED_REALTIME, 
                 frequency*60*1000, alarmIntent);
        
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);      
        
        // Saving for preferences
    	SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(context);
    	
    	SharedPreferences.Editor editor= prefs.edit();
    	
    	editor.putBoolean(NOTIFY_ON, true);
 	    editor.commit();
        
       Log.v("INFO", "Push Notifications Enabled");
    };
    // END_INCLUDE(set_alarm)

    /**
     * Cancels the alarm.
     * @param context
     */
    // BEGIN_INCLUDE(cancel_alarm)
    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.   	
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        } else {
        	alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        	Intent intent = new Intent(context, ServiceStarter.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmMgr.cancel(alarmIntent);
        }
        
        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        
        // Saving for preferences
    	SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(context);
    	
    	SharedPreferences.Editor editor= prefs.edit();
    	
    	editor.putBoolean(NOTIFY_ON, false);
 	    editor.commit();
 	    editor.apply();
        
        Log.v("INFO", "Push Notifications Disabled");

        
    }
    // END_INCLUDE(cancel_alarm)
    
}
