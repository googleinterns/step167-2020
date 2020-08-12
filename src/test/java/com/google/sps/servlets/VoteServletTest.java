package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.DBUtils;
import com.google.sps.meltingpot.data.RecipeMetadata;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.Tag;
import com.google.sps.meltingpot.data.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class VoteServletTest {
  private DBInterface db = mock(DBInterface.class);
  private final VoteServlet voteServlet = new VoteServlet(db);
  private Gson gson = new Gson();

  private static final int fakeRecipeVotes = 15;
  private static final String fakeRecipeId = "j93y48fhe3r";
  private static final String fakeUserId = "dij938d3of";
  private static final String fakeToken = "02jud849ax03e";

  @Test
  public void putMissingRecipe() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("recipeId")).thenReturn(null);
    when(request.getParameter("vote")).thenReturn("true");

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, never()).getWriter();
  }

  @Test
  public void putMissingVote() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn(null);

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, never()).getWriter();
  }

  @Test
  public void putRecipeDoesNotExist() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("true");
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(false);

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, never()).getWriter();
  }

  @Test
  public void putMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("true");
    when(request.getParameter("token")).thenReturn(null);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION)).
        thenReturn(true);
    when(mockFirebaseAuth.verifyIdToken(null, true))
        .thenThrow(new IllegalArgumentException());

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, never()).getWriter();
  }

  @Test
  public void putBadAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("true");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenThrow(new FirebaseAuthException("Invalid token", "Invalid token"));

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, never()).getWriter();
  }

  @Test
  public void putUpvotePreviouslyUpvoted() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken authToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("true");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(db.voteRecipe(fakeRecipeId, -1)).thenReturn((long)fakeRecipeVotes - 1);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenReturn(authToken);
    when(db.inUserMap(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY))
        .thenReturn(true);
    when(authToken.getUid()).thenReturn(fakeUserId);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(db).deleteUserProperty(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY);
    verify(db).voteRecipe(fakeRecipeId, -1);
    verify(response, never()).setStatus(anyInt());
    verify(response).getWriter();

    RecipeMetadata metadata = new RecipeMetadata();
    metadata.votes = fakeRecipeVotes - 1;
    Assert.assertEquals(gson.toJson(metadata), sw.toString());
  }

  @Test
  public void putDownvotePreviouslyUpvoted() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken authToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("false");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(db.voteRecipe(fakeRecipeId, -2)).thenReturn((long)fakeRecipeVotes - 2);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenReturn(authToken);
    when(db.inUserMap(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY))
        .thenReturn(true);
    when(authToken.getUid()).thenReturn(fakeUserId);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(db).setUserProperty(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY, false);
    verify(db).voteRecipe(fakeRecipeId, -2);
    verify(response, never()).setStatus(anyInt());
    verify(response).getWriter();

    RecipeMetadata metadata = new RecipeMetadata();
    metadata.votes = fakeRecipeVotes - 2;
    Assert.assertEquals(gson.toJson(metadata), sw.toString());
  }

  @Test
  public void putUpvotePreviouslyDownvoted() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken authToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("true");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(db.voteRecipe(fakeRecipeId, 2)).thenReturn((long)fakeRecipeVotes + 2);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenReturn(authToken);
    when(db.inUserMap(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY))
        .thenReturn(false);
    when(authToken.getUid()).thenReturn(fakeUserId);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(db).setUserProperty(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY, true);
    verify(db).voteRecipe(fakeRecipeId, 2);
    verify(response, never()).setStatus(anyInt());
    verify(response).getWriter();

    RecipeMetadata metadata = new RecipeMetadata();
    metadata.votes = fakeRecipeVotes + 2;
    Assert.assertEquals(gson.toJson(metadata), sw.toString());
  }

  @Test
  public void putDownvotePreviouslyDownvoted() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken authToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("false");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(db.voteRecipe(fakeRecipeId, 1)).thenReturn((long)fakeRecipeVotes + 1);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenReturn(authToken);
    when(db.inUserMap(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY))
        .thenReturn(false);
    when(authToken.getUid()).thenReturn(fakeUserId);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(db).deleteUserProperty(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY);
    verify(db).voteRecipe(fakeRecipeId, 1);
    verify(response, never()).setStatus(anyInt());
    verify(response).getWriter();

    RecipeMetadata metadata = new RecipeMetadata();
    metadata.votes = fakeRecipeVotes + 1;
    Assert.assertEquals(gson.toJson(metadata), sw.toString());
  }

  @Test
  public void putUpvotePreviouslyNeutral() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken authToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("true");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(db.voteRecipe(fakeRecipeId, 1)).thenReturn((long)fakeRecipeVotes + 1);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenReturn(authToken);
    when(db.inUserMap(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY))
        .thenReturn(null);
    when(authToken.getUid()).thenReturn(fakeUserId);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(db).setUserProperty(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY, true);
    verify(db).voteRecipe(fakeRecipeId, 1);
    verify(response, never()).setStatus(anyInt());
    verify(response).getWriter();

    RecipeMetadata metadata = new RecipeMetadata();
    metadata.votes = fakeRecipeVotes + 1;
    Assert.assertEquals(gson.toJson(metadata), sw.toString());
  }

  @Test
  public void putDownvotePreviouslyNeutral() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);
    FirebaseToken authToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeId")).thenReturn(fakeRecipeId);
    when(request.getParameter("vote")).thenReturn("false");
    when(request.getParameter("token")).thenReturn(fakeToken);
    when(db.isDocument(fakeRecipeId, DBUtils.DB_RECIPES_COLLECTION))
        .thenReturn(true);
    when(db.voteRecipe(fakeRecipeId, -1)).thenReturn((long)fakeRecipeVotes - 1);
    when(mockFirebaseAuth.verifyIdToken(fakeToken, true))
        .thenReturn(authToken);
    when(db.inUserMap(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY))
        .thenReturn(null);
    when(authToken.getUid()).thenReturn(fakeUserId);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(db).setUserProperty(fakeUserId, fakeRecipeId, User.VOTED_RECIPES_KEY, false);
    verify(db).voteRecipe(fakeRecipeId, -1);
    verify(response, never()).setStatus(anyInt());
    verify(response).getWriter();

    RecipeMetadata metadata = new RecipeMetadata();
    metadata.votes = fakeRecipeVotes - 1;
    Assert.assertEquals(gson.toJson(metadata), sw.toString());
  }
}
