FROM openjdk:23-jdk
VOLUME /tmp
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
WORKDIR /usr/src/app
RUN microdnf install findutils


ARG JAR_FILE=/usr/src/app/build/libs/*.jar

COPY . .
RUN ./gradlew clean
RUN ./gradlew build -x test

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/usr/src/app/build/libs/sbb-0.0.1-SNAPSHOT.jar" ]
