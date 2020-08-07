package com.google.sps.meltingpot.data;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.ExecutionException;

public class FirestoreDB implements DBInterface {

    public String getRecipeContent(String Id) {
        DocumentReference recipeRef = DBUtils.recipe(Id);
        DocumentSnapshot recipe = DBUtils.blockOnFuture(recipeRef.get());
        return recipe.getString(DBUtils.RECIPE_CONTENT_KEY);
    }

    public RecipeMetadata getRecipeMetadata(String Id) {
        DocumentReference recipeRef = DBUtils.recipeMetadata(Id);
        DocumentSnapshot recipeMetadataSnapshot = DBUtils.blockOnFuture(recipeRef.get());
        RecipeMetadata recipeMetadata = recipeMetadataSnapshot.toObject(RecipeMetadata.class);
        return recipeMetadata;
    }

    public String addRecipe(RecipeMetadata newRecipeMetadata, String newContent) {
        DocumentReference newContentRef = DBUtils.recipes().document();
        newRecipeMetadata.id = newContentRef.getId();
        DocumentReference newRecipeMetadataRef = DBUtils.recipeMetadata(newRecipeMetadata.id);
        DBUtils.blockOnFuture(newContentRef.set(Collections.singletonMap(DBUtils.RECIPE_CONTENT_KEY, newContent)));
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
        DBUtils.blockOnFuture(contentRef.update(DBUtils.RECIPE_CONTENT_KEY, editedContent));
        DBUtils.blockOnFuture(metadataRef.update(RecipeMetadata.TITLE_KEY, editedTitle));
    }

    public long voteRecipe(String Id, int voteDiff) {
        DocumentReference metadataRef = DBUtils.recipeMetadata(Id);
        long votes = DBUtils.blockOnFuture(metadataRef.get()).getLong(RecipeMetadata.VOTES_KEY);
        metadataRef.update(RecipeMetadata.VOTES_KEY, votes + voteDiff);
        return votes + voteDiff;
    }

    public List<RecipeMetadata> getAllRecipes(SortingMethod sortingMethod) {
        Query recipesQuery = DBUtils.recipes();
        switch(sortingMethod) {
            case TOP:
                recipesQuery.orderBy(RecipeMetadata.VOTES_KEY, Query.Direction.DESCENDING);
                break;
            case NEW:
                recipesQuery.orderBy(RecipeMetadata.TIMESTAMP_KEY, Query.Direction.DESCENDING);
                break;
        }
        return DBUtils.blockOnFuture(recipesQuery.get()).toObjects(RecipeMetadata.class);
    }

    public List<Comment> getAllCommentsInRecipe(String recipeId, SortingMethod sortingMethod) {
        Query commentsQuery = DBUtils.recipes();
        switch(sortingMethod) {
            case TOP:
                commentsQuery.orderBy(RecipeMetadata.VOTES_KEY, Query.Direction.DESCENDING);
                break;
            case NEW:
                commentsQuery.orderBy(RecipeMetadata.TIMESTAMP_KEY, Query.Direction.DESCENDING);
                break;
        }
        return DBUtils.blockOnFuture(commentsQuery.get()).toObjects(Comment.class);
    }

    public List<Tag> getAllTags(boolean getHidden) {
        Query tagsQuery = getHidden ? DBUtils.tags() : DBUtils.tags().whereEqualTo(Tag.HIDDEN_KEY, false);
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