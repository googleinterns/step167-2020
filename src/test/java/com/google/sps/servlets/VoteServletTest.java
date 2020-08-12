package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.Tag;
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

  private int recipeVote = 15;

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

    when(request.getParameter("recipeId")).thenReturn("0938ufh9e3");
    when(request.getParameter("vote")).thenReturn(null);

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response, never()).getWriter();
  }

  @Test
  public void putMissingAuth() throws IOException, FirebaseAuthException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

    when(request.getParameter("recipeId")).thenReturn("0938ufh9e3");
    when(request.getParameter("vote")).thenReturn("true");
    when(request.getParameter("token")).thenReturn(null);
    when(db.isDocument(anyString(), anyString())).thenReturn(true);
    when(mockFirebaseAuth.verifyIdToken(anyString(), eq(true)))
        .thenThrow(new IllegalArgumentException());

    Auth.testModeWithParams(mockFirebaseAuth);

    voteServlet.doPut(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, never()).getWriter();
  }
}
