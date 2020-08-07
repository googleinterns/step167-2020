package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.User;
import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

  public List<RecipeMetadata> getAllRecipes() {
    return null;
  }
  public List<Comment> getAllCommentsInRecipe(String recipeId) {
    return null;
  }
  public List<Tag> getAllTags() {
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

  public List<RecipeMetadata> getRecipesMatchingTags(List<String> tagIds) {
    Query query = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(query);
  }

  public List<RecipeMetadata> getRecipesMatchingCreator(String creatorId) {
    Query query = DBUtils.recipes().whereEqualTo("creatorId", creatorId);
    return getRecipeMetadataQuery(query);
  }

  public List<RecipeMetadata> getRecipesSavedBy(String userId) {
    List<String> saved_Ids = savedRecipeIds(userId);
    return getRecipesMatchingIDs(saved_Ids);
  }

  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids) {
    ArrayList<String> idList = new ArrayList<>();
    Iterator<String> iter = Ids.iterator();
    while (iter.hasNext()) {
      idList.add(iter.next());
    }
    Query query = DBUtils.recipes().whereIn(Recipe.ID_KEY, idList);
    return getRecipeMetadataQuery(query);
  }

  private List<RecipeMetadata> getRecipeMetadataQuery(Query query) {
    ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(querySnapshotFuture);

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
