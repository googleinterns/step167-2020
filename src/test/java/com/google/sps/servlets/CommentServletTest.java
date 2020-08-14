package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.Comment;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.SortingMethod;
import com.google.sps.meltingpot.data.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CommentServletTest {
  private DBInterface db;
  private CommentServlet commentServlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FirebaseAuth firebaseAuth;
  private UserRecord userRecord;
  private Gson gson = new Gson();

  private final Comment comment1 = new Comment("comment1", "creator1", "creator1@");
  private final Comment comment2 = new Comment("comment2", "creator2", "creator2@");
  private final Comment comment3 = new Comment("comment3", "creator3", "creator3@");
  private final List<Comment> exampleCommentList = Arrays.asList(comment1, comment2, comment3);

  private static final String resourcesPath = "target/test-classes/CommentServlet/";

  @Before
  public void setUp() {
    db = mock(DBInterface.class);
    commentServlet = new CommentServlet(db);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    firebaseAuth = mock(FirebaseAuth.class);
    userRecord = mock(UserRecord.class);
    // Inject mock Firebase Auth object into Auth class.
    Auth.testModeWithParams(firebaseAuth);
  }

  /**
   * A request has been made for comments, but no recipe ID was included.
   * Nothing should be written to the servlet's response.
   */
  @Test
  public void getRecipeIDNull() throws IOException {
    when(request.getParameter("recipeID")).thenReturn(null);

    commentServlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, never()).getWriter();
    verify(response, never()).setContentType(anyString());
  }

  /**
   * A request has been made for comments, but there were none associated with the recipe specified.
   * Nothing should be written to the servlet's response.
   */
  @Test
  public void getNullCommentsJSON() throws IOException {
    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(db.getAllCommentsInRecipe(anyString(), any(SortingMethod.class))).thenReturn(null);

    commentServlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    verify(response, never()).getWriter();
    verify(response, never()).setContentType(anyString());
  }

  /**
   * A successful request for comments has been made.
   * The servlet should respond with a list of the comments associated with the recipe.
   */
  @Test
  public void getIsSuccessful() throws IOException {
    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(db.getAllCommentsInRecipe(anyString(), any(SortingMethod.class)))
        .thenReturn(exampleCommentList);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    commentServlet.doGet(request, response);

    verify(response, times(1)).setContentType("application/json");
    Assert.assertEquals(gson.toJson(exampleCommentList), sw.toString());
  }

  /**
   * A comment post request was made, but the content field was null.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void postNullContent() throws IOException {
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "nullContentComment.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("recipeID")).thenReturn("recipeID");

    commentServlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).addComment(any(Comment.class), anyString());
  }

  /**
   * A comment post request was made, but the content field was empty.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void postEmptyContent() throws IOException {
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "emptyContentComment.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("recipeID")).thenReturn("recipeID");

    commentServlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).addComment(any(Comment.class), anyString());
  }

  /**
   * A comment post request was made, but the recipe ID was null.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void postNullRecipeID() throws IOException {
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "validComment.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("recipeID")).thenReturn(null);

    commentServlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).addComment(any(Comment.class), anyString());
  }

  /**
   * A comment post request was made, and the comment was successfully posted to Firebase.
   * The servlet should return a creation confirmation.
   */
  @Test
  public void postIsSuccessful() throws IOException, FirebaseAuthException {
    FirebaseToken firebaseToken = mock(FirebaseToken.class);

    // Stub such that the user id from the client-side token is "userID."
    when(firebaseAuth.verifyIdToken(anyString(), eq(true))).thenReturn(firebaseToken);
    when(firebaseToken.getUid()).thenReturn("userID");

    // Post a validly formatted comment with a valid query string.
    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "validComment.json")));
    when(request.getReader()).thenReturn(requestBodyReader);
    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("token")).thenReturn("validTokenEncoded");
    
    when(firebaseAuth.getUser(anyString())).thenReturn(userRecord);
    when(userRecord.getEmail()).thenReturn("johnnyappleseed@null.com");

    commentServlet.doPost(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    verify(db, times(1)).addComment(any(Comment.class), eq("recipeID"));
  }

  /**
   * A comment put request was made, but the recipe ID was null.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void putNullRecipeID() throws IOException {
    when(request.getParameter("recipeID")).thenReturn(null);
    when(request.getParameter("commentID")).thenReturn("commentID");
    when(request.getParameter("commentBody")).thenReturn("commentBody");

    commentServlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).isCreatedComment(anyString(), anyString(), anyString());
    verify(db, never()).editCommentContent(anyString(), anyString(), anyString());
  }
  
  /**
   * A comment put request was made, but the comment ID was null.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void putNullCommentID() throws IOException {
    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("commentID")).thenReturn(null);
    when(request.getParameter("commentBody")).thenReturn("commentBody");

    commentServlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).isCreatedComment(anyString(), anyString(), anyString());
    verify(db, never()).editCommentContent(anyString(), anyString(), anyString());
  }
  
  /**
   * A comment put request was made, but the comment body was null.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void putNullCommentBody() throws IOException {
    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("commentID")).thenReturn("commentID");
    when(request.getParameter("commentBody")).thenReturn(null);

    commentServlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).isCreatedComment(anyString(), anyString(), anyString());
    verify(db, never()).editCommentContent(anyString(), anyString(), anyString());
  }
  
  /**
   * A comment put request was made, but the comment body was empty.
   * The servlet should return a "bad request" error.
   */
  @Test
  public void putEmptyCommentBody() throws IOException {
    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("commentID")).thenReturn("commentID");
    when(request.getParameter("commentBody")).thenReturn("");

    commentServlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(db, never()).isCreatedComment(anyString(), anyString(), anyString());
    verify(db, never()).editCommentContent(anyString(), anyString(), anyString());
  }
  
  /**
   * A comment put request was made, but the requester was unauthorized.
   * The servlet should return a "forbidden" error.
   */
  @Test 
  public void putUnauthorized() throws IOException, FirebaseAuthException {
    FirebaseToken firebaseToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("commentID")).thenReturn("commentID");
    when(request.getParameter("commentBody")).thenReturn("commentBody");
    when(request.getParameter("token")).thenReturn("validToken");

    // Stub such that the user id from the client-side token is "userID."
    when(firebaseAuth.verifyIdToken(anyString(), eq(true))).thenReturn(firebaseToken);
    when(firebaseToken.getUid()).thenReturn("userID");

    // Stub such that user "userID" was not the comment's original poster.
    when(db.isCreatedComment("recipeID", "commentID", "userID")).thenReturn(false);

    commentServlet.doPut(request, response);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(db, never()).editCommentContent(anyString(), anyString(), anyString());
  }
  
  /**
   * A comment put request was made, and the comment was successfully edited in Firebase.
   */
  @Test 
  public void putIsSuccessFul() throws IOException, FirebaseAuthException {
    FirebaseToken firebaseToken = mock(FirebaseToken.class);

    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("commentID")).thenReturn("commentID");
    when(request.getParameter("commentBody")).thenReturn("commentBody");
    when(request.getParameter("token")).thenReturn("validToken");

    // Stub such that the user id from the client-side token is "userID."
    when(firebaseAuth.verifyIdToken(anyString(), eq(true))).thenReturn(firebaseToken);
    when(firebaseToken.getUid()).thenReturn("userID");

    // Stub such that user "userID" was not the comment's original poster.
    when(db.isCreatedComment(anyString(), anyString(), anyString())).thenReturn(true);

    commentServlet.doPut(request, response);

    verify(response, never()).setStatus(anyInt());
    verify(db, times(1)).editCommentContent("commentID", "recipeID", "commentBody");
  }
}
