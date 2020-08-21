import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import { CButton, CCol, CRow, CModal, CModalBody, CModalFooter, CModalHeader, CModalTitle } from "@coreui/react";
import RecipeCard from "../components/RecipeCard";
import requestRoute, { getTags, getRecipesVote } from "../requests";
import app from "firebase/app";
import "firebase/auth";
import FeedMap from "../components/FeedMap";

const getRecipes = async () => {
  let res = await fetch(requestRoute + "api/post");
  let data = await res.json();
  return data;
};

const Feed = props => {
  console.log(props.feedType);
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState({});
  const [mapMode, setMapMode] = useState(false);

  const [errMsg, setErrMsg] = useState("");

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
    });
  }, []);

  return (
    <>
      <CRow>
        <CCol xs="36" sm="18" md="12">
          <div className="float-right">
            <CButton onClick={() => setMapMode(!mapMode)} className="mr-1" color="primary">
              Toggle Map View
            </CButton>
          </div>
          <br></br>
          <br></br>
        </CCol>
      </CRow>
      <CRow>
        {!mapMode &&
          recipes.map((recipe, idx) => (
            <CCol xs="12" sm="6" md="4" key={idx}>
              <RecipeCard recipe={recipe} tags={tags} setErrMsg={setErrMsg} />
            </CCol>
          ))}
        {mapMode && (
          <CCol xs="36" sm="18" md="12">
            <div className="min-vh-100">
              <FeedMap recipes={recipes} />
            </div>
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
};

export default Feed;
