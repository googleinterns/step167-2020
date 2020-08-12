package com.google.sps.meltingpot.servlets;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.meltingpot.data.DBInterface;
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
    when(db.getAllTags(true)).thenReturn(Arrays.asList(Tag0, Tag1, Tag2, Tag3));

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    tagServlet.doGet(request, response);

    verify(db, times(1)).getAllTags(true);
    verify(db, never()).getTagsMatchingIds(anyList());
    Assert.assertEquals(gson.toJson(Arrays.asList(Tag0, Tag1, Tag2, Tag3)), sw.toString());
  }

  @Test
  public void getAllWithoutHidden() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameterValues("tagIds")).thenReturn(null);
    when(request.getParameter("getHidden")).thenReturn("false");
    when(db.getAllTags(false)).thenReturn(Arrays.asList(Tag2, Tag3));

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    tagServlet.doGet(request, response);

    verify(db, times(1)).getAllTags(false);
    verify(db, never()).getTagsMatchingIds(anyList());
    Assert.assertEquals(gson.toJson(Arrays.asList(Tag2, Tag3)), sw.toString());
  }

  @Test
  public void getIds() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameterValues("tagIds")).thenReturn(new String[] {"1", "2"});
    when(request.getParameter("getHidden")).thenReturn(null);
    when(db.getTagsMatchingIds(anyList())).thenReturn(Arrays.asList(Tag1, Tag2));

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);

    tagServlet.doGet(request, response);

    verify(db, never()).getAllTags(false);
    verify(db, times(1)).getTagsMatchingIds(anyList());
    Assert.assertEquals(gson.toJson(Arrays.asList(Tag1, Tag2)), sw.toString());
  }
}
