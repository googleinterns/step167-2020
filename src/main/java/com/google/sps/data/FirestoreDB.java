package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    WriteBatch batch = DBUtils.database.batch();
    batch.delete(DBUtils.recipe(Id));
    batch.delete(DBUtils.recipeMetadata(Id));
    DBUtils.blockOnFuture(batch.commit());
  }

  public void editRecipeTitleContent(String Id, String editedTitle, String editedContent) {
    DocumentReference contentRef = DBUtils.recipe(Id);
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    WriteBatch batch = DBUtils.database.batch();
    batch.update(contentRef, Recipe.CONTENT_KEY, editedContent);
    batch.update(metadataRef, RecipeMetadata.TITLE_KEY, editedTitle);
    DBUtils.blockOnFuture(batch.commit());
  }

  public Long voteRecipe(String Id, int voteDiff) {
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    ApiFuture<Long> voteTransaction = DBUtils.database.runTransaction(transaction -> {
      long votes = DBUtils.blockOnFuture(metadataRef.get()).getLong(RecipeMetadata.VOTES_KEY);
      metadataRef.update(RecipeMetadata.VOTES_KEY, votes + voteDiff);
      return votes + voteDiff;
    });
    return DBUtils.blockOnFuture(voteTransaction);
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
    Query tagsQuery = DBUtils.tags().whereIn(DBObject.ID_KEY, Ids);
    return DBUtils.blockOnFuture(tagsQuery.get()).toObjects(Tag.class);
  }

  public boolean isDocument(String docId, String collection) {
    DocumentReference docRef = DBUtils.database.collection(collection).document(docId);
    DocumentSnapshot document = DBUtils.blockOnFuture(docRef.get());
    return document.exists();
  }

  public User getUser(String userId) {
    DocumentReference userRef = DBUtils.user(userId);
    DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
    return user.toObject(User.class);
  }

  public String addUser(String userId) {
    DocumentReference newUserRef = DBUtils.users().document(userId);
    User newUser = new User(userId);
    DBUtils.blockOnFuture(newUserRef.set(newUser)); // .set() returns an ApiFuture
    return userId;
  }

  public void deleteUser(String userId) {
    DBUtils.blockOnFuture(DBUtils.user(userId).delete());
  }

  public void makeUserPropertyTrue(String userId, String objectId, String collection) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    ApiFuture addUserPropertyFuture =
        userRef.update(Collections.singletonMap(nestedPropertyName, true));
    DBUtils.blockOnFuture(addUserPropertyFuture);
  }

  public void deleteUserProperty(String userId, String objectId, String collection) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    ApiFuture removeUserPropertyFuture = userRef.update(nestedPropertyName, FieldValue.delete());
    DBUtils.blockOnFuture(removeUserPropertyFuture);
  }

  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod) {
    Query recipesQuery = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipes().whereEqualTo(Recipe.CREATOR_ID_KEY, creatorId);
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

    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(recipesQuery.get());

    if (querySnapshot == null) {
      return null;
    }

    return querySnapshot.toObjects(RecipeMetadata.class);
  }

  public Query recipesMatchingTags(Iterable<String> tagIds, Iterator<String> iter) {
    if (iter.hasNext()) {
      String nextTag = iter.next();
      return recipesMatchingTags(tagIds, iter).whereEqualTo("tags." + nextTag, true);
    }
    return DBUtils.recipes();
  }

  public List<String> savedRecipeIds(String userId) {
    DocumentReference userRef = DBUtils.user(userId);
    DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
    Map<String, Boolean> savedRecipeIdsMap =
        (Map<String, Boolean>) user.get(User.SAVED_RECIPES_KEY);
    return new ArrayList<String>(savedRecipeIdsMap.keySet());
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
    for (DocumentReference comment : DBUtils.comments(recipeId).listDocuments()) {
      comment.delete();
    }
  }

  public void editCommentContent(String Id, String recipeId, String editedContent) {
    DBUtils.blockOnFuture(
        DBUtils.comments(recipeId).document(Id).update(Comment.CONTENT_KEY, editedContent));
  }

  public boolean isCreatedComment(String recipeId, String commentId, String userId) {
    DocumentSnapshot comment = DBUtils.blockOnFuture(DBUtils.comment(recipeId, commentId).get());

    String commentCreatorId = comment.getString(CREATOR_ID_KEY);
    return commentCreatorId.equals(userId);
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
