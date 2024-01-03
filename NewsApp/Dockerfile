# Use an official OpenJDK runtime as a parent image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the application JAR file into the container at the working directory
COPY out/artifacts/NewsApp_jar/NewsApp.jar .

# Specify the command to run on container startup
CMD ["java", "-jar", "NewsApp.jar"]

