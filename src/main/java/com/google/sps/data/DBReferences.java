package com.google.sps.data;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class DBReferences {
  public static final Firestore db = FirestoreClient.getFirestore();
  public static final CollectionReference recipes = db.collection("recipes");
}
