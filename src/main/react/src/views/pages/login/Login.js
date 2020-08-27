import React from "react";
import { useHistory } from "react-router-dom";
import { CCard, CCardBody, CCol, CRow, CContainer } from "@coreui/react";
import app from "firebase/app";
import "firebase/auth";
import { createUser } from "../../../requests";

const Login = () => {
  const history = useHistory();

  let listener = app.auth().onAuthStateChanged(user => {
    listener(); // unsubscribe from this listener when called
    if (user) {
      let googleUserRegexp = /[A-z]+@google.com/;
      if (!googleUserRegexp.test(user.email)) {
        // if user is not in the google domain
        // delete account and send back to sign in page
        user.delete().then(() => {
          let provider = new app.auth.GoogleAuthProvider();
          provider.setCustomParameters({ prompt: "select_account" });
          app.auth().signInWithRedirect(provider);
        });
      } else if (user.metadata.creationTime === user.metadata.lastSignInTime) {
        // create user document if this is the user's first sign in
        user
          .getIdToken()
          .then(createUser)
          .then(() => history.push("/popular"));
      } else {
        history.push("/popular");
      }
    } else {
      let provider = new app.auth.GoogleAuthProvider();
      provider.setCustomParameters({ prompt: "select_account" });
      app.auth().signInWithRedirect(provider);
    }
  });

  return (
    <div className="c-app c-default-layout flex-row align-items-center">
      <CContainer>
        <CRow className="justify-content-center">
          <CCol>
            <CCard>
              <CCardBody>Loading...</CCardBody>
            </CCard>
          </CCol>
        </CRow>
      </CContainer>
    </div>
  );
};

export default Login;
