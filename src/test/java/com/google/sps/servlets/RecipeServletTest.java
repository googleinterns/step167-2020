package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBObject;
import com.google.sps.meltingpot.data.Recipe;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.sps.meltingpot.data.User;
import com.google.sps.meltingpot.startup.StartupShutdown;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class RecipeServletTest {
  private final DBInterface mockDbInterface = mock(DBInterface.class);
  private final RecipeServlet recipeServlet = new RecipeServlet(mockDbInterface);

  private static final String resourcesPath = "target/test-classes/RecipeServlet/";

  /** POST TESTS */

  @Test
  public void postMissingTitle() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "postMissingTitle.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    recipeServlet.doPost(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void postMissingContent() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "postMissingContent.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    recipeServlet.doPost(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void postMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "postFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    Auth.testModeWithParams(mockFirebaseAuth);

    recipeServlet.doPost(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void postBadAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "postFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("token")).thenReturn("badToken");

    Auth.testModeWithParams(mockFirebaseAuth);

    recipeServlet.doPost(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void postAllGood() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);
    UserRecord mockUserRecord = mock(UserRecord.class);
    PrintWriter pw = mock(PrintWriter.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockUserRecord.getEmail()).thenReturn("johnnyappleseed@null.com");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);
    when(mockFirebaseAuth.getUser(anyString())).thenReturn(mockUserRecord);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "postFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("token")).thenReturn("goodToken");

    doNothing().when(pw).println(anyString());
    when(response.getWriter()).thenReturn(pw);

    when(mockDbInterface.addRecipe(anyObject())).thenReturn("RECIPE_ID");

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doPost(request, response);

    verify(response).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_CREATED);
    verify(pw).println(anyString());
  }

  /*            */

  /** GET TESTS */

  @Test
  public void getNoRecipeId() throws IOException {
    Gson gson = new Gson();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter pw = mock(PrintWriter.class);

    when(request.getParameter("recipeID")).thenReturn(null);
    when(request.getParameter("token")).thenReturn(null);
    when(request.getParameter("saved")).thenReturn("false");
    when(request.getParameter("tagIDs")).thenReturn(null);

    doNothing().when(pw).println(anyString());
    when(response.getWriter()).thenReturn(pw);

    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    recipeList.add(testMetadata);
    when(mockDbInterface.getAllRecipes(anyObject())).thenReturn(recipeList);
    String expected = gson.toJson(recipeList);

    recipeServlet.doGet(request, response);

    verify(response, never()).setStatus(HttpServletResponse.SC_NO_CONTENT);
    verify(response).getWriter();
    verify(pw).println(expected);
  }

  @Test
  public void getWithRecipeId() throws IOException {
    Gson gson = new Gson();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter pw = mock(PrintWriter.class);

    when(request.getParameter("recipeID")).thenReturn("RECIPE_ID");

    Recipe testRecipe = new Recipe();
    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");
    testRecipe.metadata = testMetadata;
    testRecipe.content = "RECIPE_CONTENT";

    when(mockDbInterface.getRecipeMetadata(anyString())).thenReturn(testRecipe.metadata);
    when(mockDbInterface.getRecipeContent(anyString())).thenReturn(testRecipe.content);

    doNothing().when(pw).println(anyString());
    when(response.getWriter()).thenReturn(pw);

    String expected = gson.toJson(testRecipe);

    recipeServlet.doGet(request, response);

    verify(response, never()).setStatus(HttpServletResponse.SC_NO_CONTENT);
    verify(response).getWriter();
    verify(pw).println(expected);
  }

  @Test
  public void getWithNullJson() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("recipeID")).thenReturn(null);
    when(mockDbInterface.getAllRecipes(anyObject())).thenReturn(null);

    recipeServlet.doGet(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /*            */

  /** PUT TESTS */

  @Test
  public void putMissingContent() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putMissingContent.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    recipeServlet.doPut(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void putMissingTitle() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putMissingTitle.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    recipeServlet.doPut(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void putMissingId() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putMissingId.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    recipeServlet.doPut(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void putMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    Auth.testModeWithParams(mockFirebaseAuth);

    recipeServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void putBadAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("token")).thenReturn("badToken");

    Auth.testModeWithParams(mockFirebaseAuth);

    recipeServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void putWrongUser() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("token")).thenReturn("goodToken");

    doNothing().when(mockDbInterface).editRecipeTitleContent(anyString(), anyString(), anyString());
    when(mockDbInterface.createdRecipe(anyString(), anyString())).thenReturn(false);

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  public void putAllGood() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "putFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("token")).thenReturn("goodToken");

    doNothing().when(mockDbInterface).editRecipeTitleContent(anyString(), anyString(), anyString());
    when(mockDbInterface.createdRecipe(anyString(), anyString())).thenReturn(true);

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doPut(request, response);

    verify(mockDbInterface).editRecipeTitleContent(anyString(), anyString(), anyString());
  }

  /*               */

  /** DELETE TESTS */

  @Test
  public void deleteMissingId() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("recipeID")).thenReturn(null);

    recipeServlet.doDelete(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void deleteMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    when(request.getParameter("recipeID")).thenReturn("RECIPE_ID");

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doDelete(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void deleteBadAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));
    when(request.getParameter("recipeID")).thenReturn("RECIPE_ID");

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doDelete(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void deleteWrongUser() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    when(request.getParameter("recipeID")).thenReturn("RECIPE_ID");
    when(request.getParameter("token")).thenReturn("goodToken");

    when(mockDbInterface.createdRecipe(anyString(), anyString())).thenReturn(false);

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doDelete(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  public void deleteAllGood() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    when(request.getParameter("recipeID")).thenReturn("RECIPE_ID");
    when(request.getParameter("token")).thenReturn("goodToken");

    when(mockDbInterface.createdRecipe(anyString(), anyString())).thenReturn(true);
    doNothing().when(mockDbInterface).deleteComments(anyString());
    doNothing().when(mockDbInterface).deleteRecipe(anyString());

    Auth.testModeWithParams(mockFirebaseAuth);
    recipeServlet.doDelete(request, response);

    verify(mockDbInterface).deleteComments(anyString());
    verify(mockDbInterface).deleteRecipe(anyString());
  }

  /*                         */

  /** GETDETAILEDRECIPE TEST */

  @Test
  public void getDetailedAllGood() throws IOException, FirebaseAuthException {
    Gson gson = new Gson();
    Recipe testRecipe = new Recipe();
    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");

    testRecipe.metadata = testMetadata;
    testRecipe.content = "RECIPE_CONTENT";

    when(mockDbInterface.getRecipeMetadata(anyString())).thenReturn(testRecipe.metadata);
    when(mockDbInterface.getRecipeContent(anyString())).thenReturn(testRecipe.content);

    String actual = recipeServlet.getDetailedRecipe("ID");
    String expected = gson.toJson(testRecipe);

    verify(mockDbInterface).getRecipeMetadata(anyString());
    verify(mockDbInterface).getRecipeContent(anyString());

    Assert.assertEquals(expected, actual);
  }

  /*                      */

  /** GETRECIPELIST TESTS */

  @Test
  public void getListSavedReqMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("true");

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getRecipeList(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    Assert.assertEquals(actual, null);
  }

  @Test
  public void getListSavedReqBadAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));
    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("true");

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getRecipeList(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    Assert.assertEquals(actual, null);
  }

  @Test
  public void getListCreatorQueryMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("false");
    when(request.getParameter("tagIDs")).thenReturn(null);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getRecipeList(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    Assert.assertEquals(actual, null);
  }

  @Test
  public void getListCreatorQueryBadAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));
    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("false");
    when(request.getParameter("tagIDs")).thenReturn(null);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getRecipeList(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    Assert.assertEquals(actual, null);
  }

  @Test
  public void getListSavedReqAllGood() throws IOException, FirebaseAuthException {
    Gson gson = new Gson();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("true");

    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    recipeList.add(testMetadata);
    when(mockDbInterface.getRecipesSavedBy(anyString(), anyObject())).thenReturn(recipeList);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getRecipeList(request, response);

    verify(mockDbInterface).getRecipesSavedBy(anyString(), anyObject());

    Assert.assertEquals(actual, gson.toJson(recipeList));
  }

  @Test
  public void getListCreatorQueryAllGood() throws IOException, FirebaseAuthException {
    Gson gson = new Gson();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("false");
    when(request.getParameterValues("tagIDs")).thenReturn(null);

    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    recipeList.add(testMetadata);
    when(mockDbInterface.getRecipesMatchingCreator(anyString(), anyObject()))
        .thenReturn(recipeList);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getRecipeList(request, response);

    verify(mockDbInterface).getRecipesMatchingCreator(anyString(), anyObject());

    Assert.assertEquals(actual, gson.toJson(recipeList));
  }

  @Test
  public void getListTagQueryAllGood() throws IOException, FirebaseAuthException {
    Gson gson = new Gson();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("token")).thenReturn(null);
    when(request.getParameter("saved")).thenReturn("false");
    when(request.getParameterValues("tagIDs")).thenReturn(new String[] {"TAG1", "TAG2"});

    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    recipeList.add(testMetadata);
    when(mockDbInterface.getRecipesMatchingTags(anyObject(), anyObject())).thenReturn(recipeList);

    String actual = recipeServlet.getRecipeList(request, response);

    verify(mockDbInterface).getRecipesMatchingTags(anyObject(), anyObject());

    Assert.assertEquals(actual, gson.toJson(recipeList));
  }

  @Test
  public void getListAllQueryAllGood() throws IOException, FirebaseAuthException {
    Gson gson = new Gson();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("token")).thenReturn("USER_TOKEN");
    when(request.getParameter("saved")).thenReturn("false");
    when(request.getParameterValues("tagIDs")).thenReturn(new String[] {"TAG1", "TAG2"});

    RecipeMetadata testMetadata = new RecipeMetadata("RECIPE_ID");
    ArrayList<RecipeMetadata> recipeList = new ArrayList<>();
    recipeList.add(testMetadata);
    when(mockDbInterface.getAllRecipes(anyObject())).thenReturn(recipeList);

    String actual = recipeServlet.getRecipeList(request, response);

    verify(mockDbInterface).getAllRecipes(anyObject());

    Assert.assertEquals(actual, gson.toJson(recipeList));
  }

  /*               */

  /** GETUID TESTS */

  @Test
  public void getUidMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getUid("MISSING_TOKEN", response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Assert.assertEquals(null, actual);
  }

  @Test
  public void getUidBadAuth() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getUid("BAD_TOKEN", response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Assert.assertEquals(null, actual);
  }

  @Test
  public void getUidAllGood() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.getUid("GOOD_TOKEN", response);

    Assert.assertEquals("USER_ID", actual);
  }

  /*                  */

  /** MATCHUSER TESTS */

  @Test
  public void matchUserMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.matchUser("MISSING_TOKEN", "RECIPE_ID", response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Assert.assertEquals(null, actual);
  }

  @Test
  public void matchUserBadAuth() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean()))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.matchUser("BAD_TOKEN", "RECIPE_ID", response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Assert.assertEquals(null, actual);
  }

  @Test
  public void matchUserWrongUser() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    when(mockDbInterface.createdRecipe(anyString(), anyString())).thenReturn(false);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.matchUser("WRONG_TOKEN", "RECIPE_ID", response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);

    Assert.assertEquals(null, actual);
  }

  @Test
  public void matchUserAllGood() throws IOException, FirebaseAuthException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);

    when(mockFirebaseToken.getUid()).thenReturn("USER_ID");
    when(mockFirebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenReturn(mockFirebaseToken);

    when(mockDbInterface.createdRecipe(anyString(), anyString())).thenReturn(true);

    Auth.testModeWithParams(mockFirebaseAuth);
    String actual = recipeServlet.matchUser("GOOD_TOKEN", "RECIPE_ID", response);

    Assert.assertEquals("USER_ID", actual);
  }

  /* */
}
