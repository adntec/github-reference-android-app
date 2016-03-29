package com.envyserve.githubreference.wordpress;

import com.envyserve.githubreference.util.SerializableJSONArray;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.Date;

public class PostItem implements Serializable {

	//Auto generated
	private static final long serialVersionUID = 1L;

    private boolean isCompleted;
	
	private String title;
	private Date date;
	private String attachmentUrl;
	private String thumbnailUrl;
	private Long id;
	private String content;
	private String url;
	private String author;
	private String tag;
    private Long commentCount;
    private SerializableJSONArray commentsArray;

    public PostItem(){
        this.isCompleted = false;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getAttachmentUrl() {
		return attachmentUrl;
	}

	public void setAttachmentUrl(String attachmentUrl) {
		this.attachmentUrl = attachmentUrl;
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

    public JSONArray getCommentsArray() {
        return commentsArray.getJSONArray();
    }

    public void setCommentsArray(JSONArray commentsArray) {
        this.commentsArray = new SerializableJSONArray(commentsArray);
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public void setPostCompleted(){
        isCompleted = true;
    }

    public boolean isCompleted(){
        return isCompleted;
    }
}

