package com.google.sps.startup;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartupShutdown implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    System.out.println("Server starting up...");

    try {
      FirebaseOptions options = new FirebaseOptions.Builder()
                                    .setCredentials(GoogleCredentials.getApplicationDefault())
                                    .setDatabaseUrl("https://meltingpot-step-2020.firebaseio.com/")
                                    .build();

      FirebaseApp.initializeApp(options);

      System.out.println("FirebaseApp initialized");
    } catch (IOException e) {
      System.out.println("IOException while initializing");
      e.printStackTrace();
    }
  }
  public void contextDestroyed(ServletContextEvent event) {
    System.out.println("Server shutting down...");
  }
}