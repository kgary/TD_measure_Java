package dao;

import java.util.*;
import java.io.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;

public class KafkaDataSourceDAOImpl implements DataSourceDAO {

    private String topicName;
    private String bootstrapServers;
    private String groupId;

    public KafkaDataSourceDAOImpl(String topicName, String bootstrapServers, String groupId) {
        this.topicName = topicName;
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;        
    }

    @Override 
    public List<String> readData() throws IOException {
        List<String> messages = new ArrayList<>();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topicName));
            
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            
            for (ConsumerRecord<String, String> record : records) {
                messages.add(record.value()); 
            }
        } catch (Exception e) {
            throw new IOException("Failed to read from Kafka", e);
        }
        

        return messages;
    }
}