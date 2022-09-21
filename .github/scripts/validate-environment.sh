#!/bin/bash -e

releaseTracks=("production" "beta" "internal")
betaTracks=("beta" "internal")
declare -A variantTracks=(["release"]=" ${releaseTracks[*]} " ["beta"]=" ${betaTracks[*]} ")

packageType=${PACKAGE_TYPE,,}
variant=${VARIANT,,}
shouldPublishInPlayConsole="${SHOULD_PUBLISH_IN_PLAY_CONSOLE}"
track=${TRACK,,}
inAppUpdatePriority=${IN_APP_UPDATE_PRIORITY}

echo "Validating ${variant} environment"
echo "Build ${packageType}"
echo "Should publish in Play Console: ${shouldPublishInPlayConsole} in track ${track} for $(bc -l <<<"${userFraction}*100")% of users with priority ${inAppUpdatePriority}"

if [[ "${variant}" == "debug" ]]; then
  if [[ "${shouldPublishInPlayConsole}" == true ]]; then
      echo "Publish in Play Console requires a non-debug artifact"
	  exit 1
  fi
fi

if [[ "${shouldPublishInPlayConsole}" == true ]]; then
  if [[ "${packageType}" == "apk" ]]; then
    echo "To publish in Play Console an AAB artifact is expected"
	exit 1
  fi
fi

if ! [[ "${variantTracks[$variant]}" =~ " ${track} " ]]; then
  echo "Invalid track for variant ${variant}, possible values are ${variantTracks[$variant]}"
  exit 1
fi

invalidInAppUpdatePriorityMessage="Invalid inAppUpdatePriority, must be a value between 0 and 5, current is ${inAppUpdatePriority}"
if [[ ${inAppUpdatePriority} > 5 ]]; then
  echo ${invalidInAppUpdatePriorityMessage}
  exit 1
fi
if [[ ${inAppUpdatePriority} < 0 ]]; then
  echo ${invalidInAppUpdatePriorityMessage}
  exit 1
fi

echo "Done"
exit 0
