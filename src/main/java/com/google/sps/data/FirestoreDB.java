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
  /** 
   * Gets the content (ingredients, steps) of a recipe from Firestore.
   * @param Id the recipe's Firestore ID.
   * @return recipe content as a String
   */
  public String getRecipeContent(String Id) {
    return DBUtils.blockOnFuture(DBUtils.recipe(Id).get()).getString(Recipe.CONTENT_KEY);
  }

  /** 
   * Gets the recipe metadata of a recipe from Firestore.
   * Though recipes/their metadata belong to different collections, they have the same Firestore ID.
   * @param Id the recipe's Firestore ID.
   * @return recipe metadata
   */
  public RecipeMetadata getRecipeMetadata(String Id) {
    return DBUtils.blockOnFuture(DBUtils.recipeMetadata(Id).get()).toObject(RecipeMetadata.class);
  }

  /** 
   * Adds a recipe and its metadata to their respective collections in Firestore.
   * @param newRecipeMetadata a populated RecipeMetadata object (title, timestamp, etc)
   * @param newContent the new recipe's content, as distinct from metadata
   * @return the new recipe's Firestore doc ID
   */
  public String addRecipe(RecipeMetadata newRecipeMetadata, String newContent) {
    DocumentReference newContentRef = DBUtils.recipes().document();
    newRecipeMetadata.id = newContentRef.getId();
    DocumentReference newRecipeMetadataRef = DBUtils.recipeMetadata(newRecipeMetadata.id);
    DBUtils.blockOnFuture(
        newContentRef.set(Collections.singletonMap(Recipe.CONTENT_KEY, newContent)));
    DBUtils.blockOnFuture(newRecipeMetadataRef.set(newRecipeMetadata));
    return newRecipeMetadata.id;
  }
  
  /** 
   * Deletes a recipe's content and metadata from Firestore.
   * @param Id the recipe's Firestore ID.
   */
  public void deleteRecipe(String Id) {
    DBUtils.blockOnFuture(DBUtils.recipe(Id).delete());
    DBUtils.blockOnFuture(DBUtils.recipeMetadata(Id).delete());
  }

  /**
   * Update a recipe's title (metadata) and content.
   * @param Id recipe's Firestore ID
   * @param editedTitle new recipe title
   * @param editedContent new recipe content
   */
  public void editRecipeTitleContent(String Id, String editedTitle, String editedContent) {
    DocumentReference contentRef = DBUtils.recipe(Id);
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    DBUtils.blockOnFuture(contentRef.update(Recipe.CONTENT_KEY, editedContent));
    DBUtils.blockOnFuture(metadataRef.update(RecipeMetadata.TITLE_KEY, editedTitle));
  }
  
  /**
   *  Adjusts the vote count on a recipe.
   * @param Id recipe's Firestore ID
   * @param voteDiff int value that should be added to the current votes on the recipe
   * @return recipe vote count after the update
   */
  public long voteRecipe(String Id, int voteDiff) {
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    long votes = DBUtils.blockOnFuture(metadataRef.get()).getLong(RecipeMetadata.VOTES_KEY);
    metadataRef.update(RecipeMetadata.VOTES_KEY, votes + voteDiff);
    return votes + voteDiff;
  }
  
  /** 
   * Gets a sorted list of all recipe metadata.
   * @param sortingMethod from SortingMethod.java
   * @return ordered list of recipe metadata
   */
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

  /** 
   * Gets a sorted list of all comments associated with a recipe.
   * @param recipeId recipe Firestore ID
   * @param sortingMethod from SortingMethod.java
   * @return ordered list of recipe comments
   */
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

  /**
   * Returns all the tags in the DB.
   * @param getHidden if true, get all the tags in the DB. If false, only get non-hidden tags.
   * @return list of tags
   */
  public List<Tag> getAllTags(boolean getHidden) {
    Query tagsQuery =
        getHidden ? DBUtils.tags() : DBUtils.tags().whereEqualTo(Tag.HIDDEN_KEY, false);
    return DBUtils.blockOnFuture(tagsQuery.get()).toObjects(Tag.class);
  }
  
  /** 
   * Determines if a document exists in a Firestore collection.
   * @param docId the document's Firebase ID.
   * @param collection the collection the document should be in; MUST BE A COLLECTION CONSTANT from DBUtils.java
   * @return false if the ID is not associated with any doc in the specified collection, 
   * true if it is.
   */
  public boolean isDocument(String docId, String collection) {
    DocumentReference docRef;
    
    // Look in the appropriate bin.
    switch (collection) {
      case DBUtils.DB_RECIPES_COLLECTION:
        docRef = DBUtils.recipe(docId);
        break;
      case DBUtils.DB_USERS_COLLECTION:
        docRef = DBUtils.user(docId);
        break;
      case DBUtils.DB_TAGS_COLLECTION:
        docRef = DBUtils.tag(docId);
        break;
    }

    DocumentSnapshot document = DBUtils.blockOnFuture(docRef.get());
    return document.exists();
  }

  /** 
   * Returns a User object from userId. 
   * @param userId the user's Firebase ID.
   * @return the User object parsed from the document associated with the ID.
   */
  public User getUser(String userId) {
    DocumentReference userRef = DBUtils.user(userId);
    DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
    return user.toObject(User.class);
  }

  /** 
   * Adds a User to the db based on an input userId. 
   * @param userId the user's Firebase ID.
   * @return String userId.
   */
  public String addUser(String userId) {
    DocumentReference newUserRef = DBUtils.users().document(userId);
    User newUser = new User(userId);
    DBUtils.blockOnFuture(newUserRef.set(newUser)); // .set() returns an ApiFuture
    return userId;
  }

  /** 
   * Delete a User from the db based on an input userId. 
   * @param userId the user's Firebase ID.
   */
  public void deleteUser(String userId) {
    DBUtils.blockOnFuture(DBUtils.user(userId).delete());
  }

  /**
   * Sets a property's value to "true" for a certain user document.
   * Can be used to let a user add a recipe to saved or created, or to let user follow a tag.
   * @param userId the user's Firebase ID
   * @param objectId the ID of either a recipe if the intent is to save/create, or of a tag for tag
   *     following.
   * @param collection a KEY constant from User class indicating which mode -- save, create, or
   *     follow tag.
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
   * @param objectId the ID of either a recipe if the intent is to unsave/create, or of a tag for
   *     tag unfollowing.
   * @param collection a KEY constant from User class indicating which mode -- save, create, or tag.
   */
  public void deleteUserProperty(String userId, String objectId, String collection) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    ApiFuture removeUserPropertyFuture = userRef.update(nestedPropertyName, FieldValue.delete());
    DBUtils.blockOnFuture(removeUserPropertyFuture);
  }

  /**
   * Returns a list of all recipe metadata with any of the tags in the tag IDs list.
   * @param tagIds tags' Firestore IDs
   * @param sortingMethod such as TOP or NEW
   * @return list of recipe metadata matching one or more of the tags in tagIds param
   */
  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod) {
    Query recipesQuery = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  /**
   * Returns a list of the recipe metadata with a certain creator.
   * @param creatorId creator/user's Firestore ID
   * @param sortingMethod such as TOP or NEW
   * @return user's created recipe metadata, sorted
   */
  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipes().whereEqualTo(Recipe.CREATOR_ID_KEY, creatorId);
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }
  
  /**
   * Returns a list of the recipe metadata associated with the IDs of recipes saved by a user.
   * @param userId user's Firestore ID
   * @param sortingMethod such as TOP or NEW
   * @return user's saved recipe metadata, sorted
   */
  public List<RecipeMetadata> getRecipesSavedBy(String userId, SortingMethod sortingMethod) {
    List<String> saved_Ids = savedRecipeIds(userId);
    return getRecipesMatchingIDs(saved_Ids, sortingMethod);
  }

  /**
   * Returns a list of the recipe metadata whose IDs match any one ID in a given list of IDs.
   * @param Ids list of recipe Firestore IDs to be queried
   * @param sortingMethod such as TOP or NEW
   * @return list of RecipeMetadata whose IDs are in Ids param
   */
  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipes().whereIn(Recipe.ID_KEY, Ids);
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }
  
  /**
   * Returns an ordered list of RecipeMetadata objects based on a sorting method.
   * @param recipesQuery recipe metadata collection query
   * @param sortingMethod sorting method from SortingMethod class
   * @return ordered list of recipe metadata
   */
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

  /** 
   * Gets all of a user's saved recipe IDs.
   * @param userId the user's Firestore ID.
   * @return a list of the Firestore IDs of the user's saved recipes in no particular order.
   */
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
  
  /** 
   * Adds a Comment object to a recipe's comment subcollection in Firestore.
   * @param newComment the Comment object to be added.
   * @param recipeId the recipe's Firestore ID.
   */
  public String addComment(Comment newComment, String recipeId) {
    DocumentReference newCommentRef = DBUtils.comments(recipeId).document();
    DBUtils.blockOnFuture(newCommentRef.set(newComment));
    return newCommentRef.getId();
  }

  /** 
   * Deletes a single specified comment in a recipe's comment subcollection in Firestore.
   * @param Id the comment's Firestore ID.
   * @param recipeId the recipe's Firestore ID.
   */
  public void deleteComment(String Id, String recipeId) {
    DBUtils.blockOnFuture(DBUtils.comments(recipeId).document(Id).delete());
  }

  /** 
   * Deletes all the comments in a recipe's comment subcollection in Firestore.
   * @param recipeId the recipe's Firestore ID.
   */
  public void deleteComments(String recipeId) {
    List<QueryDocumentSnapshot> documents =
        DBUtils.blockOnFuture(DBUtils.comments(recipeId).get()).getDocuments();
    for (QueryDocumentSnapshot document : documents) {
      document.getReference().delete();
    }
  }
  
  /** 
   * Edits the content field of a comment under a certain recipe in Firestore.
   * @param Id the comment's Firebase ID.
   * @param recipeId the recipe's Firebase ID.
   * @param editedContent the new content to replace the old.
   */
  public void editCommentContent(String Id, String recipeId, String editedContent) {
    DBUtils.blockOnFuture(
        DBUtils.comments(recipeId).document(Id).update(Comment.CONTENT_KEY, editedContent));
  }
 
  /** 
   * Checks if a specified user created a specified recipe.
   * @param userId the user's Firebase ID.
   * @param recipeId the recipe's Firebase ID.
   * @return true if created, false if not or if user doesn't exist.
   */
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
  
  /** 
   * Checks if a specified user saved a specified recipe.
   * @param userId the user's Firebase ID.
   * @param recipeId the recipe's Firebase ID.
   * @return true if saved, false if not or if user doesn't exist.
   */
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
