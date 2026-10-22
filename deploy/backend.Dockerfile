# 多阶段构建：第一阶段编译（利用依赖缓存层），第二阶段只保留 JRE 与 jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY backend/pom.xml ./pom.xml
COPY backend/ ./
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app && apk add --no-cache wget
WORKDIR /app
COPY --from=build /src/co-bootstrap/target/co-bootstrap-*.jar app.jar
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -Duser.timezone=UTC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
