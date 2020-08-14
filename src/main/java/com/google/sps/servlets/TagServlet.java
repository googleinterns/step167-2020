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
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.Tag;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles tag requests. */
@WebServlet("/api/tag")
public class TagServlet extends HttpServlet {
  private Gson gson = new Gson();
  private DBInterface db;

  public TagServlet(DBInterface db) {
    this.db = db;
  }

  public TagServlet() {}

  @Override
  public void init() {
    db = new FirestoreDB();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String[] tagIds = request.getParameterValues("tagIds");
    String getHidden = request.getParameter("getHidden");

    List<Tag> tags;

    if (tagIds == null) {
      tags = db.getAllTags(getHidden != null && getHidden.equals("true"));
    } else {
      tags = db.getTagsMatchingIds(Arrays.asList(tagIds));
    }

    response.setContentType("application/json");
    response.getWriter().print(gson.toJson(tags));
  }
}
