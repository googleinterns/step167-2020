import React, { useState, useEffect, useCallback } from "react";
import PropTypes from "prop-types";
import {
  CButton,
  CCol,
  CCard,
  CCardBody,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CRow,
} from "@coreui/react";
import RecipeCard from "../components/RecipeCard";
import loading from "../assets/loading.gif";
import requestRoute, { getTags, getRecipesVote, getRecipesSaved } from "../requests";
import app from "firebase/app";
import "firebase/auth";

const getRecipes = async (feedType, page) => {
  let qs = requestRoute + "api/post?page=" + page;
  if (feedType === "saved") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "&saved=true&sort=NEW&token=" + token;
  } else if (feedType === "created") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "&sort=NEW&token=" + token;
  } else if (feedType === "popular") {
    qs += "&sort=TOP";
  } else if (feedType === "new") {
    qs += "&sort=NEW";
  }
  let res = await fetch(qs);
  let data = await res.json();
  return data;
};

const Feed = props => {
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState({});

  const [page, setPage] = useState(0);

  const [errMsg, setErrMsg] = useState("");

  const [loaded, setLoaded] = useState(false);

  const loadRecipes = useCallback(
    async (user, recipePage) => {
      let recipeData = await getRecipes(props.feedType, recipePage);
      if (user) {
        let [voteData, savedData] = await Promise.all([getRecipesVote(recipeData), getRecipesSaved(recipeData)]);
        recipeData.forEach((recipe, i) => {
          recipe.voted = voteData[i];
          recipe.saved = savedData[i];
        });
      }
      return recipeData;
    },
    [props.feedType]
  );

  const [signedIn, setSignedIn] = useState(false);

  let listener = app.auth().onAuthStateChanged(user => {
    listener();
    setSignedIn(user ? true : false);
  });

  const initRender = useCallback(async () => {
    if (signedIn) {
      // do on feedtype change and initial render
      setTags(await getTags());
      let recipeDataPage0 = await loadRecipes(app.auth().currentUser, 0);
      setRecipes([recipeDataPage0]);
      setLoaded(true); // Forces component to re-render once recipe loading finished.
    }
  }, [signedIn, loadRecipes]);

  const deleteRecipeOuter = recipeId => {
    let recipesCopy = recipes;
    recipesCopy[page] = recipesCopy[page].filter(recipe => recipe.id !== recipeId);
    setRecipes([...recipesCopy]);
    // It should be setRecipes(recipesCopy), but for some reason
    // that doesn't work and setRecipes([...recipesCopy]) does.
  };

  useEffect(() => {
    initRender();
    return () => {
      // return the cleanup function
      setRecipes([]);
      setTags({});
      setLoaded(false);
    };
  }, [loadRecipes, initRender, signedIn]);

  useEffect(() => {
    if (signedIn && loaded) {
      if (!recipes[page + 1]) {
        loadRecipes(app.auth().currentUser, page + 1).then(nextPageRecipes =>
          setRecipes([...recipes, nextPageRecipes])
        );
      }
    }
    // we do not include recipes in the dependency array
    // because that would cause an infinite rerender loop
    // eslint-disable-next-line
  }, [page, loadRecipes, signedIn, loaded]);

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
      {recipes.length > 0 && (
        <>
          <CRow>
            {recipes[page].map((recipe, idx) => (
              <CCol xs="12" sm="6" md="4" key={idx}>
                <RecipeCard recipe={recipe} tags={tags} setErrMsg={setErrMsg} deleteRecipeOuter={deleteRecipeOuter} />
              </CCol>
            ))}
          </CRow>
          <CRow style={{ marginBottom: 10 }}>
            {recipes[page - 1] && (
              <CCol>
                <CButton color="info" onClick={() => setPage(page - 1)}>
                  Prev Page
                </CButton>
              </CCol>
            )}
            {recipes[page + 1] && recipes[page + 1].length > 0 && (
              <CCol className="d-flex justify-content-end">
                <CButton color="info" onClick={() => setPage(page + 1)}>
                  Next Page
                </CButton>
              </CCol>
            )}
          </CRow>
        </>
      )}
      {recipes[page].length === 0 && (
        <CRow>
          <CCard>
            <CCardBody>There seem to be no recipes here.</CCardBody>
          </CCard>
        </CRow>
      )}
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
