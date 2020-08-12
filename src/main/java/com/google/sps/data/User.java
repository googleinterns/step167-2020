package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class User extends DBObject {
  public static final String CREATED_RECIPES_KEY = "created-recipe-ids";
  public static final String SAVED_RECIPES_KEY = "saved-recipe-ids";
  public static final String TAGS_FOLLOWED_KEY = "followed-tag-ids";

  public User(String id) {
    super(id);
  }
}
