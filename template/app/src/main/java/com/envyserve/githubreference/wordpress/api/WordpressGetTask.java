package com.envyserve.githubreference.wordpress.api;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.envyserve.githubreference.Config;
import com.envyserve.githubreference.R;
import com.envyserve.githubreference.util.Helper;
import com.envyserve.githubreference.util.Log;
import com.envyserve.githubreference.wordpress.PostItem;
import com.envyserve.githubreference.wordpress.WordpressListAdapter;
import com.envyserve.githubreference.wordpress.ui.WordpressDetailActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

public class WordpressGetTask extends AsyncTask<String, Integer, Boolean> {

    private String url;
    private boolean initialload;
    private WordpressGetTaskInfo info;
    private ArrayList<PostItem> postList;

    private static final String PER_PAGE = "15";
    private static final String PER_PAGE_RELATED = "4";
    private static final String API_LOC = "/?json=";
    private static final String API_LOC_FRIENDLY = "/api/";
    private static final String PARAMS = "date_format=U&exclude=comments,categories,custom_fields";

    public static String getRecentPosts(WordpressGetTaskInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_recent_posts");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        builder.append(PER_PAGE);
        builder.append("&page=");

        new WordpressGetTask(builder.toString(), true, info).execute();

        return builder.toString();
    }

    public static String getTagPosts(WordpressGetTaskInfo info, String tag) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_tag_posts");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        if (info.simpleMode)
            builder.append(PER_PAGE_RELATED);
        else
            builder.append(PER_PAGE);
        builder.append("&tag_slug=");
        builder.append(tag);
        builder.append("&page=");

        new WordpressGetTask(builder.toString(), true, info).execute();

        return builder.toString();
    }

    public static String getCategoryPosts(WordpressGetTaskInfo info, String category) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_category_posts");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        builder.append(PER_PAGE);
        builder.append("&category_slug=");
        builder.append(category);
        builder.append("&page=");

        new WordpressGetTask(builder.toString(), true, info).execute();

        return builder.toString();
    }

    public static String getSearchPosts(WordpressGetTaskInfo info, String query) {
        StringBuilder builder = new StringBuilder();
        builder.append(info.baseurl);
        builder.append(getApiLoc());
        builder.append("get_search_results");
        builder.append(getParams(PARAMS));
        builder.append("&count=");
        builder.append(PER_PAGE);
        builder.append("&search=");
        builder.append(query);
        builder.append("&page=");

        //TODO if a task is active, cancel it, as the search query has priority
        //if (mTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
        //    mTask.cancel(true);
        //    mTask = null;
        if (info.isLoading) {
            info.isLoading = false;
        }
        //}

        new WordpressGetTask(builder.toString(), true, info).execute();

        return builder.toString();
    }

    public static String getPostUrl(long id, String baseurl) {
        StringBuilder builder = new StringBuilder();
        builder.append(baseurl);
        builder.append(getApiLoc());
        builder.append("get_post");
        builder.append(getParams("post_id="));
        builder.append(id);

        return builder.toString();
    }

    public static void loadMorePosts(WordpressGetTaskInfo info, String withUrl) {
        new WordpressGetTask(withUrl, false, info).execute();
    }

    public WordpressGetTask(String url, boolean firstload, WordpressGetTaskInfo info) {
        this.url = url;
        this.initialload = firstload;
        this.info = info;
    }

    @Override
    protected void onPreExecute() {
        if (info.isLoading) {
            this.cancel(true);
        } else {
            info.isLoading = true;
        }

        if (initialload) {

            if (null != info.dialogLayout && info.dialogLayout.getVisibility() == View.GONE) {
                info.dialogLayout.setVisibility(View.VISIBLE);
                info.feedListView.setVisibility(View.GONE);
            }

            info.curpage = 1;

            if (null != info.feedListView) {
                info.feedListView.setAdapter(null);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !info.simpleMode) {
                info.feedListView.addFooterView(info.footerView);
            }
        } else {
            info.feedListView.addFooterView(info.footerView);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (null != postList) {
            updateList(initialload);
        } else {
            String message;
            if (!info.baseurl.startsWith("http") || info.baseurl.endsWith("/")) {
                message = "Debug info: '" + info.baseurl + "' is most likely not a valid API url. Make sure the url entered in your configuration starts with 'http' and does not end with 'api/' or '/'.";
            } else {
                message = "Debug info: We where not able to parse '" + url + "'. Make sure this url returns a valid JSON response.";
            }

            Helper.noConnection(info.context, message);
        }

        if (null != postList && postList.size() < 1 && !info.simpleMode) {
            Toast.makeText(
                    info.context,
                    info.context.getResources().getString(R.string.no_results),
                    Toast.LENGTH_LONG).show();
        }

        if (null != info.dialogLayout && info.dialogLayout.getVisibility() == View.VISIBLE) {
            info.dialogLayout.setVisibility(View.GONE);
            Helper.revealView(info.feedListView, info.frame);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                info.feedListView.removeFooterView(info.footerView);
            }
        } else {
            info.feedListView.removeFooterView(info.footerView);
        }
        info.isLoading = false;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        // String url = params[0];
        url = url + Integer.toString(info.curpage);
        info.curpage = info.curpage + 1;

        // getting JSON string from URL
        JSONObject json = Helper.getJSONObjectFromUrl(url);

        // parsing json data
        if (json != null)
            parseJson(json);
        return true;
    }

    public void parseJson(JSONObject json) {
        try {
            info.pages = json.getInt("pages");
            // parsing json object
            if (json.getString("status").equalsIgnoreCase("ok")) {
                JSONArray posts = json.getJSONArray("posts");

                postList = new ArrayList<PostItem>();

                for (int i = 0; i < posts.length(); i++) {
                    try {
                        JSONObject post = (JSONObject) posts.getJSONObject(i);
                        PostItem item = new PostItem();
                        item.setTitle(Html.fromHtml(post.getString("title"))
                                .toString());
                        item.setDate(new Date(post.getLong("date") * 1000));
                        item.setId(post.getLong("id"));
                        item.setUrl(post.getString("url"));
                        item.setContent(post.getString("content"));
                        if (post.has("author")) {
                            Object author = post.get("author");
                            if (author instanceof JSONArray
                                    && ((JSONArray) author).length() > 0) {
                                author = ((JSONArray) author).getJSONObject(0);
                            }

                            if (author instanceof JSONObject
                                    && ((JSONObject) author).has("name")) {
                                item.setAuthor(((JSONObject) author)
                                        .getString("name"));
                            }
                        }

                        if (post.has("tags") && post.getJSONArray("tags").length() > 0) {
                            item.setTag(((JSONObject) post.getJSONArray("tags").get(0)).getString("slug"));
                        }

                        // TODO do we dear to remove catch clause?
                        try {
                            boolean thumbnailfound = false;

                            if (post.has("thumbnail")) {
                                String thumbnail = post.getString("thumbnail");
                                if (!thumbnail.equals("")) {
                                    item.setThumbnailUrl(thumbnail);
                                    thumbnailfound = true;
                                }
                            }

                            if (post.has("attachments")) {

                                JSONArray attachments = post
                                        .getJSONArray("attachments");

                                // checking how many attachments post has and
                                // grabbing the first one
                                if (attachments.length() > 0) {
                                    JSONObject attachment = attachments
                                            .getJSONObject(0);

                                    item.setAttachmentUrl(attachment
                                            .getString("url"));

                                    // if we do not have a thumbnail yet, get
                                    // one now. But only if 'images' exists and is of type JSONObject
                                    if (attachment.has("images")
                                            && !thumbnailfound && attachment.optJSONObject("images") != null) {

                                        JSONObject thumbnail;
                                        if (attachment.getJSONObject("images")
                                                .has("post-thumbnail")) {
                                            thumbnail = attachment
                                                    .getJSONObject("images")
                                                    .getJSONObject(
                                                            "post-thumbnail");

                                            item.setThumbnailUrl(thumbnail
                                                    .getString("url"));
                                        } else if (attachment.getJSONObject(
                                                "images").has("thumbnail")) {
                                            thumbnail = attachment
                                                    .getJSONObject("images")
                                                    .getJSONObject("thumbnail");

                                            item.setThumbnailUrl(thumbnail
                                                    .getString("url"));
                                        }

                                    }
                                }
                            }

                            //Complete the post in the background (if enabled)
                            if (WordpressDetailActivity.PRELOAD_POSTS)
                                new BackgroundPostCompleter(item, info.baseurl, null).start();

                        } catch (Exception e) {
                            Log.v("INFO",
                                    "Item "
                                            + i
                                            + " of "
                                            + posts.length()
                                            + " will have no thumbnail or image because of exception!");
                            Log.printStackTrace(e);
                        }

                        if (!item.getId().equals(info.ignoreId)) {
                            postList.add(item);
                        }
                    } catch (Exception e) {
                        Log.v("INFO", "Item " + i + " of " + posts.length()
                                + " has been skipped due to exception!");
                        Log.printStackTrace(e);
                    }
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public void updateList(boolean initialload) {
        if (initialload) {
            info.feedListAdapter = new WordpressListAdapter(info.context, 0, postList, info.simpleMode);
            info.feedListView.setAdapter(info.feedListAdapter);
        } else {
            info.feedListAdapter.addAll(postList);
            info.feedListAdapter.notifyDataSetChanged();
        }
    }

    public static String getParams(String params) {
        String query = (Config.USE_WP_FRIENDLY) ? "?" : "&";
        return query + params;
    }

    public static String getApiLoc() {
        return (Config.USE_WP_FRIENDLY) ? API_LOC_FRIENDLY : API_LOC;
    }
}
