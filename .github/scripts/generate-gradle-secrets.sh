#!/bin/bash -e

localPropertiesPath="${GITHUB_WORKSPACE}/local.properties"

echo "Generating local properties file in ${localPropertiesPath}"
touch ${localPropertiesPath}
echo "ACCOUNT_TYPE=\"com.blindoapp\"" >> ${localPropertiesPath}
echo "BLINDO_API_URL=\"${BLINDO_API_URL}\"" >> ${localPropertiesPath}
echo "ADMOB_PUBLISHER_ID=\"${ADMOB_PUBLISHER_ID}\"" >> ${localPropertiesPath}
echo "admobApplicationId=\"${ADMOB_APPLICATION_ID}\"" >> ${localPropertiesPath}
echo "ADMOB_MAIN_BANNER=\"${ADMOB_MAIN_BANNER}\"" >> ${localPropertiesPath}
echo "ADMOB_UPLOAD_PACK_BANNER=\"${ADMOB_UPLOAD_PACK_BANNER}\"" >> ${localPropertiesPath}

echo "Done"
exit 0
