package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Transaction;
import com.google.cloud.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FirestoreDB implements DBInterface {
  private static final int RECIPES_PER_PAGE = 12;
  private static final int MAX_RECIPES_PER_REQUEST = 150;

  public String getRecipeContent(String Id) {
    return DBUtils.blockOnFuture(DBUtils.recipe(Id).get()).getString(Recipe.CONTENT_KEY);
  }

  public RecipeMetadata getRecipeMetadata(String Id) {
    return DBUtils.blockOnFuture(DBUtils.recipeMetadata(Id).get()).toObject(RecipeMetadata.class);
  }

  public String addRecipe(Recipe newRecipe) {
    DocumentReference newContentRef = DBUtils.recipes().document();
    newRecipe.metadata.id = newContentRef.getId();
    DocumentReference newRecipeMetadataRef = DBUtils.recipeMetadata(newRecipe.metadata.id);
    DBUtils.blockOnFuture(
        newContentRef.set(Collections.singletonMap(Recipe.CONTENT_KEY, newRecipe.content)));
    DBUtils.blockOnFuture(newRecipeMetadataRef.set(newRecipe.metadata));
    return newRecipe.metadata.id;
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

  public long voteRecipe(String Id, int voteDiff, Transaction t) {
    DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
    long votes = DBUtils.blockOnFuture(t.get(metadataRef)).getLong(RecipeMetadata.VOTES_KEY);
    t.update(metadataRef, RecipeMetadata.VOTES_KEY, FieldValue.increment(voteDiff));
    return votes + voteDiff;
  }

  public List<RecipeMetadata> getAllRecipes(SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipeMetadata();
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  public List<RecipeMetadata> getRecipePage(SortingMethod sortingMethod, int page) {
    Query recipesQuery = DBUtils.recipeMetadata();
    return getRecipeMetadataQuery(recipesQuery, sortingMethod, page);
  }

  public List<Comment> getAllCommentsInRecipe(String recipeId) {
    Query commentsQuery = DBUtils.comments(recipeId);
    commentsQuery = commentsQuery.orderBy(Comment.LEVEL_KEY, Query.Direction.ASCENDING);
    // blockOnFuture() returns a QuerySnapshot
    return DBUtils.blockOnFuture(commentsQuery.get()).toObjects(Comment.class);
  }

  public List<Tag> getAllTags(boolean getHidden) {
    Query tagsQuery =
        getHidden ? DBUtils.tags() : DBUtils.tags().whereEqualTo(Tag.HIDDEN_KEY, false);
    return DBUtils.blockOnFuture(tagsQuery.get()).toObjects(Tag.class);
  }

  public List<Tag> getTagsMatchingIds(List<String> Ids) {
    if (Ids == null || Ids.isEmpty()) {
      return new ArrayList<Tag>();
    }
    Query tagsQuery = DBUtils.tags().whereIn(DBObject.ID_KEY, Ids);
    QuerySnapshot tags = DBUtils.blockOnFuture(tagsQuery.get());
    // Must include this if-statement to avoid a null-pointer exception when no tag IDs are given.
    if (tags == null) {
      return new ArrayList<Tag>();
    }
    return tags.toObjects(Tag.class);
  }

  public List<String> followedTagIds(String userId) {
    DocumentReference userRef = DBUtils.user(userId);
    DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
    Map<String, Boolean> followedTagIdsMap =
        (Map<String, Boolean>) user.get(User.TAGS_FOLLOWED_KEY);
    if (followedTagIdsMap != null) {
      return new ArrayList<String>(followedTagIdsMap.keySet());
    } else {
      return new ArrayList<String>();
    }
  }

  public boolean isDocument(String docId, String collection) {
    DocumentReference docRef = DBUtils.database.collection(collection).document(docId);
    DocumentSnapshot document = DBUtils.blockOnFuture(docRef.get());
    return document.exists();
  }

  public boolean isUser(String userId) {
    DocumentReference userRef = DBUtils.user(userId);
    DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
    return user.exists();
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

  public void setUserProperty(String userId, String objectId, String collection, boolean val) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    ApiFuture addUserPropertyFuture = userRef.update(nestedPropertyName, val);
    DBUtils.blockOnFuture(addUserPropertyFuture);
  }

  public void setUserProperty(
      String userId, String objectId, String collection, boolean val, Transaction t) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    t.update(userRef, nestedPropertyName, val);
  }

  public void deleteUserProperty(String userId, String objectId, String collection) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    ApiFuture removeUserPropertyFuture = userRef.update(nestedPropertyName, FieldValue.delete());
    DBUtils.blockOnFuture(removeUserPropertyFuture);
  }

  public void deleteUserProperty(String userId, String objectId, String collection, Transaction t) {
    DocumentReference userRef = DBUtils.user(userId);
    String nestedPropertyName = DBUtils.getNestedPropertyName(collection, objectId);
    t.update(userRef, nestedPropertyName, FieldValue.delete());
  }

  public Boolean getUserProperty(String userId, String recipeId, String mapName) {
    DocumentSnapshot user = DBUtils.blockOnFuture(DBUtils.user(userId).get());

    if (!user.exists()) {
      return null;
    }
    Boolean inMap = user.getBoolean(DBUtils.getNestedPropertyName(mapName, recipeId));
    return inMap;
  }

  public Boolean[] getUserProperty(String userId, String[] recipeIds, String mapName) {
    DocumentSnapshot user = DBUtils.blockOnFuture(DBUtils.user(userId).get());

    if (!user.exists()) {
      return null;
    }

    Boolean[] inMap = new Boolean[recipeIds.length];
    for (int i = 0; i < recipeIds.length; i++) {
      inMap[i] = user.getBoolean(DBUtils.getNestedPropertyName(mapName, recipeIds[i]));
    }
    return inMap;
  }

  public Boolean getUserProperty(String userId, String recipeId, String mapName, Transaction t) {
    DocumentSnapshot user = DBUtils.blockOnFuture(t.get(DBUtils.user(userId)));

    if (!user.exists()) {
      return null;
    }
    Boolean inMap = user.getBoolean(DBUtils.getNestedPropertyName(mapName, recipeId));
    return inMap;
  }

  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod, int page) {
    Query recipesQuery = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(recipesQuery, sortingMethod, page);
  }

  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod) {
    Query recipesQuery = recipesMatchingTags(tagIds, tagIds.iterator());
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod, int page) {
    Query recipesQuery = DBUtils.recipeMetadata().whereEqualTo(Recipe.CREATOR_ID_KEY, creatorId);
    return getRecipeMetadataQuery(recipesQuery, sortingMethod, page);
  }

  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipeMetadata().whereEqualTo(Recipe.CREATOR_ID_KEY, creatorId);
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesSavedBy(
      String userId, SortingMethod sortingMethod, int page) {
    List<String> saved_Ids = savedRecipeIds(userId);
    return getRecipesMatchingIDs(saved_Ids, sortingMethod, page);
  }

  public List<RecipeMetadata> getRecipesSavedBy(String userId, SortingMethod sortingMethod) {
    List<String> saved_Ids = savedRecipeIds(userId);
    return getRecipesMatchingIDs(saved_Ids, sortingMethod);
  }

  public List<RecipeMetadata> getRecipesMatchingIDs(
      List<String> Ids, SortingMethod sortingMethod, int page) {
    Query recipesQuery = DBUtils.recipeMetadata().whereIn(Recipe.ID_KEY, Ids);
    if (Ids.size() == 0) {
      return new ArrayList<RecipeMetadata>();
    }
    return getRecipeMetadataQuery(recipesQuery, sortingMethod, page);
  }

  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids, SortingMethod sortingMethod) {
    Query recipesQuery = DBUtils.recipeMetadata().whereIn(Recipe.ID_KEY, Ids);
    if (Ids.size() == 0) {
      return new ArrayList<RecipeMetadata>();
    }
    return getRecipeMetadataQuery(recipesQuery, sortingMethod);
  }

  private List<RecipeMetadata> getRecipeMetadataQuery(
      Query recipesQuery, SortingMethod sortingMethod) {
    switch (sortingMethod) { // Note: this does not currently work with tagID queries, requires
                             // custom index
      case TOP:
        recipesQuery = recipesQuery.orderBy(Recipe.VOTES_KEY, Query.Direction.DESCENDING);
        break;
      case NEW:
        recipesQuery = recipesQuery.orderBy(Recipe.TIMESTAMP_KEY, Query.Direction.DESCENDING);
        break;
    }

    recipesQuery = recipesQuery.limit(MAX_RECIPES_PER_REQUEST);

    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(recipesQuery.get());

    if (querySnapshot == null) {
      return null;
    }

    return querySnapshot.toObjects(RecipeMetadata.class);
  }

  private List<RecipeMetadata> getRecipeMetadataQuery(
      Query recipesQuery, SortingMethod sortingMethod, int page) {
    // overloaded method for getting recipes by page num

    switch (sortingMethod) { // Note: this does not currently work with tagID queries, requires
                             // custom index
      case TOP:
        recipesQuery = recipesQuery.orderBy(Recipe.VOTES_KEY, Query.Direction.DESCENDING);
        break;
      case NEW:
        recipesQuery = recipesQuery.orderBy(Recipe.TIMESTAMP_KEY, Query.Direction.DESCENDING);
        break;
    }

    recipesQuery = recipesQuery.offset(page * RECIPES_PER_PAGE).limit(RECIPES_PER_PAGE);

    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(recipesQuery.get());

    if (querySnapshot == null) {
      return null;
    }

    return querySnapshot.toObjects(RecipeMetadata.class);
  }

  public Query recipesMatchingTags(Iterable<String> tagIds, Iterator<String> iter) {
    if (iter.hasNext()) {
      String nextTag = iter.next();
      return recipesMatchingTags(tagIds, iter).whereEqualTo("tagIds." + nextTag, true);
    }
    return DBUtils.recipeMetadata();
  }

  public List<String> savedRecipeIds(String userId) {
    DocumentReference userRef = DBUtils.user(userId);
    DocumentSnapshot user = DBUtils.blockOnFuture(userRef.get());
    Map<String, Boolean> savedRecipeIdsMap =
        (Map<String, Boolean>) user.get(User.SAVED_RECIPES_KEY);
    if (savedRecipeIdsMap != null) {
      return new ArrayList<String>(savedRecipeIdsMap.keySet());
    } else {
      return new ArrayList<String>();
    }
  }

  public String addComment(Comment newComment, String recipeId) {
    DocumentReference newCommentRef = DBUtils.comments(recipeId).document();
    newComment.id = newCommentRef.getId();
    DBUtils.blockOnFuture(newCommentRef.set(newComment));
    return newCommentRef.getId();
  }

  public void deleteComment(String commentId, String recipeId, boolean isLeaf) {
    if (isLeaf) {
      // then we can delete it entirely from the db
      DBUtils.blockOnFuture(DBUtils.comment(recipeId, commentId).delete());
    } else {
      // we need to update creatorId, ldap, and content
      DBUtils.blockOnFuture(
          DBUtils.comment(recipeId, commentId).update(Comment.CREATOR_ID_KEY, Comment.DELETED));
      DBUtils.blockOnFuture(
          DBUtils.comment(recipeId, commentId).update(Comment.LDAP_KEY, Comment.DELETED));
      DBUtils.blockOnFuture(
          DBUtils.comment(recipeId, commentId).update(Comment.CONTENT_KEY, Comment.DELETED));
    }
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

    String commentCreatorId = comment.getString(Comment.CREATOR_ID_KEY);
    return commentCreatorId.equals(userId);
  }
}
