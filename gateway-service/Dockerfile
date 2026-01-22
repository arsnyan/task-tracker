FROM alpine:3.21
RUN apk add --no-cache gcompat libstdc++
COPY build/native/nativeCompile/gateway-service /app/gateway-service
EXPOSE 8080
ENTRYPOINT ["/app/gateway-service"]