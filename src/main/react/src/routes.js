import React from 'react';

const Feed = React.lazy(() => import('./views/Feed'));
const Recipe = React.lazy(() => import('./views/Recipe'));
const AddRecipe = React.lazy(() => import('./views/AddRecipe'));
const Profile = React.lazy(() => import('./views/Profile'));

const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/popular', name: 'Popular', component: Feed, props: { feedType: 'popular' } },
  { path: '/new', name: 'New', component: Feed, props: { feedType: 'new' } },
  { path: '/recipe', name: 'Recipe', component: Recipe },
  { path: '/addrecipe', name: 'Add Recipe', component: AddRecipe },
  { path: '/profile', name: 'Profile', component: Profile },
];

export default routes;
