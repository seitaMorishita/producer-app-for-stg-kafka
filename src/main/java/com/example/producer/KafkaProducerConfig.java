package com.example.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaProducerConfig {
    public static Properties getProducerProperties() {

//        String bootstrapServers = "host.docker.internal:9093";
        String bootstrapServers = "omnia-ccbd-sens-kafka-sens-kafka-stg01.kaas.jpe2b.dcnw.rakuten:9092,omnia-ccbd-sens-kafka-sens-kafka-stg02.kaas.jpe2b.dcnw.rakuten:9092";
        String saslMechanism = "GSSAPI";
        String keyTabPath = "/app/kerberos-config/batch-order-miguel-stg.keytab";
        String principal = "batch-order-miguel-stg@KAFKA200.GEAP.RAKUTEN.COM";
        String serviceName = "sens-kafka-stg";
        String securityProtocol = "SASL_PLAINTEXT";
        String jaasConfig = String.format(
                "com.sun.security.auth.module.Krb5LoginModule required " +
                        "useKeyTab=true " +
                        "storeKey=true " +
                        "keyTab=\"%s\" " +
                        "principal=\"%s\" " +
                        "serviceName=\"%s\";",
                keyTabPath, principal, serviceName
        );

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        props.put("security.protocol",securityProtocol);
        return props;
    }
}
