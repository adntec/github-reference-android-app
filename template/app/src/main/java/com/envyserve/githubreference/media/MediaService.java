package com.envyserve.githubreference.media;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.envyserve.githubreference.R;
import com.envyserve.githubreference.media.ui.MediaFragment;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener {

    private static MediaPlayer player;
    private static MediaFragment mediaFragment;
    private static Timer timer;
    private static String url;
    private static PhoneStateListener phoneStateListener;
    private static Context context;

    static final int NOTIFICATION_ID = 2015;
    static final int META_UPDATE_INTERVAL = 5000;
    public static String NOTIFICATION_FILTER = "stop_playing";
    
    public MediaService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // if the mediaPlayer object is null, create it
        if(player == null)
            player = new MediaPlayer();
        // acquire partial wake lock to allow background playback
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        
    	registerReceiver(stopServiceReceiver, new IntentFilter(NOTIFICATION_FILTER));
    	
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                	resetPlayer(MediaService.this);
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Could resume music here, but listener is removed.
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                	resetPlayer(MediaService.this);
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        context = this;
    }

    public static MediaPlayer get() {
        return player;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        startPlaying(this);
    }
    
    public static void startPlaying(Context c){
    	player.start();

        updateNotification(c, null);
        
        TelephonyManager mgr = (TelephonyManager) c.getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        timer = new Timer();
        timer.schedule(new UpdateMediaInfo(), 1, META_UPDATE_INTERVAL);
    }
    
    public static void resetPlayer(Context c){
    	player.reset();
        NotificationManager nManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancel(NOTIFICATION_ID);
        
        TelephonyManager mgr = (TelephonyManager) c.getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }
    
    public static void setDataSource(String source) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
    	player.setDataSource(source);
    	url = source;
    }
    
    public static String getDataSource(){
    	return url;
    }
    
    public static void setMediaFragment(MediaFragment fragment){
    	mediaFragment = fragment;

        //Perform an instant update of the media info
        new Thread(new UpdateMediaInfo()).start();
    }

    // release mediaPlayer object on service destruction
    @Override
    public void onDestroy() {
        super.onDestroy();

        resetPlayer(this);
        if(player != null){
            player.release();
            player = null;
        }

        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        resetPlayer(this);
    }
    
    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {   
    	  @Override
    	  public void onReceive(Context context, Intent intent) {
    		  resetPlayer(context);
    		  if (mediaFragment != null)
    			  mediaFragment.updateButtons();
    	  }
    };

    public static void updateNotification(Context c, String playingInfo){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(c)
                        .setSmallIcon(R.drawable.ic_radio_playing)
                        .setContentTitle(c.getResources().getString(R.string.notification_playing));

        if (playingInfo != null){
            builder.setContentText(playingInfo);
        }


        PendingIntent contentIntent = PendingIntent.getBroadcast(c, 0, new Intent(NOTIFICATION_FILTER), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    static class UpdateMediaInfo extends TimerTask {

        //Perform a task in the background
        public void run()
        {
            final String mediaInfo = getMediaInfo();

            //Only if we actually have mediaInfo and a fragment to show it in, perform an update
            if (mediaFragment != null
                    && mediaFragment.getActivity() != null
                    && mediaInfo != null
                    && !mediaInfo.equals("")){

                //Update on the UI thread
                mediaFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaFragment.updateMediaInfoFromBackground(mediaInfo);
                        updateNotification(mediaFragment.getActivity(), mediaInfo);
                    }
                });
            } else if (mediaInfo != null && !mediaInfo.equals("")){
                updateNotification(context, mediaInfo);
            }
        }

    }

    private static String getMediaInfo(){
        try {
            FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
            mmr.setDataSource(url);

            String album =  mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
            String title =  mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
            String track=  mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK);
            String meta =  mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ICY_METADATA);
            String artist =  mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);

            //byte[] artwork = mmr.getEmbeddedPicture();
            //Bitmap bmp= BitmapFactory.decodeByteArray(artwork, 0, artwork.length);

            //Compose an information String
            String info = "";
            if (meta != null){
                //Parse the metadata
                HashMap<String, String> metadata = new HashMap();
                String[] metaParts = meta.split(";");
                Pattern p = Pattern.compile("^([a-zA-Z]+)=\\'([^\\']*)\\'$");
                Matcher m;
                for (int i = 0; i < metaParts.length; i++) {
                    m = p.matcher(metaParts[i]);
                    if (m.find()) {
                        metadata.put((String)m.group(1), (String)m.group(2));
                    }
                }

                info = metadata.get("StreamTitle");
            } else {
                if (title != null && !title.equals("") && !title.equals("null")){
                    info += title;
                } else if (track != null && !title.equals("") && !title.equals("null")){
                    info += track;
                } else if (album != null && !album.equals("") && !album.equals("null")){
                    info += album;
                }

                if (artist != null && !artist.equals("") && !artist.equals("null")) {
                    info += (info.length() > 0) ? " - " + artist : artist;
                }
            }

            mmr.release();

            return info;
        } catch (Exception e){
            //Non-essential
            return null;
        }
    }
}
