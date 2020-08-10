package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.firebase.cloud.FirestoreClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreDB implements DBInterface {
    public String getRecipeContent(String Id) {
        return null;
    }

    public RecipeMetadata getRecipeMetadata(String Id) {
        return null;
    }
    public String addRecipe(RecipeMetadata newRecipe, String newContent) {
        return null;
    }
    public void deleteRecipe(String Id) {
        return;
    }

    public void editRecipeTitleContent(String editedTitle, String editedContent) {
        return;
    }

    public Iterable<RecipeMetadata> getAllRecipes() {
        return null;
    }
    public Iterable<Comment> getAllCommentsInRecipe(String recipeId) {
        return null;
    }
    public Iterable<Tag> getAllTags() {
        return null;
    }

    /** Returns a User object from userId. */
    public User getUser(String userId) {
      DocumentReference userRef = DBUtils.user(userId);
      DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
      return user.toObject(User.class);
    }
   
    /** Adds a User to the db based on an input userId. */
    public String addUser(String userId) {
      DocumentReference newUserRef = DBUtils.users().document(userId);
      User newUser = new User(userId);
      DBUtils.blockOnFuture(newUserRef.set(newUser));  // .set() returns an ApiFuture
      return userId;
    }
    
    /** Delete a User from the db based on an input userId. */
    public void deleteUser(String userId) {
      DBUtils.blockOnFuture(DBUtils.user(userId).delete());
    }
    
    /**
     * Sets a property's value to "true" for a certain user document.
     * Can be used to let a user add a recipe to saved or created, or to let user follow a tag.
     * @param userId the user's Firebase ID
     * @param objectId the ID of either a recipe if the intent is to save/create, or of a tag for tag following.
     * @param collection a constant from User class indicating which mode -- save, create, or follow tag.
     */
    public void makeUserPropertyTrue(String userId, String objectId, String collection) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName;
      
      switch (collection) {
        case User.SAVE:
          nestedPropertyName = DBUtils.getNestedPropertyName(User.SAVED_RECIPES_KEY, objectId);
          break;
        case User.CREATE:
          nestedPropertyName = DBUtils.getNestedPropertyName(User.CREATED_RECIPES_KEY, objectId);
          break;
        case User.TAG:
          nestedPropertyName = DBUtils.getNestedPropertyName(User.TAGS_FOLLOWED_KEY, objectId);
          break;
      }

      ApiFuture addUserPropertyFuture = 
          userRef.update(Collections.singletonMap(nestedPropertyName, true));
      DBUtils.blockOnFuture(addUserPropertyFuture);
    }

    /**
     * Deletes a property value for a certain user document.
     * Can be used to let a user delete a recipe from saved or created, or to let user unfollow a tag.
     * @param userId the user's Firebase ID
     * @param objectId the ID of either a recipe if the intent is to unsave/create, or of a tag for tag unfollowing.
     * @param collection a constant from User class indicating which mode -- save, create, or tag.
     */
    public void deleteUserProperty(String userId, String objectId, String collection) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName;
      
      switch (collection) {
        case User.SAVE:
          nestedPropertyName = DBUtils.getNestedPropertyName(User.SAVED_RECIPES_KEY, objectId);
          break;
        case User.CREATE:
          nestedPropertyName = DBUtils.getNestedPropertyName(User.CREATED_RECIPES_KEY, objectId);
          break;
        case User.TAG:
          nestedPropertyName = DBUtils.getNestedPropertyName(User.TAGS_FOLLOWED_KEY, objectId);
          break;
      }

      ApiFuture removeUserPropertyFuture = 
          userRef.update(nestedPropertyName, FieldValue.delete());
      DBUtils.blockOnFuture(removeUserPropertyFuture);
    }

    public Iterable<RecipeMetadata> getRecipesMatchingTags(Iterable<String> tagIds) {
        return null;
    }
    public Iterable<RecipeMetadata> getRecipesMatchingCreator(String creatorId) {
        return null;
    }
    public Iterable<RecipeMetadata> getRecipesMatchingIDs(Iterable<String> Ids) {
        return null;
    }

    public Comment addComment(Comment newComment) {
        return null;
    }
    public void deleteComment(String Id, String recipeId) {
        return;
    }
    public void editCommentContent(String editedContent) {
        return;
    }
}