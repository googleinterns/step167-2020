package com.google.sps.meltingpot.data;

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
        return null;
    }
    public void editRecipeTitleContent(String editedTitle, String editedContent) {
        return null;
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