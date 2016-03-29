package com.envyserve.githubreference.yt.ui;

import com.envyserve.githubreference.Config;
import com.envyserve.githubreference.R;
import com.envyserve.githubreference.comments.CommentsActivity;
import com.envyserve.githubreference.fav.FavDbAdapter;
import com.envyserve.githubreference.util.DetailActivity;
import com.envyserve.githubreference.util.Helper;
import com.envyserve.githubreference.util.WebHelper;
import com.envyserve.githubreference.web.WebviewActivity;
import com.envyserve.githubreference.yt.api.object.Video;
import com.envyserve.githubreference.yt.player.YouTubePlayerActivity;
import com.squareup.picasso.Picasso;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is used to display the details of a video
 */

public class VideoDetailActivity extends DetailActivity {

	private FavDbAdapter mDbHelper;
	private TextView mPresentation;
	private Video video;

    public static final String EXTRA_VIDEO = "videoitem";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Use the general detaillayout and set the viewstub for youtube
		setContentView(R.layout.activity_details);
		ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
		stub.setLayoutResource(R.layout.activity_youtube_detail);
		View inflated = stub.inflate();

		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		mPresentation = (TextView) findViewById(R.id.youtubetitle);
		TextView detailsDescription = (TextView) findViewById(R.id.youtubedescription);
		TextView detailsSubTitle = (TextView) findViewById(R.id.youtubesubtitle);

        video = (Video) getIntent().getSerializableExtra(EXTRA_VIDEO);

        detailsDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                WebHelper.getWebViewFontSize(this));

		mPresentation.setText(video.getTitle());
		detailsDescription.setText(video.getDescription());
		
		String subText = getResources().getString(R.string.video_subtitle_start) + 
				video.getUpdated() +
				getResources().getString(R.string.video_subtitle_end) + 
				video.getChannel();
		detailsSubTitle.setText(subText);

		Helper.admobLoader(this, getResources(), findViewById(R.id.adView));

		thumb = (ImageView) findViewById(R.id.image);
		coolblue = (RelativeLayout) findViewById(R.id.coolblue);

		Picasso.with(this).load(video.getImage()).into(thumb);

		setUpHeader(video.getImage());

		ImageButton btnPlay = (ImageButton) findViewById(R.id.playbutton);
		btnPlay.bringToFront();
		// Listening to button event
		btnPlay.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(VideoDetailActivity.this,
						YouTubePlayerActivity.class);
				intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, video.getId());
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

		Button btnFav = (Button) findViewById(R.id.favorite);

		// Listening to button event
		btnFav.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mDbHelper = new FavDbAdapter(VideoDetailActivity.this);
				mDbHelper.open();

				if (mDbHelper.checkEvent(video.getTitle(), video, FavDbAdapter.KEY_YOUTUBE)) {
					// Item is new
					mDbHelper.addFavorite(video.getTitle(), video, FavDbAdapter.KEY_YOUTUBE);
					Toast toast = Toast
							.makeText(VideoDetailActivity.this, getResources()
									.getString(R.string.favorite_success),
									Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(
							VideoDetailActivity.this,
							getResources().getString(
									R.string.favorite_duplicate),
							Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		Button btnComment = (Button) findViewById(R.id.comments);

		// Listening to button event
		btnComment.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// Start NewActivity.class
				Intent commentIntent = new Intent(VideoDetailActivity.this,
						CommentsActivity.class);
				commentIntent.putExtra(CommentsActivity.DATA_TYPE,
						CommentsActivity.YOUTUBE);
				commentIntent.putExtra(CommentsActivity.DATA_ID,
						video.getId());
				startActivity(commentIntent);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_share:
			String applicationName = getResources()
					.getString(R.string.app_name);
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);

			String urlvalue = getResources().getString(
					R.string.video_share_begin);
			String seenvalue = getResources().getString(
					R.string.video_share_middle);
			String appvalue = getResources()
					.getString(R.string.video_share_end);
			// this is the text that will be shared
			sendIntent.putExtra(Intent.EXTRA_TEXT, (urlvalue
					+ "http://youtube.com/watch?v=" + video.getId() + seenvalue
					+ applicationName + appvalue));
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, video.getTitle());
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources()
					.getString(R.string.share_header)));

			return true;
		case R.id.menu_view:
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("vnd.youtube:" + video.getId()));
				startActivity(intent);
			} catch (ActivityNotFoundException ex) {
				Intent mIntent = new Intent(VideoDetailActivity.this, WebviewActivity.class);
				mIntent.putExtra(WebviewActivity.OPEN_EXTERNAL, Config.OPEN_EXPLICIT_EXTERNAL);
				mIntent.putExtra(WebviewActivity.URL, "http://www.youtube.com/watch?v=" + video.getId());
				startActivity(mIntent);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.youtube_detail_menu, menu);
		return true;
	}

}
