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
import { getRecipes, getTags, getVoteData, getSavedData } from "../requests";
import app from "firebase/app";
import "firebase/auth";
import FeedMap from "../components/FeedMap";
import TagSelect from "../components/TagSelect";

const MapPage = props => {
  console.log(props.feedType);
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState({});
  const [selectedTags, setSelectedTags] = useState({});
  const [loaded, setLoaded] = useState(false);
  const [mapCenter, setMapCenter] = useState("");

  const [errMsg, setErrMsg] = useState("");

  let searchBarVal = "";

  useEffect(() => {
    // do on first render
    app.auth().onAuthStateChanged(async user => {
      let recipeData = await getRecipes(null, null, selectedTags);
      setRecipes(recipeData);
      setLoaded(true); // Forces Map component to re-render once recipe loading finished.
      getTags().then(setTags);
      if (user) {
        let [voteData, savedData] = await Promise.all([getVoteData(recipeData), getSavedData(recipeData)]);
        recipeData.forEach((recipe, i) => {
          recipe.voted = voteData[i];
          recipe.saved = savedData[i];
        });
      }
    });
  }, [props.feedType, selectedTags]);

  return (
    <>
      <CRow>
        {loaded && (
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
                  <CCol>
                    <TagSelect tags={selectedTags} setTags={setSelectedTags} />
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
