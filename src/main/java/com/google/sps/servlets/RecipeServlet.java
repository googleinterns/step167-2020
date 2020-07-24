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

package com.google.sps.servlets;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.sps.data.DatabaseReferences;
import com.google.sps.data.Post;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/post")
public class RecipeServlet extends HttpServlet {
  private Gson gson;

  @Override
  public void init() {
    gson = new Gson();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {}

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Post newPost = gson.fromJson(data, Post.class);
    if (newPost.title == null || newPost.content == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DatabaseReferences.POSTS.push().setValueAsync(newPost);
    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {}
}
