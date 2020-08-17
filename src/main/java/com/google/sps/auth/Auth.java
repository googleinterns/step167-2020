package com.google.sps.meltingpot.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.sps.meltingpot.data.DBUtils;
import javax.servlet.http.HttpServletResponse;

public class Auth {
  private static FirebaseAuth firebaseAuth;

  public static void testModeWithParams(FirebaseAuth mockFirebaseAuth) {
    // Use this method to inject mock authentication when testing
    // SHOULD ONLY BE USED IN TESTS
    firebaseAuth = mockFirebaseAuth;
  }

  public static void productionMode() {
    firebaseAuth = FirebaseAuth.getInstance();
  }

  /**
   * Checks the authentication of a given token and returns a unique UID.
   * If the token is not valid, returns null.
   */
  public static String getUid(String token, HttpServletResponse response) {
    FirebaseToken decodedToken = verifyIdToken(token);
    if (decodedToken == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }
    return decodedToken.getUid();
  }

  public static FirebaseToken verifyIdToken(String idToken) {
    try {
      FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken, true);
      return decodedToken;
    } catch (IllegalArgumentException | FirebaseAuthException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getUserEmail(String uid) {
    try {
      UserRecord usr = firebaseAuth.getUser(uid);
      return usr.getEmail();
    } catch (IllegalArgumentException | FirebaseAuthException e) {
      e.printStackTrace();
      return null;
    }
  }
}
