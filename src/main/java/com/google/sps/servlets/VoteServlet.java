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

import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.Tag;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.sps.meltingpot.data.User;
import com.google.firebase.auth.FirebaseToken;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles recipe vote requests. */
@WebServlet("/api/vote")
public class VoteServlet extends HttpServlet {
  private Gson gson = new Gson();
  private DBInterface db;

  public VoteServlet(DBInterface db) {
    this.db = db;
  }

  public VoteServlet() {}

  @Override
  public void init() {
    db = new FirestoreDB();
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeId = request.getParameter("recipeId");
    String vote = request.getParameter("vote");
    String token = request.getParameter("token");

    if(recipeId == null || (!vote.equals("true") && !vote.equals("false"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    FirebaseToken authToken;
    if((authToken = Auth.verifyIdToken(token)) != null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    
    /*
    Expected Behavior: Go to position in table
    vote \  Current Position | Upvoted | Neutral | Downvoted
    --------------------------------------------------------------
    True (upvote request)    | Neutal  | Upvote  | Upvote
    False (downvote request) | Downvote| Downvote| Neutral
    */

    RecipeMetadata metadata = new RecipeMetadata();
    Boolean votedRecipe = db.inUserMap(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY);
    if(votedRecipe != null) {
      if(votedRecipe == true) { // previously upvoted
        if(vote.equals("true")) { //request wants to reset the upvote
          db.deleteUserProperty(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY);
          metadata.votes = db.voteRecipe(recipeId, -1);
        } else { // request wants to change upvote to downvote
          db.setUserProperty(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY, false);
          metadata.votes = db.voteRecipe(recipeId, -2);
        }
      } else { // previously downvoted 
        if(vote.equals("true")) { // request wants to change downvote to upvote
          db.setUserProperty(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY, true);
          metadata.votes = db.voteRecipe(recipeId, 2);
        } else { // request wants to reset the downvote
          db.deleteUserProperty(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY);
          metadata.votes = db.voteRecipe(recipeId, 1);
        }
      }
    } else {
      if(vote.equals("true")) { // request wants to upvote
        db.setUserProperty(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY, true);
        metadata.votes = db.voteRecipe(recipeId, 1);
      } else { // request wants to downvote
        db.setUserProperty(authToken.getUid(), recipeId, User.VOTED_RECIPES_KEY, false);
        metadata.votes = db.voteRecipe(recipeId, -1);
      }
    }

    response.setContentType("application/json");
    response.getWriter().print(gson.toJson(metadata));
  }
}
