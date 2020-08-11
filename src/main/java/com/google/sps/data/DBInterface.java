package com.google.sps.meltingpot.data;

import java.util.List;

public interface DBInterface {
  /**
   * Returns the content of a recipe.
   * @param   id  the uid of the recipe
   * @return      the content of the recipe as a String
   */
  public String getRecipeContent(String Id);

  /**
   * Returns the metadata of a recipe.
   * @param   id  the uid of the recipe
   * @return      the metadata of the recipe as a RecipeMetadata Object
   */
  public RecipeMetadata getRecipeMetadata(String Id);

  /**
   * Adds a recipe (consisting of content and metadata) to the database.
   * This method always waits for the database operation to finish before returning.
   * @param   newRecipeMetadata  the metadata of the new recipe
   * @param   newContent         the content of the new recipe
   * @return                     the id of the new recipe in the database
   */
  public String addRecipe(RecipeMetadata newRecipeMetadata, String newContent);

  /**
   * Deletes a recipe (consisting of content and metadata) to the database.
   * This method always waits for the database operation to finish before returning.
   * @param   id  the uid of the recipe
   */
  public void deleteRecipe(String Id);

  /**
   * Edits the title (in the metadata) and the content of a recipe.
   * This method always waits for the database operation to finish before returning.
   * @param   Id              the uid of the recipe
   * @param   editedTitle     the new title of the recipe
   * @param   editedContent   the new content of the recipe
   */
  public void editRecipeTitleContent(String Id, String editedTitle, String editedContent);

  /**
   * Modifies the vote count of a recipe by adding voteDiff to it.
   * This method always waits for the database operation to finish before returning.
   * @param   Id           the uid of the recipe
   * @param   voteDiff     the votes added to the current vote count
   * @return               the new vote count of the recipe (null if transaction fails)
   */
  public Long voteRecipe(String Id, int voteDiff);

  /**
   * Gets a list of all recipe's metadata sorted in the given order.
   * @param   sortingMethod    the method of sorting the recipes
   * @return                   a List of RecipeMetadata in the database sorted by the given
   *     sortingMethod
   */
  public List<RecipeMetadata> getAllRecipes(SortingMethod sortingMethod);

  /**
   * Gets a list of all comments associated with a particular recipe,
   * sorted in the given order.
   * @param   recipeId         the uid of the recipe that the requested comments are associated with
   * @param   sortingMethod    the method of sorting the comments
   * @return                   a List of Comments in the database sorted by the given sortingMethod
   */
  public List<Comment> getAllCommentsInRecipe(String recipeId, SortingMethod sortingMethod);

  /**
   * Gets a list of all tags (with hidden tags if requested)
   * @param   getHidden    will return hidden tags if true
   * @return               a List of Tags in the database
   */
  public List<Tag> getAllTags(boolean getHidden);

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
  public void editCommentContent(String Id, String recipeId, String editedContent);

  public List<String> savedRecipeIds(String userId);
  public boolean createdRecipe(String userId, String recipeId);
  public boolean isSavedRecipe(String userId, String recipeId);
}
