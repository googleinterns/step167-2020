package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.Query;
import java.util.Iterator;
import java.util.List;

public interface DBInterface {
  /**
   * Gets the content (ingredients, steps) of a recipe from Firestore.
   *
   * @param Id the recipe's Firestore ID.
   * @return recipe content as a String
   */
  public String getRecipeContent(String Id);

  /**
   * Gets the recipe metadata of a recipe from Firestore. Though recipes/their metadata belong to
   * different collections, they have the same Firestore ID.
   *
   * @param Id the recipe's Firestore ID.
   * @return recipe metadata
   */
  public RecipeMetadata getRecipeMetadata(String Id);

  /**
   * Adds a recipe and its metadata to their respective collections in Firestore.
   *
   * @param newRecipeMetadata a populated RecipeMetadata object (title, timestamp, etc)
   * @param newContent the new recipe's content, as distinct from metadata
   * @return the new recipe's Firestore doc ID
   */
  public String addRecipe(RecipeMetadata newRecipeMetadata, String newContent);

  /**
   * Deletes a recipe's content and metadata from Firestore.
   *
   * @param Id the recipe's Firestore ID.
   */
  public void deleteRecipe(String Id);

  /**
   * Update a recipe's title (metadata) and content.
   *
   * @param Id recipe's Firestore ID
   * @param editedTitle new recipe title
   * @param editedContent new recipe content
   */
  public void editRecipeTitleContent(String Id, String editedTitle, String editedContent);

  /**
   * Adjusts the vote count on a recipe.
   *
   * @param Id recipe's Firestore ID
   * @param voteDiff int value that should be added to the current votes on the recipe
   * @return recipe vote count after the update
   */
  public Long voteRecipe(String Id, int voteDiff);

  /**
   * Gets a sorted list of all recipe metadata.
   *
   * @param sortingMethod from SortingMethod.java
   * @return ordered list of recipe metadata
   */
  public List<RecipeMetadata> getAllRecipes(SortingMethod sortingMethod);

  /**
   * Gets a sorted list of all comments associated with a recipe.
   *
   * @param recipeId recipe Firestore ID
   * @param sortingMethod from SortingMethod.java
   * @return ordered list of recipe comments
   */
  public List<Comment> getAllCommentsInRecipe(String recipeId, SortingMethod sortingMethod);

  /**
   * Returns all the tags in the DB.
   *
   * @param getHidden if true, get all the tags in the DB. If false, only get non-hidden tags.
   * @return list of tags
   */
  public List<Tag> getAllTags(boolean getHidden);

  /**
   * Returns all the tags asssociated with an id in the given list of ids.
   *
   * @param Ids list of tag ids to get
   * @return list of tags
   */
  public List<Tag> getTagsMatchingIds(List<String> Ids);

  /**
   * Determines if a document exists in a Firestore collection.
   *
   * @param docId the document's Firebase ID.
   * @param collection the collection the document should be in; MUST BE A COLLECTION CONSTANT from
   *     DBUtils.java
   * @return false if the ID is not associated with any doc in the specified collection, true if it
   *     is.
   */
  public boolean isDocument(String docId, String collection);

  /**
   * Returns a User object from userId.
   *
   * @param userId the user's Firebase ID.
   * @return the User object parsed from the document associated with the ID.
   */
  public User getUser(String userId);

  /**
   * Adds a User to the db based on an input userId.
   *
   * @param userId the user's Firebase ID.
   * @return String userId.
   */
  public String addUser(String userId);

  /**
   * Delete a User from the db based on an input userId.
   *
   * @param userId the user's Firebase ID.
   */
  public void deleteUser(String userId);

  /**
   * Sets a property's value to "true" for a certain user document. Can be used to let a user add a
   * recipe to saved or created, or to let user follow a tag.
   *
   * @param userId the user's Firebase ID
   * @param objectId the ID of either a recipe if the intent is to save/create, or of a tag for tag
   *     following.
   * @param collection a KEY constant from User class indicating which mode -- save, create, or
   *     follow tag.
   */
  public void setUserProperty(String userId, String objectId, String collection, boolean val);

  /**
   * Deletes a property value for a certain user document. Can be used to let a user delete a recipe
   * from saved or created, or to let user unfollow a tag.
   *
   * @param userId the user's Firebase ID
   * @param objectId the ID of either a recipe if the intent is to unsave/create, or of a tag for
   *     tag unfollowing.
   * @param collection a KEY constant from User class indicating which mode -- save, create, or tag.
   */
  public void deleteUserProperty(String userId, String objectId, String collection);

  /**
   * Checks if a specified user has a given recipe as true in a given map field.
   *
   * @param userId the user's Firebase ID.
   * @param recipeId the recipe's Firebase ID.
   * @return true if it is in the map as true, false if in map as false, null if not in map or user does not exist
   */
  public Boolean inUserMap(String userId, String recipeId, String mapName);

  /**
   * Returns a list of all recipe metadata with any of the tags in the tag IDs list.
   *
   * @param tagIds tags' Firestore IDs
   * @param sortingMethod such as TOP or NEW
   * @return list of recipe metadata matching one or more of the tags in tagIds param
   */
  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod);

  /**
   * Returns a list of the recipe metadata with a certain creator.
   *
   * @param creatorId creator/user's Firestore ID
   * @param sortingMethod such as TOP or NEW
   * @return user's created recipe metadata, sorted
   */
  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod);

  /**
   * Returns a list of the recipe metadata associated with the IDs of recipes saved by a user.
   *
   * @param userId user's Firestore ID
   * @param sortingMethod such as TOP or NEW
   * @return user's saved recipe metadata, sorted
   */
  public List<RecipeMetadata> getRecipesSavedBy(String userId, SortingMethod sortingMethod);

  /**
   * Returns a list of the recipe metadata whose IDs match any one ID in a given list of IDs.
   *
   * @param Ids list of recipe Firestore IDs to be queried
   * @param sortingMethod such as TOP or NEW
   * @return list of RecipeMetadata whose IDs are in Ids param
   */
  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids, SortingMethod sortingMethod);

  /**
   * Recursively constructs a query on recipes matching any of the tags passed in eg.
   * DBUtils.recipes().whereEqualTo("tag_N-1", true).whereEqualTo("tag_N-2", true)...
   *
   * @param tagIds an iterable object holding tag Ids
   * @param iter iterator object
   * @return a query that includes all of the recipes which match a tag passed in
   */
  public Query recipesMatchingTags(Iterable<String> tagIds, Iterator<String> iter);

  /**
   * Adds a Comment object to a recipe's comment subcollection in Firestore.
   *
   * @param newComment the Comment object to be added.
   * @param recipeId the recipe's Firestore ID.
   */
  public String addComment(Comment newComment, String recipeId);

  /**
   * Deletes a single specified comment in a recipe's comment subcollection in Firestore.
   *
   * @param Id the comment's Firestore ID.
   * @param recipeId the recipe's Firestore ID.
   */
  public void deleteComment(String Id, String recipeId);

  /**
   * Deletes all the comments in a recipe's comment subcollection in Firestore.
   *
   * @param recipeId the recipe's Firestore ID.
   */
  public void deleteComments(String recipeId);

  /**
   * Edits the content field of a comment under a certain recipe in Firestore.
   *
   * @param Id the comment's Firebase ID.
   * @param recipeId the recipe's Firebase ID.
   * @param editedContent the new content to replace the old.
   */
  public void editCommentContent(String Id, String recipeId, String editedContent);

  /**
   * Gets all of a user's saved recipe IDs.
   *
   * @param userId the user's Firestore ID.
   * @return a list of the Firestore IDs of the user's saved recipes in no particular order.
   */
  public List<String> savedRecipeIds(String userId);
}
