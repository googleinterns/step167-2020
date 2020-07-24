package com.google.sps.data;

public class Post {
  public static final String TITLE_KEY = "title";
  public static final String CONTENT_KEY = "content";

  public String uid;
  public String title;
  public String content;

  public Post(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public Post(String uid, String title, String content) {
    this.uid = uid;
    this.title = title;
    this.content = content;
  }
}
