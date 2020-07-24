package com.google.sps.data;

public class Comment {

	public static final String CONTENT_KEY = "content";
	public static final String POST_ID_KEY = "post-id";
	public static final String USER_ID_KEY = "user-id";

	public String uid;
	public String content;
	public String postId;
	public String userId;

	public Comment(String uid, String content, String postId, String userId) {
		this.uid = uid;
		this.content = content;
		this.postId = postId;
		this.userId = userId;
	}

	public Comment(String content, String postId, String userId) {
		this.content = content;
		this.postId = postId;
		this.userId = userId;
	}
}