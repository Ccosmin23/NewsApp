#FROM openjdk:11
#
#WORKDIR /app
#COPY . /app
#
#RUN javac architecture/*.java
#RUN javac Main.java
#
#CMD ["java", "architecture/*.java"]
#CMD ["java", "model/*.java"]
#CMD ["java", "repository/*.java"]
#CMD ["java", "service/*.java"]
#CMD ["java", "ui/*.java"]
#
#CMD ["java", "Main.java"]
#CMD echo "started"




# Use an official OpenJDK runtime as a parent image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the application JAR file into the container at the working directory
COPY NewsApp.jar .

# Specify the command to run on container startup
CMD ["java", "-jar", "NewsApp.jar"]
