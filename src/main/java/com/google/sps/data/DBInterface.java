package com.google.sps.meltingpot.data;

import java.util.List;

public interface DBInterface {
  public String getRecipeContent(String Id);
  public RecipeMetadata getRecipeMetadata(String Id);
  public String addRecipe(Recipe newRecipe);
  public void deleteRecipe(String Id);

  public void editRecipeTitleContent(String Id, String editedTitle, String editedContent);
  public long voteRecipe(String Id, int voteDiff);

  public Iterable<RecipeMetadata> getAllRecipes(SortingMethod sortingMethod);
  public Iterable<Comment> getAllCommentsInRecipe(String recipeId, SortingMethod sortingMethod);
  public Iterable<Tag> getAllTags(boolean getHidden);

  public User getUser(String userId);
  public String addUser(String userId);
  public void deleteUser(String userId);
  public void makeUserPropertyTrue(String userId, String objectId, String collection);
  public void deleteUserProperty(String userId, String objectId, String collection);

  public List<RecipeMetadata> getRecipesMatchingTags(
      List<String> tagIds, SortingMethod sortingMethod);
  public List<RecipeMetadata> getRecipesMatchingCreator(
      String creatorId, SortingMethod sortingMethod);
  public List<RecipeMetadata> getRecipesSavedBy(String userId, SortingMethod sortingMethod);
  public List<RecipeMetadata> getRecipesMatchingIDs(List<String> Ids, SortingMethod sortingMethod);

  public String addComment(Comment newComment, String recipeId);
  public void deleteComment(String Id, String recipeId);
  public void deleteComments(String recipeId);
  public void editCommentContent(String Id, String recipeId, String editedContent);

  public List<String> savedRecipeIds(String userId);
  public boolean createdRecipe(String userId, String recipeId);
  public boolean isSavedRecipe(String userId, String recipeId);
}
