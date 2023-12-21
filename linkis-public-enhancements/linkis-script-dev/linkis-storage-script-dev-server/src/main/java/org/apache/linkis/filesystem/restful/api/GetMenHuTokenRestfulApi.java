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

package org.apache.linkis.filesystem.restful.api;

import org.apache.linkis.filesystem.conf.WorkSpaceConfiguration;
import org.apache.linkis.filesystem.constant.WorkSpaceConstants;
import org.apache.linkis.filesystem.entity.menhu.*;
import org.apache.linkis.filesystem.util.HttpUtils;
import org.apache.linkis.filesystem.util.PinyinUtil;
import org.apache.linkis.server.Message;

import org.apache.commons.lang.StringUtils;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 获取门户token接口
 * @create: 2023/04/13
 * @author: colourness
 */
@RestController
@RequestMapping(path = "/filesystem")
public class GetMenHuTokenRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(GetMenHuTokenRestfulApi.class);

  @RequestMapping(path = "/getMHLoginUrl", method = RequestMethod.POST)
  public Message getMHLoginUrl(HttpServletRequest req) {
    Map<String, Object> getUrlBody = new HashMap<String, Object>();
    getUrlBody.put("systemKey", WorkSpaceConfiguration.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    String getUrlRequestBody = JSON.toJSONString(getUrlBody);
    Map<String, String> getUrlHeader = new HashMap<>();
    getUrlHeader.put("Service-Id", WorkSpaceConfiguration.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getUrlHeader.put("Content-Type", "application/json");
    logger.info(
        "getUrlRequestEntity.url={}", WorkSpaceConfiguration.BIG_DATA_MENHU_GETURL_URL.getValue());
    logger.info("getUrlRequestEntity.header={}", JSON.toJSONString(getUrlHeader));
    logger.info("getUrlRequestEntity.body={}", getUrlRequestBody);
    String responseUrl =
        HttpUtils.sendSSLPost(
            WorkSpaceConfiguration.BIG_DATA_MENHU_GETURL_URL.getValue(),
            getUrlHeader,
            getUrlRequestBody,
            null,
            "POST");
    logger.info("responseUrlEntity.entity={}", responseUrl);
    if (StringUtils.isNotBlank(responseUrl)) {
      // 请求接口成功
      ResponseBase getUrlResponseBody = JSON.parseObject(responseUrl, ResponseBase.class);
      MHLoginUrl urlResult =
          JSON.parseObject(JSON.toJSONString(getUrlResponseBody.getData()), MHLoginUrl.class);
      logger.info("loginUrl={}", urlResult.getLoginUrl());
      return Message.ok("").data("data", urlResult);
    }
    return Message.error("未获取到用户登录地址").data("loginUrl", "");
  }

  @RequestMapping(path = "/getToken", method = RequestMethod.POST)
  public Message getToken(HttpServletRequest req, @RequestBody RequestCheckCode requestCheckCode) {
    if (Objects.isNull(requestCheckCode)) {
      return Message.error("未获取到用户checkCode信息").data("token", "");
    }
    if (StringUtils.isBlank(requestCheckCode.getCheckCode())) {
      return Message.error("未获取到用户checkCode信息").data("token", "");
    }
    /*LoginBaseInfo loginBaseInfo = new LoginBaseInfo();
    loginBaseInfo.setToken("1234567token");
    loginBaseInfo.setUsername("熊翼");
    loginBaseInfo.setUserId("60100");
    return Message.ok("").data("data", loginBaseInfo);*/
    Map<String, Object> getTokenBody = new HashMap<String, Object>();
    getTokenBody.put("systemKey", WorkSpaceConfiguration.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    getTokenBody.put("checkCode", requestCheckCode.getCheckCode());
    String getTokenRequestBody = JSON.toJSONString(getTokenBody);
    Map<String, String> getTokenHeader = new HashMap<>();
    getTokenHeader.put("Service-Id", WorkSpaceConfiguration.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getTokenHeader.put("Content-Type", "application/json");
    logger.info(
        "getTokenRequestEntity.url={}",
        WorkSpaceConfiguration.BIG_DATA_MENHU_GETTOKEN_URL.getValue());
    logger.info("getTokenRequestEntity.header={}", JSON.toJSONString(getTokenHeader));
    logger.info("getTokenRequestEntity.body={}", getTokenRequestBody);
    String responseToken =
        HttpUtils.sendSSLPost(
            WorkSpaceConfiguration.BIG_DATA_MENHU_GETTOKEN_URL.getValue(),
            getTokenHeader,
            getTokenRequestBody,
            null,
            "POST");
    logger.info("responseTokenEntity.entity={}", responseToken);
    if (StringUtils.isNotBlank(responseToken)) {
      // 请求接口成功
      ResponseBase getTokenResponseBody = JSON.parseObject(responseToken, ResponseBase.class);
      MHToken tokenResult =
          JSON.parseObject(JSON.toJSONString(getTokenResponseBody.getData()), MHToken.class);
      logger.info("token={}", tokenResult.getToken());
      LoginBaseInfo loginBaseInfo = new LoginBaseInfo();
      loginBaseInfo.setToken(tokenResult.getToken());
      // 调用门户接口通过token获取用户信息
      Map<String, String> getUserHeaders = new HashMap<String, String>();
      getUserHeaders.put("Authorization", WorkSpaceConstants.TOKEN_PREFIX + tokenResult.getToken());
      getUserHeaders.put("Service-Id", WorkSpaceConfiguration.BIG_DATA_MENHU_SERVICE_ID.getValue());
      getUserHeaders.put("content-type", "application/json");
      Map<String, Object> getUserBody = new HashMap<String, Object>();
      getUserBody.put("systemKey", WorkSpaceConfiguration.BIG_DATA_MENHU_SYSTEMKEY.getValue());
      String getUserRequestBody = JSON.toJSONString(getUserBody);
      // 请求参数包含body和header内容
      logger.info(
          "getUserRequestEntity.url={}",
          WorkSpaceConfiguration.BIG_DATA_MENHU_GETUSERBYTOKEN_URL.getValue());
      String responseUserInfo =
          HttpUtils.sendSSLPost(
              WorkSpaceConfiguration.BIG_DATA_MENHU_GETUSERBYTOKEN_URL.getValue(),
              getUserHeaders,
              getUserRequestBody,
              null,
              "POST");
      logger.info("responseUserInfoEntity.entity={}", responseUserInfo);
      // 按照接口返回类型转化成实体类
      if (StringUtils.isNotBlank(responseUserInfo)) {
        // 请求接口成功
        ResponseBase getUserResponseBody = JSON.parseObject(responseUserInfo, ResponseBase.class);
        MHUserResult userResult =
            JSON.parseObject(JSON.toJSONString(getUserResponseBody.getData()), MHUserResult.class);
        loginBaseInfo.setUsername(userResult.getUser().getUserName());
        loginBaseInfo.setUserId(
            PinyinUtil.toPinyinSub(userResult.getUser().getUserName())
                + userResult.getUser().getUserId());
      }
      return Message.ok("").data("data", loginBaseInfo);
    }
    return Message.error("未获取到用户token信息").data("token", "");
  }
}
