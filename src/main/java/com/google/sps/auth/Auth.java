package com.google.sps.meltingpot.auth;

import com.google.sps.meltingpot.data.DBReferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public class Auth {
    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static FirebaseToken verifyIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken, true);
            return decodedToken;
        } catch (IllegalArgumentException e) {
            System.out.println(e.toString());
            return null;
        } catch (FirebaseAuthException e) {
            System.out.println(e.toString());
            return null;
        }
    }
}