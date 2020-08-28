import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import { CBadge, CCard, CCardBody, CCardFooter, CCardHeader, CLink } from "@coreui/react";
import CIcon from "@coreui/icons-react";
import requestRoute from "../requests";
import app from "firebase/app";
import "firebase/auth";

const RecipeCard = props => {
  const recipe = props.recipe;

  const [votes, setVotes] = useState(props.recipe.votes);
  const [downvote, setDownvote] = useState(false);
  const [upvote, setUpvote] = useState(false);
  const [save, setSave] = useState(props.recipe.saved);

  useEffect(() => {
    setVotes(props.recipe.votes);
    setUpvote(props.recipe.voted === true);
    setDownvote(props.recipe.voted === false);
  }, [props]);

  const deleteRecipe = async () => {
    let idToken = await app.auth().currentUser.getIdToken();
    fetch(requestRoute + "api/post?recipeID=" + recipe.id + "&token=" + idToken, { method: "DELETE" });
    if (props.deleteRecipeOuter) {
      // meant for feed to delete recipecard, map to delete marker, etc
      props.deleteRecipeOuter(recipe.id);
    }
  };

  const toggleSave = () => {
    app
      .auth()
      .currentUser.getIdToken()
      .then(idToken =>
        fetch(
          requestRoute + "api/user?recipeID=" + recipe.id + "&token=" + idToken + "&type=" + (save ? "UNSAVE" : "SAVE"),
          { method: "PUT" }
        )
      );
    setSave(!save);
  };

  const toggleVote = vote => {
    if (app.auth().currentUser) {
      app
        .auth()
        .currentUser.getIdToken()
        .then(idToken => {
          fetch(requestRoute + "api/vote?recipeId=" + recipe.id + "&vote=" + vote + "&token=" + idToken, {
            method: "PUT",
          }).then(response => {
            if (!response.ok) {
              props.setErrMsg("Error " + response.status.toString());
            }
          });
        })
        .catch(() => props.setErrMsg("Error! Could not retrieve user ID token."));
      if (vote) {
        // upvote clicked
        if (!upvote && !downvote) {
          setUpvote(true);
          setVotes(votes + 1);
        } else if (upvote && !downvote) {
          setUpvote(false);
          setVotes(votes - 1);
        } else if (!upvote && downvote) {
          setDownvote(false);
          setUpvote(true);
          setVotes(votes + 2);
        }
      } else {
        // downvote clicked
        if (!upvote && !downvote) {
          setDownvote(true);
          setVotes(votes - 1);
        } else if (!upvote && downvote) {
          setDownvote(false);
          setVotes(votes + 1);
        } else if (upvote && !downvote) {
          setUpvote(false);
          setDownvote(true);
          setVotes(votes - 2);
        }
      }
    } else {
      props.setErrMsg("User must be signed in to vote.");
    }
  };

  return (
    <CCard>
      <CCardHeader>
        {recipe.title}
        <div className="card-header-actions">
          <CLink className="card-header-action" onClick={() => toggleVote(true)}>
            <CIcon name="cil-arrow-circle-top" className={upvote ? "text-danger" : ""} />
          </CLink>
          <CLink className="card-header-action" onClick={() => toggleVote(false)}>
            <CIcon name="cil-arrow-circle-bottom" className={downvote ? "text-info" : ""} />
          </CLink>
          <CLink className="card-header-action" onClick={toggleSave} style={{ marginRight: 5 }}>
            <CIcon name="cil-save" className={save ? "text-success" : ""} />
          </CLink>
          {app.auth().currentUser.uid === recipe.creatorId && (
            <CLink className="card-header-action" onClick={deleteRecipe} style={{ marginRight: 5 }}>
              <CIcon name="cil-trash" />
            </CLink>
          )}
          <CBadge shape="pill" color="primary">
            {votes}
          </CBadge>
        </div>
      </CCardHeader>
      <CLink href={"/#/recipe?id=" + recipe.id}>
        <CCardBody>
          <img
            src="https://www.cookingclassy.com/wp-content/uploads/2019/07/birthday-cake-4-500x500.jpg"
            style={{ width: "70%", height: "70%", marginLeft: "15%" }}
            alt={recipe.title}
          />
        </CCardBody>
      </CLink>
      <CCardFooter>
        {recipe.creatorLdap.split("@")[0]}
        <div className="card-header-actions">
          {Object.keys(recipe.tagIds).map(
            (tagId, idx) =>
              props.tags[tagId] && (
                <CBadge color="success" key={idx} style={{ marginLeft: 3 }}>
                  {props.tags[tagId].name}
                </CBadge>
              )
          )}
        </div>
      </CCardFooter>
    </CCard>
  );
};

RecipeCard.propTypes = {
  recipe: PropTypes.object,
  setErrMsg: PropTypes.func,
  tags: PropTypes.object,
  deleteRecipeOuter: PropTypes.func,
};

export default RecipeCard;
