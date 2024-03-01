FROM openjdk:17
VOLUME /tmp
EXPOSE 10001
ADD user-0.0.1-SNAPSHOT.jar webapp.jar
RUN bash -c 'touch /webapp.jar'
ENTRYPOINT ["java","-jar","/webapp.jar"]
