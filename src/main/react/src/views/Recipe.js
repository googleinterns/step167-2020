import React, { useState, useEffect } from "react";
import { useHistory } from "react-router-dom";
import { CButton, CBadge, CLink, CCard, CCardBody, CCardHeader, CTextarea } from "@coreui/react";
import CIcon from "@coreui/icons-react";
import { MarkdownPreview } from "react-marked-markdown";
import requestRoute, { getTags } from "../requests";
import Page404 from "./pages/page404/Page404";
import app from "firebase/app";
import "firebase/auth";

const getRecipe = async (id) => {
  let res = await fetch(requestRoute + "api/post?recipeID=" + id);
  let data = await res.json();
  return data;
};

const getComments = async (id) => {
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

  const [commentContent, setCommentContent] = useState("");

  const [signedIn, setSignedIn] = useState(false);

  app.auth().onAuthStateChanged((user) => setSignedIn(user ? true : false));

  const history = useHistory();
  var searchParams = new URLSearchParams(history.location.search);
  const id = searchParams.get("id");
  const [notFound, setNotFound] = useState(false);

  const submitComment = async () => {
    let idToken = await app.auth().currentUser.getIdToken();
    fetch(requestRoute + "api/comment?recipeID=" + id + "&token=" + idToken, {
      method: "POST",
      body: JSON.stringify({
        content: commentContent,
      }),
    });
    let newComment = {
      content: commentContent,
      ldap: app.auth().currentUser.email,
    };
    setComments([newComment].concat(comments));
  };

  const toggleVote = (vote) => {
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
  };

  useEffect(() => {
    if (id && id !== "") {
      getRecipe(id).then((data) => {
        if (JSON.stringify(data) !== "{}") {
          setRecipe(data);
          setVotes(data.metadata.votes);
          getTags(data.metadata.tagIds).then((tags) => setTags(tags));
          getComments(id).then((commentData) => setComments(commentData));
        } else {
          setNotFound(true);
        }
      });
    } else {
      setNotFound(true);
    }
  }, [id]);

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
      </CCard>
      {signedIn && (
        <CCard>
          <CCardHeader>
            Add a comment
            <CButton type="submit" size="sm" color="primary" className="float-right" onClick={submitComment}>
              Submit
            </CButton>
          </CCardHeader>
          <CCardBody>
            <CTextarea
              name="textarea-input"
              id="textarea-input"
              rows="2"
              onChange={(event) => setCommentContent(event.target.value)}
            />
          </CCardBody>
        </CCard>
      )}
      {comments.map((comment, i) => (
        <CCard key={i}>
          <CCardHeader>{comment.ldap.split("@")[0]}</CCardHeader>
          <CCardBody>{comment.content}</CCardBody>
        </CCard>
      ))}
    </>
  );
};

export default Recipe;
