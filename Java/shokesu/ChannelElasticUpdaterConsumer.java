package com.shokesu.channel.elasticupdater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.shokesu.collector.utils.elastic.ElasticClient;
import com.shokesu.collector.utils.mongo.MongoUtils;
import com.shokesu.collector.utils.rabbit.ElasticProperties;
import com.shokesu.collector.utils.tools.ContentUtils;

public class ChannelElasticUpdaterConsumer {

  private final static String QUEUE_NAME = "channels_updater";

  final static Logger log = LoggerFactory.getLogger(ChannelElasticUpdaterConsumer.class);

  private static Connection connection;
  private static Channel channel;
  private static BulkProcessor bulkProcessor;
  private static int prefetchCount = 505;
  private static MongoUtils mongoUtils;
  private static MongoCollection<Document> channelsCollection;

  public static void main(String[] args) {

    final ElasticProperties elasticProperties = new ElasticProperties();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(elasticProperties.user);
    factory.setPassword(elasticProperties.userPassword);
    // factory.setVirtualHost(virtualHost);
    factory
        .setHost((args.length > 1 && !"".equals(args[1])) ? args[1] : elasticProperties.rabbitIp);
    factory.setPort(elasticProperties.rabbitPort);

    mongoUtils = new MongoUtils();
    channelsCollection = mongoUtils.getChannelsCollection();

    try {
      connection = factory.newConnection();

      connection.addShutdownListener(new ShutdownListener() {
        public void shutdownCompleted(ShutdownSignalException cause) {
          log.error("Error, la conexion se ha cortado: " + cause.getMessage());
          System.exit(0);
        }
      });

      channel = connection.createChannel();
      channel.basicQos(prefetchCount);
      Map<String, Object> arguments = new HashMap<String, Object>();
      arguments.put("x-max-priority", 2);
      channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);

      Client client = ElasticClient.createClient(elasticProperties);

      bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
        public void beforeBulk(long executionId, BulkRequest request) {
        }

        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        }

        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

        }
      }).setBulkActions(1000).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
          .setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(1).build();

      ArrayList<ObjectId> channelList = new ArrayList<ObjectId>();
      ArrayList<Long> ackList = new ArrayList<Long>();
      // ArrayList<Long> previousExecution = new ArrayList<Long>();
      // previousExecution.add(Calendar.getInstance().getTimeInMillis());

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
            AMQP.BasicProperties properties, byte[] body) throws IOException {

          try {

            JSONObject messageObject = new JSONObject(new String(body, "UTF-8"));

            ackList.add(envelope.getDeliveryTag());
            log.debug("Lectura del mensaje original: " + messageObject.toString());
            // leer los posts, obtenerlos de mongo y mandándolos a elastic
            JSONArray channels = messageObject.getJSONArray("channels");
            log.debug("Canales a actualizar:" + channels.length());

            // TODO: Revisar esto y evitar que se encolen tantos canales
            final int CHUNK_SIZE = 500;
            int chunks = channels.length() / CHUNK_SIZE + 1;
            for (int chunk = 0; chunk < chunks; chunk++) {
              for (int i = chunk * CHUNK_SIZE; i < channels.length()
                  && i < (chunk + 1) * CHUNK_SIZE; i++) {
                channelList.add(new ObjectId(channels.getString(i)));
              }

              // if (channelList.size() > 500
              // || previousExecution.get(0) < Calendar.getInstance().getTimeInMillis() - 5000) {

              // previousExecution.remove(0);
              // previousExecution.add(Calendar.getInstance().getTimeInMillis());
              ArrayList<Document> channelDocumentList = channelsCollection
                  .find(new Document("_id", new Document("$in", channelList)))
                  .into(new ArrayList<Document>());

              channelList.clear();
              for (Document document : channelDocumentList) {

                JSONObject userObject = (JSONObject) ContentUtils
                    .transformChannelFromMongoToElastic(document, true, true);

                insertDocument(userObject, "channels");
              }
            }
            // }
          } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            restartApplication();
          } finally {
            if (!channel.isOpen()) {
              log.error("El canal se habia cerrado");
              restartApplication();
            } else {
              if (channelList.isEmpty()) {
                for (long ack : ackList) {
                  channel.basicAck(ack, false);
                  log.debug("Envio de ack realizado");
                }
                ackList.clear();
              }
            }
          }
        }

        private void insertDocument(JSONObject document, String type) throws JSONException {
          IndexRequest indexRequest = new IndexRequest(
              elasticProperties.getSaveIndex(document, type.toLowerCase()), type.toLowerCase(),
              document.getString("mongoid"));
          log.debug(document.toString());
          log.debug(document.getString("mongoid"));
          indexRequest.source(document.toString());
          if (document.has("original_tags")) {
            log.debug(document.getJSONArray("original_tags").toString());
          }
          bulkProcessor.add(indexRequest);
        }
      };
      channel.basicConsume(QUEUE_NAME, false, consumer);
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
      restartApplication();
    }
  }

  private static void restartApplication() {
    try {
      System.exit(0);
    } catch (Exception e) {
      log.error(e.getMessage());
      System.exit(0);
    }
  }
}
