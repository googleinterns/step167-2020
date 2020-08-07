package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.GeoPoint;
import java.util.List;

public class RecipeMetadata extends DBObject {

    public static final String TITLE_KEY = "title";

    public String title;
    public String creatorId;
    public String creatorLdap;
    public long timestamp;
    public List<Tag> tags;
    public long votes;
    public GeoPoint location;

    public RecipeMetadata(){super();}

    public RecipeMetadata(String id) {
      super(id);
    }
}