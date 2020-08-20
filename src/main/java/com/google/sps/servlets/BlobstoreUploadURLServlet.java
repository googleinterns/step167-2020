package com.google.sps.meltingpot.servlets;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the fetch() function requests the /blobstore-upload-url URL, the content of the response is
 * the URL that allows a user to upload a file to Blobstore.
 */
@WebServlet("/blobstore-upload")
public class BlobstoreUploadURLServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    // Here we get the Blobstore upload URL and forward POST requests to RecipeServlet.
    String uploadUrl = blobstoreService.createUploadUrl("/api/post");

    response.setContentType("text/html");
    response.getWriter().println(uploadUrl);
  }
}
