/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.rm.external.yarn;

import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.exception.RMErrorException;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.rm.external.domain.ExternalAppInfo;
import org.apache.linkis.manager.rm.external.domain.ExternalResourceIdentifier;
import org.apache.linkis.manager.rm.external.domain.ExternalResourceProvider;
import org.apache.linkis.manager.rm.external.request.ExternalResourceRequester;
import org.apache.linkis.manager.rm.utils.RequestKerberosUrlUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.*;

public class YarnResourceRequester implements ExternalResourceRequester {
  private static final Logger logger = LoggerFactory.getLogger(YarnResourceRequester.class);

  private final String HASTATE_ACTIVE = "active";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private ExternalResourceProvider provider = null;
  private final Map<String, String> rmAddressMap = new ConcurrentHashMap<>();

  private String getAuthorizationStr() {
    String user = (String) provider.getConfigMap().getOrDefault("user", "");
    String pwd = (String) provider.getConfigMap().getOrDefault("pwd", "");
    String authKey = user + ":" + pwd;
    return Base64.getMimeEncoder().encodeToString(authKey.getBytes());
  }

  @Override
  public NodeResource requestResourceInfo(
      ExternalResourceIdentifier identifier, ExternalResourceProvider provider) {
    String rmWebHaAddress = (String) provider.getConfigMap().get("rmWebAddress");
    this.provider = provider;
    String rmWebAddress = getAndUpdateActiveRmWebAddress(rmWebHaAddress);
    logger.info("rmWebAddress: " + rmWebAddress);
    String queueName = ((YarnResourceIdentifier) identifier).getQueueName();

    String realQueueName = "root." + queueName;

    return LinkisUtils.tryCatch(
        () -> {
          Pair<YarnResource, YarnResource> yarnResource =
              getResources(rmWebAddress, realQueueName, queueName);

          CommonNodeResource nodeResource = new CommonNodeResource();
          nodeResource.setMaxResource(yarnResource.getKey());
          nodeResource.setUsedResource(yarnResource.getValue());
          return nodeResource;
        },
        t -> {
          throw new RMErrorException(
              YARN_QUEUE_EXCEPTION.getErrorCode(), YARN_QUEUE_EXCEPTION.getErrorDesc(), t);
        });
  }

  public Optional<YarnResource> maxEffectiveHandle(
      Optional<JsonNode> queueValue, String rmWebAddress, String queueName) {
    try {
      JsonNode metrics = getResponseByUrl("metrics", rmWebAddress);
      JsonNode clusterMetrics = metrics.path("clusterMetrics");
      long totalMemory = clusterMetrics.path("totalMB").asLong();
      long totalCores = clusterMetrics.path("totalVirtualCores").asLong();
      if (queueValue.isPresent()) {
        JsonNode jsonNode = queueValue.get();
        double absoluteCapacity = jsonNode.path("absoluteCapacity").asDouble();

        YarnResource yarnResource =
            new YarnResource(
                (long) Math.floor(absoluteCapacity * totalMemory * 1024L * 1024L / 100),
                (int) Math.floor(absoluteCapacity * totalCores / 100),
                0,
                queueName,
                "");
        return Optional.of(yarnResource);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.warn("maxEffectiveHandle parse failed", e);
      return Optional.empty();
    }
  }

  public static Optional getQueue(JsonNode queues, String realQueueName) {
    if (queues instanceof ArrayNode) {
      for (JsonNode q : (ArrayNode) queues) {
        String yarnQueueName = q.get("queueName").asText();
        if (yarnQueueName.equals(realQueueName)) {
          return Optional.of(q);
        } else if (realQueueName.startsWith(yarnQueueName + ".")) {
          Optional childQueue = getQueue(getChildQueues(q), realQueueName);
          if (childQueue.isPresent()) {
            return childQueue;
          }
        }
      }
      return Optional.empty();
    } else if (queues instanceof ObjectNode) {
      ObjectNode queueObj = (ObjectNode) queues;
      JsonNode queueName = queueObj.get("queueName");
      if (queueName != null && queueName.asText().equals(realQueueName)) {
        return Optional.of(queueObj);
      } else {
        JsonNode childQueues = queueObj.get("childQueues");
        if (childQueues == null) {
          return Optional.empty();
        } else {
          return getQueue(childQueues, realQueueName);
        }
      }
    } else {
      return Optional.empty();
    }
  }

  public static JsonNode getChildQueues(JsonNode resp) {
    JsonNode queues = resp.get("childQueues").get("queue");
    if (queues != null
        && !queues.isNull()
        && !queues.isMissingNode()
        && queues.isArray()
        && ((ArrayNode) queues).size() > 0) {
      return queues;
    } else {
      return resp.get("childQueues");
    }
  }

  public Optional<JsonNode> getQueueOfCapacity(JsonNode queues, String realQueueName) {
    if (queues.isArray()) {
      for (JsonNode q : queues) {
        String yarnQueueName = q.get("queueName").asText();
        if (yarnQueueName.equals(realQueueName)) {
          return Optional.of(q);
        } else if (q.has("queues")) {
          Optional<JsonNode> matchQueue = getQueueOfCapacity(q.get("queues"), realQueueName);
          if (matchQueue.isPresent()) {
            return matchQueue;
          }
        }
      }
      return Optional.empty();
    } else if (queues.isObject()) {
      if (queues.has("queueName") && queues.get("queueName").asText().equals(realQueueName)) {
        return Optional.of(queues);
      } else if (queues.has("queues")) {
        Optional<JsonNode> matchQueue = getQueueOfCapacity(queues.get("queues"), realQueueName);
        if (matchQueue.isPresent()) {
          return matchQueue;
        }
      }
      return Optional.empty();
    } else {
      return Optional.empty();
    }
  }

  static JsonNode getChildQueuesOfCapacity(JsonNode resp) {
    // = resp \ "queues" \ "queue"
    return resp.path("queues").path("queue");
  }

  public Pair<YarnResource, YarnResource> getResources(
      String rmWebAddress, String realQueueName, String queueName) {
    JsonNode resp = getResponseByUrl("scheduler", rmWebAddress);
    JsonNode schedulerInfo = resp.path("scheduler").path("schedulerInfo");
    // XiongYi
    // String schedulerType = schedulerInfo.path("type").asText();
    String schedulerType = "SuperiorScheduler";
    logger.info("schedulerType: " + schedulerType);
    if ("capacityScheduler".equals(schedulerType)) {
      realQueueName = queueName;
      JsonNode childQueues = getChildQueuesOfCapacity(schedulerInfo);
      Optional<JsonNode> queue = getQueueOfCapacity(childQueues, realQueueName);
      if (!queue.isPresent()) {
        logger.debug(
            "cannot find any information about queue " + queueName + ", response: " + resp);
        throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode(),
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc(), queueName));
      }
      return Pair.of(
          maxEffectiveHandle(queue, rmWebAddress, queueName).get(),
          getYarnResource(queue.map(node -> node.path("resourcesUsed")), queueName).get());

    } else if ("fairScheduler".equals(schedulerType)) {
      JsonNode childQueues = getChildQueues(schedulerInfo.path("rootQueue"));
      Optional<JsonNode> queue = getQueue(childQueues, realQueueName);
      if (!queue.isPresent()) {
        logger.debug(
            "cannot find any information about queue " + queueName + ", response: " + resp);
        throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode(),
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc(), queueName));
      }
      Optional<JsonNode> maxResources = queue.map(node -> node.path("maxResources"));
      Optional<JsonNode> usedResources = queue.map(node -> node.path("usedResources"));
      return Pair.of(
          getYarnResource(maxResources, queueName).get(),
          getYarnResource(usedResources, queueName).get());

    }
    // XiongYi
    else if ("SuperiorScheduler".equals(schedulerType)) {
      // 在 Java 中，我们通常不会像 Scala 那样隐式地定义变量，这可能导致类型错误
      // 我们需要明确声明其类型
      Optional<JsonNode> queue = null;
      JsonNode childQueues = resp.get("queuelist");
      if (childQueues != null) {
        for (JsonNode q : childQueues) {
          String yarnQueueName = q.get("name").asText();
          if (yarnQueueName.equals(realQueueName)) {
            queue = Optional.of(q);
          }
        }
      }
      if (!queue.isPresent()) {
        // 类似于 Scala 的 logger.debug 和抛出异常
        logger.debug(
            "cannot find any information about queue " + realQueueName + ", response: " + resp);
        throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode(),
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc(), realQueueName));
      }
      JsonNode resource_inuse = queue.get().get("resource_inuse");
      JsonNode resource_maximum = queue.get().get("resource_maximum");
      YarnResource resourceInuse =
          new YarnResource(
              (resource_inuse.get("memory")).longValue() * 1024L * 1024L,
              (resource_inuse.get("vcores")).intValue(),
              0,
              queueName);
      YarnResource resourceMax =
          new YarnResource(
              (resource_maximum.get("memory")).longValue() * 1024L * 1024L,
              (resource_maximum.get("vcores")).intValue(),
              0,
              queueName);
      // number_running_application
      // 类似于 Scala 的 logger.info
      logger.info(resource_inuse.toString());
      // 这里假设 maxEffectiveHandle 是对 queue 进行某种操作返回一个结果，这里不再详述
      return Pair.of(resourceMax, resourceInuse);
    } else {
      logger.debug(
          "only support fairScheduler or capacityScheduler, schedulerType: "
              + schedulerType
              + ", response: "
              + resp);
      throw new RMWarnException(
          ONLY_SUPPORT_FAIRORCAPA.getErrorCode(),
          MessageFormat.format(ONLY_SUPPORT_FAIRORCAPA.getErrorDesc(), schedulerType));
    }
  }

  public static Optional<YarnResource> getYarnResource(
      Optional<JsonNode> jsonNode, String queueName) {
    if (jsonNode.isPresent()) {
      JsonNode r = jsonNode.get();
      return Optional.of(
          new YarnResource(
              r.get("memory").asLong() * 1024L * 1024L, r.get("vCores").asInt(), 0, queueName, ""));
    }
    return Optional.empty();
  }

  public static Optional<YarnResource> getAllocatedYarnResource(
      Optional<JsonNode> jsonNode, String queueName) {
    if (jsonNode.isPresent()) {
      JsonNode r = jsonNode.get();
      return Optional.of(
          new YarnResource(
              r.get("allocatedMB").asLong() * 1024L * 1024L,
              r.get("allocatedVCores").asInt(),
              0,
              queueName,
              ""));
    }
    return Optional.empty();
  }

  @Override
  public List<ExternalAppInfo> requestAppInfo(
      ExternalResourceIdentifier identifier, ExternalResourceProvider provider) {
    String rmWebHaAddress = (String) provider.getConfigMap().get("rmWebAddress");
    String rmWebAddress = getAndUpdateActiveRmWebAddress(rmWebHaAddress);
    String queueName = ((YarnResourceIdentifier) identifier).getQueueName();

    String realQueueName = "root." + queueName;

    JsonNode resp = getResponseByUrl("apps", rmWebAddress).path("apps").path("app");
    if (resp.isMissingNode()) {
      return new ArrayList<>();
    }
    ArrayNode appArray = (ArrayNode) resp;

    List<ExternalAppInfo> appInfoList = new ArrayList<>();
    Iterator<JsonNode> iterator = appArray.elements();
    while (iterator.hasNext()) {
      JsonNode app = iterator.next();
      String yarnQueueName = app.get("queue").asText();
      String state = app.get("state").asText();
      if (yarnQueueName.equals(realQueueName)
          && (state.equals("RUNNING") || state.equals("ACCEPTED"))) {
        String id = app.get("id").asText();
        String user = app.get("user").asText();
        String applicationType = app.get("applicationType").asText();
        Optional<YarnResource> yarnResource =
            getAllocatedYarnResource(Optional.ofNullable(app), queueName);
        if (yarnResource.isPresent()) {
          YarnAppInfo appInfo =
              new YarnAppInfo(id, user, state, applicationType, yarnResource.get());
          appInfoList.add(appInfo);
        }
      }
    }
    return appInfoList;
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Yarn;
  }

  private JsonNode getResponseByUrl(String url, String rmWebAddress) {
    // XiongYi
    HttpGet httpGet = null;
    if (url.equals("scheduler")) {
      httpGet = new HttpGet(rmWebAddress + "/ws/v1/sscheduler/queues/list");
    } else {
      httpGet = new HttpGet(rmWebAddress + "/ws/v1/cluster/" + url);
    }

    httpGet.addHeader("Accept", "application/json");
    Object authorEnable = this.provider.getConfigMap().get("authorEnable");
    HttpResponse httpResponse = null;
    if (authorEnable instanceof Boolean) {
      if ((Boolean) authorEnable) {
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + getAuthorizationStr());
      }
    }
    Object kerberosEnable = this.provider.getConfigMap().get("kerberosEnable");
    if (kerberosEnable instanceof Boolean) {
      if ((Boolean) kerberosEnable) {
        String principalName = (String) this.provider.getConfigMap().get("principalName");
        String keytabPath = (String) this.provider.getConfigMap().get("keytabPath");
        String krb5Path = (String) this.provider.getConfigMap().get("krb5Path");
        if (StringUtils.isNotBlank(krb5Path)) {
          logger.warn(
              "krb5Path: {} has been specified, but not allow to be set to avoid conflict",
              krb5Path);
        }
        RequestKerberosUrlUtils requestKuu =
            new RequestKerberosUrlUtils(principalName, keytabPath, false);
        HttpResponse response = null;
        if (url.equals("scheduler")) {
          response =
              requestKuu.callRestUrl(rmWebAddress + "/ws/v1/sscheduler/queues/list", principalName);
        } else {
          response = requestKuu.callRestUrl(rmWebAddress + "/ws/v1/cluster/" + url, principalName);
        }
        httpResponse = response;
      } else {
        HttpResponse response = null;
        try {
          CloseableHttpClient httpClient = HttpClients.createDefault();
          response = httpClient.execute(httpGet);
        } catch (IOException e) {
          logger.warn("getResponseByUrl failed", e);
          throw new RMErrorException(
              YARN_QUEUE_EXCEPTION.getErrorCode(), YARN_QUEUE_EXCEPTION.getErrorDesc(), e);
        }
        httpResponse = response;
      }
    } else {
      HttpResponse response = null;
      try {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        response = httpClient.execute(httpGet);
      } catch (IOException e) {
        logger.warn("getResponseByUrl failed", e);
        throw new RMErrorException(
            YARN_QUEUE_EXCEPTION.getErrorCode(), YARN_QUEUE_EXCEPTION.getErrorDesc(), e);
      }
      httpResponse = response;
    }

    String entityString = "";
    try {
      entityString = EntityUtils.toString(httpResponse.getEntity());
    } catch (IOException e) {
      logger.warn("getResponseByUrl failed", e);
      throw new RMErrorException(
          YARN_QUEUE_EXCEPTION.getErrorCode(), YARN_QUEUE_EXCEPTION.getErrorDesc(), e);
    }
    JsonNode jsonNode = null;
    try {
      jsonNode = objectMapper.readTree(entityString);
    } catch (Exception e) {
      logger.warn("getResponseByUrl failed", e);
      throw new RMErrorException(
          YARN_QUEUE_EXCEPTION.getErrorCode(), YARN_QUEUE_EXCEPTION.getErrorDesc(), e);
    }

    return jsonNode;
  }

  public String getAndUpdateActiveRmWebAddress(String haAddress) {
    // todo check if it will stuck for many requests
    String activeAddress = rmAddressMap.get(haAddress);
    if (StringUtils.isBlank(activeAddress)) {
      synchronized (haAddress.intern()) {
        if (StringUtils.isBlank(activeAddress)) {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "Cannot find value of haAddress : "
                    + haAddress
                    + " in cacheMap with size "
                    + rmAddressMap.size());
          }
          if (StringUtils.isNotBlank(haAddress)) {
            String[] addresses =
                haAddress.split(RMConfiguration.DEFAULT_YARN_RM_WEB_ADDRESS_DELIMITER.getValue());
            for (String address : addresses) {
              try {
                JsonNode response = getResponseByUrl("info", address);
                JsonNode haStateValue = response.path("clusterInfo").path("haState");
                if (!haStateValue.isMissingNode() && haStateValue.isTextual()) {
                  String haState = haStateValue.asText();
                  if (HASTATE_ACTIVE.equalsIgnoreCase(haState)) {
                    activeAddress = address;
                  } else {
                    logger.warn("Resourcemanager : " + address + " haState : " + haState);
                  }
                }
              } catch (Exception e) {
                logger.error("Get Yarn resourcemanager info error, " + e.getMessage(), e);
              }
            }
          }
          if (StringUtils.isNotBlank(activeAddress)) {
            if (logger.isDebugEnabled()) {
              logger.debug("Put (" + haAddress + ", " + activeAddress + ") to cacheMap.");
            }
            rmAddressMap.put(haAddress, activeAddress);
          } else {
            throw new RMErrorException(
                GET_YARN_EXCEPTION.getErrorCode(),
                MessageFormat.format(GET_YARN_EXCEPTION.getErrorDesc(), haAddress));
          }
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Get active rm address : " + activeAddress + " from haAddress : " + haAddress);
    }
    return activeAddress;
  }

  @Override
  public Boolean reloadExternalResourceAddress(ExternalResourceProvider provider) {
    if (null == provider) {
      rmAddressMap.clear();
    } else {
      String rmWebHaAddress = (String) provider.getConfigMap().get("rmWebAddress");
      rmAddressMap.remove(rmWebHaAddress);
      getAndUpdateActiveRmWebAddress(rmWebHaAddress);
    }
    return true;
  }
}
