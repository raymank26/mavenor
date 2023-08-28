FROM --platform=linux/x86_64 openjdk:17-alpine
LABEL maintainer=antonermak
COPY mavenor mavenor/
ENV JAVA_OPTS "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5555"
ENTRYPOINT ["./mavenor/bin/mavenor"]
EXPOSE 8080
