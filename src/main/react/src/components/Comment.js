import React, { useState } from "react";
import PropTypes from "prop-types";
import { CButton, CLink, CCard, CCardBody, CCardHeader, CTextarea } from "@coreui/react";
import CIcon from "@coreui/icons-react";
import app from "firebase/app";
import "firebase/auth";

const Comment = props => {
  const [editMode, setEditMode] = useState(false);

  const [editedContent, setEditedContent] = useState(props.comment.content);

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
          {userIsCommentCreator && !editMode && (
            <>
              <CLink className="card-header-action" onClick={() => setEditMode(!editMode)}>
                <CIcon name="cil-pencil" />
              </CLink>
              <CLink className="card-header-action" onClick={() => {}}>
                <CIcon name="cil-trash" />
              </CLink>
            </>
          )}
          {editMode && (
            <>
              <CButton size="sm" color="primary" className="float-right" onClick={() => console.log(editedContent)}>
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
        {editMode && (
          <CTextarea
            onChange={event => setEditedContent(event.target.value)}
            rows="2"
            defaultValue={props.comment.content}
          />
        )}
      </CCardBody>
    </CCard>
  );
};

Comment.propTypes = {
  comment: PropTypes.object,
  signedIn: PropTypes.bool,
  recipePosterLdap: PropTypes.string,
};

export default Comment;
