package com.google.sps.meltingpot.data;

public interface DBInterface {

    String getRecipeContent(String Id)
    RecipeMetadata getRecipeMetadata(String Id)
    String addRecipe(RecipeMetadata newRecipe, String newContent)
    void deleteRecipe(String Id)
    void editRecipeTitleContent(String editedTitle, String editedContent)

    Iterable<RecipeMetadata> getAllRecipes()
    Iterable<Comment> getAllCommentsInRecipe(recipeId)
    Iterable<Tag> getAllTags()

    User getUser(String userId)
    String addUser()
    void deleteUser(String userId)
    void addRecipeIdToCreated(String userId, String recipeId)
    void addRecipeIdToSaved(String userId, String recipeId)
    void followTag(String userId, String tagId)

    Iterable<RecipeMetadata> getRecipesMatchingTags(Iterable<String> tagIds)
    Iterable<RecipeMetadata> getRecipesMatchingCreator(creatorId)
    Iterable<RecipeMetadata> getRecipesMatchingIDs(Iterable<String> Ids)

    Comment addComment(Comment newComment)
    void deleteComment(String Id, recipeId)
    void editCommentContent(String editedContent)
}