

/**
 * Here is is the code snippet to initialize Firebase Messaging in the Service
 * Worker when your app is not hosted on Firebase Hosting.
*/
 // [START initialize_firebase_in_sw]
 // Give the service worker access to Firebase Messaging.
 // Note that you can only use Firebase Messaging here. Other Firebase libraries
 // are not available in the service worker.
 importScripts('https://www.gstatic.com/firebasejs/8.2.1/firebase-app.js');
 importScripts('https://www.gstatic.com/firebasejs/8.2.1/firebase-messaging.js');


 // Initialize the Firebase app in the service worker by passing in
 // your app's Firebase config object.
 // https://firebase.google.com/docs/web/setup#config-object
 firebase.initializeApp({
  apiKey: "AIzaSyAgEjZfREN0137FJoKpdzSmEQac_yGEhXU",
  authDomain: "fir-demo-84240.firebaseapp.com",
  projectId: "fir-demo-84240",
  storageBucket: "fir-demo-84240.appspot.com",
  messagingSenderId: "334600601730",
  appId: "1:334600601730:web:69d6eb0731d4703070349b",
  measurementId: "G-22NMFT1SGB"
 });
 
 // importScripts('https://www.gstatic.com/firebase/init.js');

 // Retrieve an instance of Firebase Messaging so that it can handle background
 // messages.
 const messaging = firebase.messaging();
 // [END initialize_firebase_in_sw]



// If you would like to customize notifications that are received in the
// background (Web app is closed or not in browser focus) then you should
// implement this optional method.
// [START on_background_message]
messaging.onBackgroundMessage(function(payload) {
  console.log('[firebase-messaging-sw.js] Received background message ', payload);
  // Customize notification here
  const notificationTitle = 'Background Message Title';
  const notificationOptions = {
    body: 'Background Message body.',
    icon: '/firebase-logo.png'
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
// [END on_background_message]