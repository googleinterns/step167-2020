package com.google.sps.startup;

import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseApp;
import com.google.auth.oauth2.GoogleCredentials;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

@WebListener
public class StartupShutdown implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        System.out.println("Server starting up...");

        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .setDatabaseUrl("https://meltingpot-step-2020.firebaseio.com/")
            .build();

            System.out.println("FirebaseApp initialized");
        } catch (IOException e) {
            System.out.println("IOException while initializing");
        }
    }
    public void contextDestroyed(ServletContextEvent event)
    {
        System.out.println("Server shutting down...");
    }
}