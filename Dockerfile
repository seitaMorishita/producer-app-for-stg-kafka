FROM maven:3.8.1-openjdk-11 AS build

WORKDIR /app

COPY pom.xml /app/
RUN mvn dependency:go-offline

COPY src /app/src

RUN mvn clean package

#FROM openjdk:11-jre-slim
FROM registry-jpe2.r-local.net/ccbd-sens-batch-order-miguel/openjdk@sha256:285c61a1e5e6b7b3709729b69558670148c5fdc6eb7104fae7dd370042c51430 as package

WORKDIR /app

COPY --from=build /app/target/producer-app-for-stg-kafka-1.0-SNAPSHOT.jar /app/producer-app-for-stg-kafka.jar

# Kerberos認証の設定ファイルをコピー
COPY kerberos-config/ /app/kerberos-config/

# ログディレクトリを作成
RUN mkdir -p /usr/local/logs

# アプリケーションを実行
CMD ["java", "-Djava.security.auth.login.config=/app/kerberos-config/kafka_client_jaas.conf", \
             "-Djava.security.krb5.conf=/app/kerberos-config/krb5.conf", \
             "-jar", "producer-app-for-stg-kafka.jar"]