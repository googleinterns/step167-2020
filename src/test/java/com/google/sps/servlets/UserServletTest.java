package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class UserServletTest {
  private DBInterface db;
  private UserServlet userServlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FirebaseAuth firebaseAuth;

  @Before 
  public void setUp(){
    db = mock(DBInterface.class);
    userServlet = new UserServlet(db);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    firebaseAuth = mock(FirebaseAuth.class);
  }
  
  /** 
   * An unauthorized request to add a user to Firestore has been made. 
   * No user should be added.
   */
  @Test
  public void postUnauthorized() throws IOException, FirebaseAuthException {
    when(firebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    
    // Inject mock authentication obj.
    Auth.testModeWithParams(firebaseAuth);

    userServlet.doPost(request, response);
    
    verify(db, never()).addUser(anyString());
    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
  
  /** An authorized request was made to add a user to Firestore; the user should be added. */
  @Test
  public void postCreated() throws IOException, FirebaseAuthException {
    FirebaseToken firebaseToken = mock(FirebaseToken.class);

    when(request.getParameter("token")).thenReturn("validTokenEncoded");
    
    when(firebaseToken.getUid()).thenReturn("userID");
    when(firebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenReturn(firebaseToken);
    
    // Inject mock authentication obj.
    Auth.testModeWithParams(firebaseAuth);

    userServlet.doPost(request, response);

    verify(db, times(1)).addUser("userID");
    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_CREATED);
  }
 
  /** 
   * An unauthorized request to edit a user in Firestore has been made. 
   * The user should not be changed.
   */
  @Test
  public void putUnauthorized() throws IOException, FirebaseAuthException {

    when(firebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());
    
    // Inject mock authentication obj.
    Auth.testModeWithParams(firebaseAuth);

    userServlet.doPut(request, response);
    
    verify(db, never()).makeUserPropertyTrue(anyString(), anyString(), anyString());
    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
  
  /** 
   * An authorized request was made for a user to save a recipe, but no recipe ID was given.
   * The recipe should not be saved.
   */
  @Test
  public void putSaveRequestRecipeIdNull() throws IOException, FirebaseAuthException {
    FirebaseToken firebaseToken = mock(FirebaseToken.class); 

    when(request.getParameter("recipeID")).thenReturn(null);
    when(request.getParameter("token")).thenReturn("validToken");
    when(request.getParameter("saved")).thenReturn("true");

    when(firebaseToken.getUid()).thenReturn("userID");
    when(firebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenReturn(firebaseToken);
    
    // Inject mock authentication obj.
    Auth.testModeWithParams(firebaseAuth);  

    userServlet.doPut(request, response);

    verify(db, never()).makeUserPropertyTrue(anyString(), anyString(), anyString());
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }
  
  /** 
   * An authorized request was made for a user to save a recipe.
   * The recipe should be saved under the user.
   */
  @Test
  public void putSaveRequestSuccess() throws IOException, FirebaseAuthException {
    FirebaseToken firebaseToken = mock(FirebaseToken.class); 

    when(request.getParameter("recipeID")).thenReturn("recipeID");
    when(request.getParameter("token")).thenReturn("validToken");
    when(request.getParameter("saved")).thenReturn("true");

    when(firebaseToken.getUid()).thenReturn("userID");
    when(firebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenReturn(firebaseToken);
    
    // Inject mock authentication obj.
    Auth.testModeWithParams(firebaseAuth);  

    userServlet.doPut(request, response);

    verify(db, times(1)).makeUserPropertyTrue("userID", "recipeID", User.SAVED_RECIPES_KEY);
    verify(response).setStatus(HttpServletResponse.SC_OK);
  }
}
