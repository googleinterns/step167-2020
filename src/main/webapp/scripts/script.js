// When the HTML file using this script loads all of its content, including
// images, CSS, scripts.
window.onload = function() {
  initApp();
};

// Set up listeners for button clicks, authentication state change (user signs
// in or out). The auth state listener is called whenever user signs in or out &
// updates the UI.
function initApp() {
  document.getElementById('login').addEventListener('click', toggleLogin);
  document.getElementById('signup').addEventListener('click', onSignup);

  firebase.auth().onAuthStateChanged(function(user) {
    // User is signed in.
    if (user) {
      document.getElementById('login').textContent = 'Log out';
      document.getElementById('login-status').textContent = 'Signed in.';
    } else {  // User signed out.
      document.getElementById('login').textContent = 'Log in';
      document.getElementById('login-status').textContent = 'Signed out.';
    }
    document.getElementById('login').disabled = false;
  });
}

function toggleLogin() {
  // If the user is signed in, sign them out.
  if (firebase.auth().currentUser) {
    firebase.auth().signOut();
  } else {  // Sign in.
    var email = document.getElementById('email').value;
    var password = document.getElementById('password').value;

    if (email == null || email.length < 2) {
      alert('Please enter an email address.');
      return;
    }
    if (password == null || password.length < 2) {
      alert('Please enter a password.');
      return;
    }

    firebase.auth()
        .signInWithEmailAndPassword(email, password)
        .then(function onSuccess() {
          // TODO: send token to server
          firebase.auth()
              .currentUser.getIdToken(/* forceRefresh */ true)
              .then(function(idToken) {
                // Body type defaults to JSON.
                fetch('/login', {method: 'POST', body: idToken});
              })
              .catch(function(error) {
                alert('Token retrieval failed.');
              });
        })
        .catch(function onFailure(error) {
          var errorCode = error.code;
          var errorMessage = error.message;

          if (errorCode === 'auth/wrong-passcode') {
            alert('Wrong password.');
          } else {
            alert(errorMessage);
          }
        });
    // document.getElementById("login").disabled = true;
  }
}

function onSignup() {
  // TODO
}
