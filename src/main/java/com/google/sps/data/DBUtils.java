package com.google.sps.meltingpot.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DBUtils {
  private static final Firestore database = FirestoreClient.getFirestore();
  private static final CollectionReference recipesReference = database.collection("recipes");
  private static final CollectionReference usersReference = database.collection("users");
  private static final CollectionReference tagsReference = database.collection("tags");

  //   private static final Firestore actualDatabase = FirestoreClient.getFirestore();
  //   private static final CollectionReference actualRecipesReference =
  //   actualDatabase.collection("recipes"); private static final CollectionReference
  //   actualUsersReference = actualDatabase.collection("users"); private static final
  //   CollectionReference actualTagsReference = actualDatabase.collection("tags");

  private static final String DB_COMMENTS = "comment-collection";

  //   private static Firestore database;
  //   private static CollectionReference recipesReference;
  //   private static CollectionReference usersReference;
  //   private static CollectionReference tagsReference;

  //   public static void testModeWithParams(Firestore db, CollectionReference recipesRef) {
  //     // Use this method to inject mock db and recipe reference when testing
  //     // SHOULD ONLY BE USED IN TESTS
  //     database = db;
  //     recipesReference = recipesRef;
  //   }

  //   public static void productionMode() {
  //     database = actualDatabase;
  //     recipesReference = actualRecipesReference;
  //     usersReference = actualUsersReference;
  //     tagsReference = actualTagsReference;
  //   }

  public static Firestore db() {
    return database;
  }

  public static CollectionReference users() {
    return usersReference;
  }

  public static DocumentReference user(String userID) {
    return usersReference.document(userID);
  }

  public static String getNestedPropertyName(String property, String nestedProperty) {
    return property + "." + nestedProperty;
  }

  public static CollectionReference recipes() {
    return recipesReference;
  }

  public static CollectionReference tags() {
    return tagsReference;
  }

  /** Return a list of all recipe IDs. */
  public static ArrayList<String> allRecipeIds() {
    ApiFuture<QuerySnapshot> querySnapshotFuture = recipesReference.get();
    ArrayList<String> recipeIdList = new ArrayList<>();
    QuerySnapshot querySnapshot = DBUtils.blockOnFuture(querySnapshotFuture);

    if (querySnapshot == null) {
      return null;
    }

    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
      recipeIdList.add(document.toObject(Recipe.class).id);
    }

    return recipeIdList;
  }

  public static CollectionReference comments(String recipeID) {
    return recipesReference.document(recipeID).collection(DB_COMMENTS);
  }

  public static DocumentReference comment(String recipeId, String commentId) {
    return recipesReference.document(recipeId).collection(DB_COMMENTS).document(commentId);
  }

  public static String commentsCollectionName() {
    return DB_COMMENTS;
  }

  public static <T> T blockOnFuture(ApiFuture<T> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }
}
