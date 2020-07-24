package com.google.sps.data;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class DatabaseReferences {
  public static final Firestore DB = FirestoreClient.getFirestore();
  // public static final CollectionReference ROOT = DB.collection();
  public static final CollectionReference POSTS = DB.collection("recipes");
}
