package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.GeoPoint;
import java.util.Map;

public class RecipeMetadata extends DBObject {
  public static final String TITLE_KEY = "title";
  public static final String VOTES_KEY = "votes";
  public static final String TIMESTAMP_KEY = "timestamp";

  public String title;
  public String creatorId;
  public String creatorLdap;
  public long timestamp;
  public Map<String, Boolean> tagIds;
  public long votes;
  public GeoPoint location;

  public RecipeMetadata() {
    super();
  }

  public RecipeMetadata(String id) {
    super(id);
  }
}