/**
 * When the HTML file using this script loads all of its content, including
 * images, CSS, scripts.
 */
window.onload = function() {
  initApp();
};

/**
 * Set up listeners for button clicks, authentication state change (user signs
 * in or out). The auth state listener is called whenever user signs in or out &
 * updates the UI.
 */
function initApp() {
  document.getElementById('login').addEventListener('click', toggleLogin);
  document.getElementById('signup').addEventListener('click', onSignup);
  document.getElementById('password-reset').addEventListener('click', sendPasswordResetEmail);

  firebase.auth().onAuthStateChanged(function(user) {
    // User is signed in.
    if (user) {
      var displayName = user.displayName;
      var email = user.email;
      var emailVerified = user.emailVerified;
      var photoURL = user.photoURL;
      var isAnonymous = user.isAnonymous;
      var uid = user.uid;
      var providerData = user.providerData;

      document.getElementById('login').textContent = 'Log out';
      document.getElementById('login-status').textContent = 'Signed in.';
    } else {  // User signed out.
      document.getElementById('login').textContent = 'Log in';
      document.getElementById('login-status').textContent = 'Signed out.';
    }
    document.getElementById('login').disabled = false;
  });
}

/** 
 * If user is currently logged in, log them out; if logged out, log them in.
 */
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
        .catch(function onFailure(error) {
          var errorCode = error.code;
          var errorMessage = error.message;

          if (errorCode === 'auth/wrong-passcode') {
            alert('Wrong password.');
          } else {
            alert(errorMessage);
          }
        });
  }
}

/**
 * Attempt to create a new user account using the email/password HTML fields.
 */
function onSignup() {
  var email = document.getElementById("email").value;
  var password = document.getElementById("password").value;

  if (email == null || email.length < 2) {
    alert("Please enter a valid email address.");
  }
  if (password == null || password.length < 2) {
    alert("Please enter a valid password.");
  }
  
  // If email/pass are of appropriate length, try to create a new account.
  firebase.auth().createUserWithEmailAndPassword(email, password)
      .catch(function onFailure(error) {
        var errorCode = error.code;
        var errorMessage = error.message;

        if (errorCode == 'auth/weak-password') {
          alert('The password is too weak. Passwords must be at least 6 characters long.');
        } else if (errorCode == 'auth/invalid-email') {
          alert('Please enter a valid email address.');
        } else if (errorCode == 'auth/email-already-in-use') {
          alert('This email address is already associated with a user.');
        } else { alert(errorMessage); }
  });
}

function sendPasswordResetEmail() {
  var email = document.getElementById("email").value;
    
  firebase.auth().sendPasswordResetEmail(email).then(function onSuccess() {
    alert('Password Reset Email Sent!');
    }).catch(function(error) {
      // Handle errors.
      var errorCode = error.code;
      var errorMessage = error.message;
    
      if (errorCode == 'auth/invalid-email') {
        alert("Please enter a valid email address.");
      } else if (errorCode == 'auth/user-not-found') {
        alert("This user was not found.");
      } else { alert(errorMessage); }
    });
}