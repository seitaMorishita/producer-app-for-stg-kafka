FROM maven:3.8.1-openjdk-11 AS build

WORKDIR /app

COPY pom.xml /app/
RUN mvn dependency:go-offline

COPY src /app/src

RUN mvn clean package

FROM openjdk:11-jre-slim
ARG GROUP_ID=1000
ARG GROUP_NAME=bizexpuser
ARG USER_ID=1000
ARG USER_NAME=bizexpuser

RUN groupadd -g $GROUP_ID $GROUP_NAME && useradd -g $GROUP_ID -u $USER_ID -m $USER_NAME

WORKDIR /app

COPY --from=build /app/target/producer-app-for-stg-kafka-1.0-SNAPSHOT.jar /app/producer-app-for-stg-kafka.jar
RUN chown -R ${USER_ID}:${GROUP_ID} /app/producer-app-for-stg-kafka.jar


# Kerberos認証の設定ファイルをコピー
COPY kerberos-config/ /app/kerberos-config/

# ログディレクトリを作成
RUN mkdir -p /usr/local/logs
RUN chown -R ${USER_ID}:${GROUP_ID} /usr/local/logs

USER 1000

# アプリケーションを実行
CMD ["java", "-Djava.security.krb5.conf=/app/kerberos-config/krb5.conf", \
             "-jar", "producer-app-for-stg-kafka.jar"]
#FROM registry-jpe1.r-local.net/ccbd-sens-sandbox-kafka-test/docker-container/producer-app-for-stg-kafka@sha256:efd8286811eec847982e260ae8584483d2f84ac4eabdfb388fbcec60fc66ffd9