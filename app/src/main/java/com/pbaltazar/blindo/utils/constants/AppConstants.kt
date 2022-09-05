package com.pbaltazar.blindo.utils.constants

import com.pbaltazar.blindo.BuildConfig

// Authentication
const val ACCOUNT_TYPE = "com.blindoapp"

// Notifications
const val IS_SUBSCRIBED_TO_TOPIC_PREFIX = "isSubscribedTo"
const val VISION_NOTIFICATION_CHANNEL = "visionNotificationChannel"
const val NEWS_NOTIFICATION_CHANNEL = "newsNotificationChannel"
const val PURCHASES_NOTIFICATION_CHANNEL = "purchasesNotificationChannel"

// Ads
const val NO_PERSONALIZED_ADS = "npa"
const val NO_PERSONALIZED_ADS_YES = "1"
const val NO_PERSONALIZED_ADS_NO = "0"
const val ADS_CLIENT_ID = "com.google.android.gms.ads.MobileAds"
const val ADS_CONSENT_STATUS = "adsConsentStatus"
const val DEFAULT_ADS_CONSENT_STATUS = "UNKNOWN"

// Tutorial and splash screen
const val IS_FIRST_TIME = "isFirstTime"
const val IS_PRIVACY_POLICY_ACCEPTED = "isPrivacyPolicyAccepted"
const val IS_VISION_INTRODUCED = "isVisionIntroduced"
const val IS_PUSH_NOTIFICATIONS_CONFIGURED = "isPushNotificationsConfigured"

// Blindo Vision
const val VISION_LANGUAGE = "vision__language"
const val VISION_AUTO_DISCARD_NOTIFICATIONS = "vision__autoDiscardNotifications"

// Fragments and dialogs arguments
const val AUTH_CANCELED_ON_DIALOG = "authCanceledOnDialog"
const val ARGUMENT_CONSENT_STATUS = "consentStatus"
const val ARGUMENT_QUERY = "query"
const val ARGUMENT_REQUIRE_REFRESH_FILTERS = "requireRefreshFilters"
const val ARGUMENT_APP = "app"
const val ARGUMENT_PACK = "pack"
const val ARGUMENT_RATING = "rating"

// Labels packs
const val DOWNLOADS_DIR = "downloads"
const val LABELS_PROVIDER = "${BuildConfig.APPLICATION_ID}.providers.LabelsProvider"

// Talkback
const val TALKBACK_PACKAGE = "com.google.android.marvin.talkback"
const val TALKBACK_IMPORT_LABELS = "com.google.android.accessibility.talkback.labeling.LabelImportActivity"
const val TALKBACK_LABELS_ARRAY = "labels_array"
const val TALKBACK_ARRAY_PACKAGE_NAME = "package_name"
const val TALKBACK_ARRAY_PACKAGE_VERSION = "package_version"
const val TALKBACK_ARRAY_PACKAGE_SIGNATURE = "package_signature"
const val TALKBACK_ARRAY_VIEW_NAME = "view_name"
const val TALKBACK_ARRAY_LABEL_TEXT = "label_text"
const val TALKBACK_ARRAY_LOCALE = "locale"

// Legal
const val TERMS_AND_CONDITIONS_LINK = "https://blindoapp.com/terms-and-conditions"
