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
