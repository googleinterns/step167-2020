package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.sps.meltingpot.data.DBUtils;

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

  public User getUser(String userId) {
    return null;
  }
  public String addUser() {
    return null;
  }
  public void deleteUser(String userId) {
    return;
  }
  public void addRecipeIdToCreated(String userId, String recipeId) {
    return;
  }
  public void addRecipeIdToSaved(String userId, String recipeId) {
    return;
  }
  public void followTag(String userId, String tagId) {
    return;
  }

  public Iterable<RecipeMetadata> getRecipesMatchingTags(Iterable<String> tagIds) {
    Query query = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(query);
  }

  public Iterable<RecipeMetadata> getRecipesMatchingCreator(String creatorId) {
    Query query = DBUtils.recipes().whereEqualTo("creatorId", creatorId);
    return getRecipeMetadataQuery(query);
  }

  public Iterable<RecipeMetadata> getRecipesSavedBy(String userId) {
    Iterable<String> saved_Ids = User.savedRecipeIds(userId);
    return getRecipesMatchingIDs(saved_Ids);
  }

  public Iterable<RecipeMetadata> getRecipesMatchingIDs(Iterable<String> Ids) {
    Query query = DBUtils.recipes().whereIn(Recipe.ID_KEY, Ids);
    return getRecipeMetadataQuery(query);
  }

  private Iterable<RecipeMetadata> getRecipeMetadataQuery(Query query) {
    ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(querySnapshotFuture);

    if (querySnapshot == null) {
      return null;
    }

    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
      RecipeMetadata recipe = new RecipeMetadata(document.get("id"));
      recipe.title = document.get("title");
      recipe.creatorId = document.get("creatorId");
      recipe.creatorLdap = document.get("creatorLdap");
      recipe.timestamp = document.get("timestamp");
      recipe.tags = document.get("tags");
      recipe.votes = document.get("votes");
      recipe.location = document.get("location");
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

  public String addComment(Comment newComment, String recipeId) {
    DocumentReference newCommentRef = DBUtils.comments(recipeId).document();
    ApiFuture addCommentFuture = newCommentRef.set(newComment);
    DBUtils.blockOnFuture(addCommentFuture);
    return newCommentRef.getId();
  }

  public void deleteComment(String Id, String recipeId) {
    ApiFuture<WriteResult> deleteCommentFuture = DBUtils.comments(recipeId).document(Id).delete();
    WriteResult writeResult = DBUtils.blockOnFuture(deleteCommentFuture);
  }

  public void editCommentContent(String Id, String recipeId, String editedContent) {
    DocumentReference commentRef = DBUtils.comments(recipeId).document(Id);
    ApiFuture editCommentFuture = commentRef.update("content", editedContent);
    DBUtils.blockOnFuture(editCommentFuture);
  }
}
