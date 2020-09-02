import React from 'react';

const FeedWithSearch = React.lazy(() => import('./views/FeedWithSearch'));
const Recipe = React.lazy(() => import('./views/Recipe'));
const AddRecipe = React.lazy(() => import('./views/AddRecipe'));
const MapPage = React.lazy(() => import('./views/MapPage'));
const Profile = React.lazy(() => import('./views/Profile'));

const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/popular', name: 'Popular', component: FeedWithSearch, props: { feedType: 'popular' } },
  { path: '/followed', name: 'Followed', component: Feed, props: { feedType: 'followed-tags'} },
  { path: '/new', name: 'New', component: FeedWithSearch, props: { feedType: 'new' } },
  { path: '/recipe', name: 'Recipe', component: Recipe },
  { path: '/addrecipe', name: 'Add Recipe', component: AddRecipe },
  { path: '/map', name: 'Recipe Map', component: MapPage, props: { feedType: 'popular' } },
  { path: '/profile', name: 'Profile', component: Profile },
];

export default routes;
