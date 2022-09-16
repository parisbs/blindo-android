#!/bin/bash -e

googleServicesJsonPath="${GITHUB_WORKSPACE}/app/google-services.json"

echo "Generating Google Services configuration file in ${googleServicesJsonPath}"
echo ${GOOGLE_SERVICES_JSON} | base64 --decode > ${googleServicesJsonPath}

echo "Done"
exit 0
