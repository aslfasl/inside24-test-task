FROM openjdk:11
ADD target/task-0.0.1-SNAPSHOT.jar task-0.0.1-SNAPSHOT.jar
EXPOSE 6060
ENTRYPOINT ["java", "-jar", "task-0.0.1-SNAPSHOT.jar"]

