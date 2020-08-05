package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.sps.meltingpot.data.DBUtils;
import java.util.Date;

public class Comment {
  public static final String CONTENT_KEY = "content";
  public static final String DATE_KEY = "date";
  public static final String CREATOR_ID_KEY = "creatorId";

  public String content;
  public final Date date;
  public String id;
  public String creatorId;

  public Comment(String content, String id, String creatorId) {
    this.content = content;
    this.date = new Date();
    this.id = id;
    this.creatorId = creatorId;
  }

  // TODO: get user display name (or username) from db collection.
/*
  public static boolean createdbyUser(String recipeId, String commentId, String userId) {
    DocumentSnapshot comment = DBUtils.blockOnFuture(DBUtils.comment(recipeId, commentId).get());

    String commentCreatorId = comment.getString(CREATOR_ID_KEY);
    return commentCreatorId.equals(userId);
  }*/
}