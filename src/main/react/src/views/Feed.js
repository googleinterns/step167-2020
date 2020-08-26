import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import {
  CButton,
  CCard,
  CCardBody,
  CCol,
  CRow,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from "@coreui/react";
import RecipeCard from "../components/RecipeCard";
import loading from "../assets/loading.gif";
import requestRoute, { getTags, getRecipesVote, getRecipesSaved } from "../requests";
import app from "firebase/app";
import "firebase/auth";
import FeedMap from "../components/FeedMap";

const getRecipes = async feedType => {
  let qs = requestRoute + "api/post";
  if (feedType === "saved") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "?saved=true&sort=NEW&token=" + token;
  } else if (feedType === "created") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "?sort=NEW&token=" + token;
  } else if (feedType === "popular") {
    qs += "?sort=TOP";
  } else if(feedType === "new") {
    qs += "?sort=NEW";
  }
  let res = await fetch(qs);
  let data = await res.json();
  return data;
};

const Feed = props => {
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState({});

  const [errMsg, setErrMsg] = useState("");

  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    // do on feedtype change and initial render
    let listener = app.auth().onAuthStateChanged(async user => {
      listener();
      let recipeData = await getRecipes(props.feedType);
      let tagIds = {};
      recipeData.forEach(recipe => Object.assign(tagIds, recipe.tagIds));
      setTags(await getTags(tagIds));
      if (user) {
        let voteData = await getRecipesVote(recipeData);
        recipeData.forEach((recipe, i) => (recipe.voted = voteData[i]));
        let savedData = await getRecipesSaved(recipeData);
        recipeData.forEach((recipe, i) => (recipe.saved = savedData[i]));
      }
      setRecipes(recipeData);
      setLoaded(true); // Forces component to re-render once recipe loading finished.
    });
    return () => {
      // return the cleanup function
      setRecipes([]);
      setTags({});
      setLoaded(false);
    };
  }, [props.feedType]);

  if (!loaded) {
    return (
      <CRow className="justify-content-center">
        <CCol sm={2}>
          <img src={loading} alt="loading..." />
        </CCol>
      </CRow>
    );
  }

  return (
    <>
      <CRow>
        {!props.mapMode &&
          recipes.length > 0 &&
          recipes.map((recipe, idx) => (
            <CCol xs="12" sm="6" md="4" key={idx}>
              <RecipeCard recipe={recipe} tags={tags} setErrMsg={setErrMsg} />
            </CCol>
          ))}
        {!props.mapMode && recipes.length === 0 && (
          <CCard>
            <CCardBody>There seem to be no recipes here.</CCardBody>
          </CCard>
        )}
        {props.mapMode && (
          <CCol xs="36" sm="18" md="12">
            <CCard>
              <CCardBody>
                <div className="min-vh-100">
                  <FeedMap recipes={recipes} tags={tags} setErrMsg={setErrMsg} />
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

Feed.propTypes = {
  feedType: PropTypes.string,
  mapMode: PropTypes.bool,
};

export default Feed;
