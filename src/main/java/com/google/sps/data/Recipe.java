package com.google.sps.meltingpot.data;

public class Recipe extends DBObject {
  public static final String CONTENT_KEY = "content";
  public static final String CREATOR_ID_KEY = "creatorId";
  public static final String ID_KEY = "id";
  public static final String IM_KEY = "blobKey";
  public static final String TITLE_KEY = "title";
  public static final String VOTES_KEY = "votes";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String TAG_IDS_KEY = "tagIds";

  public RecipeMetadata metadata;
  public String content;
  public String blobKey;

  public Recipe() {}
  
  public Recipe(String id){
      super(id);
  }

  public Recipe(String content, RecipeMetadata metadata) {
    this.content = content;
    this.metadata = metadata;
  }
}
