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