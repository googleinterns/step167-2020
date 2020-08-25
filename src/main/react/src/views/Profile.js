import React, { useState, useEffect } from "react";
import {
  CCard,
  CCardBody,
  CRow,
  CCol,
  CImg,
  CCardHeader,
  CNav,
  CNavItem,
  CNavLink,
  CButton,
  CBadge,
} from "@coreui/react";
import Select from "react-select";
import Feed from "./Feed";
import requestRoute, { getTags } from "../requests";
import app from "firebase/app";
import "firebase/auth";

const Profile = () => {
  const [signedIn, setSignedIn] = useState(false);
  const [profilePic, setProfilePic] = useState(null);

  const [feedType, setFeedType] = useState("saved");

  const [allTags, setAllTags] = useState({});
  const [selectedTagId, setSelectedTagId] = useState(null);
  const [followedTagIds, setFollowedTagIds] = useState([]);

  const followTag = async (user, tagId) => {
    if (!followedTagIds.includes(tagId)) {
      setFollowedTagIds(followedTagIds.concat(tagId));
      let idToken = await user.getIdToken();
      fetch(requestRoute + "api/user?type=FOLLOW_TAG&tagID=" + tagId + "&token=" + idToken, { method: "PUT" });
    }
  };

  const unfollowTag = async (user, tagId) => {
    if (followedTagIds.includes(tagId)) {
      setFollowedTagIds(followedTagIds.filter(id => id !== tagId));
      let idToken = await user.getIdToken();
      fetch(requestRoute + "api/user?type=UNFOLLOW_TAG&tagID=" + tagId + "&token=" + idToken, { method: "PUT" });
    }
  };

  const getFollowedTags = async user => {
    let idToken = await user.getIdToken();
    let tagsJson = await fetch(requestRoute + "api/user?type=FOLLOW_TAG&token=" + idToken);
    let tagsData = await tagsJson.json();
    return tagsData;
  };

  let listener = app.auth().onAuthStateChanged(user => {
    if (user) {
      listener(); // unsubscribe from this listener
      setSignedIn(true);
      setProfilePic(user.providerData[0].photoURL);
    }
  });

  useEffect(() => {
    getTags().then(setAllTags);
    let listener2 = app.auth().onAuthStateChanged(async user => getFollowedTags(user).then(setFollowedTagIds));
    return () => {
      listener2();
      setFollowedTagIds([]);
      setAllTags({});
    };
  }, []);

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
                <CCol sm={4}>
                  <CRow>
                    <CCol sm={12}>
                      <Select
                        id="tag-follow"
                        name="tag-follow"
                        options={Object.keys(allTags).map(tagId => {
                          return { label: allTags[tagId].name, value: tagId };
                        })}
                        onChange={selected => setSelectedTagId(selected.value)}
                      />
                    </CCol>
                  </CRow>
                  <CRow>
                    <CCol sm={12}>
                      {followedTagIds.map(
                        (tagId, idx) =>
                          allTags[tagId] && (
                            <CBadge color="success" key={idx} style={{ marginLeft: 3 }}>
                              {allTags[tagId].name}
                            </CBadge>
                          )
                      )}
                    </CCol>
                  </CRow>
                </CCol>
                <CCol sm={2}>
                  <CButton color="primary" onClick={() => followTag(app.auth().currentUser, selectedTagId)}>
                    Follow
                  </CButton>
                  <CButton
                    color="danger"
                    style={{ marginLeft: 5 }}
                    onClick={() => unfollowTag(app.auth().currentUser, selectedTagId)}
                  >
                    Unfollow
                  </CButton>
                </CCol>
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
