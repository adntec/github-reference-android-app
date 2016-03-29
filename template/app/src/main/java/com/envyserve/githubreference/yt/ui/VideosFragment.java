package com.envyserve.githubreference.yt.ui;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.envyserve.githubreference.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnAttachStateChangeListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import android.support.v7.widget.SearchView.OnQueryTextListener;

import com.envyserve.githubreference.MainActivity;
import com.envyserve.githubreference.R;
import com.envyserve.githubreference.util.Helper;
import com.envyserve.githubreference.yt.api.RetrieveVideos;
import com.envyserve.githubreference.yt.api.object.ReturnItem;
import com.envyserve.githubreference.yt.api.object.Video;
import com.envyserve.githubreference.yt.VideosAdapter;

/**
 * This activity is used to display a list of vidoes
 */
public class VideosFragment extends Fragment {
    //Layout references
	private ListView listView;
	private View footerView;
    public RelativeLayout pDialog;

    private LinearLayout ll;
	private Activity mAct;

    //Stores information
    private ArrayList<Video> videoList;
    private VideosAdapter videoAdapter;
    private RetrieveVideos videoApiClient;

    //Keeping track of location & status
	private String upcomingPageToken;
	private boolean isLoading = true;
    private int currentType;
    private String searchQuery;

    private static int TYPE_SEARCH = 1;
    private static int TYPE_PLAYLIST = 2;

    @SuppressLint("InflateParams")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ll = (LinearLayout) inflater.inflate(R.layout.fragment_list_nopadding, container, false);
		setHasOptionsMenu(true);

        //checking if the user has just opened the app
		footerView = inflater.inflate(R.layout.listview_footer, null);
        pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
        listView = (ListView) ll.findViewById(R.id.list);

        listView.setOnItemClickListener(new OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
        		Object o = listView.getItemAtPosition(position);
        		Video video = (Video) o;
        		Intent intent = new Intent(mAct, VideoDetailActivity.class);
                intent.putExtra(VideoDetailActivity.EXTRA_VIDEO, video);
    		    startActivity(intent);
           	}
    	});

        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if (listView == null)
                    return;

                if (listView.getCount() == 0)
                    return;

                int l = visibleItemCount + firstVisibleItem;
                if (l >= totalItemCount && !isLoading) {
                    // It is time to add new data. We call the listener
                    if (null != upcomingPageToken) {
                        loadVideos(upcomingPageToken);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        return ll;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        String apiKey = getResources().getString(R.string.google_server_key);
        videoApiClient = new RetrieveVideos(mAct, apiKey);

        videoList = new ArrayList<Video>();
        videoAdapter = new VideosAdapter(mAct, videoList);
        listView.setAdapter(videoAdapter);

        //Set the default type
        currentType = TYPE_PLAYLIST;
        //Load the youtube videos
        loadVideos(null);
    }

    //@param nextpagetoken leave at null to get first page
    //currentType must be set
    private void loadVideos(String nextPageToken){
        String channeldid = null;
        if (getPassedData().length > 1) {
            channeldid = getPassedData()[1];
        }

        String param = null;
        if (currentType == TYPE_PLAYLIST) {
            param = getPassedData()[0];
        } else if (currentType == TYPE_SEARCH) {
            param = searchQuery;
        }

        loadVideosInList(nextPageToken, param, channeldid);
    }

    //@param nextPageToken the token of the page to load, null if the first page
    //@param param the username or query
    //@param retrievaltype the type of retrieval to do, either TYPE_SEARCH or TYPE_PLAYLIST
	private void loadVideosInList(final String nextPageToken, final String param, final String channelID) {

        listView.addFooterView(footerView);
        isLoading = true;

        if (nextPageToken == null) {
            videoList.clear();
            videoAdapter.notifyDataSetChanged();
            upcomingPageToken = null;
        }

		AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                ReturnItem result = null;
                if (currentType == TYPE_SEARCH) {
                    result = videoApiClient.getSearchVideos(param, channelID, nextPageToken);
                    Log.v("INFO", "Performing search");
                } else if (currentType == TYPE_PLAYLIST) {
                    result = videoApiClient.getUserVideos(param, nextPageToken);
                }

                final ArrayList<Video> videos = result.getList();
                upcomingPageToken = result.getPageToken();

                mAct.runOnUiThread(new Runnable() {
                    public void run() {

                        listView.removeFooterView(footerView);
                        isLoading = false;

                        //Hide the loading layout that is shown during the initial load
                        if (pDialog.getVisibility() == View.VISIBLE) {
                            pDialog.setVisibility(View.INVISIBLE);
                            Helper.revealView(listView, ll);
                        }

                        if (videos != null) {
                            if (videos.size() > 0)
                                videoList.addAll(videos);
                        } else {
                            Helper.noConnection(mAct);
                        }

                        videoAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

	}
    
    private String[] getPassedData(){
    	String[] parts = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
    	return parts;
    }

    /**
    private class DownloadFilesTask extends AsyncTask<String, Integer, Void> {

		@Override
		protected void onPreExecute() {
			if (initialload){
				pDialog = (RelativeLayout) ll.findViewById(R.id.progressBarHolder);
				if (pDialog.getVisibility() == View.GONE) {
					pDialog.setVisibility(View.VISIBLE);
					listView.setVisibility(View.GONE);
				}
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					listView.addFooterView(footerView);
				}
			} else {
				listView.addFooterView(footerView);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				if (null != videoList) {
					updateList();
				} else {
					String message = null;
					if (GOOGLE_API_KEY.length() != 39 || GOOGLE_API_KEY.equals("YOURSERVERKEYHERE"))
						message = "Debug info: Google API Server key '" + GOOGLE_API_KEY + "' most likely invalid.";
					Helper.noConnection(mAct, message);
				}
			
				if (null != videoList && videoList.size() < 1){
					Toast.makeText(
						getActivity(), 
						getResources().getString(R.string.no_results), 
						Toast.LENGTH_LONG).show();
				}

				if (pDialog.getVisibility() == View.VISIBLE) {
					pDialog.setVisibility(View.GONE);
					//feedListView.setVisibility(View.VISIBLE);
					Helper.revealView(listView,ll);
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						listView.removeFooterView(footerView);
					}
				} else {
					listView.removeFooterView(footerView);
				}
			}catch (Exception e){
				Log.printStackTrace(e);
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			String url = params[0];
			if (null != pagetoken){
				url = url + "&pageToken=" + pagetoken;
			}

            ReturnItem item = RetrieveVideos.getVideos(url, mAct);
            videoList = item.getList();
            pagetoken = item.getPageToken();
			return null;
		}
	}
	**/

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.refresh_menu, menu);
    	
	    //set & get the search button in the actionbar 
		final SearchView searchView = new SearchView(mAct);

	    searchView.setQueryHint(getResources().getString(R.string.video_search_hint));
	    searchView.setOnQueryTextListener(new OnQueryTextListener() {
	        	
	    	@Override
	    	public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                currentType = TYPE_SEARCH;
	        	loadVideos(null);
	        	searchView.clearFocus();
	            return true;
	       }

	       @Override
	       public boolean onQueryTextChange(String newText) {
	        	return false;
	       }

	    });
	        
    	String[] parts = getPassedData();

    	searchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View arg0) {
            	if (!isLoading){
                    currentType = TYPE_PLAYLIST;
                    searchQuery = null;
    	    		loadVideos(null);
    	    	} 
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                // search was opened
            }
        });
    	

	    if (parts.length == 2){
	         menu.add("search")
	         	.setIcon(R.drawable.ic_action_search)
	         	.setActionView(searchView)
	         	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	    }
	    //TODO make menu an xml item
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.refresh:
	    	if (!isLoading){
	    		loadVideos(null);
	    	} else {
	    		Toast.makeText(mAct, getString(R.string.already_loading), Toast.LENGTH_LONG).show();
	    	}
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
}