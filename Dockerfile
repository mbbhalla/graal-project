FROM oracle/graalvm-ce:latest
COPY build/libs/graal-project.jar graal-project.jar
CMD java ${JAVA_OPTS} -jar graal-project.jar

