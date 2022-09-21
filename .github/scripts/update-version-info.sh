#!/bin/bash -e

declare -A versionNameUpdateLevels=(["major"]=0 ["minor"]=1 ["patch"]=2)

variant=${VARIANT,,}
shouldUpdateVersionName="${SHOULD_UPDATE_VERSION_NAME}"
versionNameUpdateLevel=${VERSION_NAME_UPDATE_LEVEL,,}
versionFile="${GITHUB_WORKSPACE}/version.gradle"

echo "Verifying current version info"

mapfile -t < "${versionFile}"

IFS=":" read -a previousVersionCodeArray <<<${MAPFILE[2]}
previousVersionCode=${previousVersionCodeArray[1]:1:-1}

IFS=":" read -a previousVersionNameArray <<<${MAPFILE[3]}
previousVersionName=${previousVersionNameArray[1]:2:-1}

echo ::set-output name=previous-version-code::"${previousVersionCode}"
echo ::set-output name=previous-version-name::"${previousVersionName}"

if [[ "${variant}" == "debug" ]]; then
  echo "Debug variants should not be updated"
  echo ::set-output name=updated-version-code::""
  echo ::set-output name=updated-version-name::""
  echo ::set-output name=version-info-commit-msg::""
  echo "Done"
  exit 0
fi

echo "Updating version info"

if [[ "${shouldUpdateVersionName}" == true ]]; then
  if ! [[ -n "${versionNameUpdateLevels[$versionNameUpdateLevel]}" ]]; then
    echo "Invalid versionNameUpdateLevel, possible values are 'security', 'minor' or 'major'"
    exit 1
  fi
else
  echo ::set-output name=updated-version-code::""
  echo ::set-output name=updated-version-name::""
  echo ::set-output name=version-info-commit-msg::""
  echo "Done"
  exit 0
fi

updatedVersionCode="$((previousVersionCode+1))"

IFS="." read -a versionNameArray <<<${previousVersionName}
majorLevel=${versionNameArray[0]}
minorLevel=${versionNameArray[1]}
patchLevel=${versionNameArray[2]}
if [[ "${versionNameUpdateLevel}" == "major" ]]; then
  majorLevel=$((majorLevel+1))
  minorLevel=0
  patchLevel=0
elif [[ "${versionNameUpdateLevel}" == "minor" ]]; then
  minorLevel=$((minorLevel+1))
  patchLevel=0
else
  patchLevel=$((patchLevel+1))
fi
updatedVersionName="${majorLevel}.${minorLevel}.${patchLevel}"

rm -rf ${versionFile}

touch ${versionFile}
echo "ext {" >> ${versionFile}
echo "    blindo = [" >> ${versionFile}
echo "        versionCode: ${updatedVersionCode}," >> ${versionFile}
echo "        versionName: \"${updatedVersionName}\"" >> ${versionFile}
echo "    ]" >> ${versionFile}
echo "}" >> ${versionFile}

echo ::set-output name=updated-version-code::"${updatedVersionCode}"
echo ::set-output name=updated-version-name="${updatedVersionName}"

commitMsg="${variant~} version cut ${updatedVersionName} build ${updatedVersionCode}"
echo ::set-output name=version-info-commit-msg::"${commitMsg}"

echo "Done"
exit 0
