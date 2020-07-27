package com.google.meltingpot;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class DBReferences {
  private static final Firestore database = FirestoreClient.getFirestore();
  private static final CollectionReference recipesReference = database.collection("recipes");

  public static Firestore db() {
    return database;
  }

  public static CollectionReference recipes() {
    return recipesReference;
  }
}
