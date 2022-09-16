#!/bin/bash -e

declare -A packageTypes=(["apk"]="assemble" ["aab"]="bundle")
declare -A availableVariants=(["release"]=0 ["beta"]=1 ["debug"]=2)

packageType=${1,,}
variant=${2,,}

if ! [[ -n "${packageTypes[$packageType]}" ]]; then
  echo "Invalid packageType, possible values are 'apk' or 'aab'"
  exit 1
fi

if ! [[ -n "${availableVariants[$variant]}" ]]; then
  echo "Invalid variant, possible values are 'release', 'beta' or 'debug'"
  exit 1
fi

compilationRunner="${packageTypes[$packageType]}${variant^}"

bash ./gradlew :app:$compilationRunner

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

mappingFilePath="${GITHUB_WORKSPACE}/app/build/outputs/mapping/${variant}/mapping.txt"

if ! test -f "$mappingFilePath"; then
  mappingFilePath=""
fi

echo ::set-output name=mapping-file-path::"${mappingFilePath}"

exit 0
