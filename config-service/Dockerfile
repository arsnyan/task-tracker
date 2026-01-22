FROM alpine:3.21
RUN apk add --no-cache gcompat libstdc++
COPY build/native/nativeCompile/config-service /app/config-service
EXPOSE 8888
ENTRYPOINT["/app/config-service"]