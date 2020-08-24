import React from 'react';

const Feed = React.lazy(() => import('./views/Feed'));
const Recipe = React.lazy(() => import('./views/Recipe'));
const AddRecipe = React.lazy(() => import('./views/AddRecipe'));

const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/popular', name: 'Popular', component: Feed, props: { feedType: 'popular', mapMode: false } },
  { path: '/new', name: 'New', component: Feed, props: { feedType: 'new', mapMode: false } },
  { path: '/recipe', name: 'Recipe', component: Recipe },
  { path: '/addrecipe', name: 'Add Recipe', component: AddRecipe },
  { path: '/map', name: 'Recipe Map', component: Feed, props: { feedType: 'popular', mapMode: true } },
];

export default routes;
