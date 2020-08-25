import React, { useState } from "react";
import { CCard, CCardBody, CRow, CCol, CImg, CCardHeader, CNav, CNavItem, CNavLink } from "@coreui/react";
import Feed from "./Feed";
import app from "firebase/app";
import "firebase/auth";

const Profile = () => {
  const [signedIn, setSignedIn] = useState(false);
  const [profilePic, setProfilePic] = useState(null);

  const [feedType, setFeedType] = useState("saved");

  let listener = app.auth().onAuthStateChanged(user => {
    if (user) {
      listener(); // unsubscribe from this listener when required to login
      setSignedIn(true);
      setProfilePic(user.providerData[0].photoURL);
    }
  });

  return (
    <>
      {signedIn && (
        <>
          <CCard>
            <CCardBody>
              <CRow>
                <CCol sm={3}>
                  <CImg
                    src={profilePic}
                    className="c-avatar-img"
                    alt={app.auth().currentUser.email}
                    style={{ width: "80%" }}
                  />
                </CCol>
                <CCol sm={3}>
                  <h2>{app.auth().currentUser.displayName}</h2>
                  <h4>{app.auth().currentUser.email.split("@")[0]}</h4>
                </CCol>
                <CCol sm={6}>TODO: Display followed tags here and give a way to follow / unfollow tags</CCol>
              </CRow>
            </CCardBody>
          </CCard>
          <CRow>
            <CCol sm={2}>
              <CCard>
                <CCardHeader>
                  <CNav variant="pills" vertical={true}>
                    <CNavItem>
                      <CNavLink onClick={() => setFeedType("saved")}>Saved Recipes</CNavLink>
                    </CNavItem>
                    <CNavItem>
                      <CNavLink onClick={() => setFeedType("created")}>Created Recipes</CNavLink>
                    </CNavItem>
                  </CNav>
                </CCardHeader>
              </CCard>
            </CCol>
            <CCol sm={10}>
              <Feed feedType={feedType} />
            </CCol>
          </CRow>
        </>
      )}
    </>
  );
};

export default Profile;
