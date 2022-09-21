#!/bin/bash -e

declare -A packageTypes=(["apk"]="assemble" ["aab"]="bundle")
declare -A availableVariants=(["release"]=0 ["beta"]=1 ["debug"]=2)

packageType=${PACKAGE_TYPE,,}
variant=${VARIANT,,}

if ! [[ -n "${packageTypes[$packageType]}" ]]; then
  echo "Invalid packageType, possible values are 'apk' or 'aab'"
  exit 1
fi

if ! [[ -n "${availableVariants[$variant]}" ]]; then
  echo "Invalid variant, possible values are 'release', 'beta' or 'debug'"
  exit 1
fi

compilationRunner="${packageTypes[$packageType]}${variant^}"
compilationLog="${GITHUB_WORKSPACE}/build.log"

bash ./gradlew :app:$compilationRunner --no-daemon 2>&1 | tee ${compilationLog}

echo ::set-output name=build-log-file-path::"${compilationLog}"

packageTypePath="${packageTypes[$packageType]}"
if [[ "${packageType}" == "apk" ]]; then
  packageTypePath="${packageType}"
fi

packageFilePath="${GITHUB_WORKSPACE}/app/build/outputs/${packageTypePath}/${variant}/app-${variant}.${packageType}"

if ! test -f "$packageFilePath"; then
  echo "Package file in ${packageFilePath} no exists"
  exit 1
else
  echo ::set-output name=package-file-path::"${packageFilePath}"
fi

mappingPath="${GITHUB_WORKSPACE}/app/build/outputs/mapping/${variant}"
mappingFilePath="${mappingPath}/mapping.txt"

if ! test -f "$mappingFilePath"; then
  mappingFilePath=""
fi

echo ::set-output name=mapping-path::"${mappingPath}"
echo ::set-output name=mapping-file-path::"${mappingFilePath}"

echo ::set-output name=whatsnew-path::"${GITHUB_WORKSPACE}/assets/whatsnew/"

exit 0
