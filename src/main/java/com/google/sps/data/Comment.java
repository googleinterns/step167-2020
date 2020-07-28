package com.google.sps.meltingpot.data;

import java.util.Date;

public class Comment {
  public static final String CONTENT_KEY = "content";
  public static final String DATE_KEY = "date";

  public String content;
  public final Date date;

  public Comment(String content) {
    this.content = content;
    this.date = new Date();
  }

  // TODO: get user display name (or username) from db collection.
}
