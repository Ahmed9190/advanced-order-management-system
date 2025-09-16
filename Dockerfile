# Use Eclipse Temurin JRE 17 as the base image (production-optimized)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
  adduser -u 1001 -S appuser -G appgroup

COPY target/order-service-*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT [ "java", "-jar", "app.jar" ]