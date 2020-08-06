package com.google.sps.meltingpot.data;

public interface DBInterface {
  public String getRecipeContent(String Id);
  public RecipeMetadata getRecipeMetadata(String Id);
  public String addRecipe(RecipeMetadata newRecipe, String newContent);
  public void deleteRecipe(String Id);
  public void editRecipeTitleContent(String editedTitle, String editedContent);

  public Iterable<RecipeMetadata> getAllRecipes();
  public Iterable<Comment> getAllCommentsInRecipe(String recipeId);
  public Iterable<Tag> getAllTags();

  public User getUser(String userId);
  public String addUser();
  public void deleteUser(String userId);
  public void addRecipeIdToCreated(String userId, String recipeId);
  public void addRecipeIdToSaved(String userId, String recipeId);
  public void followTag(String userId, String tagId);

  public Iterable<RecipeMetadata> getRecipesMatchingTags(Iterable<String> tagIds);
  public Iterable<RecipeMetadata> getRecipesMatchingCreator(String creatorId);
  public Iterable<RecipeMetadata> getRecipesSavedBy(String userId);
  public Iterable<RecipeMetadata> getRecipesMatchingIDs(Iterable<String> Ids);

  public String addComment(Comment newComment, String recipeId);
  public void deleteComment(String Id, String recipeId);
  public void editCommentContent(String Id, String recipeId, String editedContent);
}
