FROM adoptopenjdk/openjdk11:latest
ADD /build/libs/challange-0.0.1-SNAPSHOT.jar challange.jar
ENTRYPOINT ["java", "-jar", "dockerdemo.jar"]
