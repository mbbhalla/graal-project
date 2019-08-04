./gradlew clean jar

native-image \
    -H:ReflectionConfigurationFiles=reflection-config.json \
    -H:ResourceConfigurationFiles=resources-config.json \
    -jar build/libs/bootstrap.jar  \
    --no-server \
    --enable-http --enable-https --enable-url-protocols=http,https \
    -Djava.net.preferIPv4Stack=true

zip deployment.zip bootstrap
