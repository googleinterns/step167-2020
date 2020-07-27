package com.google.sps.meltingpot.data;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.CollectionReference;

public class DBReferences {
	public static final Firestore db = FirestoreClient.getFirestore();
	public static final CollectionReference recipes = db.collection("recipes");
}