// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.meltingpot.servlets;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.Comment;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBObject;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.SortingMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles comment requests. */
@WebServlet("/api/comment")
public class CommentServlet extends HttpServlet {
  private Gson gson = new Gson();
  private Date date = new Date();
  private DBInterface db;

  public CommentServlet(DBInterface mock) {
    this.db = mock;
  }

  public CommentServlet() {}

  @Override
  public void init() {
    db = new FirestoreDB();
  }

  /** As of 8.31.20, returns all comments */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    String json;

    if (recipeID == null) {
      System.err.println("No recipe ID was provided.");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } else {
      json = gson.toJson(db.getAllCommentsInRecipe(recipeID));
    }

    if (json == null || json.equals(gson.toJson(null))) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }

    response.setContentType("application/json");
    response.getWriter().print(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentData =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    String recipeID = request.getParameter("recipeID"); // Parent recipe of comment.

    String token = request.getParameter("token");

    Comment newComment = gson.fromJson(commentData, Comment.class);
    if (recipeID == null || newComment.content == null || newComment.content.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return;
    }

    newComment.creatorId = uid;
    newComment.votes = 0;
    newComment.timestamp = date.getTime();
    newComment.ldap = Auth.getUserEmail(uid);
    // Call FirestoreDB addComment method.
    String commentId = db.addComment(newComment, recipeID);
    response.getWriter().print(gson.toJson(new DBObject(commentId)));
    response.setStatus(HttpServletResponse.SC_CREATED);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    String commentID = request.getParameter("commentID");
    String commentBody = request.getParameter("commentBody");

    if (recipeID == null || commentID == null || commentBody == null || commentBody.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return;
    }

    if (!db.isCreatedComment(recipeID, commentID, uid)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // Call FirestoreDB editCommentContent() method.
    db.editCommentContent(commentID, recipeID, commentBody);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String recipeID = request.getParameter("recipeID");
    String commentID = request.getParameter("commentID");

    if (recipeID == null || commentID == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    db.deleteComment(commentID, recipeID);
  }
}
