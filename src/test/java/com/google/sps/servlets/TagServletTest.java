package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.gson.Gson;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBInterface;
import com.google.sps.meltingpot.data.Tag;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.Arrays;

@RunWith(JUnit4.class)
public final class TagServletTest {
  private DBInterface db = mock(DBInterface.class);
  private final TagServlet tagServlet = new TagServlet(db);
  private Gson gson = new Gson();

  private final Tag Tag0 = new Tag("0", "salt", true, "ingredient");
  private final Tag Tag1 = new Tag("1", "pepper", true, "ingredient");
  private final Tag Tag2 = new Tag("2", "italian", false, "country");
  private final Tag Tag3 = new Tag("3", "argentinian", false, "country");

  @Test
  public void getAllWithHidden() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameterValues("tagIds")).thenReturn(null);
    when(request.getParameter("getHidden")).thenReturn("true");
    when(db.getAllTags(true)).thenReturn(Arrays.asList(Tag0,Tag1,Tag2,Tag3));

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    tagServlet.doGet(request, response);

    verify(db, times(1)).getAllTags(true);
    verify(db, never()).getTagsMatchingIds(anyList());
    Assert.assertEquals(gson.toJson(Arrays.asList(Tag0,Tag1,Tag2,Tag3)), sw.toString());
  }

  @Test
  public void getAllWithoutHidden() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameterValues("tagIds")).thenReturn(null);
    when(request.getParameter("getHidden")).thenReturn("false");
    when(db.getAllTags(false)).thenReturn(Arrays.asList(Tag2,Tag3));

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    tagServlet.doGet(request, response);

    verify(db, times(1)).getAllTags(false);
    verify(db, never()).getTagsMatchingIds(anyList());
    Assert.assertEquals(gson.toJson(Arrays.asList(Tag2,Tag3)), sw.toString());
  }

  @Test
  public void getIds() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameterValues("tagIds")).thenReturn(new String[]{"1","2"});
    when(request.getParameter("getHidden")).thenReturn(null);
    when(db.getTagsMatchingIds(anyList())).thenReturn(Arrays.asList(Tag1,Tag2));

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    tagServlet.doGet(request, response);

    verify(db, never()).getAllTags(false);
    verify(db, times(1)).getTagsMatchingIds(anyList());
    Assert.assertEquals(gson.toJson(Arrays.asList(Tag1,Tag2)), sw.toString());
  } 
}
