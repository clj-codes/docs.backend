# Dockerfile for fly.io deploy

FROM openjdk:11-slim-buster AS runtime
COPY service.jar /app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=90 -XshowSettings:vm"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
