package com.google.sps.meltingpot.servlets;

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
import com.google.gson.Gson;
import com.google.sps.meltingpot.startup.StartupShutdown;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

@RunWith(JUnit4.class)
public final class RecipeServletTest {
  private final RecipeServlet recipeServlet = new RecipeServlet();

  private static final String resourcesPath = "target/test-classes/RecipeServlet/";

  @BeforeClass
  public static void setup() {
    if (!StartupShutdown.isFirebaseAppRunning) {
      StartupShutdown startup = new StartupShutdown();
      ServletContextEvent mockContextEvent = mock(ServletContextEvent.class);
      startup.contextInitialized(mockContextEvent);
    }
  }

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
  public void postMissingAuth() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    BufferedReader requestBodyReader =
        new BufferedReader(new FileReader(new File(resourcesPath + "postFullBody.json")));
    when(request.getReader()).thenReturn(requestBodyReader);

    recipeServlet.doPost(request, response);

    verify(response, never()).getWriter();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
