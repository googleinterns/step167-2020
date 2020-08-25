import app from 'firebase/app';
import 'firebase/auth';

const requestRoute = "http://localhost:8080/";

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

const getRecipesVote = async (recipes) => {
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

const getRecipesSaved = async (recipes) => {
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
  getTags,
  getRecipesVote,
  getRecipesSaved,
  createUser
};
export default requestRoute;