package com.google.sps.meltingpot.data;

public class Recipe extends DBObject {
  public static final String CONTENT_KEY = "content";
  public static final String TITLE_KEY = "title";
  public static final String CREATOR_ID_KEY = "creatorId";
  public static final String ID_KEY = "id";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String VOTES_KEY = "votes";

  public RecipeMetadata metadata;
  public String content;

  public Recipe() {}
}
