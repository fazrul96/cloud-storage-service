FROM openjdk:17-jdk-slim

LABEL maintainer="Fazrul" \
      version="1.0" \
      description="Cloud Storage application container"

# Install dependencies for AWS CLI v2 and networking tools
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    groff \
    less \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install AWS CLI v2
RUN curl --fail -L "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o awscliv2.zip \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf awscliv2.zip

RUN aws --version

# Runtime arguments
ARG ACTIVE_PROFILE
ARG AWS_DEFAULT_REGION
ARG AWS_BUCKET_NAME
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

# Environment variables (can also be overridden at runtime)
ENV ACTIVE_PROFILE=${ACTIVE_PROFILE}
ENV AWS_DEFAULT_REGION=${AWS_DEFAULT_REGION}
ENV AWS_BUCKET_NAME=${AWS_BUCKET_NAME}
ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
ENV S3_FILE_KEY=common-config/spring-boot/$ACTIVE_PROFILE/deployment.properties

RUN mkdir -p /app/common-config/

WORKDIR /app

RUN aws s3 cp s3://$AWS_BUCKET_NAME/$S3_FILE_KEY /app/$S3_FILE_KEY

COPY build/libs/webtoon-service.jar app.jar

#COPY build/common-config app/common-config/

EXPOSE 8084

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$ACTIVE_PROFILE -jar app.jar"]
