package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.GeoPoint;

public class RecipeMetadata extends DBObject {
  public String title;
  public String creatorId;
  public String creatorLdap;
  public long timestamp;
  public Iterable<String> tagIds;
  public long votes;
  public GeoPoint location;

  public RecipeMetadata(String id) {
    super(id);
  }
}
