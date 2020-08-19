import React, {
  useState,
  useEffect
} from 'react'
import {
  CBadge,
  CButton,
  CCard,
  CCardBody,
  CCardFooter,
  CCardHeader,
  CCol,
  CRow,
  CCollapse,
  CFade,
  CSwitch,
  CLink,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import RecipeCard from '../components/RecipeCard';
import requestRoute, {
  getTags,
  getRecipesVote
} from '../requests';
import app from 'firebase/app';
import 'firebase/auth';

const Feed = (props) => {
  console.log(props.feedType);
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState({});

  const [errMsg, setErrMsg] = useState("");

  const getRecipes = async () => {
    let res = await fetch(requestRoute + "api/post");
    let data = await res.json();
    return data;
  }

  let loaded = 0;
  let recipeDataCopy = [];

  useEffect(() => { loaded = 1; }, []);

  app.auth().onAuthStateChanged(async (user) => { // called every render
    if (loaded === 1) {
      let recipeData = await getRecipes();
      let tagIds = {}
      recipeData.forEach(recipe => Object.assign(tagIds, recipe.tagIds));
      recipeDataCopy = recipeData;
      loaded = 2;
      setTags(await getTags(tagIds));
    }
    if (loaded === 2) {
      if (user) {
        let voteData = await getRecipesVote(recipeDataCopy);
        recipeDataCopy.forEach((recipe, i) => recipe.voted = voteData[i]);
        setRecipes(recipeDataCopy);
      } else {
        setRecipes(recipeDataCopy);
      }
    }
  });

  return (
    <>
      <CRow>
        {
          recipes.map((recipe, idx) => 
              <CCol xs="12" sm="6" md="4" key={idx} >
                <RecipeCard recipe={recipe} tags={tags} setErrMsg={setErrMsg} />
              </CCol>
            )
        }
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

export default Feed;
