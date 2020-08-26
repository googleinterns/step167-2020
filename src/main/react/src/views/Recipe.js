import React, { useState, useEffect, useRef } from "react";
import { useHistory } from "react-router-dom";
import {
  CButton,
  CBadge,
  CLink,
  CCard,
  CCardBody,
  CCardHeader,
  CCardFooter,
  CTextarea,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from "@coreui/react";
import CIcon from "@coreui/icons-react";
import Comment from "../components/Comment";
import { MarkdownPreview } from "react-marked-markdown";
import requestRoute, { getTags, getRecipesVote } from "../requests";
import Page404 from "./pages/page404/Page404";
import app from "firebase/app";
import "firebase/auth";

const getRecipe = async id => {
  let res = await fetch(requestRoute + "api/post?recipeID=" + id);
  let data = await res.json();
  return data;
};

const getComments = async id => {
  let res = await fetch(requestRoute + "api/comment?recipeID=" + id);
  let data = await res.json();
  return data;
};

const Recipe = () => {
  const [recipe, setRecipe] = useState({
    content: "",
    metadata: {
      votes: 0,
      title: "",
      id: "",
      timestamp: 0,
      tagIds: {},
      creatorLdap: "@",
      creatorId: "",
    },
  });
  const [tags, setTags] = useState({});
  const [votes, setVotes] = useState(0);
  const [comments, setComments] = useState([]);
  const [downvote, setDownvote] = useState(false);
  const [upvote, setUpvote] = useState(false);
  const [save, setSave] = useState(false);

  const newCommentInput = useRef("");

  const [errMsg, setErrMsg] = useState("");

  const [signedIn, setSignedIn] = useState(false);

  app.auth().onAuthStateChanged(user => setSignedIn(user ? true : false));

  const history = useHistory();
  var searchParams = new URLSearchParams(history.location.search);
  const recipeId = searchParams.get("id");
  const [notFound, setNotFound] = useState(false);

  const submitComment = async () => {
    if (newCommentInput.current.value === "") {
      setErrMsg("Empty comments are not allowed");
      return;
    }
    let idToken = await app.auth().currentUser.getIdToken();
    let res = await fetch(requestRoute + "api/comment?recipeID=" + recipeId + "&token=" + idToken, {
      method: "POST",
      body: JSON.stringify({
        content: newCommentInput.current.value,
      }),
    });
    let commentData = await res.json();
    let newComment = {
      content: newCommentInput.current.value,
      ldap: app.auth().currentUser.email,
      id: commentData.id,
    };
    setComments([newComment].concat(comments));
  };

  const deleteComment = async commentId => {
    let idToken = await app.auth().currentUser.getIdToken();
    fetch(requestRoute + "api/comment?recipeID=" + recipeId + "&commentID=" + commentId + "&token=" + idToken, {
      method: "DELETE",
    });
    setComments(comments.filter(comment => comment.id !== commentId));
  };

  const editComment = (commentId, idx, newContent) => {
    app
      .auth()
      .currentUser.getIdToken()
      .then(idToken => {
        fetch(
          requestRoute +
            "api/comment?recipeID=" +
            recipeId +
            "&commentID=" +
            commentId +
            "&commentBody=" +
            newContent +
            "&token=" +
            idToken,
          {
            method: "PUT",
          }
        );
      });
    let newComments = [...comments];
    let editedComment = {};
    Object.assign(editedComment, comments[idx]);
    editedComment.content = newContent;
    newComments[idx] = editedComment;
    setComments(newComments);
  };

  const toggleVote = vote => {
    if (app.auth().currentUser) {
      app
        .auth()
        .currentUser.getIdToken()
        .then(idToken => {
          fetch(requestRoute + "api/vote?recipeId=" + recipeId + "&vote=" + vote + "&token=" + idToken, {
            method: "PUT",
          }).then(response => {
            if (!response.ok) {
              setErrMsg("Error " + response.status.toString());
            }
          });
        })
        .catch(() => setErrMsg("Error! Could not retrieve user ID token."));
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
      setErrMsg("User must be signed in to vote.");
    }
  };

  useEffect(() => {
    app.auth().onAuthStateChanged(async user => {
      if (recipeId && recipeId !== "") {
        let recipeData = await getRecipe(recipeId);
        if (JSON.stringify(recipeData) !== "{}") {
          setTags(await getTags(recipeData.metadata.tagIds));
          setVotes(recipeData.metadata.votes);
          setComments(await getComments(recipeId));
          setRecipe(recipeData);
          if (user) {
            let voteData = (await getRecipesVote([recipeData.metadata]))[0];
            if (voteData) {
              setUpvote(true);
            } else if (voteData === false) {
              // needs to be explicitly false, not null
              setDownvote(true);
            }
          }
        } else {
          setNotFound(true);
        }
      } else {
        setNotFound(true);
      }
    });
  }, [recipeId]);

  if (notFound) {
    return <Page404 />;
  }

  return (
    <>
      <CCard>
        <CCardHeader>
          {recipe.metadata.title}
          <div className="card-header-actions">
            {Object.keys(tags).map((tagId, idx) => (
              <CBadge color="success" key={idx} style={{ marginRight: 5 }}>
                {tags[tagId].name}
              </CBadge>
            ))}
            <CLink className="card-header-action" onClick={() => toggleVote(true)}>
              <CIcon name="cil-arrow-circle-top" className={upvote ? "text-danger" : ""} />
            </CLink>
            <CLink className="card-header-action" onClick={() => toggleVote(false)}>
              <CIcon name="cil-arrow-circle-bottom" className={downvote ? "text-info" : ""} />
            </CLink>
            <CLink className="card-header-action" onClick={() => setSave(!save)} style={{ marginRight: 5 }}>
              <CIcon name="cil-save" className={save ? "text-success" : ""} />
            </CLink>
            <CBadge shape="pill" color="primary">
              {votes}
            </CBadge>
          </div>
        </CCardHeader>
        <CCardBody>
          <MarkdownPreview value={recipe.content} />
        </CCardBody>
        <CCardFooter>
          <div className="card-header-actions">
            <CIcon name="cil-aperture" className="text-primary" style={{ marginRight: 4 }} />
            {recipe.metadata.creatorLdap.split("@")[0]}
          </div>
        </CCardFooter>
      </CCard>
      {signedIn && (
        <CCard>
          <CCardHeader>
            Add a comment
            <CButton size="sm" color="primary" className="float-right" onClick={submitComment}>
              Submit
            </CButton>
          </CCardHeader>
          <CCardBody>
            <CTextarea rows="2" innerRef={newCommentInput} />
          </CCardBody>
        </CCard>
      )}
      {comments.map((comment, i) => (
        <Comment
          key={i}
          comment={comment}
          signedIn={signedIn}
          recipePosterLdap={recipe.metadata.creatorLdap}
          recipeId={recipeId}
          delete={() => deleteComment(comment.id)}
          edit={newContent => editComment(comment.id, i, newContent)}
          setErrMsg={setErrMsg}
        />
      ))}
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

export default Recipe;
