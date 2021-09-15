# Blindo App for Android
Blindo is the first and the biggest labels repository for Android screenreaders in the world, we work as a social app where anybody can collaborate uploading new labels for screenreaders and all these labels can be downloaded with just one click, also the collaborators can rate the usability and accessibility of any Android app to share with the world the best accessible apps and allow the blind community to use Android easier.  
Keep your screenreaders labels safe with our unlimited and free labels backup to avoid missing labels when you change your device, let us analice your installed apps to create a custom labels pack with all available labels for you and forget all the headeaches that the unlabeled buttons give.  
Translate labels of other users to your language and grow up your collection to have the better accessibility experience in Android and colaborate rating the usability and accessibility level of your Android apps to help other blind users to find the best apps.  
Join us now and be an important colaborator of the biggest accessibility community for Android.

## Setup project
1. On Android Studio open an existing project selecting the build.gradle file in the root project path
2. Create the file app/src/main/res/values/secrets.xml with the following untranslatable strings:
  * facebook_application_id
  * fb_login_protocol_scheme
  * twitter_consumer_key
  * twitter_consumer_secret
  * admob_application_id
  * admob__main_banner
  * admob__upload_pack_banner
  * admob__install_pack_intersticial
3. Copy and rename the file env.properties.example to env.properties and set the values for all the environment variables
4. Copy and rename the file keystore.properties.example to keystore.properties and set all the values, ensure that you use a keystore authorized on the Firebase project
5. Add the file app/google-services.json, you can download this file from the Firebase project
6. Build or run the app
