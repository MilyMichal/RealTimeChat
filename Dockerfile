FROM openjdk:17-alpine
WORKDIR /app
COPY build/libs/RealTimeChat-0.0.1-SNAPSHOT.jar app.jar
COPY profilePic /app/profilePic
ENTRYPOINT ["java","-jar","app.jar"]