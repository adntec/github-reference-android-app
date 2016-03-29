package com.envyserve.githubreference.web;

import com.envyserve.githubreference.BackPressFragment;
import com.envyserve.githubreference.MainActivity;
import com.envyserve.githubreference.R;
import com.envyserve.githubreference.fav.FavDbAdapter;
import com.envyserve.githubreference.util.Helper;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * This activity is used to display webpages
 */

public class WebviewFragment extends Fragment implements BackPressFragment {

	//Static
	public static final String HIDE_NAVIGATION = "hide_navigation";
	public static final String LOAD_DATA = "loadwithdata";

	//References
	private Activity mAct;
	private FavDbAdapter mDbHelper;

	//Layout with interaction
	private WebView browser;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	//Layouts
	private ImageButton webBackButton;
	private ImageButton webForwButton;
	private LinearLayout ll;

	//HTML5 video
	private View mCustomView;
	private int mOriginalSystemUiVisibility;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Return the existing layout if there is a savedInstance of this fragment
		if (savedInstanceState != null) { return ll; }

		ll = (LinearLayout) inflater.inflate(R.layout.fragment_webview,
				container, false);

		setHasOptionsMenu(true);

		browser = (WebView) ll.findViewById(R.id.webView);
		mSwipeRefreshLayout = (SwipeRefreshLayout) ll.findViewById(R.id.refreshlayout);

		// settings some settings like zooming etc in seperate method for
		// suppresslint
		browserSettings();

		browser.setWebViewClient(new WebViewClient() {
			// Make sure any url clicked is opened in webview
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if ((url.contains("market://") || url.contains("mailto:")
						|| url.contains("play.google") || url.contains("tel:") || url
						.contains("vid:")) == true) {
					// Load new URL Don't override URL Link
					view.getContext().startActivity(
							new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

					return true;
				}
				// Return true to override url loading (In this case do
				// nothing).
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(browser, url);

				adjustControls();
			}

		});

		// has all to do with progress bar
		browser.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (mSwipeRefreshLayout.isRefreshing()) {
					if (progress == 100) {
						mSwipeRefreshLayout.setRefreshing(false);
					}
				} else if (progress < 100){
                    //If we do not hide the navigation, show refreshing
					if (!WebviewFragment.this.getArguments().containsKey(HIDE_NAVIGATION)  ||
							WebviewFragment.this.getArguments().getBoolean(HIDE_NAVIGATION) == false)
					    mSwipeRefreshLayout.setRefreshing(true);
				}
			}

			@SuppressLint("InlinedApi")
			@Override
			public void onShowCustomView(View view,
										 WebChromeClient.CustomViewCallback callback) {
				// if a view already exists then immediately terminate the new one
				if (mCustomView != null) {
					onHideCustomView();
					return;
				}

				// 1. Stash the current state
				mCustomView = view;
                mCustomView.setBackgroundColor(Color.BLACK);
				mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();

				// 2. Stash the custom view callback
				mCustomViewCallback = callback;

				// 3. Add the custom view to the view hierarchy
				FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
				decor.addView(mCustomView, new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));


				// 4. Change the state of the window
				getActivity().getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
								View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
								View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
								View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
								View.SYSTEM_UI_FLAG_FULLSCREEN |
								View.SYSTEM_UI_FLAG_IMMERSIVE);
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}

			@Override
			public void onHideCustomView() {
				// 1. Remove the custom view
				FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
				decor.removeView(mCustomView);
				mCustomView = null;

				// 2. Restore the state to it's original form
				getActivity().getWindow().getDecorView()
						.setSystemUiVisibility(mOriginalSystemUiVisibility);

                //TODO Find a better solution to the keyboard not showing after custom view is hidden
                //The user will come from landscape, so we'll first 'rotate' to portrait (rotation fixes a bug of the keybaord not showing)
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                //The we'll restore to the detected orientation (by immediately rotating back, the user should not notice any difference and/or flickering).
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

				// 3. Call the custom view callback
				mCustomViewCallback.onCustomViewHidden();
				mCustomViewCallback = null;


            }

		});

		browser.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
										String contentDisposition, String mimetype,
										long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		// setting an on touch listener
		browser.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:
						if (!v.hasFocus()) {
							v.requestFocus();
						}
						break;
				}
				return false;
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				browser.reload();
			}
		});

		return ll;
	}// of oncreateview

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();

		setRetainInstance(true);

		String weburl = getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
		String data = getArguments().containsKey(LOAD_DATA) ? getArguments().getString(LOAD_DATA) : null;
		if (checkConnectivity() || weburl.startsWith("file:///android_asset/")) {
			//If this is the first time, load the initial url, otherwise restore the view if necessairy
			if (savedInstanceState == null) {
				//If we have HTML data to load, do so, else load the url.
				if (data != null) {
					browser.loadDataWithBaseURL(weburl, data, "text/html", "UTF-8", "");
				} else {
					browser.loadUrl(weburl);
				}
			} else if (mCustomView != null){
				FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
				((ViewGroup) mCustomView.getParent()).removeView(mCustomView);
				decor.addView(mCustomView, new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		browser.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		browser.onResume();

		if (!this.getArguments().containsKey(HIDE_NAVIGATION)  ||
				this.getArguments().getBoolean(HIDE_NAVIGATION) == false){

			ActionBar actionBar = ((AppCompatActivity) mAct)
				.getSupportActionBar();

			if (mAct instanceof WebviewActivity) {
				actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
			} else {
				actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			}

			View view = mAct.getLayoutInflater().inflate(R.layout.fragment_webview_actionbar, null);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL);
			actionBar.setCustomView(view, lp);

			webBackButton = (ImageButton) mAct.findViewById(R.id.goBack);
			webForwButton = (ImageButton) mAct.findViewById(R.id.goForward);

			webBackButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (browser.canGoBack())
						browser.goBack();
				}
			});
			webForwButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (browser.canGoForward())
						browser.goForward();
				}
			});
		} else {
			mSwipeRefreshLayout.setEnabled(false);
		}

		adjustControls();
	}

	@Override
	public void onStop() {
		super.onStop();

        if (!this.getArguments().containsKey(HIDE_NAVIGATION)  ||
                this.getArguments().getBoolean(HIDE_NAVIGATION) == false) {

            ActionBar actionBar = ((AppCompatActivity) getActivity())
                    .getSupportActionBar();

            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        }

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			shareURL();
			return true;
		case R.id.favorite:
			mDbHelper = new FavDbAdapter(mAct);
			mDbHelper.open();

			String title = browser.getTitle();
			String url = browser.getUrl();

			if (mDbHelper.checkEvent(title, url, FavDbAdapter.KEY_WEB)) {
				// This item is new
				mDbHelper.addFavorite(title, url, FavDbAdapter.KEY_WEB);
				Toast toast = Toast.makeText(mAct,
						getResources().getString(R.string.favorite_success),
						Toast.LENGTH_LONG);
				toast.show();
			} else {
				Toast toast = Toast.makeText(mAct,
						getResources().getString(R.string.favorite_duplicate),
						Toast.LENGTH_LONG);
				toast.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.webview_menu, menu);
	}

	// Checking for an internet connection
	private boolean checkConnectivity() {
		boolean enabled = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) mAct
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if ((info == null || !info.isConnected() || !info.isAvailable())) {
			enabled = false;
			
			Helper.noConnection(mAct);
		}
		
		return enabled;
	}

	public void adjustControls() {
		webBackButton = (ImageButton) mAct.findViewById(R.id.goBack);
		webForwButton = (ImageButton) mAct.findViewById(R.id.goForward);

		if (webBackButton == null || webForwButton == null) return;

		if (browser.canGoBack()) {
			webBackButton.setColorFilter(Color.argb(255, 255, 255, 255));
		} else {
			webBackButton.setColorFilter(Color.argb(255, 0, 0, 0));
		}
		if (browser.canGoForward()) {
			webForwButton.setColorFilter(Color.argb(255, 255, 255, 255));
		} else {
			webForwButton.setColorFilter(Color.argb(255, 0, 0, 0));
		}
	}

	// sharing
	private void shareURL() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		String appname = getString(R.string.app_name);
		shareIntent.putExtra(Intent.EXTRA_TEXT,
				(getResources().getString(R.string.web_share_begin)) + appname
						+ getResources().getString(R.string.web_share_end)
						+ browser.getUrl());
		startActivity(Intent.createChooser(shareIntent, getResources()
				.getString(R.string.share)));
	}

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	private void browserSettings() {
		// set javascript and zoom and some other settings
		browser.getSettings().setJavaScriptEnabled(true);
		browser.getSettings().setBuiltInZoomControls(true);
		browser.getSettings().setDisplayZoomControls(false);
		browser.getSettings().setAppCacheEnabled(true);
		browser.getSettings().setDatabaseEnabled(true);
		browser.getSettings().setDomStorageEnabled(true);
		browser.getSettings().setUseWideViewPort(true);
		browser.getSettings().setLoadWithOverviewMode(true);

		// enable all plugins (flash)
		browser.getSettings().setPluginState(PluginState.ON);
	}

	@Override
	public boolean handleBackPress() {
		if (browser.canGoBack()){
			browser.goBack();
			return true;
		}

		return false;
	}
}
