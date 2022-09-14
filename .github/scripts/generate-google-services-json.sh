#!/bin/bash -e

googleServicesJsonPath="${GITHUB_WORKSPACE}/app/google-services.json"

echo "Generating Google Services configuration file in ${googleServicesJsonPath}"
touch ${googleServicesJsonPath}
echo '${GOOGLE_SERVICES_JSON}' >> ${googleServicesJsonPath}

echo "Done"
