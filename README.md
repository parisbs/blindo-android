# Blindo App for Android

## Status

This repository is published as **historical open source code**. The project has been **discontinued**, no active development or support is provided, and there are no guarantees of any kind. It is released under the GNU Affero General Public License v3.0 (or any later version) so that its code remains useful to the community for educational, accessibility, and research purposes.

Contributions are not actively reviewed. Forks and derivative works are welcome under the terms of the AGPL-3.0-or-later license (see [LICENSE](LICENSE) and [NOTICE](NOTICE)).

## What's Blindo?

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

## License

Copyright (C) 2022-2026 Paris N. Baltazar Salguero

This program is free software: you can redistribute it and/or modify it under the terms of the **GNU Affero General Public License v3.0** as published by the Free Software Foundation. See the [LICENSE](LICENSE) file for the full license text.

Third-party components used by this project retain their own licenses. See the [NOTICE](NOTICE) file for details.
