package com.example.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaProducerApp {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = KafkaProducerConfig.getProducerProperties();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String topic = "stg_rise_db_created_order_information";

        int i = 0;
        long dateNum = 11111111;
        String orderNumber = "514523-" + dateNum + "-00000101";
        while (true) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, orderNumber, orderInfo(dateNum));
            System.out.println("Producing batch: " + i);
            producer.send(record);
            i += 1;
            dateNum++;
        }
    }

    private static String orderInfo(long dateNum) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));

        String formattedDate = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String orderInfo = "{\"resourceId\":\"514523-" + dateNum + "-00000101\",\"action\":\"CREATE\",\"body\":{\"orderNumber\":\"514523-" + dateNum + "-00000101\",\"orderDate\":\"" + formattedDate + "\",\"shop\":{\"shopId\":514523,\"shopUrl\":\"globalfpd01\"}}}";
        return orderInfo;
    }
}
