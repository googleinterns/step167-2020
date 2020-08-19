import React, {
  useState
} from 'react'
import { useHistory } from 'react-router-dom'
import {
  CButton,
  CCard,
  CCardBody,
  CCol,
  CContainer,
  CForm,
  CInput,
  CInputGroup,
  CInputGroupPrepend,
  CInputGroupText,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CRow
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import app from 'firebase/app'
import 'firebase/auth'
import requestRoute from '../../../requests';

const Register = () => {
  const history = useHistory();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordCopy, setPasswordCopy] = useState('');

  const [errMsg, setErrMsg] = useState("");

  const createUser = (idToken) => {
    fetch(requestRoute + "api/user?token=" + idToken, {
      method: "POST"
    })
  }

  const createAccount = () => {
    if (password !== passwordCopy) {
      setErrMsg("Passwords do not match.");
      return;
    }
    app
      .auth()
      .createUserWithEmailAndPassword(email, password)
      .then(() => {
        // Create new user document on the back end.
        app.auth().currentUser.getIdToken().then(createUser);
      })
      .catch(err => setErrMsg(err.message))
      .then(() => history.push("/top"));
  };

  app.auth().onAuthStateChanged(function (user) {
    if (user) {
      console.log('This is the user: ', user)
    } else {
      // No user is signed in.
      console.log('There is no logged in user');
    }
  });

  return (
    <div className="c-app c-default-layout flex-row align-items-center">
      <CContainer>
        <CRow className="justify-content-center">
          <CCol md="9" lg="7" xl="6">
            <CCard className="mx-4">
              <CCardBody className="p-4">
                <CForm>
                  <h1>Register</h1>
                  <p className="text-muted">Create your account</p>
                  <CInputGroup className="mb-3">
                    <CInputGroupPrepend>
                      <CInputGroupText>
                        <CIcon name="cil-user" />
                      </CInputGroupText>
                    </CInputGroupPrepend>
                    <CInput
                      id="email"
                      type="email"
                      placeholder="Email"
                      autoComplete="email"
                      onChange={event => setEmail(event.target.value)}
                    />
                  </CInputGroup>
                  <CInputGroup className="mb-3">
                    <CInputGroupPrepend>
                      <CInputGroupText>
                        <CIcon name="cil-lock-locked" />
                      </CInputGroupText>
                    </CInputGroupPrepend>
                    <CInput
                      id="password"
                      type="password"
                      placeholder="Password"
                      autoComplete="new-password"
                      onChange={event => setPassword(event.target.value)}
                    />
                  </CInputGroup>
                  <CInputGroup className="mb-4">
                    <CInputGroupPrepend>
                      <CInputGroupText>
                        <CIcon name="cil-lock-locked" />
                      </CInputGroupText>
                    </CInputGroupPrepend>
                    <CInput
                      id="password-copy"
                      type="password"
                      placeholder="Repeat password"
                      autoComplete="new-password"
                      onChange={event => setPasswordCopy(event.target.value)}
                    />
                  </CInputGroup>
                  <CButton color="success" onClick={createAccount} block>Create Account</CButton>
                </CForm>
              </CCardBody>
            </CCard>
          </CCol>
        </CRow>
      </CContainer>
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
    </div>
  )
}

export default Register
