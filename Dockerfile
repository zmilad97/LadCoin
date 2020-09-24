FROM openjdk:16-jdk-alpine 

# add directly the jar
COPY target/*.jar /app.jar

EXPOSE 8181

CMD echo "The core will start..." && \
    java -Djava.io.tmpdir=/var/tmp \
         -Djava.security.egd=file:/dev/./urandom \
         -jar /app.jar
