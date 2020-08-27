import React, { useState } from "react";
import { useHistory } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { CHeader, CToggler, CHeaderBrand, CHeaderNav, CBreadcrumbRouter, CImg, CLink, CButton } from "@coreui/react";
import app from "firebase/app";
import "firebase/auth";

// routes config
import routes from "../routes";

const TheHeader = () => {
  const dispatch = useDispatch();
  const sidebarShow = useSelector(state => state.sidebarShow);

  const [signedIn, setSignedIn] = useState(false);
  const [profilePic, setProfilePic] = useState(null);

  const toggleSidebar = () => {
    const val = [true, "responsive"].includes(sidebarShow) ? false : "responsive";
    dispatch({ type: "set", sidebarShow: val });
  };

  const toggleSidebarMobile = () => {
    const val = [false, "responsive"].includes(sidebarShow) ? true : "responsive";
    dispatch({ type: "set", sidebarShow: val });
  };

  const history = useHistory();

  let listener = app.auth().onAuthStateChanged(user => {
    if (!user) {
      listener(); // unsubscribe from this listener when required to login
      history.push("/login");
    } else {
      setSignedIn(true);
      setProfilePic(user.providerData[0].photoURL);
    }
  });

  return (
    <CHeader withSubheader>
      <CToggler inHeader className="ml-md-3 d-lg-none" onClick={toggleSidebarMobile} />
      <CToggler inHeader className="ml-3 d-md-down-none" onClick={toggleSidebar} />
      <CHeaderBrand className="mx-auto d-lg-none"></CHeaderBrand>

      <CHeaderNav className="d-md-down-none mr-auto">
        <CBreadcrumbRouter className="border-0 c-subheader-nav m-0 px-0 px-md-3" routes={routes} />
      </CHeaderNav>

      <CHeaderNav className="px-3">
        {signedIn && (
          <>
            <CButton onClick={() => app.auth().signOut()}>Sign Out</CButton>
            {profilePic && (
              <div className="c-avatar">
                <CLink href="/#/profile">
                  <CImg src={profilePic} className="c-avatar-img" alt={app.auth().currentUser.email} />
                </CLink>
              </div>
            )}
          </>
        )}
      </CHeaderNav>
    </CHeader>
  );
};

export default TheHeader;
