package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import java.util.concurrent.ExecutionException;

public class User {
  public static final String CREATED_RECIPES_KEY = "created-recipe-ids";

  public static boolean createdRecipe(String userId, String recipeId) {
    DocumentReference userRef = DBReferences.user(userId);
    ApiFuture<DocumentSnapshot> userFuture = userRef.get();
    try {
      DocumentSnapshot user = userFuture.get();
      if (!user.exists()) {
        return false;
      }
      Boolean userCreatedRecipe =
          user.getBoolean(DBReferences.getNestedPropertyName(CREATED_RECIPES_KEY, recipeId));
      // note that userCreatedRecipe is a Boolean, not a boolean
      if (userCreatedRecipe == null || userCreatedRecipe == false) {
        return false;
      }
      return true;
    } catch (InterruptedException e) {
      System.out.println("Attempt to query user raised exception: " + e);
    } catch (ExecutionException e) {
      System.out.println("Attempt to query user raised exception: " + e);
    }
    return true;
  }
}