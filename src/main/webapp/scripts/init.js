/**
 * Script to initialize Firebase on the client side.
 * Should be used in the landing page's HTML.
 */

// Your web app's Firebase configuration
var firebaseConfig = {
  apiKey: 'AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc',
  authDomain: 'meltingpot-step-2020.firebaseapp.com',
  databaseURL: 'https://meltingpot-step-2020.firebaseio.com',
  projectId: 'meltingpot-step-2020',
  storageBucket: 'meltingpot-step-2020.appspot.com',
  messagingSenderId: '495013387451',
  appId: '1:495013387451:web:9827c75184cf5510749d9e',
  measurementId: 'G-8YRW8QZ2XG',
}
                     // Initialize Firebase
                     firebase.initializeApp(firebaseConfig)
firebase.analytics()
firebase.auth()