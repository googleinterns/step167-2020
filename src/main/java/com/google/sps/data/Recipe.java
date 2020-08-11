package com.google.sps.meltingpot.data;

public class Recipe {
  public static final String CONTENT_KEY = "content";

  public RecipeMetadata metadata;
  public String content;

  public Recipe(String content, RecipeMetadata metadata) {
    this.content = content;
    this.metadata = metadata;
  }
}
