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
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.sps.data.Recipe;
import com.google.sps.data.DBReferences;

/** Handles recipe post requests. */
@WebServlet("/api/post")
public class RecipeServlet extends HttpServlet
{

    private Gson gson = new Gson();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {}

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Recipe newRecipe = gson.fromJson(data, Recipe.class);
        if(newRecipe.content == null || newRecipe.title == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        DBReferences.recipes.document().set(newRecipe);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {}

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws IOException {}
}
