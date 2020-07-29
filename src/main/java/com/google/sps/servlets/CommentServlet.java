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
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.sps.meltingpot.data.Comment;
import com.google.sps.meltingpot.data.DBReferences;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles comment requests. */
@WebServlet("/api/comment")
public class CommentServlet extends HttpServlet {
  private Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeID = request.getParameter("recipeID");
    String json;

    if (recipeID == null) {
      System.err.println("No recipe ID was provided.");
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    } else {
      json = getComments(recipeID, response);
    }

    if (json == null) {
      // Error message is printed by getComments().
      return;
    }

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /**
   * Gets all the comments associated with a certain recipe, based on recipe ID.
   * For now, returns all the comments flatly. (prototype)
   */
  private String getComments(String recipeID, HttpServletResponse response) {
    Query query = DBReferences.comments(recipeID);
    ApiFuture<QuerySnapshot> querySnapshot = query.get();
    ArrayList<Object> commentsList = new ArrayList<>();

    try {
      for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
        commentsList.add(document.getData());
      }
      return gson.toJson(commentsList);
    } catch (InterruptedException e) {
      System.out.println("Attempt to query recipes raised exception: " + e);
    } catch (ExecutionException e) {
      System.out.println("Attempt to query recipes raised exception: " + e);
    }

    return null;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentData =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    String recipeID = request.getParameter("recipeID"); // Parent recipe of comment.

    Comment newComment = gson.fromJson(commentData, Comment.class);
    if (newComment.content == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    DBReferences.comments(recipeID).document().set(newComment);
    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String recipeID = request.getParameter("recipeID");
    String commentID = request.getParameter("commentID");
    String json;

    if (recipeID == null || commentID == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    ApiFuture<WriteResult> writeResult = DBReferences.comments(recipeID).document(commentID).delete();
  }