package com.envyserve.githubreference.wordpress.api;

import com.envyserve.githubreference.util.Helper;
import com.envyserve.githubreference.util.Log;
import com.envyserve.githubreference.wordpress.PostItem;

import org.json.JSONObject;

public final class BackgroundPostCompleter extends Thread{
    private PostItem item;
    private String apiBase;
    private BackgroundPostCompleterListener listener;

    public BackgroundPostCompleter(PostItem item, String apiBase, BackgroundPostCompleterListener listener){
        this.item = item;
        this.apiBase = apiBase;
        this.listener = listener;
    }

    @Override
    public void run() {
        String url = WordpressGetTask.getPostUrl(item.getId(), apiBase);

        // getting JSON string from URL
        JSONObject json = Helper.getJSONObjectFromUrl(url);

        // parsing json data
        try {
            // parsing json object
            if (json.getString("status").equalsIgnoreCase("ok")) {
                JSONObject post = json.getJSONObject("post");

                item.setContent(post.getString("content"));
                item.setCommentCount(Long.valueOf(post.getInt("comment_count")));
                item.setCommentsArray(post.getJSONArray("comments"));

                item.setPostCompleted();

                //Make aware that we have completed the item
                if (listener != null){
                    listener.completed(item);
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);

            //We weren't able to complete the item, so call the listener with null as an indication of fail
            if (listener != null){
                listener.completed(null);
            }
        }
    }

    public interface BackgroundPostCompleterListener {
        void completed(PostItem item);
    }
}