# Blindo App for Android
![Beta deployment status](https://github.com/parisbs/blindo-android/actions/workflows/deploy-blindo-beta-version.yaml/badge.svg)

Blindo is the first and the biggest labels repository for Android screenreaders in the world, we work as a social app where anybody can collaborate uploading new labels for screenreaders and all these labels can be downloaded with just one click, also the collaborators can rate the usability and accessibility of any Android app to share with the world the best accessible apps and allow the blind community to use Android easier.  
Keep your screenreaders labels safe with our unlimited and free labels backup to avoid missing labels when you change your device, let us analice your installed apps to create a custom labels pack with all available labels for you and forget all the headeaches that the unlabeled buttons give.  
Translate labels of other users to your language and grow up your collection to have the better accessibility experience in Android and colaborate rating the usability and accessibility level of your Android apps to help other blind users to find the best apps.  
Join us now and be an important colaborator of the biggest accessibility community for Android.

## Setup project
1. On Android Studio open an existing project selecting the build.gradle file in the root project path
2. Add the following keys/values in your local.properties file, you can create a debug/beta/release properties file instead:
  * ACCOUNT_TYPE="com.blindoapp
  * BLINDO_API_URL
  * ADMOB_PUBLISHER_ID
  * admobApplicationId
  * ADMOB_MAIN_BANNER="the-unit-id-of-this-ad-banner"
  * ADMOB_UPLOAD_PACK_BANNER="the-unit-id-of-this-ad-banner"
  * TEST_DEVICE_ID="the-device-id-to-test-ad-consent"
  * DEBUG_GEOGRAPHY="DEBUG_GEOGRAPHY_EEA"
3. Create the file app/src/main/res/values/secrets.xml with the following untranslatable strings:
  * facebook_application_id
  * fb_login_protocol_scheme
  * twitter_consumer_key
  * twitter_consumer_secret
4. Copy and rename the file keystore.properties.example to keystore.properties and set all the values, ensure that you use a keystore authorized on the Firebase project
5. Add the file app/google-services.json, you can download this file from the Firebase project
6. Build or run the app
