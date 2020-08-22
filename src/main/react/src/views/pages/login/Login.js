import React from "react";
import { useHistory } from "react-router-dom";
import {
  CCard,
  CCardBody,
  CCardGroup,
  CCol,
  CRow,
  CContainer
} from "@coreui/react";
import app from "firebase/app";
import "firebase/auth";
import { createUser } from "../../../requests";

const Login = () => {
  const history = useHistory();

  let listener = app.auth().onAuthStateChanged(user => {
    listener(); // unsubscribe from this listener when called
    if (user) {
      console.log(user.metadata.creationTime);
      console.log(user.metadata.lastSignInTime);
      if (user.metadata.creationTime === user.metadata.lastSignInTime) {
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
                <CCardBody>
                  Loading...
                </CCardBody>
              </CCard>
          </CCol>
        </CRow>
      </CContainer>
    </div>
  );
};

export default Login;
