package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.sps.meltingpot.data.DBUtils;

public class User {
  public static final String CREATED_RECIPES_KEY = "created-recipe-ids";
  public static final String SAVED_RECIPES_KEY = "saved-recipe-ids";
  public static final String TAGS_FOLLOWED_KEY = "followed-tag-ids";

  public static boolean createdRecipe(String userId, String recipeId) {
    DocumentReference userRef = DBUtils.user(userId);
    ApiFuture<DocumentSnapshot> userFuture = userRef.get();
    DocumentSnapshot user = DBUtils.blockOnFuture(userFuture);
    if (!user.exists()) {
      return false;
    }
    Boolean userCreatedRecipe =
        user.getBoolean(DBUtils.getNestedPropertyName(CREATED_RECIPES_KEY, recipeId));
    // note that userCreatedRecipe is a Boolean, not a boolean
    return userCreatedRecipe != null && userCreatedRecipe == true;
  }
}