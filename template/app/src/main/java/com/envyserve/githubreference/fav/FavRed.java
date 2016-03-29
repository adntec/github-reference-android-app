package com.envyserve.githubreference.fav;

import com.envyserve.githubreference.rss.ui.RssDetailActivity;
import com.envyserve.githubreference.web.WebviewActivity;
import com.envyserve.githubreference.wordpress.ui.WordpressDetailActivity;
import com.envyserve.githubreference.yt.ui.VideoDetailActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import java.io.Serializable;

/**
 * This activity redirects the user to the correct activity for viewing the
 * saved favorite item
 */

public class FavRed extends Activity {

    private Long mRowId;
    private FavDbAdapter mDbHelper;

    String title;
    Serializable object;
    int provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new FavDbAdapter(this);
        mDbHelper.open();

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(FavDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(FavDbAdapter.KEY_ROWID)
                    : null;
        }

        getData();
        openActivity();
    }

    @SuppressWarnings("deprecation")
    private void getData() {
        if (mRowId != null) {
            Cursor note = mDbHelper.getFavorite(mRowId);
            startManagingCursor(note);
            title = note.getString(note.getColumnIndexOrThrow(FavDbAdapter.KEY_TITLE));
            object = FavDbAdapter.readSerializedObject(note.getBlob(note.getColumnIndexOrThrow(FavDbAdapter.KEY_OBJECT)));
            provider = note.getInt(note.getColumnIndexOrThrow(FavDbAdapter.KEY_PROVIDER));
        }
    }

    private void openActivity() {
        if (FavDbAdapter.KEY_YOUTUBE == provider) {
            Intent intent = new Intent(this, VideoDetailActivity.class);
            intent.putExtra(VideoDetailActivity.EXTRA_VIDEO, object);
            startActivity(intent);
        } else if (FavDbAdapter.KEY_RSS == provider) {
            Intent intent = new Intent(this, RssDetailActivity.class);
            intent.putExtra(RssDetailActivity.EXTRA_RSSITEM, object);
            startActivity(intent);
        } else if (FavDbAdapter.KEY_WEB == provider) {
            Intent mIntent = new Intent(FavRed.this, WebviewActivity.class);
            mIntent.putExtra(WebviewActivity.URL, object);
            startActivity(mIntent);
        } else if (FavDbAdapter.KEY_WORDPRESS == provider) {
            Intent intent = new Intent(this, WordpressDetailActivity.class);
            intent.putExtra(WordpressDetailActivity.EXTRA_POSTITEM, object);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}