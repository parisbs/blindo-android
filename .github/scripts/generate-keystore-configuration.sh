#!/bin/bash -e

storeFilePath="${GITHUB_WORKSPACE}/${STORE_FILE}"
debugStoreFilePath="${GITHUB_WORKSPACE}/debug_${STORE_FILE}"

echo "Generating store file in ${storeFilePath}"
echo ${ENCODED_STORE} | base64 --decode > ${storeFilePath}
echo "Generating debug store file in ${debugStoreFilePath}"
echo ${DEBUG_ENCODED_STORE} | base64 --decode > ${debugStoreFilePath}

keystorePropertiesPath="${GITHUB_WORKSPACE}/keystore.properties"

echo "Generating keystore properties in ${keystorePropertiesPath}"
touch ${keystorePropertiesPath}
echo "DEBUG_STORE_FILE=${debugStoreFilePath}" >> ${keystorePropertiesPath}
echo "DEBUG_STORE_PASSWORD=${DEBUG_STORE_PASSWORD}" >> ${keystorePropertiesPath}
echo "DEBUG_KEY_ALIAS=${DEBUG_KEY_ALIAS}" >> ${keystorePropertiesPath}
echo "DEBUG_KEY_PASSWORD=${DEBUG_KEY_PASSWORD}" >> ${keystorePropertiesPath}
echo "STORE_FILE=${storeFilePath}" >> ${keystorePropertiesPath}
echo "STORE_PASSWORD=${STORE_PASSWORD}" >> ${keystorePropertiesPath}
echo "KEY_ALIAS=${KEY_ALIAS}" >> ${keystorePropertiesPath}
echo "KEY_PASSWORD=${KEY_PASSWORD}" >> ${keystorePropertiesPath}

echo "Done"
exit 0
