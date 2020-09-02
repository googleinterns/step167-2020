import React, { useState, useRef } from "react";
import PropTypes from "prop-types";
import { CButton, CLink, CCard, CCardBody, CCardHeader, CTextarea, CCardFooter } from "@coreui/react";
import CIcon from "@coreui/icons-react";
import app from "firebase/app";
import "firebase/auth";

const Comment = props => {
  const [editMode, setEditMode] = useState(false);
  const [replyMode, setReplyMode] = useState(false);

  const newContent = useRef(props.comment.content);
  const replyContent = useRef("");

  const userIsCommentCreator = props.signedIn && props.comment.ldap === app.auth().currentUser.email;

  return (
    <CCard>
      <CCardHeader>
        {props.comment.ldap === props.recipePosterLdap && (
          <CIcon name="cil-aperture" className="text-primary" style={{ marginLeft: 2, marginRight: 2 }} />
        )}
        {userIsCommentCreator && (
          <CIcon name="cil-user" className="text-info" style={{ marginLeft: 2, marginRight: 2 }} />
        )}
        {" " + props.comment.ldap.split("@")[0]}
        <div className="card-header-actions">
          {!replyMode && !editMode && (
            <CLink className="card-header-action" onClick={() => setReplyMode(!replyMode)}>
              <CIcon name="cil-level-down" />
            </CLink>
          )}
          {userIsCommentCreator && !replyMode && !editMode && (
            <>
              <CLink className="card-header-action" onClick={() => setEditMode(!editMode)}>
                <CIcon name="cil-pencil" />
              </CLink>
              <CLink className="card-header-action" onClick={() => props.delete(props.comment.id)}>
                <CIcon name="cil-trash" />
              </CLink>
            </>
          )}
          {editMode && (
            <>
              <CButton
                size="sm"
                color="primary"
                className="float-right"
                onClick={() => {
                  if (newContent.current.value === "") {
                    props.setErrMsg("Empty comments are not allwoed.");
                    return;
                  }
                  props.edit(props.comment.id, newContent.current.value);
                  setEditMode(false);
                }}
              >
                Submit
              </CButton>
              <CButton variant="ghost" size="sm" className="float-right" onClick={() => setEditMode(false)}>
                Cancel
              </CButton>
            </>
          )}
        </div>
      </CCardHeader>
      <CCardBody>
        {!editMode && props.comment.content}
        {editMode && <CTextarea rows="2" innerRef={newContent} defaultValue={props.comment.content} />}
        {replyMode && (
          <>
            <br />
            <br />
            <CCard>
              <CCardHeader>Reply</CCardHeader>
              <CCardBody>
                <CTextarea rows="2" innerRef={replyContent} />
              </CCardBody>
              <CCardFooter>
                <CButton
                  color="primary"
                  size="sm"
                  className="float-right"
                  onClick={() => {
                    setReplyMode(false);
                    props.add(replyContent.current.value, props.comment.id);
                  }}
                >
                  Submit
                </CButton>
                <CButton variant="ghost" size="sm" className="float-right" onClick={() => setReplyMode(false)}>
                  Cancel
                </CButton>
              </CCardFooter>
            </CCard>
          </>
        )}
        {
          props.comment.replies.length > 0 && (
            <>
              <br />
              <br />
            </>
          ) /**spacing between content & replies */
        }
        {props.comment.replies.map((reply, i) => (
          <Comment
            key={i}
            comment={reply}
            signedIn={props.signedIn}
            recipePosterLdap={props.recipePosterLdap}
            recipeId={props.recipeId}
            delete={props.delete}
            edit={props.edit}
            add={props.add}
            setErrMsg={props.setErrMsg}
          />
        ))}
      </CCardBody>
    </CCard>
  );
};

Comment.propTypes = {
  comment: PropTypes.object,
  signedIn: PropTypes.bool,
  recipePosterLdap: PropTypes.string,
  delete: PropTypes.func,
  edit: PropTypes.func,
  add: PropTypes.func,
  recipeId: PropTypes.string,
  setErrMsg: PropTypes.func,
};

export default Comment;
