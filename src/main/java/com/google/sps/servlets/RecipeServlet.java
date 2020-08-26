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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBObject;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.FirestoreDB;
import com.google.sps.meltingpot.data.Recipe;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.sps.meltingpot.data.SortingMethod;
import com.google.sps.meltingpot.data.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles recipe post requests. */
@WebServlet("/api/post")
public class RecipeServlet extends HttpServlet {
  private Gson gson = new Gson();
  private Date date = new Date();
  private DBInterface db;

  private final Logger logger = Logger.getLogger(RecipeServlet.class.getName());

  public RecipeServlet() {}

  public RecipeServlet(DBInterface mockInterface) {
    db = mockInterface;
  }

  @Override
  public void init() {
    db = new FirestoreDB();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String recipeId = request.getParameter("recipeID");
    String json;

    if (recipeId == null) {
      json = getRecipeList(request, response);
    } else {
      json = getDetailedRecipe(recipeId);
    }

    if (json == null || json.equals(gson.toJson(null))) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String title = request.getParameter("title");
    String content = request.getParameter("content");
    String[] tags =
        request.getParameter("tagIds").replaceAll("\\[", "").replaceAll("\\]", "").split(",");

    BlobKey imageKey = getImageKey(request, "image");
    String imKey = "";
    if (imageKey != null) {
      // Create string from key to store.
      imKey = imageKey.getKeyString();
    }

    RecipeMetadata newMetadata = new RecipeMetadata();
    newMetadata.title = title;
    newMetadata.tagIds = new HashMap<String, Boolean>();
    newMetadata.blobKey = imKey;
    for (String tag : tags) {
      newMetadata.tagIds.put(tag, true);
    }
    Recipe newRecipe = new Recipe(content, newMetadata);

    if (newRecipe.content == null || newRecipe.metadata == null
        || newRecipe.metadata.title == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return;
    }

    newRecipe.metadata.creatorId = uid;
    newRecipe.metadata.votes = 0;
    newRecipe.metadata.timestamp = date.getTime();
    newRecipe.metadata.creatorLdap = Auth.getUserEmail(uid);
    String recipeId = db.addRecipe(newRecipe);

    db.setUserProperty(uid, recipeId, User.CREATED_RECIPES_KEY, true);

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(new DBObject(recipeId)));
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Recipe newRecipe = gson.fromJson(data, Recipe.class);
    if (newRecipe == null || newRecipe.metadata == null || newRecipe.content == null
        || newRecipe.content.isEmpty() || newRecipe.metadata.title == null
        || newRecipe.metadata.id == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = matchUser(token, newRecipe.metadata.id, response);
    if (uid == null) {
      return;
    }

    db.editRecipeTitleContent(newRecipe.metadata.id, newRecipe.metadata.title, newRecipe.content);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String recipeId = request.getParameter("recipeID");
    if (recipeId == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String token = request.getParameter("token");
    String uid = matchUser(token, recipeId, response);
    if (uid == null) {
      return;
    }

    db.deleteComments(recipeId);
    db.deleteUserProperty(uid, recipeId, User.CREATED_RECIPES_KEY);
    db.deleteRecipe(recipeId);
  }

  protected String getRecipeList(HttpServletRequest request, HttpServletResponse response) {
    String creatorToken = request.getParameter("token");

    String tagIDs[] = request.getParameterValues("tagIDs");
    boolean isSavedRequest = Boolean.parseBoolean(request.getParameter("saved"));

    boolean isTagQuery = (tagIDs != null && tagIDs.length > 0 && !tagIDs[0].equals("None"));
    boolean isCreatorQuery = (creatorToken != null && !creatorToken.equals("None"));

    if (isSavedRequest || (isCreatorQuery && !isTagQuery)) {
      // If frontend is requesting saved recipes or created recipes of a given user,
      // make sure they are authenticated
      String uid = Auth.getUid(creatorToken, response);
      if (uid == null) {
        return null;
      }

      // Then perform the corresponding query
      if (isSavedRequest) {
        return gson.toJson(db.getRecipesSavedBy(uid, SortingMethod.TOP));
      } else {
        return gson.toJson(db.getRecipesMatchingCreator(uid, SortingMethod.TOP));
      }
    } else if (isTagQuery && !isCreatorQuery) {
      // If the frontend is requesting recipes satisfying a certain set of tags,
      // then perform the query
      return gson.toJson(db.getRecipesMatchingTags(Arrays.asList(tagIDs), SortingMethod.TOP));
    } else { // Currently addresses cases where frontend is requesting both a tag query and
             // a creator query, or none of the above query types
      return gson.toJson(db.getAllRecipes(SortingMethod.TOP));
    }
  }

  protected String getDetailedRecipe(String recipeId) throws IOException {
    Recipe recipe = new Recipe(db.getRecipeContent(recipeId), db.getRecipeMetadata(recipeId));
    return gson.toJson(recipe);
  }

  /**
   * Checks if token valid, then if corresponding user created the given recipe. If either
   * fails, returns null and sets HttpServletResponse status accordingly
   */
  protected String matchUser(String token, String recipeId, HttpServletResponse response) {
    String uid = Auth.getUid(token, response);
    if (uid == null) {
      return null;
    }

    Boolean userCreatedRecipe = db.getUserProperty(uid, recipeId, User.CREATED_RECIPES_KEY);
    if (userCreatedRecipe == null || !userCreatedRecipe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return null;
    }

    return uid;
  }

  /** Return the key of the uploaded image (null if no upload or if not image). */
  private BlobKey getImageKey(HttpServletRequest request, String formElementID) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    // Blobstore maps form element ID to a list of keys of the blobs uploaded by the form.
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formElementID);

    // User submitted form without file selected, so no blobKey.
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Form only allowed for one Blob input, so get first blobkey.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form with no file, but an empty key was created--delete it.
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Check to make sure the file uploaded was an image.
    // If the first five letters of the type are not "image," then return null and delete Blob from
    // storage.
    String fileType = blobInfo.getContentType();
    // System.out.println(fileType);
    if (!fileType.substring(0, 5).equals("image")) {
      blobstoreService.delete(blobKey);
      System.err.println("File uploaded was not an image");
      return null;
    }

    // In previous versions, ImagesServices was used to get a URL pointing to the uploaded img.
    // Here, we simpy return the Blob's key so that it can later be served directly.
    return blobKey;
  }

  /**
   * Blobstore stores files as binary data; this func retrieves the data stored at imageKey.
   */
  private byte[] getBlobBytes(BlobKey imageKey) throws IOException {
    BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currByteIndex = 0;
    int bytesLength = 0;
    do {
      // Fetch a portion of the image bytes from Blobstore.
      byte[] temp = bs.fetchData(imageKey, currByteIndex, currByteIndex + fetchSize - 1);
      bytesLength = temp.length;
      outputBytes.write(temp);

      currByteIndex += fetchSize;
    } while (bytesLength
        >= fetchSize); // If fewer bytes than requested are read, then the end was reached.
    return outputBytes.toByteArray();
  }
}
