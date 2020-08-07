package com.google.sps.meltingpot.data;

import java.util.List;

public interface DBInterface {
  public String getRecipeContent(String Id);
  public RecipeMetadata getRecipeMetadata(String Id);
  public String addRecipe(RecipeMetadata newRecipe, String newContent);
  public void deleteRecipe(String Id);
  public void editRecipeTitleContent(String editedTitle, String editedContent);

  public List<RecipeMetadata> getAllRecipes();
  public List<Comment> getAllCommentsInRecipe(String recipeId);
  public List<Tag> getAllTags();

  public User getUser(String userId);
  public String addUser();
  public void deleteUser(String userId);
  public void addRecipeIdToCreated(String userId, String recipeId);
  public void addRecipeIdToSaved(String userId, String recipeId);
  public void followTag(String userId, String tagId);

  public List<RecipeMetadata> getRecipesMatchingTags(List<String> tagIds);
  public List<RecipeMetadata> getRecipesMatchingCreator(String creatorId);
  public List<RecipeMetadata> getRecipesSavedBy(String userId);
  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids);

  public String addComment(Comment newComment, String recipeId);
  public void deleteComment(String Id, String recipeId);
  public void editCommentContent(String Id, String recipeId, String editedContent);

  public List<String> savedRecipeIds(String userId);
  public boolean createdRecipe(String userId, String recipeId);
  public boolean isSavedRecipe(String userId, String recipeId);
}
