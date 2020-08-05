package com.google.sps.meltingpot.startup;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.sps.meltingpot.auth.Auth;
import com.google.sps.meltingpot.data.DBUtils;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartupShutdown implements ServletContextListener {
  public static boolean isFirebaseAppRunning = false;

  @Override
  public void contextInitialized(ServletContextEvent event) {
    System.out.println("Server starting up...");

    try {
      FirebaseOptions options = new FirebaseOptions.Builder()
                                    .setCredentials(GoogleCredentials.getApplicationDefault())
                                    .build();

      FirebaseApp.initializeApp(options);
      isFirebaseAppRunning = true;
      System.out.println("FirebaseApp initialized");

      DBUtils.productionMode();
      Auth.productionMode();

    } catch (IOException e) {
      System.out.println("IOException while initializing");
      e.printStackTrace();
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    System.out.println("Server shutting down...");
  }
}
