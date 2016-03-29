package com.envyserve.githubreference.media.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.envyserve.githubreference.MainActivity;
import com.envyserve.githubreference.PermissionsFragment;
import com.envyserve.githubreference.R;
import com.envyserve.githubreference.media.LineRenderer;
import com.envyserve.githubreference.media.MediaService;
import com.envyserve.githubreference.media.UrlParser;
import com.envyserve.githubreference.media.VisualizerView;
import com.envyserve.githubreference.util.Helper;
import com.envyserve.githubreference.util.Log;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  This activity is used to listen to a radio station
 */

public class MediaFragment extends Fragment implements OnClickListener, MediaPlayer.OnPreparedListener, PermissionsFragment {

    private ProgressBar loadingIndicator;
    private Button buttonPlay;
    private Button buttonStopPlay;
    private VisualizerView mVisualizerView;
    private String urlToPlay;
    Activity mAct;
    
    private LinearLayout ll;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	ll = (LinearLayout) inflater.inflate(R.layout.fragment_media, container, false);
        
        initializeUIElements();

	    return ll;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		
		Helper.isOnline(mAct, true);
		
		if (null == MediaService.get())
			mAct.startService(new Intent(mAct, MediaService.class));

        MediaService.setMediaFragment(this);

        final String radio = MediaFragment.this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];

        //Parse the url on the background
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                urlToPlay = (UrlParser.getUrl(radio));

                if (null != MediaService.get() && MediaService.get().isPlaying()){
                    if (!MediaService.getDataSource().equals(urlToPlay)){
                        Toast.makeText(mAct, getResources().getString(R.string.radio_playing_other), Toast.LENGTH_LONG).show();
                    }
                }
            }

        });

    }

    private void initializeUIElements() {
        loadingIndicator = (ProgressBar) ll.findViewById(R.id.loadingIndicator);
        loadingIndicator.setMax(100);
        loadingIndicator.setVisibility(View.INVISIBLE);

        buttonPlay = (Button) ll.findViewById(R.id.btn_play);
        buttonPlay.setOnClickListener(this);

        buttonStopPlay = (Button) ll.findViewById(R.id.btn_pause);
        buttonStopPlay.setOnClickListener(this);

        updateButtons();

        mVisualizerView = (VisualizerView) ll.findViewById(R.id.visualizerView);

    }

    public void updateButtons(){
        if (null != MediaService.get() && MediaService.get().isPlaying()){
        	buttonPlay.setEnabled(false);
        	buttonStopPlay.setEnabled(true);
        } else {
            buttonPlay.setEnabled(true);
            buttonStopPlay.setEnabled(false);

            updateMediaInfoFromBackground(null);
        }
    }

    public void onClick(View v) {
        if (v == buttonPlay) {
            if (urlToPlay != null) {
                // set up listener
                MediaService.get().setOnPreparedListener(this);

                startPlaying();
                AudioManager am = (AudioManager) mAct.getSystemService(Context.AUDIO_SERVICE);
                int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (volume_level < 2) {
                    Toast.makeText(mAct, getResources().getString(R.string.volume_low), Toast.LENGTH_SHORT).show();
                }
            } else {
                //The loading of urlToPlay should happen almost instantly, so this code should never be reached
            }
        } else if (v == buttonStopPlay) {
            stopPlaying();
        }
    }

    private void startPlaying() {
        buttonPlay.setEnabled(false);
        buttonStopPlay.setEnabled(true);

        loadingIndicator.setVisibility(View.VISIBLE);

        try {
			MediaService.setDataSource(urlToPlay);
		} catch (IllegalArgumentException e) {
			Log.printStackTrace(e);
		} catch (SecurityException e) {
			Log.printStackTrace(e);
		} catch (IllegalStateException e) {
			Log.printStackTrace(e);
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
        MediaService.get().prepareAsync();

    }

    private void stopPlaying() {
        MediaService.resetPlayer(mAct);
        mVisualizerView.setEnabled(false);

        updateButtons();

        loadingIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        MediaService.startPlaying(mAct);
        loadingIndicator.setVisibility(View.INVISIBLE);

        try {
            if (null == mVisualizerView.getVisualizer()) {
                mVisualizerView.link(MediaService.get());
                addLineRenderer();
            }
        } catch (Exception e){
            //We can live without the visualizer, it's better the no audio. But inform the user about it.
            Toast.makeText(mAct, "Could not start visualizer", Toast.LENGTH_SHORT);
            Log.printStackTrace(e);
        }
    }


    private void addLineRenderer()
    {
      Paint linePaint = new Paint();
      linePaint.setStrokeWidth(1f);
      linePaint.setAntiAlias(true);
      linePaint.setColor(Color.argb(88, 0, 128, 255));

      Paint lineFlashPaint = new Paint();
      lineFlashPaint.setStrokeWidth(5f);
      lineFlashPaint.setAntiAlias(true);
      lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
      LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
      mVisualizerView.addRenderer(lineRenderer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    //@param info - the text to be updated. Giving a null string will hide the info.
    public void updateMediaInfoFromBackground(String info) {
        TextView nowPlayingTitle = (TextView) ll.findViewById(R.id.now_playing_title);
        TextView nowPlaying = (TextView) ll.findViewById(R.id.now_playing);

        if (info != null)
            nowPlaying.setText(info);

        if (info != null && nowPlayingTitle.getVisibility() == View.GONE){
            nowPlayingTitle.setVisibility(View.VISIBLE);
            nowPlaying.setVisibility(View.VISIBLE);
        } else if (info == null){
            nowPlayingTitle.setVisibility(View.GONE);
            nowPlaying.setVisibility(View.GONE);
        }
    }

    @Override
    public String[] requiredPermissions() {
        return new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE};
    }
}