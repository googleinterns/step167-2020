package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.gson.Gson;
import com.google.sps.meltingpot.data.DBUtils;
import java.util.ArrayList;
import java.util.List;

public class User extends DBObject {
  public static final String CREATED_RECIPES_KEY = "created-recipe-ids";
  public static final String SAVED_RECIPES_KEY = "saved-recipe-ids";

  public User(String id) {
    super(id);
  }

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
    return userCreatedRecipe != null && userCreatedRecipe;
  }

  public static boolean isSavedRecipe(String userId, String recipeId) {
    DocumentReference userRef = DBUtils.user(userId);
    ApiFuture<DocumentSnapshot> userFuture = userRef.get();
    DocumentSnapshot user = DBUtils.blockOnFuture(userFuture);

    if (!user.exists()) {
      return false;
    }
    Boolean userSavedRecipe =
        user.getBoolean(DBUtils.getNestedPropertyName(SAVED_RECIPES_KEY, recipeId));
    return userSavedRecipe != null && userSavedRecipe;
  }

  public static ArrayList<String> createdRecipeIds(String userId) {
    ArrayList<String> allRecipeIds = DBUtils.allRecipeIds();
    ArrayList<String> createdRecipeIds = new ArrayList<String>();

    for (String recipeID : allRecipeIds) {
      if (createdRecipe(userId, recipeID)) {
        createdRecipeIds.add(recipeID);
      }
    }
    return createdRecipeIds;
  }

  public static ArrayList<String> savedRecipeIds(String userId) {
    ArrayList<String> allRecipeIds = DBUtils.allRecipeIds();
    ArrayList<String> savedRecipeIds = new ArrayList<String>();

    for (String recipeID : allRecipeIds) {
      if (isSavedRecipe(userId, recipeID)) {
        savedRecipeIds.add(recipeID);
      }
    }
    return savedRecipeIds;
  }
}