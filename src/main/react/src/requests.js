import app from 'firebase/app';
import 'firebase/auth';

const requestRoute = "http://localhost:8080/";
const mapsApiKey = "AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc";

const getRecipes = async (feedType, page, tags, sortFollowed) => {
  let qs = requestRoute + "api/post?";
  qs += ((page || page === 0) ? "page=" + page : "");
  if (feedType === "saved") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "&saved=true&sort=NEW&token=" + token;
  } else if (feedType === "created") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "&sort=NEW&token=" + token;
  } else if (feedType === "followed-tags") {
    let token = await app.auth().currentUser.getIdToken();
    qs += "&followed-tags=true&token=" + token + "&sort=" + sortFollowed;
  } else if (feedType === "popular") {
    qs += "&sort=TOP";
  } else if (feedType === "new") {
    qs += "&sort=NEW";
  } 
  if (tags) {
    let tagsQuery = Object.keys(tags).map(id => "tagIDs=" + id);
    qs += "&" + tagsQuery.join("&");
  }
  console.log(qs);
  let res = await fetch(qs);
  console.log(JSON.stringify(res));
  let data = await res.json();
  console.log(data);
  return data;
};

const getTags = async (tagIds) => {
  let qs;
  if (tagIds === undefined) {
    qs = requestRoute + "api/tag";
  } else {
    qs = requestRoute + "api/tag?tagIds=&"; // use one empty string tag so we don't get all tags when we need none
    let qtagIds = Object.keys(tagIds).map(id => "tagIds=" + id);
    qs += qtagIds.join("&");
  }
  let res = await fetch(qs);
  let data = await res.json();
  let tagObj = {};
  data.forEach(tag => tagObj[tag.id] = tag);
  return tagObj;
}

const getVoteData = async (recipes) => {
  if(JSON.stringify(recipes) === "[]") {
    return [];
  }
  if (app.auth().currentUser) {
    let idToken = await app.auth().currentUser.getIdToken();
    let qs = requestRoute + "api/vote?token=" + idToken + "&";
    let recipeIds = recipes.map(recipe => "recipeIds=" + recipe.id);
    qs += recipeIds.join("&");
    let res = await fetch(qs);
    let data = await res.json();
    return data;
  }
}

const getSavedData = async (recipes) => {
  if(JSON.stringify(recipes) === "[]") {
    return [];
  }
  if (app.auth().currentUser) {
    let idToken = await app.auth().currentUser.getIdToken();
    let qs = requestRoute + "api/user?token=" + idToken + "&type=SAVE&";
    let recipeIds = recipes.map(recipe => "recipeID=" + recipe.id);
    qs += recipeIds.join("&");
    let res = await fetch(qs);
    let data = await res.json();
    return data;
  }
}

const createUser = async (idToken) => {
  await fetch(requestRoute + "api/user?token=" + idToken, {
    method: "POST"
  })
}

export {
  getRecipes,
  getTags,
  getVoteData,
  getSavedData,
  createUser,
  mapsApiKey
};
export default requestRoute;