import React, {
  useState,
  useEffect
} from 'react'
import { useHistory } from 'react-router-dom'
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
  CFormText,
  CTextarea,
  CButton,
  CRow,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { MarkdownPreview } from 'react-marked-markdown'
import Select from 'react-select';
import app from 'firebase/app';
import 'firebase/auth';
import requestRoute, {
  getTags
} from '../requests';

const AddRecipe = () => {
  const history = useHistory();

  const [title, setTitle] = useState("Title")
  const [content, setContent] = useState("Content");
  const [tagIds, setTagIds] = useState({});

  const [errMsg, setErrMsg] = useState("");

  const [allTags, setAllTags] = useState([]);

  let signedIn = false;

  const MAX_TAGS = 3;

  app.auth().onAuthStateChanged(user => signedIn = user ? true : false);

  useEffect(() => {
    getTags().then(data => setAllTags(data));
  }, []);

  const postRecipe = () => {
    if (signedIn) {
      app
        .auth()
        .currentUser.getIdToken()
        .then(function (idToken) {
          // Send token to backend via HTTPS
          fetch(requestRoute + "api/post?token=" + idToken, {
            method: "POST",
            body: JSON.stringify({
              metadata: {
                title: title,
                tagIds: tagIds,
              },
              content: content,
            }),
          }).then((response) => {
            if (!response.ok) {
              setErrMsg("Error " + response.status.toString())
              return;
            }
            response.json().then(data => history.push("/recipe?id=" + data.id))
          })
        })
        .catch(function (error) {
          setErrMsg("Error! Could not retrieve user ID token.")
        })
    } else {
      setErrMsg("User must be signed in to submit a recipe.")
    }
  }

  return (
    <>
      <CRow>
        <CCol sm={6} >
          <CCard >
            <CCardHeader >
              Add Recipe
            </CCardHeader>
            <CCardBody >
              <CForm action="" method="post" encType="multipart/form-data" className="form-horizontal">
                <CFormGroup row>
                  <CCol md="3">
                    <CLabel htmlFor="text-input">Title</CLabel>
                  </CCol>
                  <CCol xs="12" md="9">
                    <CInput
                      id="text-input"
                      name="text-input"
                      onChange={event => setTitle(event.target.value)}
                    />
                  </CCol>
                </CFormGroup>
                <CFormGroup row>
                  <CCol md="3">
                    <CLabel htmlFor="text-input">Tags</CLabel>
                  </CCol>
                  <CCol xs="12" md="9">
                    <Select
                      id="tags-select"
                      name="tags-select"
                      isMulti
                      options={Object.keys(tagIds).length >= MAX_TAGS ? [] :
                        Object.keys(allTags).map(tagId => {
                          return { label: allTags[tagId].name, value: tagId }
                        })
                      }
                      onChange={selected => {
                        if (selected === null) {
                          setTagIds({});
                          return;
                        }
                        let newTagIds = {};
                        selected.forEach(tag => newTagIds[tag.value] = true);
                        setTagIds(newTagIds);
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
                      We support <CLink href="https://www.markdownguide.org/basic-syntax/" target="_blank" >Markdown</CLink>
                    </CFormText>
                  </CCol>
                </CFormGroup>
              </CForm>
            </CCardBody>
            <CCardFooter>
              <CButton
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
        <CCol sm={6} >
          <CCard >
            <CCardHeader >
              Preview
            </CCardHeader>
            <CCardHeader >
              {title}
              <div className="card-header-actions">
                {Object.keys(tagIds).map((tagId, idx) => (
                  <CBadge color="success" key={idx} style={{ marginRight: 5 }}>{allTags[tagId].name}</CBadge>
                ))}
              </div>
            </CCardHeader>
            <CCardBody >
              <MarkdownPreview value={content} />
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
      <CModal
        show={errMsg !== ""}
        onClose={() => setErrMsg("")}
        color="danger"
        size="sm"
      >
        <CModalHeader closeButton>
          <CModalTitle>ERROR!!</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {errMsg}
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setErrMsg("")}>Ok</CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default AddRecipe;