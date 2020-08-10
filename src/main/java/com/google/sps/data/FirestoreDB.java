package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.User;
import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreDB implements DBInterface {
  public String getRecipeContent(String Id) {
    return DBUtils.blockOnFuture(DBUtils.recipe(Id).get()).getString(Recipe.CONTENT_KEY);
  }

  public RecipeMetadata getRecipeMetadata(String Id) {
    return DBUtils.blockOnFuture(DBUtils.recipeMetadata(Id).get()).toObject(RecipeMetadata.class);
  }

  public String addRecipe(RecipeMetadata newRecipeMetadata, String newContent) {
    DocumentReference newContentRef = DBUtils.recipes().document();
    newRecipeMetadata.id = newContentRef.getId();
    DocumentReference newRecipeMetadataRef = DBUtils.recipeMetadata(newRecipeMetadata.id);
    DBUtils.blockOnFuture(
        newContentRef.set(Collections.singletonMap(Recipe.CONTENT_KEY, newContent)));
    DBUtils.blockOnFuture(newRecipeMetadataRef.set(newRecipeMetadata));
    return newRecipeMetadata.id;
  }
  public void deleteRecipe(String Id) {
    DBUtils.blockOnFuture(DBUtils.recipe(Id).delete());
    DBUtils.blockOnFuture(DBUtils.recipeMetadata(Id).delete());
  }

  public void editRecipeTitleContent(String Id, String editedTitle, String editedContent) {
    DocumentReference contentRef = DBUtils.recipe(Id);
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    DBUtils.blockOnFuture(contentRef.update(Recipe.CONTENT_KEY, editedContent));
    DBUtils.blockOnFuture(metadataRef.update(RecipeMetadata.TITLE_KEY, editedTitle));
  }

  public long voteRecipe(String Id, int voteDiff) {
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    long votes = DBUtils.blockOnFuture(metadataRef.get()).getLong(RecipeMetadata.VOTES_KEY);
    metadataRef.update(RecipeMetadata.VOTES_KEY, votes + voteDiff);
    return votes + voteDiff;
  }

  public List<RecipeMetadata> getAllRecipes(SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipeMetadata();
    switch (sortingMethod) {
      case TOP:
        recipesQuery = recipesQuery.orderBy(RecipeMetadata.VOTES_KEY, Query.Direction.DESCENDING);
        break;
      case NEW:
        recipesQuery =
            recipesQuery.orderBy(RecipeMetadata.TIMESTAMP_KEY, Query.Direction.DESCENDING);
        break;
    }
    return DBUtils.blockOnFuture(recipesQuery.get()).toObjects(RecipeMetadata.class);
  }

  public List<Comment> getAllCommentsInRecipe(String recipeId, SortingMethod sortingMethod) {
    Query commentsQuery = DBUtils.comments(recipeId);
    switch (sortingMethod) {
      case TOP:
        commentsQuery = commentsQuery.orderBy(Comment.VOTES_KEY, Query.Direction.DESCENDING);
        break;
      case NEW:
        commentsQuery = commentsQuery.orderBy(Comment.TIMESTAMP_KEY, Query.Direction.DESCENDING);
        break;
    }
    return DBUtils.blockOnFuture(commentsQuery.get()).toObjects(Comment.class);
  }

  public List<Tag> getAllTags(boolean getHidden) {
    Query tagsQuery =
        getHidden ? DBUtils.tags() : DBUtils.tags().whereEqualTo(Tag.HIDDEN_KEY, false);
    return DBUtils.blockOnFuture(tagsQuery.get()).toObjects(Tag.class);
  }
  
  public List<Tag> getTagsMatchingIds(List<String> Ids) {
    Query tagsQuery = DBUtils.tags().whereIn(Tag.ID_KEY, Ids);
    return DBUtils.blockOnFuture(tagsQuery.get()).toObjects(Tag.class);
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
     * @param collection a KEY constant from User class indicating which mode -- save, create, or follow tag.
     */
    public void makeUserPropertyTrue(String userId, String objectId, String collection) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
      ApiFuture addUserPropertyFuture = 
          userRef.update(Collections.singletonMap(nestedPropertyName, true));
      DBUtils.blockOnFuture(addUserPropertyFuture);
    }

    /**
     * Deletes a property value for a certain user document.
     * Can be used to let a user delete a recipe from saved or created, or to let user unfollow a tag.
     * @param userId the user's Firebase ID
     * @param objectId the ID of either a recipe if the intent is to unsave/create, or of a tag for tag unfollowing.
     * @param collection a KEY constant from User class indicating which mode -- save, create, or tag.
     */
    public void deleteUserProperty(String userId, String objectId, String collection) {
      DocumentReference userRef = DBUtils.user(userId);
      String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
      ApiFuture removeUserPropertyFuture = 
          userRef.update(nestedPropertyName, FieldValue.delete());
      DBUtils.blockOnFuture(removeUserPropertyFuture);
    }

  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod) {
    Query recipesQuery = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipes().whereEqualTo("creatorId", creatorId);
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesSavedBy(String userId, SortingMethod sortingMethod) {
    List<String> saved_Ids = savedRecipeIds(userId);
    return getRecipesMatchingIDs(saved_Ids, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipes().whereIn(Recipe.ID_KEY, Ids);
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  private List<RecipeMetadata> getRecipeMetadataQuery(
      Query recipesQuery, SortingMethod sortingMethod) {
    switch (sortingMethod) {
      case TOP:
        recipesQuery = recipesQuery.orderBy(Recipe.VOTES_KEY, Query.Direction.DESCENDING);
        break;
      case NEW:
        recipesQuery = recipesQuery.orderBy(Recipe.TIMESTAMP_KEY, Query.Direction.DESCENDING);
        break;
    }

    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(recipesQuery.get());

    if (querySnapshot == null) {
      return null;
    }

    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
      RecipeMetadata recipe = document.toObject(RecipeMetadata.class);
      recipeList.add(recipe);
    }
    return recipeList;
  }

  /**
   * Recursively constructs a query on recipes matching all tags passed in
   *  eg. DBUtils.recipes().whereEqualTo("tag_N-1", true).whereEqualTo("tag_N-2", true)...
   */
  private Query recipesMatchingTags(Iterable<String> tagIds, Iterator<String> iter) {
    if (iter.hasNext()) {
      String nextTag = iter.next();
      return recipesMatchingTags(tagIds, iter).whereEqualTo("tags." + nextTag, true);
    }
    return DBUtils.recipes();
  }

  public List<String> savedRecipeIds(String userId) {
    List<String> allRecipeIds = DBUtils.allRecipeIds();
    ArrayList<String> savedRecipeIds = new ArrayList<String>();

    for (String recipeID : allRecipeIds) {
      if (isSavedRecipe(userId, recipeID)) {
        savedRecipeIds.add(recipeID);
      }
    }
    return savedRecipeIds;
  }

  public String addComment(Comment newComment, String recipeId) {
    DocumentReference newCommentRef = DBUtils.comments(recipeId).document();
    DBUtils.blockOnFuture(newCommentRef.set(newComment));
    return newCommentRef.getId();
  }

  public void deleteComment(String Id, String recipeId) {
    DBUtils.blockOnFuture(DBUtils.comments(recipeId).document(Id).delete());
  }

  public void deleteComments(String recipeId) {
    List<QueryDocumentSnapshot> documents =
        DBUtils.blockOnFuture(DBUtils.comments(recipeId).get()).getDocuments();
    for (QueryDocumentSnapshot document : documents) {
      document.getReference().delete();
    }
  }

  public void editCommentContent(String Id, String recipeId, String editedContent) {
    DBUtils.blockOnFuture(
        DBUtils.comments(recipeId).document(Id).update(Comment.CONTENT_KEY, editedContent));
  }

  public boolean createdRecipe(String userId, String recipeId) {
    DocumentReference userRef = DBUtils.user(userId);
    ApiFuture<DocumentSnapshot> userFuture = userRef.get();
    DocumentSnapshot user = DBUtils.blockOnFuture(userFuture);
    if (!user.exists()) {
      return false;
    }
    Boolean userCreatedRecipe =
        user.getBoolean(DBUtils.getNestedPropertyName(User.CREATED_RECIPES_KEY, recipeId));
    // note that userCreatedRecipe is a Boolean, not a boolean
    return userCreatedRecipe != null && userCreatedRecipe;
  }

  public boolean isSavedRecipe(String userId, String recipeId) {
    DocumentReference userRef = DBUtils.user(userId);
    ApiFuture<DocumentSnapshot> userFuture = userRef.get();
    DocumentSnapshot user = DBUtils.blockOnFuture(userFuture);

    if (!user.exists()) {
      return false;
    }
    Boolean userSavedRecipe =
        user.getBoolean(DBUtils.getNestedPropertyName(User.SAVED_RECIPES_KEY, recipeId));
    return userSavedRecipe != null && userSavedRecipe;
  }
}
