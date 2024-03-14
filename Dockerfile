FROM openjdk:17
VOLUME /tmp
EXPOSE 10001
ADD user-0.0.1-SNAPSHOT.jar webapp.jar
RUN bash -c 'mkdir -p /pic/user'
RUN bash -c 'mkdir /pic/cover'
RUN bash -c 'mkdir /video'
ENTRYPOINT ["java","-jar","/webapp.jar"]
