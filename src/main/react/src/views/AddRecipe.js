import React, { useState } from "react";
import { useHistory } from "react-router-dom";
import {
  CBadge,
  CLink,
  CCard,
  CCardBody,
  CCardFooter,
  CCardHeader,
  CForm,
  CFormGroup,
  CCol,
  CLabel,
  CInput,
  CInputFile,
  CFormText,
  CTextarea,
  CButton,
  CRow,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from "@coreui/react";
import { MarkdownPreview } from "react-marked-markdown";
import app from "firebase/app";
import "firebase/auth";
import "firebase/storage";
import { v4 as uuidv4 } from "uuid";
import requestRoute from "../requests";
import RecipeUploadMap from "../components/RecipeUploadMap";
import TagSelect from "../components/TagSelect";

const AddRecipe = () => {
  const history = useHistory();

  const [title, setTitle] = useState("Title");
  const [content, setContent] = useState("Content");
  const [selectedTags, setSelectedTags] = useState({});
  const [file, setFile] = useState(null);
  const [location, setLocation] = useState(null);
  const [decodedRegion, setDecodedRegion] = useState("");

  const [mapModal, setMapModal] = useState(false);

  const toggle = () => {
    setMapModal(!mapModal);
  };

  const [errMsg, setErrMsg] = useState("");

  const [submitted, setSubmitted] = useState(false);

  let signedIn = false;

  const MAX_TAGS = 3;

  app.auth().onAuthStateChanged(user => (signedIn = user ? true : false));

  const postRecipe = async () => {
    if (file === null) {
      setErrMsg("Upload a picture of your recipe!");
      return;
    } else if (title === "Title" || title === "") {
      setErrMsg("Your title cannot be empty!");
      return;
    } else if (content === "Content" || content === "content") {
      setErrMsg("The content cannot be empty!");
      return;
    }
    setSubmitted(true);
    if (signedIn) {
      let tokenPromise = app
        .auth()
        .currentUser.getIdToken()
        .catch(() => setErrMsg("Error! Could not retrieve user ID token."));
      let fileRef = app.auth().currentUser.uid + "/" + uuidv4();
      let imageUploadPromise = app
        .storage()
        .ref(fileRef)
        .put(file)
        .then(() => app.storage().ref(fileRef).getDownloadURL());
      let [idToken, imageUrl] = await Promise.all([tokenPromise, imageUploadPromise]);
      let tagIds = {};
      Object.keys(selectedTags).forEach(tag => (tagIds[tag] = true));
      fetch(requestRoute + "api/post?token=" + idToken, {
        method: "POST",
        body: JSON.stringify({
          metadata: {
            title: title,
            tagIds: tagIds,
            location: location,
            imageUrl: imageUrl,
          },
          content: content,
        }),
      }).then(response => {
        if (!response.ok) {
          setErrMsg("Error " + response.status.toString());
          return;
        }
        response.json().then(data => history.push("/recipe?id=" + data.id));
      });
    } else {
      setErrMsg("User must be signed in to submit a recipe.");
    }
  };

  return (
    <>
      <CRow>
        <CCol sm={6}>
          <CCard>
            <CCardHeader>Add Recipe</CCardHeader>
            <CCardBody>
              <CForm action="" method="post" encType="multipart/form-data" className="form-horizontal">
                <CFormGroup row>
                  <CCol md="3">
                    <CLabel htmlFor="text-input">Title</CLabel>
                  </CCol>
                  <CCol xs="12" md="9">
                    <CInput id="text-input" name="text-input" onChange={event => setTitle(event.target.value)} />
                  </CCol>
                </CFormGroup>
                <CFormGroup row>
                  <CCol md="3">
                    <CLabel htmlFor="text-input">Tags</CLabel>
                  </CCol>
                  <CCol xs="12" md="9">
                    <TagSelect tags={selectedTags} setTags={setSelectedTags} maxTags={MAX_TAGS} />
                    <CFormText className="help-block">Max {MAX_TAGS} tags</CFormText>
                  </CCol>
                </CFormGroup>
                <CFormGroup row>
                  <CCol md="3">
                    <CLabel htmlFor="text-input">Add a picture</CLabel>
                  </CCol>
                  <CCol xs="12" md="9">
                    <CInputFile
                      onChange={e => {
                        if (e.target.files[0]) setFile(e.target.files[0]);
                      }}
                    />
                  </CCol>
                </CFormGroup>
                <CFormGroup row>
                  <CCol md="3">
                    <CLabel htmlFor="textarea-input">Content</CLabel>
                  </CCol>
                  <CCol xs="12" md="9">
                    <CTextarea
                      name="textarea-input"
                      id="textarea-input"
                      rows="12"
                      onChange={event => setContent(event.target.value)}
                    />
                    <CFormText className="help-block">
                      We support{" "}
                      <CLink href="https://www.markdownguide.org/basic-syntax/" target="_blank">
                        Markdown
                      </CLink>
                    </CFormText>
                  </CCol>
                </CFormGroup>
                <CFormGroup row>
                  <CCol md="3"></CCol>
                  <CCol xs="4" md="3">
                    <CButton onClick={toggle} className="mr-1" color="secondary">
                      Select Location
                    </CButton>
                  </CCol>
                  <CCol xs="8" md="6">
                    {decodedRegion && <p>{decodedRegion}</p>}
                    {!decodedRegion && <p>None selected</p>}
                  </CCol>
                </CFormGroup>
              </CForm>
              <CModal show={mapModal} onClose={toggle} size="xl">
                <CModalHeader>Where does this recipe come from?</CModalHeader>
                <CModalBody>
                  <div className="min-vh-100">
                    <RecipeUploadMap
                      location={location}
                      setLocation={setLocation}
                      decodedRegion={decodedRegion}
                      setDecodedRegion={setDecodedRegion}
                    />
                  </div>
                </CModalBody>
                <CModalFooter>
                  <CButton color="primary" onClick={toggle}>
                    Done
                  </CButton>
                </CModalFooter>
              </CModal>
            </CCardBody>
            <CCardFooter>
              <CButton
                disabled={submitted}
                type="submit"
                size="sm"
                color="primary"
                className="float-right"
                onClick={postRecipe}
              >
                Submit
              </CButton>
            </CCardFooter>
          </CCard>
        </CCol>
        <CCol sm={6}>
          <CCard>
            <CCardHeader>Preview</CCardHeader>
            <CCardHeader>
              {title}
              <div className="card-header-actions">
                {Object.keys(selectedTags).map((tag, idx) => (
                  <CBadge color="success" key={idx} style={{ marginRight: 5 }}>
                    {selectedTags[tag]}
                  </CBadge>
                ))}
              </div>
            </CCardHeader>
            <CCardBody>
              <MarkdownPreview value={content} />
            </CCardBody>
          </CCard>
        </CCol>
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

export default AddRecipe;
