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
    
    /** Add recipe id: true as a singleton map to user's created recipes field. */
    public void addRecipeIdToCreated(String userId, String recipeId) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(User.CREATED_RECIPES_KEY, recipeId);
      ApiFuture addToCreatedPostsFuture =
          userRef.update(Collections.singletonMap(nestedPropertyName, true));
      DBUtils.blockOnFuture(addToCreatedPostsFuture);
    }
    
    /** Remove a recipe from a user's created (when recipe is deleted). */
    public void removeRecipeIdFromCreated(String userId, String recipeId) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(User.CREATED_RECIPES_KEY, recipeId);
      Map<String, Object> update = new HashMap<>();
      update.put(nestedPropertyName, FieldValue.delete());
      DBUtils.blockOnFuture(userRef.update(update));
    }
    
    /** Add recipe id: true as a singleton map to user's saved recipes field. */
    public void addRecipeIdToSaved(String userId, String recipeId) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(User.SAVED_RECIPES_KEY, recipeId);
      ApiFuture addToSavedPostsFuture =
          userRef.update(Collections.singletonMap(nestedPropertyName, true));
      DBUtils.blockOnFuture(addToSavedPostsFuture);
    }

    /** Remove a recipe from a user's created (when recipe is deleted or unsaved). */
    public void removeRecipeIdFromSaved(String userId, String recipeId) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(User.SAVED_RECIPES_KEY, recipeId);
      Map<String, Object> update = new HashMap<>();
      update.put(nestedPropertyName, FieldValue.delete());
      DBUtils.blockOnFuture(userRef.update(update));
    }

    /** Add tag id: true as a singleton map to user's tags followed field. */
    public void followTag(String userId, String tagId) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(User.TAGS_FOLLOWED_KEY, tagId);
      ApiFuture addToTagsFollowedFuture =
          userRef.update(Collections.singletonMap(nestedPropertyName, true));
      DBUtils.blockOnFuture(addToTagsFollowedFuture);
    }
   
    /** Remove a tag from a user's followed tags. */
    public void unfollowTag(String userId, String tagId) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(User.TAGS_FOLLOWED_KEY, tagId);
      Map<String, Object> update = new HashMap<>();
      update.put(nestedPropertyName, FieldValue.delete());
      DBUtils.blockOnFuture(userRef.update(update));
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