package com.envyserve.githubreference.wordpress.api;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.envyserve.githubreference.wordpress.WordpressListAdapter;

public class WordpressGetTaskInfo{

	public boolean isLoading;
	public Integer pages;
	public Integer curpage = 1;
	public String baseurl;
	public Boolean simpleMode = false;
	
	public Long ignoreId = 0L;
	
	Context context;
	
	public ListView feedListView = null;
	public WordpressListAdapter feedListAdapter = null;
	
	public View footerView;
	public View dialogLayout;
	public View frame;
	
	public WordpressGetTaskInfo(View footerView, ListView listView, Context context, View dialogLayout, View frame, String baseurl, Boolean simpleMode) {
		this.footerView = footerView;
		this.feedListView = listView;
		this.context = context;
		this.dialogLayout = dialogLayout;
		this.frame = frame;
		this.baseurl = baseurl;
		this.simpleMode = simpleMode;
	}

}
