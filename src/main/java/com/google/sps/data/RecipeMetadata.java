package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.GeoPoint;
import java.util.Map;

public class RecipeMetadata extends DBObject {
  public static final String CREATOR_ID_KEY = "creatorId";
  public static final String ID_KEY = "id";
  public static final String TITLE_KEY = "title";
  public static final String VOTES_KEY = "votes";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String TAG_IDS_KEY = "tagIds";

  public String title;
  public String creatorId;
  public String creatorLdap;
  public String imageUrl;
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

  public long getVotes() {
    return this.votes;
  }

  public long getTimestamp() {
    return this.timestamp;
  }
}
