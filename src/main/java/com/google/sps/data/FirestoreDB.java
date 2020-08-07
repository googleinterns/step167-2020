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
        Map<String, Boolean> tagIdsMap = (Map<String, Boolean>) recipeMetadataSnapshot.get(DBUtils.TAG_IDS_KEY);
        Query tagsQuery = DBUtils.tags().whereIn(DBObject.ID_KEY, new ArrayList<String>(tagIdsMap.keySet()));
        recipeMetadata.tags = DBUtils.blockOnFuture(tagsQuery.get()).toObjects(Tag.class);
        return recipeMetadata;
    }
    public String addRecipe(RecipeMetadata newRecipeMetadata, String newContent) {
        DocumentReference newContentRef = DBUtils.recipes().document();
        newRecipeMetadata.id = newContentRef.getId();
        List<Tag> tags = newRecipeMetadata.tags;
        newRecipeMetadata.tags = null;
        DocumentReference newRecipeMetadataRef = DBUtils.recipeMetadata(newRecipeMetadata.id);
        DBUtils.blockOnFuture(newContentRef.set(Collections.singletonMap(DBUtils.RECIPE_CONTENT_KEY, newContent)));
        DBUtils.blockOnFuture(newRecipeMetadataRef.set(newRecipeMetadata));
        Map<String, Object> tagIdsMap = new HashMap();
        tagIdsMap.put("tags", FieldValue.delete()); // delete the tags field in the db
        for(Tag tag : tags) tagIdsMap.put(DBUtils.getNestedPropertyName(DBUtils.TAG_IDS_KEY, tag.id), true);
        System.out.println(tagIdsMap);
        DBUtils.blockOnFuture(newRecipeMetadataRef.update(tagIdsMap));
        return newRecipeMetadata.id;
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