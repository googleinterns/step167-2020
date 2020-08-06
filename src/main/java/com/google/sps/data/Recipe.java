package com.google.sps.meltingpot.data;

public class Recipe extends DBObject {
  public static final String CONTENT_KEY = "content";
  public static final String TITLE_KEY = "title";
  public static final String CREATOR_ID_KEY = "creatorId";
  public static final String ID_KEY = "id";

  public String title;
  public String content;
  public String creatorId;

  /** Empty constructor needed to deserialize Recipe object in Firestore queries. */
  public Recipe() {}

  public Recipe(String id, String title, String content, String creatorId) {
    super(id);
    this.title = title;
    this.content = content;
    this.creatorId = creatorId;
  }
}
