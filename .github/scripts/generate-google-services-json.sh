#!/bin/bash -e

serviceAccountPath="${GITHUB_WORKSPACE}/service-account.json"
googleServicesJsonPath="${GITHUB_WORKSPACE}/app/google-services.json"

echo ${PLAY_CONSOLE_SERVICE_ACCOUNT} | base64 --decode > ${serviceAccountPath}
echo ::set-output name=service-account-json::"${serviceAccountPath}"

echo "Generating Google Services configuration file in ${googleServicesJsonPath}"
echo ${GOOGLE_SERVICES_JSON} | base64 --decode > ${googleServicesJsonPath}

echo "Done"
exit 0
