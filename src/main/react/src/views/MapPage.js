import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CInput,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CRow,
} from "@coreui/react";
import requestRoute, { getTags, getRecipesVote } from "../requests";
import app from "firebase/app";
import "firebase/auth";
import FeedMap from "../components/FeedMap";

const getRecipes = async () => {
  let res = await fetch(requestRoute + "api/post");
  let data = await res.json();
  return data;
};

const MapPage = props => {
  console.log(props.feedType);
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState({});
  const [ready, setReady] = useState(false);
  const [mapCenter, setMapCenter] = useState("");

  const [errMsg, setErrMsg] = useState("");

  let searchBarVal = "";

  useEffect(() => {
    // do on first render
    app.auth().onAuthStateChanged(async user => {
      let recipeData = await getRecipes();
      let tagIds = {};
      recipeData.forEach(recipe => Object.assign(tagIds, recipe.tagIds));
      setTags(await getTags(tagIds));
      if (user) {
        let voteData = await getRecipesVote(recipeData);
        recipeData.forEach((recipe, i) => (recipe.voted = voteData[i]));
      }
      setRecipes(recipeData);
      setReady(true); // Forces Map component to re-render once recipe loading finished.
      // Necessary for InfoWindows to be populated
    });
  }, []);

  return (
    <>
      <CRow>
        {ready && (
          <CCol xs="36" sm="18" md="12">
            <CCard>
              <CCardHeader>
                <CRow>
                  <CCol>
                    <CInput
                      onChange={event => (searchBarVal = event.target.value)}
                      placeholder="Search By Location"
                    ></CInput>
                  </CCol>
                  <CCol>
                    <CButton color="primary" onClick={() => setMapCenter(searchBarVal)}>
                      Go
                    </CButton>
                  </CCol>
                </CRow>
              </CCardHeader>
              <CCardBody>
                <div className="min-vh-100">
                  <FeedMap recipes={recipes} tags={tags} setErrMsg={setErrMsg} mapCenter={mapCenter} />
                </div>
              </CCardBody>
            </CCard>
          </CCol>
        )}
      </CRow>
      <CModal show={errMsg !== ""} onClose={() => setErrMsg("")} color="danger" size="sm">
        <CModalHeader closeButton>
          <CModalTitle>ERROR!!</CModalTitle>
        </CModalHeader>
        <CModalBody>{errMsg}</CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setErrMsg("")}>
            Ok
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  );
};

MapPage.propTypes = {
  feedType: PropTypes.string,
};

export default MapPage;
