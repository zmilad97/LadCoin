FROM openjdk:16

RUN uname -srm
RUN apt update && apt install -y maven
COPY . /project
RUN  cd /project && mvn package

#run the spring boot application
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dblabla", "-jar","/project/target/demo-0.0.1-SNAPSHOT.jar"]


