package com.google.sps.data;

import java.util.List;

public class Post {

	public static final String CONTENT_KEY = "content";
	public static final String COMMENT_IDS_KEY = "commentIds";

	public String uid;
	public String content;
	public List<String> commentIds;
}