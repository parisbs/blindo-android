#!/bin/bash -e

secretsXmlPath="${GITHUB_WORKSPACE}/app/src/main/res/values/secrets.xml"

echo "Generating Firebase UI Auth secrets in ${secretsXmlPath}"
touch ${secretsXmlPath}
echo '<?xml version="1.0" encoding="utf-8"?>' >> ${secretsXmlPath}
echo '<resources>' >> ${secretsXmlPath}
echo '    <string name="facebook_application_id" translatable="false">${FACEBOOK_APPLICATION_ID}</string>' >> ${secretsXmlPath}
echo '    <string name="fb_login_protocol_scheme" translatable="false">${FB_LOGIN_PROTOCOL_SCHEME}</string>' >> ${secretsXmlPath}
echo '    <string name="twitter_consumer_key" translatable="false">${TWITTER_CONSUMER_KEY}</string>' >> ${secretsXmlPath}
echo '    <string name="twitter_consumer_secret" translatable="false">${TWITTER_CONSUMER_SECRET}</string>' >> ${secretsXmlPath}
echo '</resources>' >> ${secretsXmlPath}

echo "Done"
