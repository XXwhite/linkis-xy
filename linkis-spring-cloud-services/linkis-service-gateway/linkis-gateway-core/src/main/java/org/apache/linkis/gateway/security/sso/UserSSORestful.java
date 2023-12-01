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

package org.apache.linkis.gateway.security.sso;

import org.apache.linkis.gateway.config.GatewayConfiguration;
import org.apache.linkis.gateway.http.GatewayContext;
import org.apache.linkis.gateway.security.GatewaySSOUtils;
import org.apache.linkis.gateway.security.conf.SSOCommonConfig;
import org.apache.linkis.gateway.security.constants.Constants;
import org.apache.linkis.gateway.security.entity.menhu.MHToken;
import org.apache.linkis.gateway.security.entity.menhu.MHUserResult;
import org.apache.linkis.gateway.security.entity.menhu.ResponseBase;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.apache.linkis.server.security.SSOUtils;

import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 用户SSO接口
 * @create: 2023/06/26
 * @author: colourness
 */
@Component
public class UserSSORestful {
  private static final Logger logger = LoggerFactory.getLogger(UserSSORestful.class);

  public void doUserRequest(GatewayContext gatewayContext) {
    /*String userURI = ServerConfiguration.BDP_SSO_SERVER_USER_URI().getValue();
    if (!userURI.endsWith("/")) {
      userURI += "/";
    }*/
    // String path = gatewayContext.getRequest().getRequestURI().replace(userURI, "");
    logger.info("***** UserSSORestful doUserRequest *****");
    Message message = null;
    try {
      message = checkLogin(gatewayContext);
    } catch (Exception ex) {
      message = loginSSO(gatewayContext);
    }
    gatewayContext.getResponse().write(JSON.toJSONString(message));
    gatewayContext.getResponse().setStatus(Message.messageToHttpStatus(message));
    gatewayContext.getResponse().sendResponse();
  }

  public Message checkLogin(GatewayContext gatewayContext) {
    String loginUser = GatewaySSOUtils.getLoginUsername(gatewayContext);
    return Message.ok(
            loginUser + "Already logged in, please log out before signing in(已经登录，请先退出再进行登录)！")
        .data("userName", loginUser);
  }

  public Message loginSSO(GatewayContext gatewayContext) {
    Message message = tryLoginSSO(gatewayContext);
    message
        .data("sessionTimeOut", SSOUtils.getSessionTimeOut())
        .data("enableWatermark", GatewayConfiguration.ENABLE_WATER_MARK().getValue());
    // if (securityHooks != null) securityHooks.foreach(_.postLogin(gatewayContext));
    return message;
  }
  // 门户checkCode标识
  private String MH_CHECK_CODE = "checkCode";

  public Message tryLoginSSO(GatewayContext gatewayContext) {
    logger.info("***** UserSSORestful tryLoginSSO *****");
    logger.info(
        "***** gatewayContext.getRequest={}", JSON.toJSONString(gatewayContext.getRequest()));
    String requestBody = gatewayContext.getRequest().getRequestBody();
    if (StringUtils.isBlank(requestBody)) {
      return Message.error("未获取到用户checkCode信息").data("token", "");
    }
    Map<String, Object> json = BDPJettyServerHelper.gson().fromJson(requestBody, Map.class);
    String checkCode = (String) json.getOrDefault(MH_CHECK_CODE, null);
    logger.info("checkCode======{}", checkCode);
    // 测试
    // GatewaySSOUtils.setLoginUser(gatewayContext, "60100");
    // return Message.ok("login successful(登录成功)！").data("userName", "60100").data("isAdmin",
    // false).data("token","123123abc");
    Map<String, Object> getTokenBody = new HashMap<String, Object>();
    getTokenBody.put("systemKey", SSOCommonConfig.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    getTokenBody.put("checkCode", checkCode);
    String getTokenRequestBody = JSON.toJSONString(getTokenBody);
    Map<String, String> getTokenHeader = new HashMap<>();
    getTokenHeader.put("Service-Id", SSOCommonConfig.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getTokenHeader.put("Content-Type", "application/json");
    logger.info(
        "getTokenRequestEntity.url={}", SSOCommonConfig.BIG_DATA_MENHU_GETTOKEN_URL.getValue());
    logger.info("getTokenRequestEntity.header={}", JSON.toJSONString(getTokenHeader));
    logger.info("getTokenRequestEntity.body={}", getTokenRequestBody);
    String responseToken =
        HttpUtils.sendSSLPost(
            SSOCommonConfig.BIG_DATA_MENHU_GETTOKEN_URL.getValue(),
            getTokenHeader,
            getTokenRequestBody,
            null,
            "POST");
    logger.info("responseTokenEntity.entity={}", responseToken);
    if (StringUtils.isBlank(responseToken)) {
      return Message.error("未获取到用户token信息").data("token", "");
    }
    // 请求接口成功
    ResponseBase getTokenResponseBody = JSON.parseObject(responseToken, ResponseBase.class);
    MHToken tokenResult =
        JSON.parseObject(JSON.toJSONString(getTokenResponseBody.getData()), MHToken.class);
    logger.info("token={}", tokenResult.getToken());
    // 调用门户接口通过token获取用户信息
    Map<String, String> getUserHeaders = new HashMap<String, String>();
    getUserHeaders.put("Authorization", Constants.TOKEN_PREFIX + tokenResult.getToken());
    getUserHeaders.put("Service-Id", SSOCommonConfig.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getUserHeaders.put("content-type", "application/json");
    Map<String, Object> getUserBody = new HashMap<String, Object>();
    getUserBody.put("systemKey", SSOCommonConfig.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    String getUserRequestBody = JSON.toJSONString(getUserBody);
    // 请求参数包含body和header内容
    logger.info(
        "getUserRequestEntity.url={}",
        SSOCommonConfig.BIG_DATA_MENHU_GETUSERBYTOKEN_URL.getValue());
    String responseUserInfo =
        HttpUtils.sendSSLPost(
            SSOCommonConfig.BIG_DATA_MENHU_GETUSERBYTOKEN_URL.getValue(),
            getUserHeaders,
            getUserRequestBody,
            null,
            "POST");
    logger.info("responseUserInfoEntity.entity={}", responseUserInfo);
    if (StringUtils.isBlank(responseUserInfo)) {
      return Message.error("未获取到用户信息").data("token", "");
    }
    // 按照接口返回类型转化成实体类
    // 请求接口成功
    ResponseBase getUserResponseBody = JSON.parseObject(responseUserInfo, ResponseBase.class);
    MHUserResult userResult =
        JSON.parseObject(JSON.toJSONString(getUserResponseBody.getData()), MHUserResult.class);
    GatewaySSOUtils.setLoginUser(gatewayContext, userResult.getUser().getUserId());

    Message message =
        Message.ok("login successful(登录成功)！")
            .data("userName", userResult.getUser().getUserId())
            .data("userCnName", userResult.getUser().getUserName())
            .data("isAdmin", false)
            .data("token", tokenResult.getToken());
    clearExpireCookie(gatewayContext);
    return message;
  }

  public void clearExpireCookie(GatewayContext gatewayContext) {
    List<Cookie> cookieList =
        gatewayContext.getRequest().getCookies().entrySet().stream()
            .flatMap(x -> Arrays.stream(x.getValue()))
            .collect(Collectors.toList());
    // Cookie cookie = null;
    /*if (!cookieList.isEmpty()) {
      cookie = cookieList.get(0);
      logger.info("SSO_COOKIE_NAME {}", cookie.getName());
      logger.info("SSO_COOKIE_VALUE {}", cookie.getValue());
      logger.info("SSO_COOKIE_MAX_AGE {}", cookie.getMaxAge());
      logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }*/
    // String token = cookie.getValue();
    List<Cookie> expireCookies =
        cookieList.stream()
            .filter(
                cookie ->
                    cookie
                        .getName()
                        .equals(
                            ServerConfiguration.LINKIS_SERVER_SESSION_TICKETID_KEY().getValue()))
            .collect(Collectors.toList());

    String[] host = gatewayContext.getRequest().getHeaders().get("Host");
    if (host != null && host.length > 0) {
      int maxDomainLevel = host[0].split("\\.").length;
      for (int level = 1; level <= maxDomainLevel; level++) {
        for (Cookie cookie : expireCookies) {
          Cookie tmp = new Cookie(cookie.getName(), null);
          tmp.setPath("/");
          tmp.setMaxAge(0);
          String domain = GatewaySSOUtils.getCookieDomain(host[0], level);
          tmp.setDomain(domain);
          gatewayContext.getResponse().addCookie(tmp);
          logger.info(
              "success clear user cookie: {}--{}",
              ServerConfiguration.LINKIS_SERVER_SESSION_TICKETID_KEY().getValue(),
              domain);
        }
      }
    }
  }

  public static void main(String[] args) {
    // String s =
    // "{\"code\":\"000000\",\"message\":\"成功\",\"data\":{\"user\":{\"userId\":1,\"userName\":\"若依\",\"phone\":\"15888888888\",\"sex\":\"1\",\"status\":\"0\",\"deptName\":\"泗河镇\",\"deptCode\":null,\"deptId\":100,\"deptType\":\"0\",\"certNo\":\"admin\"},\"roles\":[{\"roleId\":1,\"systemId\":null,\"roleKey\":\"admin\",\"menuList\":[]},{\"roleId\":120100,\"systemId\":5,\"roleKey\":\"1\",\"menuList\":[]}]}}";
    String s1 =
        "{\"code\":\"000000\",\"message\":\"成功\",\"data\":{\"user\":{\"userId\":60100,\"userName\":\"李超\",\"phone\":\"15011413438\",\"sex\":\"0\",\"status\":\"0\",\"deptName\":\"泗河镇\",\"deptCode\":null,\"deptId\":100,\"deptType\":\"0\",\"certNo\":\"220881198801070711\"},\"roles\":[{\"roleId\":2,\"systemId\":210009,\"roleKey\":\"common\",\"menuList\":[]}]}}";
    ResponseBase getUserResponseBody = JSON.parseObject(s1, ResponseBase.class);
    MHUserResult userResult =
        JSON.parseObject(JSON.toJSONString(getUserResponseBody.getData()), MHUserResult.class);
    System.out.println(JSON.toJSONString(userResult));
  }
}
