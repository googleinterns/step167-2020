package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class DBReferences {
  private static final Firestore database = FirestoreClient.getFirestore();
  private static final CollectionReference recipesReference = database.collection("recipes");
  private static final String DB_COMMENTS = "comment-collection";

  public static Firestore db() {
    return database;
  }

  public static CollectionReference recipes() {
    return recipesReference;
  }

  public static CollectionReference comments(String recipeID) {
    return recipesReference.document(recipeID).collection(DB_COMMENTS);
  }
}
