package com.google.sps.meltingpot.data;

public class Recipe extends DBObject {
  public static final String CONTENT_KEY = "content";
  public static final String TITLE_KEY = "title";

  public String title;
  public String content;
  public String creatorId;

  public Recipe(String id, String title, String content, String creatorId) {
    super(id);
    this.title = title;
    this.content = content;
    this.creatorId = creatorId;
  }
}
