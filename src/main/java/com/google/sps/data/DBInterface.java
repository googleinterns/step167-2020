package com.google.sps.meltingpot.data;

public interface DBInterface {
  public String getRecipeContent(String Id);
  public RecipeMetadata getRecipeMetadata(String Id);
  public String addRecipe(RecipeMetadata newRecipe, String newContent);
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

  public Iterable<RecipeMetadata> getRecipesMatchingTags(Iterable<String> tagIds);
  public Iterable<RecipeMetadata> getRecipesMatchingCreator(String creatorId);
  public Iterable<RecipeMetadata> getRecipesMatchingIDs(Iterable<String> Ids);

  public Comment addComment(Comment newComment);
  public void deleteComment(String Id, String recipeId);
  public void editCommentContent(String editedContent);
}
