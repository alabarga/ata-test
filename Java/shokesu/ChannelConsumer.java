package com.shokesu.channel.consumer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtubereporting.model.Report;
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
import com.shokesu.collector.entities.ChannelHistoryItem;
import com.shokesu.collector.utils.apis.PrivateApisUtils;
import com.shokesu.collector.utils.apis.PrivateYoutubeApi;
import com.shokesu.collector.utils.elastic.ElasticClient;
import com.shokesu.collector.utils.mongo.MongoUtils;
import com.shokesu.collector.utils.rabbit.ElasticProperties;
import com.shokesu.collector.utils.tools.Cons;
import com.shokesu.collector.utils.tools.DateUtils;
import com.shokesu.providers.AnalyticsProvider;
import com.shokesu.providers.FacebookProvider;
import com.shokesu.providers.GooglePlusProvider;
import com.shokesu.providers.InstagramProvider;
import com.shokesu.providers.ProviderClass;
import com.shokesu.providers.TwitterProvider;
import com.shokesu.providers.YoutubeProvider;
import com.shokesu.providers.YoutubeReport;
import com.shokesu.providers.YoutubeReportingUtils;

public class ChannelConsumer {

  private static String QUEUE_NAME = "";
  private final static String QUEUE_NAME_PUBLISH = "channels_updater";

  private static String provider = "twitter";

  final static Logger log = LoggerFactory.getLogger(ChannelConsumer.class);

  private static MongoCollection<Document> channelsCollection;
  private static MongoCollection<Document> privatePageApis;
  private static Connection connection;
  private static Connection connectionPublish;
  private static Connection connectionPublishConsumer;
  private static Channel rabbitChannel;
  private static Channel channelPublish;
  private static Channel channelPublishSameToConsumer;
  private static int prefetchCount = 1;
  private static BulkProcessor bulkProcessor;

  private static MongoUtils mongo;

  public static void main(String[] args) {

    provider = args[0];
    QUEUE_NAME = "requester.channel." + provider;

    mongo = new MongoUtils();
    channelsCollection = mongo.getChannelsCollection();
    privatePageApis = mongo.getCollection("private_page_apis");

    final ElasticProperties elasticProperties = new ElasticProperties();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(elasticProperties.user);
    factory.setPassword(elasticProperties.userPassword);
    factory.setHost(elasticProperties.rabbitIp);
    factory.setPort(elasticProperties.rabbitPort);

    try {
      final ProviderClass consumer;
      switch (provider) {
      case "twitter":
        consumer = new TwitterProvider(true, "channel");
        break;
      case "youtube":
        consumer = new YoutubeProvider(true, "channel");
        break;
      case "googleplus":
        consumer = new GooglePlusProvider(true, "channel");
        break;
      case "facebook":
        consumer = new FacebookProvider(true, "channel");
        break;
      case "instagram":
        consumer = new InstagramProvider(true, "channel");
        break;
      case "analytics":
        consumer = new AnalyticsProvider(true, "channel");
        break;
      default:
        consumer = new TwitterProvider(true, "channel");
        break;
      }

      connection = factory.newConnection();
      connectionPublish = factory.newConnection();
      connectionPublishConsumer = factory.newConnection();

      connection.addShutdownListener(new ShutdownListener() {
        public void shutdownCompleted(ShutdownSignalException cause) {
          log.error("Error, la conexion se ha cortado: " + cause.getMessage());
          System.exit(0);
        }
      });

      connectionPublish.addShutdownListener(new ShutdownListener() {
        public void shutdownCompleted(ShutdownSignalException cause) {
          log.error("Error, la conexion se ha cortado: " + cause.getMessage());
          System.exit(0);
        }
      });

      connectionPublishConsumer.addShutdownListener(new ShutdownListener() {
        public void shutdownCompleted(ShutdownSignalException cause) {
          log.error("Error, la conexion se ha cortado: " + cause.getMessage());
          System.exit(0);
        }
      });

      channelPublish = connectionPublish.createChannel();
      channelPublish.basicQos(prefetchCount);

      channelPublishSameToConsumer = connectionPublish.createChannel();
      channelPublishSameToConsumer.basicQos(prefetchCount);

      rabbitChannel = connection.createChannel();
      rabbitChannel.basicQos(prefetchCount);

      Map<String, Object> arguments = new HashMap<String, Object>();
      arguments.put("x-max-priority", 2);
      rabbitChannel.queueDeclare(QUEUE_NAME, true, false, false, arguments);

      channelPublish.queueDeclare(QUEUE_NAME_PUBLISH, true, false, false, arguments);

      channelPublishSameToConsumer.queueDeclare(QUEUE_NAME, true, false, false, arguments);

      Client client = ElasticClient.getInstance().getClient();

      bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
        public void beforeBulk(long executionId, BulkRequest request) {
        }

        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        }

        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

        }
      }).setBulkActions(1000).setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
          .setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(1).build();

      Consumer consumerRabbit = new DefaultConsumer(rabbitChannel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
            AMQP.BasicProperties properties, byte[] body) throws IOException {
          log.debug("leyendo mensaje");
          try {

            ArrayList<JSONObject> profileList = new ArrayList<JSONObject>();
            JSONObject messageObject = new JSONObject(new String(body, "UTF-8"));

            JSONArray ids = messageObject.getJSONArray("ids");
            try {
              JSONObject resultProfile = null;
              if (messageObject.has("isProtected") && messageObject.getBoolean("isProtected")) {
                Document privateApi = privatePageApis
                    .find(new Document(Cons.CHANNELPROVIDERID, ids.getString(0))).first();

                if (privateApi == null) {
                  // intento buscar por página
                  privateApi = privatePageApis
                      .find(new Document(Cons.PAGES_ADMIN + "." + Cons.CHANNELPROVIDERID,
                          ids.getString(0)))
                      .first();
                }
                if (privateApi == null) {
                  throw new Exception();
                }

                Document credentialsDocument = (Document) privateApi.get(Cons.CREDENTIALS);
                JSONObject credentialsObject = new JSONObject(credentialsDocument.toJson());

                resultProfile = consumer.getProfileInfo(credentialsObject);
                if (resultProfile != null) {
                  profileList.add(resultProfile);
                } else if (resultProfile == null && "instagram".equals(provider)) {
                  ArrayList<String> instagramProfile = new ArrayList<String>();
                  instagramProfile.add(ids.getString(0));
                  updateElastic(instagramProfile);
                }
              } else {

                if ("twitter".equals(provider)) {
                  profileList = consumer.searchProfileByIds(ids, consumer.getCredentialsVariable());
                } else if ("analytics".equals(provider)) {
                  JSONObject channelObject = consumer.searchProfileById(ids.getString(0),
                      new JSONObject());
                  if (channelObject != null) {
                    profileList.add(channelObject);
                  }
                } else {
                  if ("facebook".equals(provider) && ids.optJSONObject(0) != null) {
                    String groupId = ids.optJSONObject(0).getString("id");
                    // resultProfile = consumer.searchProfileByIdGroup(groupId,
                    // consumer.getCredentialsVariable());

                  } else {
                    resultProfile = consumer.searchProfileById(ids.getString(0),
                        consumer.getCredentialsVariable());
                  }

                  if (resultProfile != null) {
                    profileList.add(resultProfile);
                  } else if (resultProfile == null && "instagram".equals(provider)) {
                    ArrayList<String> instagramProfile = new ArrayList<String>();
                    instagramProfile.add(ids.getString(0));
                    updateElastic(instagramProfile);
                  }
                }
              }
            } catch (Exception e) {
              log.error(e.getMessage());
              e.printStackTrace();
              // publicar uno a uno
              if (ids.length() > 1) {
                for (int i = 0; i < ids.length(); i++) {
                  JSONObject rabbitMessage = new JSONObject();
                  JSONArray arrayRabbit = new JSONArray();

                  arrayRabbit.put(ids.get(i));
                  rabbitMessage.put("ids", arrayRabbit);
                  channelPublishSameToConsumer.basicPublish("", QUEUE_NAME, null,
                      rabbitMessage.toString().getBytes());
                }
              } else if (ids.length() == 1) {
                // actualizo el perfil como actualizado para que no de problemas
                Date dateWithoutTime;
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                dateWithoutTime = cal.getTime();
                if (ids.get(0) instanceof JSONObject) {
                  channelsCollection.updateOne(
                      new Document("channel_provider_id", ids.getJSONObject(0).getString("id")),
                      new Document("$set", new Document("updated", dateWithoutTime)));
                } else {
                  channelsCollection.updateOne(new Document("channel_provider_id", ids.get(0)),
                      new Document("$set", new Document("updated", dateWithoutTime)));
                }

              }

              if (!rabbitChannel.isOpen()) {
                log.error("El canal se habia cerrado");
                restartApplication();
              } else {
                rabbitChannel.basicAck(envelope.getDeliveryTag(), false);
                log.debug("Envio de ack realizado");
              }
              restartApplication();
            }

            boolean updated = checkIfHasBeenUpdated(profileList, ids, messageObject);

            if (updated) {

              JSONObject rabbitMessage = new JSONObject();
              ArrayList<String> profileIds = new ArrayList<String>();
              for (JSONObject profile : profileList) {
                Document channel = channelsCollection.find(
                    new Document("channel_provider_id", profile.getString("channel_provider_id")))
                    .first();
                profileIds.add(channel.get("_id").toString());
              }

              rabbitMessage.put("channels", profileIds);

              channelPublish.basicPublish("", QUEUE_NAME_PUBLISH, null,
                  rabbitMessage.toString().getBytes());

            }

            switch (provider) {
            case "twitter":
              Thread.sleep(6000);
              break;
            case "youtube":
              Thread.sleep(10000);
              break;
            case "googleplus":
              Thread.sleep(25000);
              break;
            case "facebook":
              Thread.sleep(30000);
              break;
            case "instagram":
              Thread.sleep(150000);
              break;
            case "analytics":
              Thread.sleep(20000);
              break;
            default:
              Thread.sleep(60000);
              break;
            }
          } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            restartApplication();
          } finally {
            if (!rabbitChannel.isOpen()) {
              log.error("El canal se habia cerrado");
              restartApplication();
            } else {
              rabbitChannel.basicAck(envelope.getDeliveryTag(), false);
              log.debug("Envio de ack realizado");
            }
          }
        }

        private void updateElastic(ArrayList<String> instagramProfile) throws IOException {

          JSONObject rabbitMessage = new JSONObject();
          ArrayList<String> profileIds = new ArrayList<String>();
          for (String channelProviderid : instagramProfile) {
            Document channel = channelsCollection
                .find(new Document("channel_provider_id", channelProviderid)).first();
            profileIds.add(channel.get("_id").toString());
          }

          rabbitMessage.put("channels", profileIds);

          channelPublish.basicPublish("", QUEUE_NAME_PUBLISH, null,
              rabbitMessage.toString().getBytes());
        }

        private boolean checkIfHasBeenUpdated(ArrayList<JSONObject> profileList, JSONArray ids,
            JSONObject messageObject) throws JSONException, IOException, ParseException {
          boolean hasChangesGlobal = false;
          boolean hasChanges = false;

          for (JSONObject profile : profileList) {
            Document channel = channelsCollection
                .find(new Document("channel_provider_id", profile.getString("channel_provider_id")))
                .first();
            if (channel == null) {
              continue;
            }
            if ((channel.containsKey("name") && channel.get("name") != null && profile.has("name")
                && profile.opt("name") != null
                && !channel.getString("name").equals(profile.getString("name")))
                || (!channel.containsKey("name") && profile.has("name")
                    && profile.opt("name") != null)) {
              channel.put("name", profile.getString("name"));
              hasChanges = true;
            }

            if ((channel.containsKey("username") && channel.get("username") != null
                && profile.has("username") && profile.opt("username") != null
                && !channel.getString("username").equals(profile.getString("username")))
                || (!channel.containsKey("username") && profile.has("username")
                    && profile.opt("username") != null)) {
              channel.put("username", profile.getString("username"));
              hasChanges = true;
            }

            if ((channel.containsKey("description") && channel.get("description") != null
                && profile.has("description") && profile.opt("description") != null
                && !channel.getString("description").equals(profile.getString("description")))
                || (!channel.containsKey("description") && profile.has("description")
                    && profile.opt("description") != null)) {
              channel.put("description", profile.getString("description"));
              hasChanges = true;
            }

            if ((channel.containsKey("photo") && channel.get("photo") != null
                && profile.has("photo") && profile.opt("photo") != null
                && !channel.getString("photo").equals(profile.getString("photo")))
                || (!channel.containsKey("username") && profile.has("username")
                    && profile.opt("username") != null)) {
              channel.put("photo", profile.getString("photo"));
              hasChanges = true;
            }
            if (channel.containsKey("url") && channel.get("url") != null && profile.has("url")
                && profile.opt("url") != null
                && !channel.getString("url").equals(profile.getString("url"))
                || (!channel.containsKey("url") && profile.has("url")
                    && profile.opt("url") != null)) {
              channel.put("url", profile.getString("url"));
              hasChanges = true;
            }

            if ((channel.containsKey("followers_count") && channel.get("followers_count") != null
                && profile.has("followers_count") && profile.opt("followers_count") != null
                && channel.get("followers_count") != profile.get("followers_count"))
                || (!channel.containsKey("followers_count") && profile.has("followers_count")
                    && profile.opt("followers_count") != null)) {
              channel.put("followers_count", profile.getLong("followers_count"));
              hasChanges = true;
            }
            if ((channel.containsKey("friends_count") && channel.get("friends_count") != null
                && profile.has("friends_count") && profile.opt("friends_count") != null
                && channel.get("friends_count") != profile.get("friends_count"))
                || (!channel.containsKey("friends_count") && profile.has("friends_count")
                    && profile.opt("friends_count") != null)) {
              channel.put("friends_count", profile.getLong("friends_count"));
              hasChanges = true;
            }
            if ((channel.containsKey("listed_count") && channel.get("listed_count") != null
                && profile.has("listed_count") && profile.opt("listed_count") != null
                && channel.get("listed_count") != profile.get("listed_count"))
                || (!channel.containsKey("listed_count") && profile.has("listed_count")
                    && profile.opt("listed_count") != null)) {
              channel.put("listed_count", profile.getLong("listed_count"));
              hasChanges = true;
            }

            if ((channel.containsKey("verified") && channel.get("verified") != null
                && profile.has("verified") && profile.opt("verified") != null
                && channel.get("verified") != profile.get("verified"))
                || (!channel.containsKey("verified") && profile.has("verified")
                    && profile.opt("verified") != null)) {
              if (!"".equals(profile.get("verified"))) {
                channel.put("verified", profile.optBoolean("verified"));
                hasChanges = true;
              }
            }

            if ((channel.containsKey("favourites_count") && channel.get("favourites_count") != null
                && profile.has("favourites_count") && profile.opt("favourites_count") != null
                && channel.get("favourites_count") != profile.get("favourites_count"))
                || (!channel.containsKey("favourites_count") && profile.has("favourites_count")
                    && profile.opt("favourites_count") != null)) {
              channel.put("favourites_count", profile.getLong("favourites_count"));
              hasChanges = true;
            }
            if ((channel.containsKey("posts_count") && channel.get("posts_count") != null
                && profile.has("posts_count") && profile.opt("posts_count") != null
                && channel.get("posts_count") != profile.get("posts_count"))
                || (!channel.containsKey("posts_count") && profile.has("posts_count")
                    && profile.opt("posts_count") != null)) {
              channel.put("posts_count", profile.getLong("posts_count"));
              hasChanges = true;
            }

            if ((channel.containsKey("created_at") && channel.get("created_at") != null
                && profile.has("created_at") && profile.opt("created_at") != null
                && channel.get("created_at") != profile.get("created_at"))
                || (!channel.containsKey("created_at") && profile.has("created_at")
                    && profile.opt("created_at") != null)) {
              channel.put("created_at", Integer.parseInt(profile.get("created_at").toString()));
              hasChanges = true;
            }

            if ((channel.containsKey("email") && channel.get("email") != null
                && profile.has("email") && profile.opt("email") != null
                && !channel.get("email").equals(profile.get("email")))
                || (!channel.containsKey("email") && profile.has("email")
                    && profile.opt("email") != null)) {
              channel.put("email", profile.getString("email"));
              hasChanges = true;
            }

            if ((channel.containsKey("owner") && channel.get("owner") != null
                && profile.has("owner") && profile.opt("owner") != null
                && channel.get("owner").toString().equals(profile.get("owner")))
                || (!channel.containsKey("owner") && profile.has("owner")
                    && profile.opt("owner") != null)) {
              channel.put("owner", profile.getJSONObject("owner"));
              hasChanges = true;
            }

            Date dateWithoutTime;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateWithoutTime = cal.getTime();
            System.out.println(dateWithoutTime); // TODO: Remove

            ObjectId objectId = channel.getObjectId("_id");
            if (hasChanges) {
              channel.put("updated", dateWithoutTime);
              channel.remove("_id");
              channelsCollection.updateOne(new Document("_id", objectId),
                  new Document("$set", channel));
              hasChanges = false;
              hasChangesGlobal = true;
            } else {
              channelsCollection.updateOne(new Document("_id", objectId),
                  new Document("$set", new Document("updated", dateWithoutTime)));
              hasChanges = false;
            }

            Map<String, ChannelHistoryItem> channelHistoryItems = new HashMap<String, ChannelHistoryItem>();
            ChannelHistoryItem channelHistoryItem = new ChannelHistoryItem(objectId,
                dateWithoutTime);
            channelHistoryItems.put(dateWithoutTime.toString(), channelHistoryItem);

            channelHistoryItem.setFollowersCount(channel.get("followers_count"));
            channelHistoryItem.setFriendsCount(channel.get("friends_count"));
            channelHistoryItem.setFavouriteCount(channel.get("favourites_count"));
            channelHistoryItem.setPostCount(channel.get("posts_count"));
            if (channel.containsKey("reply_count")) {
              channelHistoryItem.setReplyCount(channel.get("reply_count"));
            }
            if (channel.containsKey("view_count")) {
              channelHistoryItem.setViewCount(channel.get("view_count"));
            }

            switch (channel.getString("provider")) {
            case "youtube":
              try {
                String lastUserActivityReportCreationDate = null;
                String lastTrafficSourcesRepportsCreationDate = null;
                String lastDemographicsReportsCreationDate = null;
                String lastPlaybackLocationsReportsCreationDate = null;

                YoutubeReportingUtils youtubeReportingUtils = new YoutubeReportingUtils(
                    channel.getString("channel_provider_id"));
                if (youtubeReportingUtils.hasCredentials()) {
                  System.out.println("Retrieving reports from Youtube...");
                  List<Report> userActivityReports = youtubeReportingUtils
                      .getReports("channel_basic_a2", "User activity");
                  System.out.println(userActivityReports.size() + " user activity reports found");
                  for (Report report : userActivityReports) {
                    lastUserActivityReportCreationDate = report.getCreateTime();
                    System.out.println("User activity report creation time: "
                        + lastUserActivityReportCreationDate);
                    Thread.sleep(1000); // TODO: Evitar consumir la cuota
                    System.out.println("Date from report: " + report.getStartTime()); // TODO:
                    // Remove
                    Date channelHistoryDate = DateUtils.fromStringTimeToDate(report.getStartTime());

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(channelHistoryDate);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                    calendar.getTime();
                    channelHistoryDate = new Date(calendar.getTimeInMillis());

                    System.out.println("Date for the channel history item: " + channelHistoryDate);
                    // TODO:
                    // Remove
                    YoutubeReport youtubeReport = youtubeReportingUtils
                        .getReportFromUrl(report.getDownloadUrl());

                    ChannelHistoryItem channelHistoryItemForReports = channelHistoryItems
                        .get(channelHistoryDate.toString());
                    if (channelHistoryItemForReports == null) {
                      channelHistoryItemForReports = new ChannelHistoryItem(objectId,
                          channelHistoryDate);
                      channelHistoryItems.put(channelHistoryDate.toString(),
                          channelHistoryItemForReports);
                    }

                    Document metrics = channelHistoryItemForReports.getMetrics();
                    Document youtubeUserActivity = new Document();
                    youtubeUserActivity.put("views", youtubeReport.sumColumnAsInt("views"));
                    youtubeUserActivity.put("comments", youtubeReport.sumColumnAsInt("comments"));
                    youtubeUserActivity.put("likes", youtubeReport.sumColumnAsInt("likes"));
                    youtubeUserActivity.put("dislikes", youtubeReport.sumColumnAsInt("dislikes"));
                    youtubeUserActivity.put("shares", youtubeReport.sumColumnAsInt("shares"));
                    youtubeUserActivity.put("watch_time_minutes",
                        youtubeReport.sumColumnAsDouble("watch_time_minutes"));
                    youtubeUserActivity.put("average_view_duration_seconds",
                        youtubeReport.averageColumnAsDouble("average_view_duration_seconds"));
                    youtubeUserActivity.put("average_view_duration_percentage",
                        youtubeReport.averageColumnAsDouble("average_view_duration_percentage"));
                    metrics.put("youtubeUserActivity", youtubeUserActivity);

                    Document youtubeUserActivityByCountry = new Document();
                    { // views
                      Map<String, Integer> averageViewDurationPercentageByCountry = youtubeReport
                          .sumColumnAsIntFilteredByField("views", "country_code");
                      for (String country : averageViewDurationPercentageByCountry.keySet()) {
                        Document countryDocument = (Document) youtubeUserActivityByCountry
                            .getOrDefault(country.toLowerCase(), new Document());
                        countryDocument.put("views",
                            averageViewDurationPercentageByCountry.get(country));
                        youtubeUserActivityByCountry.put(country.toLowerCase(), countryDocument);
                      }
                    }
                    { // comments
                      Map<String, Integer> averageViewDurationPercentageByCountry = youtubeReport
                          .sumColumnAsIntFilteredByField("comments", "country_code");
                      for (String country : averageViewDurationPercentageByCountry.keySet()) {
                        Document countryDocument = (Document) youtubeUserActivityByCountry
                            .getOrDefault(country.toLowerCase(), new Document());
                        countryDocument.put("comments",
                            averageViewDurationPercentageByCountry.get(country));
                        youtubeUserActivityByCountry.put(country.toLowerCase(), countryDocument);
                      }
                    }
                    { // likes
                      Map<String, Integer> averageViewDurationPercentageByCountry = youtubeReport
                          .sumColumnAsIntFilteredByField("likes", "country_code");
                      for (String country : averageViewDurationPercentageByCountry.keySet()) {
                        Document countryDocument = (Document) youtubeUserActivityByCountry
                            .getOrDefault(country.toLowerCase(), new Document());
                        countryDocument.put("likes",
                            averageViewDurationPercentageByCountry.get(country));
                        youtubeUserActivityByCountry.put(country.toLowerCase(), countryDocument);
                      }
                    }
                    { // dislikes
                      Map<String, Integer> averageViewDurationPercentageByCountry = youtubeReport
                          .sumColumnAsIntFilteredByField("dislikes", "country_code");
                      for (String country : averageViewDurationPercentageByCountry.keySet()) {
                        Document countryDocument = (Document) youtubeUserActivityByCountry
                            .getOrDefault(country.toLowerCase(), new Document());
                        countryDocument.put("dislikes",
                            averageViewDurationPercentageByCountry.get(country));
                        youtubeUserActivityByCountry.put(country.toLowerCase(), countryDocument);
                      }
                    }
                    { // shares
                      Map<String, Integer> averageViewDurationPercentageByCountry = youtubeReport
                          .sumColumnAsIntFilteredByField("shares", "country_code");
                      for (String country : averageViewDurationPercentageByCountry.keySet()) {
                        Document countryDocument = (Document) youtubeUserActivityByCountry
                            .getOrDefault(country.toLowerCase(), new Document());
                        countryDocument.put("shares",
                            averageViewDurationPercentageByCountry.get(country));
                        youtubeUserActivityByCountry.put(country.toLowerCase(), countryDocument);
                      }
                    }
                    { // watch_time_minutes
                      Map<String, Double> averageViewDurationPercentageByCountry = youtubeReport
                          .sumColumnAsDoubleFilteredByField("watch_time_minutes", "country_code");
                      for (String country : averageViewDurationPercentageByCountry.keySet()) {
                        Document countryDocument = (Document) youtubeUserActivityByCountry
                            .getOrDefault(country.toLowerCase(), new Document());
                        countryDocument.put("watch_time_minutes",
                            averageViewDurationPercentageByCountry.get(country));
                        youtubeUserActivityByCountry.put(country.toLowerCase(), countryDocument);
                      }
                    }

                    metrics.put("youtubeUserActivityByCountry", youtubeUserActivityByCountry);
                    channelHistoryItemForReports.setMetrics(metrics);
                  }

                  Thread.sleep(10000); // TODO: Evitar consumir la cuota

                  {
                    List<Report> trafficSourcesRepports = youtubeReportingUtils
                        .getReports("channel_traffic_source_a2", "Traffic sources");
                    System.out
                        .println(trafficSourcesRepports.size() + " traffic sources reports found");
                    for (Report report : trafficSourcesRepports) {
                      System.out.println("Date from report: " + report.getStartTime()); // TODO:
                                                                                        // Remove
                      lastTrafficSourcesRepportsCreationDate = report.getCreateTime();
                      System.out.println("Traffic sources report creation time: "
                          + lastTrafficSourcesRepportsCreationDate);
                      Thread.sleep(1000); // TODO: Evitar consumir la cuota
                      Date channelHistoryDate = DateUtils
                          .fromStringTimeToDate(report.getStartTime());

                      Calendar calendar = Calendar.getInstance();
                      calendar.setTime(channelHistoryDate);
                      calendar.set(Calendar.HOUR_OF_DAY, 0);
                      calendar.set(Calendar.MINUTE, 0);
                      calendar.set(Calendar.SECOND, 0);
                      calendar.set(Calendar.MILLISECOND, 0);
                      calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                      calendar.getTime();
                      channelHistoryDate = new Date(calendar.getTimeInMillis());

                      System.out
                          .println("Date for the channel history item: " + channelHistoryDate); // TODO:
                      // Remove
                      YoutubeReport youtubeReport = youtubeReportingUtils
                          .getReportFromUrl(report.getDownloadUrl());

                      ChannelHistoryItem channelHistoryItemForReports = channelHistoryItems
                          .get(channelHistoryDate.toString());
                      if (channelHistoryItemForReports == null) {
                        channelHistoryItemForReports = new ChannelHistoryItem(objectId,
                            channelHistoryDate);
                        channelHistoryItems.put(channelHistoryDate.toString(),
                            channelHistoryItemForReports);
                        System.out.println("not found");
                      } else {
                        System.out.println("found");
                      }

                      Document metrics = channelHistoryItemForReports.getMetrics();
                      Document youtubeTrafficSources = new Document();

                      { // views
                        Map<String, Integer> viewsByTrafficSource = youtubeReport
                            .sumColumnAsIntFilteredByField("views", "traffic_source_type");
                        for (String trafficSourceCode : viewsByTrafficSource.keySet()) {
                          String trafficSource = YoutubeReportingUtils
                              .getTrafficSourceNameFromCode(trafficSourceCode);
                          Document trafficSourceDocument = (Document) youtubeTrafficSources
                              .getOrDefault(trafficSource, new Document());
                          trafficSourceDocument.put("views",
                              viewsByTrafficSource.get(trafficSourceCode));
                          youtubeTrafficSources.put(trafficSource, trafficSourceDocument);
                        }
                      }
                      { // watch_time_minutes
                        Map<String, Double> watchTimeMinutesByTrafficSource = youtubeReport
                            .sumColumnAsDoubleFilteredByField("watch_time_minutes",
                                "traffic_source_type");
                        for (String trafficSourceCode : watchTimeMinutesByTrafficSource.keySet()) {
                          String trafficSource = YoutubeReportingUtils
                              .getTrafficSourceNameFromCode(trafficSourceCode);
                          Document trafficSourceDocument = (Document) youtubeTrafficSources
                              .getOrDefault(trafficSource, new Document());
                          trafficSourceDocument.put("watch_time_minutes",
                              watchTimeMinutesByTrafficSource.get(trafficSourceCode));
                          youtubeTrafficSources.put(trafficSource, trafficSourceDocument);
                        }
                      }

                      metrics.put("youtubeTrafficSources", youtubeTrafficSources);
                      channelHistoryItemForReports.setMetrics(metrics);

                    }
                  }

                  Thread.sleep(10000); // TODO: Evitar consumir la cuota

                  {
                    List<Report> playbackLocationsReports = youtubeReportingUtils
                        .getReports("channel_playback_location_a2", "Playback locations");
                    System.out.println(
                        playbackLocationsReports.size() + " playback locations reports found");
                    for (Report report : playbackLocationsReports) {
                      Thread.sleep(1000); // TODO: Evitar consumir la cuota
                      System.out.println("Date from report: " + report.getStartTime()); // TODO:
                                                                                        // Remove
                      lastPlaybackLocationsReportsCreationDate = report.getCreateTime();
                      System.out.println("Playback locations reportt creation time: "
                          + lastPlaybackLocationsReportsCreationDate);
                      Thread.sleep(1000); // TODO: Evitar consumir la cuota
                      Date channelHistoryDate = DateUtils
                          .fromStringTimeToDate(report.getStartTime());

                      Calendar calendar = Calendar.getInstance();
                      calendar.setTime(channelHistoryDate);
                      calendar.set(Calendar.HOUR_OF_DAY, 0);
                      calendar.set(Calendar.MINUTE, 0);
                      calendar.set(Calendar.SECOND, 0);
                      calendar.set(Calendar.MILLISECOND, 0);
                      calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                      calendar.getTime();
                      channelHistoryDate = new Date(calendar.getTimeInMillis());

                      System.out
                          .println("Date for the channel history item: " + channelHistoryDate); // TODO:
                      // Remove
                      YoutubeReport youtubeReport = youtubeReportingUtils
                          .getReportFromUrl(report.getDownloadUrl());

                      ChannelHistoryItem channelHistoryItemForReports = channelHistoryItems
                          .get(channelHistoryDate.toString());
                      if (channelHistoryItemForReports == null) {
                        channelHistoryItemForReports = new ChannelHistoryItem(objectId,
                            channelHistoryDate);
                        channelHistoryItems.put(channelHistoryDate.toString(),
                            channelHistoryItemForReports);
                        System.out.println("not found");
                      } else {
                        System.out.println("found");
                      }

                      Document metrics = channelHistoryItemForReports.getMetrics();
                      Document youtubePlaybackLocations = new Document();

                      { // views
                        Map<String, Integer> viewsByTrafficSource = youtubeReport
                            .sumColumnAsIntFilteredByField("views", "playback_location_type");
                        for (String playbackLocationCode : viewsByTrafficSource.keySet()) {
                          String playbackLocation = YoutubeReportingUtils
                              .getPlaybackLocationNameFromCode(playbackLocationCode);
                          Document trafficSourceDocument = (Document) youtubePlaybackLocations
                              .getOrDefault(playbackLocation, new Document());
                          trafficSourceDocument.put("views",
                              viewsByTrafficSource.get(playbackLocationCode));
                          youtubePlaybackLocations.put(playbackLocation, trafficSourceDocument);
                        }
                      }
                      { // watch_time_minutes
                        Map<String, Double> watchTimeMinutesByTrafficSource = youtubeReport
                            .sumColumnAsDoubleFilteredByField("watch_time_minutes",
                                "playback_location_type");
                        for (String playbackLocationCode : watchTimeMinutesByTrafficSource
                            .keySet()) {
                          String playbackLocation = YoutubeReportingUtils
                              .getPlaybackLocationNameFromCode(playbackLocationCode);
                          Document trafficSourceDocument = (Document) youtubePlaybackLocations
                              .getOrDefault(playbackLocation, new Document());
                          trafficSourceDocument.put("watch_time_minutes",
                              watchTimeMinutesByTrafficSource.get(playbackLocationCode));
                          youtubePlaybackLocations.put(playbackLocation, trafficSourceDocument);
                        }
                      }

                      metrics.put("youtubePlaybackLocations", youtubePlaybackLocations);
                      channelHistoryItemForReports.setMetrics(metrics);

                    }
                  }

                  Thread.sleep(10000); // TODO: Evitar consumir la cuota

                  {
                    List<Report> demographicsReports = youtubeReportingUtils
                        .getReports("channel_demographics_a1", "Demographics");
                    System.out.println(demographicsReports.size() + " demographics reports found");
                    for (Report report : demographicsReports) {
                      Thread.sleep(1000); // TODO: Evitar consumir la cuota
                      System.out.println("Date from report: " + report.getStartTime()); // TODO:
                                                                                        // Remove
                      lastDemographicsReportsCreationDate = report.getCreateTime();
                      System.out.println("Demographics report creation time: "
                          + lastDemographicsReportsCreationDate);
                      Date channelHistoryDate = DateUtils
                          .fromStringTimeToDate(report.getStartTime());

                      Calendar calendar = Calendar.getInstance();
                      calendar.setTime(channelHistoryDate);
                      calendar.set(Calendar.HOUR_OF_DAY, 0);
                      calendar.set(Calendar.MINUTE, 0);
                      calendar.set(Calendar.SECOND, 0);
                      calendar.set(Calendar.MILLISECOND, 0);
                      calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                      calendar.getTime();
                      channelHistoryDate = new Date(calendar.getTimeInMillis());

                      System.out
                          .println("Date for the channel history item: " + channelHistoryDate); // TODO:
                      // Remove
                      YoutubeReport youtubeReport = youtubeReportingUtils
                          .getReportFromUrl(report.getDownloadUrl());

                      ChannelHistoryItem channelHistoryItemForReports = channelHistoryItems
                          .get(channelHistoryDate.toString());
                      if (channelHistoryItemForReports == null) {
                        channelHistoryItemForReports = new ChannelHistoryItem(objectId,
                            channelHistoryDate);
                        channelHistoryItems.put(channelHistoryDate.toString(),
                            channelHistoryItemForReports);
                        System.out.println("not found");
                      } else {
                        System.out.println("found");
                      }

                      Document metrics = channelHistoryItemForReports.getMetrics();
                      Document youtubeDemographicsByGender = new Document();
                      Document youtubeDemographicsByAge = new Document();

                      // TODO: %
                      { // gender views
                        Map<String, Integer> viewsByTrafficSource = youtubeReport
                            .sumColumnAsIntFilteredByField("views", "gender");
                        for (String gender : viewsByTrafficSource.keySet()) {
                          Document trafficSourceDocument = (Document) youtubeDemographicsByGender
                              .getOrDefault(gender, new Document());
                          trafficSourceDocument.put("views", viewsByTrafficSource.get(gender));
                          youtubeDemographicsByGender.put(gender, trafficSourceDocument);
                        }
                      }
                      { // age_group views
                        Map<String, Integer> viewsByTrafficSource = youtubeReport
                            .sumColumnAsIntFilteredByField("views", "age_group");
                        for (String age_group : viewsByTrafficSource.keySet()) {
                          Document trafficSourceDocument = (Document) youtubeDemographicsByAge
                              .getOrDefault(age_group, new Document());
                          trafficSourceDocument.put("views", viewsByTrafficSource.get(age_group));
                          youtubeDemographicsByAge.put(age_group, trafficSourceDocument);
                        }
                      }
                      { // gender watch_time_minutes
                        Map<String, Double> watchTimeMinutesByTrafficSource = youtubeReport
                            .sumColumnAsDoubleFilteredByField("watch_time_minutes", "gender");
                        for (String gender : watchTimeMinutesByTrafficSource.keySet()) {
                          Document trafficSourceDocument = (Document) youtubeDemographicsByGender
                              .getOrDefault(gender, new Document());
                          trafficSourceDocument.put("watch_time_minutes",
                              watchTimeMinutesByTrafficSource.get(gender));
                          youtubeDemographicsByGender.put(gender, trafficSourceDocument);
                        }
                      }
                      { // age_group watch_time_minutes
                        Map<String, Double> watchTimeMinutesByTrafficSource = youtubeReport
                            .sumColumnAsDoubleFilteredByField("watch_time_minutes", "age_group");
                        for (String age_group : watchTimeMinutesByTrafficSource.keySet()) {
                          Document trafficSourceDocument = (Document) youtubeDemographicsByGender
                              .getOrDefault(age_group, new Document());
                          trafficSourceDocument.put("watch_time_minutes",
                              watchTimeMinutesByTrafficSource.get(age_group));
                          youtubeDemographicsByGender.put(age_group, trafficSourceDocument);
                        }
                      }

                      metrics.put("youtubeDemographicsByGender", youtubeDemographicsByGender);
                      metrics.put("youtubeDemographicsByAge", youtubeDemographicsByAge);
                      channelHistoryItemForReports.setMetrics(metrics);
                    }
                  }

                  // Saving information about the last processed repots
                  PrivateYoutubeApi youtubeApi = youtubeReportingUtils.getApi();
                  if (lastUserActivityReportCreationDate != null) {
                    youtubeApi.setLastReportCreationTime("channel_basic_a2", "User activity",
                        lastUserActivityReportCreationDate);
                  }
                  if (lastTrafficSourcesRepportsCreationDate != null) {
                    youtubeApi.setLastReportCreationTime("channel_traffic_source_a2",
                        "Traffic sources", lastTrafficSourcesRepportsCreationDate);
                  }
                  if (lastPlaybackLocationsReportsCreationDate != null) {
                    youtubeApi.setLastReportCreationTime("channel_playback_location_a2",
                        "Playback locations", lastPlaybackLocationsReportsCreationDate);
                  }
                  if (lastDemographicsReportsCreationDate != null) {
                    youtubeApi.setLastReportCreationTime("channel_demographics_a1", "Demographics",
                        lastDemographicsReportsCreationDate);
                  }
                  youtubeApi.setUpdatedNow();
                  PrivateApisUtils.update(youtubeApi);
                }
              } catch (Exception e) {
                log.error(e.toString());
                // TODO: Hacer esto bien, de momento es para que no pete con el problema de Google y
                // el error 500
              }
              break;
            case "analytics":
              String privatePageApiId = messageObject.getString("privatePageApiId");

              Document privatePageApi = privatePageApis
                  .find(new Document("_id", new ObjectId(privatePageApiId))).first();

              Date dateWithoutTimeAnalytics;
              Calendar cal2 = Calendar.getInstance();

              cal2.set(Calendar.HOUR_OF_DAY, 0);
              cal2.set(Calendar.MINUTE, 0);
              cal2.set(Calendar.SECOND, 0);
              cal2.set(Calendar.MILLISECOND, 0);
              cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
              dateWithoutTimeAnalytics = cal2.getTime();
              System.out.println(dateWithoutTime);

              // for (JSONObject profileObject : profileList) {
              consumer.setInsightData(profile.getString("channel_provider_id"), privatePageApi,
                  dateWithoutTimeAnalytics, "today", bulkProcessor);
              log.debug(
                  "TODAY para el channel provider Id: " + profile.getString("channel_provider_id"));
              // }

              privatePageApis.updateOne(new Document("_id", new ObjectId(privatePageApiId)),
                  new Document("$set", new Document("updated", dateWithoutTime)));

              cal2.add(Calendar.DATE, -1);
              dateWithoutTimeAnalytics = cal2.getTime();

              // for (JSONObject profileObject : profileList) {
              consumer.setInsightData(profile.getString("channel_provider_id"), privatePageApi,
                  dateWithoutTimeAnalytics, "yesterday", bulkProcessor);
              log.debug("YESTERDAY para el channel provider Id: "
                  + profile.getString("channel_provider_id"));
              // }

              break;
            }

            // Storing into Elastic and Mongo
            for (Entry<String, ChannelHistoryItem> entry : channelHistoryItems.entrySet()) {
              entry.getValue().save(bulkProcessor);
            }
          }

          return hasChangesGlobal;
        }

        // private void insertDocument(Document document, String type, String mongoId)
        // throws JSONException, IOException {
        // IndexRequest indexRequest = new IndexRequest(elasticProperties.elasticIndex,
        // type.toLowerCase(), mongoId);
        // log.debug(document.toString());
        // log.debug("mongoId: " + mongoId);
        //
        // Document elasticDocument = new ElasticChannelHistories(document).build();
        // indexRequest.source(ContentUtils.convertToString(elasticDocument));
        // bulkProcessor.add(indexRequest);
        // }
      };
      rabbitChannel.basicConsume(QUEUE_NAME, false, consumerRabbit);
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
      e.printStackTrace();
      System.exit(0);
    }
  }
}
