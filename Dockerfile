FROM openjdk:11-jre-slim

WORKDIR /app

# Mavenビルドの成果物をコピー
COPY target/producer-app-for-stg-kafka-1.0-SNAPSHOT.jar /app/producer-app-for-stg-kafka.jar

# Kerberos認証の設定ファイルをコピー
COPY kerberos-config/ /app/kerberos-config/

# ログディレクトリを作成
RUN mkdir -p /usr/local/logs

# アプリケーションを実行
CMD ["java", "-Djava.security.auth.login.config=/app/kerberos-config/kafka_client_jaas.conf", \
             "-Djava.security.krb5.conf=/app/kerberos-config/krb5.conf", \
             "-jar", "producer-app-for-stg-kafka.jar"]