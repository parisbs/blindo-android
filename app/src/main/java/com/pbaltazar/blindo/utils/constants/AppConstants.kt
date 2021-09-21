package com.pbaltazar.blindo.utils.constants

import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.R

const val UPDATES_CHANNEL = "blindoUpdatesChannel"

const val NO_PERSONALIZED_ADS = "npa"
const val NO_PERSONALIZED_ADS_YES = "1"
const val NO_PERSONALIZED_ADS_NO = "0"

const val ADS_CLIENT_ID = "com.google.android.gms.ads.MobileAds"

const val ARGUMENTS_SHOW_NAVIGATE_UP = "showNavigateUp"
const val ARGUMENTS_CONSENT_STATUS = "consentStatus"

const val ADS_CONSENT_STATUS = "adsConsentStatus"
const val IS_FIRST_TIME = "isFirstTime"
const val IS_PRIVACY_POLICY_ACCEPTED = "isPrivacyPolicyAccepted"

const val APPS_PAGE_SIZE = "appsPageSize"
const val APP_SORT = "appsort"
const val PACKS_PAGE_SIZE = "packsPageSize"
const val PACK_SORT = "packSort"
const val COMMENTS_PAGE_SIZE = "commentsPageSize"
const val COMMENT_SORT = "commentSort"

const val DEFAULT_ADS_CONSENT_STATUS = "UNKNOWN"

const val TERMS_AND_CONDITIONS_LINK = "https://blindoapp.com/terms-and-conditions"

const val AUTH_CANCELED_ON_DIALOG = "authCanceledOnDialog"
const val REFRESH_USER_FROM_PREMIUM = "refreshUserFromPremium"

const val ARGUMENTS_QUERY = "query"
const val ARGUMENTS_APP = "app"
const val ARGUMENTS_APP_TAB = "app_tab"
val APP_DETAILS_TAB_TITLES = arrayOf(
    R.string.appdetails__details_tab,
    R.string.appdetails__packs_tab,
    R.string.appdetails__comments_tab
)

const val RECEIPT_ORIGIN_KEY = "origin"
const val RECEIPT_SKU_KEY = "sku"
const val RECEIPT_TOKEN_KEY = "token"
const val RECEIPT_PACKAGENAME_KEY = "packageName"
const val RECEIPT_PRODUCTID_KEY = "productId"
const val RECEIPT_PURCHASETOKEN_KEY = "purchaseToken"

const val ARGUMENTS_PACK = "pack"
const val ARGUMENTS_RATING = "rating"

const val INTERTITIAL_ADS_MINIMUM_VISUALIZATION = 15000L

const val DOWNLOADS_DIR = "downloads"
const val LABELS_PROVIDER = "${BuildConfig.APPLICATION_ID}.providers.LabelsProvider"
const val TALKBACK_PACKAGE = "com.google.android.marvin.talkback"
const val TALKBACK_IMPORT_LABELS = "com.google.android.accessibility.talkback.labeling.LabelImportActivity"

const val MONTHLY_SUBSCRIPTION_SKU = "blindo_monthly_subscription"

const val MANAGE_SUBS_URI = "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"

const val TALKBACK_LABELS_ARRAY = "labels_array"
const val TALKBACK_ARRAY_PACKAGE_NAME = "package_name"
const val TALKBACK_ARRAY_PACKAGE_VERSION = "package_version"
const val TALKBACK_ARRAY_PACKAGE_SIGNATURE = "package_signature"
const val TALKBACK_ARRAY_VIEW_NAME = "view_name"
const val TALKBACK_ARRAY_LABEL_TEXT = "label_text"
const val TALKBACK_ARRAY_LOCALE = "locale"
