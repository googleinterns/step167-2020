function getRecipeList() {  // eslint-disable-line no-unused-vars
  fetch('/api/post').then((response) => response.json()).then((recipes) => {
    const recipeEl = document.getElementById('recipe-list');
    removeAllChildNodes(recipeEl);
    recipes.forEach((recipe) => {
      recipeEl.appendChild(createRecipeListElement(recipe));
    });
  });
}

function getDetailedRecipe() {  // eslint-disable-line no-unused-vars
  fetch('/api/post?recipeID=zElvE9jKb1BX2CDN608O')
      .then((response) => response.json())
      .then((recipe) => {
        const recipeEl = document.getElementById('recipe');
        removeAllChildNodes(recipeEl);
        recipeEl.appendChild(createRecipeListElement(recipe));
      });
}

function getComments() {  // eslint-disable-line no-unused-vars
}

function removeAllChildNodes(parent) {
  while (parent.firstChild) {
    parent.removeChild(parent.firstChild);
  }
}

function createRecipeListElement(recipe) {
  const recipeElement = document.createElement('div');
  recipeElement.appendChild(createBasicElement(recipe.title, 'h4'));
  recipeElement.appendChild(createBasicElement(recipe.content, 'p'));
  return recipeElement;
}

function createBasicElement(text, type) {
  const basicElement = document.createElement(type);
  if (text != '') basicElement.innerText = text;
  return basicElement;
}
