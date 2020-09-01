package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;

public class Comment extends DBObject {
  public static final String CONTENT_KEY = "content";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String CREATOR_ID_KEY = "creatorId";
  public static final String VOTES_KEY = "votes";
  public static final String LDAP_KEY = "ldap";
  public static final String PARENT_ID_KEY = "parentId";

  public static final String DELETED = "[deleted]";

  public String content;
  public long timestamp;
  public String creatorId;
  public long votes;
  public String ldap;
  public String parentId;

  public Comment() {
    super();
  }

  public Comment(String id) {
    super(id);
  }

  public Comment(String content, String creatorId, String ldap) {
    this.content = content;
    this.timestamp = System.currentTimeMillis();
    this.creatorId = creatorId;
    this.votes = 0;
    this.ldap = ldap;
  }

  public static boolean createdbyUser(String recipeId, String commentId, String userId) {
    DocumentSnapshot comment = DBUtils.blockOnFuture(DBUtils.comment(recipeId, commentId).get());

    String commentCreatorId = comment.getString(CREATOR_ID_KEY);
    return commentCreatorId.equals(userId);
  }
}
