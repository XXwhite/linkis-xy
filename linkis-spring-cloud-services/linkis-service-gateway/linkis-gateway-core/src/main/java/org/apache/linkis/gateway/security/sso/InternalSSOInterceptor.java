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

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.gateway.http.GatewayContext;
import org.apache.linkis.gateway.security.conf.SSOCommonConfig;
import org.apache.linkis.gateway.security.constants.Constants;
import org.apache.linkis.gateway.security.entity.menhu.MHLoginUrl;
import org.apache.linkis.gateway.security.entity.menhu.MHUserResult;
import org.apache.linkis.gateway.security.entity.menhu.ResponseBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author colourness
 * @description 实现内部sso认证拦截器
 * @date 2023/3/10
 */
public class InternalSSOInterceptor implements SSOInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(InternalSSOInterceptor.class);

  /**
   * 如果打开SSO单点登录功能，当前端跳转SSO登录页面登录成功后，前端再次转发请求给gateway。 用户需实现该接口，通过Request返回user
   *
   * @param gatewayContext
   * @return
   */
  @Override
  public String getUser(GatewayContext gatewayContext) {
    // ApplicationContext context = DataWorkCloudApplication.getApplicationContext();
    // TokenService tokenService = context.getBean(TokenService.class);

    List<Cookie> cookieList =
        gatewayContext.getRequest().getCookies().entrySet().stream()
            .flatMap(x -> Arrays.stream(x.getValue()))
            .filter(c -> SSOCommonConfig.SSO_TOKEN_HEADER.getValue().equals(c.getName()))
            .collect(Collectors.toList());
    Cookie cookie = null;
    if (!cookieList.isEmpty()) {
      cookie = cookieList.get(0);
      logger.info("SSO_COOKIE_NAME {}", cookie.getName());
      logger.info("SSO_COOKIE_VALUE {}", cookie.getValue());
      logger.info("SSO_COOKIE_MAX_AGE {}", cookie.getMaxAge());
      logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
    if (cookie == null) {
      return "";
    }
    String token = cookie.getValue();
    logger.info("token={}", token);
    if (StringUtils.isBlank(token)) {
      return "";
    }
    // return "60100";
    // 按照接口返回类型转化成实体类
    Map<String, Object> getUserBody = new HashMap<String, Object>();
    getUserBody.put("systemKey", SSOCommonConfig.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    String getUserRequestBody = JSON.toJSONString(getUserBody);
    Map<String, String> getUserHeaders = new HashMap<String, String>();
    getUserHeaders.put("Authorization", Constants.TOKEN_PREFIX + token);
    getUserHeaders.put("Service-Id", SSOCommonConfig.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getUserHeaders.put("Content-Type", "application/json");
    logger.info(
        "getUserRequestEntity.url={}",
        SSOCommonConfig.BIG_DATA_MENHU_GETUSERBYTOKEN_URL.getValue());
    logger.info("getUserRequestEntity.header={}", JSON.toJSONString(getUserHeaders));
    logger.info("getUserRequestEntity.body={}", getUserRequestBody);
    String responseUserInfo =
        HttpUtils.sendSSLPost(
            SSOCommonConfig.BIG_DATA_MENHU_GETUSERBYTOKEN_URL.getValue(),
            getUserHeaders,
            getUserRequestBody,
            null,
            "POST");
    logger.info("getUserResponseEntity.entity={}", responseUserInfo);
    // 按照接口返回类型转化成实体类
    if (StringUtils.isNotBlank(responseUserInfo)) {
      // 请求接口成功
      ResponseBase getUserResponseBody = JSON.parseObject(responseUserInfo, ResponseBase.class);
      MHUserResult userResult =
          JSON.parseObject(JSON.toJSONString(getUserResponseBody.getData()), MHUserResult.class);
      // String userName = GatewaySSOUtils.getLoginUsername(gatewayContext);
      // if (StringUtils.isBlank(userName)) {
      // GatewaySSOUtils.setLoginUser(gatewayContext, userResult.getUser().getUserId());
      // }

      // TODO 需要确认用户是否在dss平台已创建，如果不存在需要跳到固定页面提示
      return userResult.getUser().getUserId();
    } else {
      logger.warn("获取门户用户信息接口异常");
    }
    return "";
  }

  /**
   * 通过前端的requestUrl，用户传回一个可跳转的SSO登录页面URL。 要求：需带上原请求URL，以便登录成功后能跳转回来
   *
   * @param requestUrl
   * @return
   */
  @Override
  public String redirectTo(URI requestUrl) {
    logger.info("URI.getPath()={}", requestUrl.getPath());
    logger.info("URI.toString()={}", requestUrl.toString());
    logger.info("URI.port()={}", requestUrl.getPort());
    logger.info("URI.host()={}", requestUrl.getHost());
    Map<String, Object> body = new HashMap<String, Object>();
    body.put("systemKey", SSOCommonConfig.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    String requestBody = JSON.toJSONString(body);
    Map<String, String> getUrlHeaders = new HashMap<String, String>();
    getUrlHeaders.put("Service-Id", SSOCommonConfig.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getUrlHeaders.put("Content-Type", "application/json");
    // 请求参数包含body和header内容
    String responseUserInfo =
        HttpUtils.sendSSLPost(
            SSOCommonConfig.BIG_DATA_MENHU_GETURL_URL.getValue(),
            getUrlHeaders,
            requestBody,
            null,
            "POST");
    logger.info("response.entity={}", responseUserInfo);
    // 按照接口返回类型转化成实体类
    if (StringUtils.isNotBlank(responseUserInfo)) {
      ResponseBase responseBody = JSON.parseObject(responseUserInfo, ResponseBase.class);
      MHLoginUrl loginUrl =
          JSON.parseObject(JSON.toJSONString(responseBody.getData()), MHLoginUrl.class);
      logger.info(
          "redirectTo is url={}",
          loginUrl.getLoginUrl() + SSOCommonConfig.BIG_DATA_MENHU_THIRD_SYS_BACK_URL.getValue());
      return loginUrl.getLoginUrl() + SSOCommonConfig.BIG_DATA_MENHU_THIRD_SYS_BACK_URL.getValue();
    } else {
      logger.warn("获取门户登录地址接口异常");
    }
    return null;
  }

  /**
   * gateway退出时，会调用此接口，以保证gateway清除cookie后，SSO单点登录也会把登录信息清除掉
   *
   * @param gatewayContext
   */
  @Override
  public void logout(GatewayContext gatewayContext) {
    List<Cookie> cookieList =
        gatewayContext.getRequest().getCookies().entrySet().stream()
            .flatMap(x -> Arrays.stream(x.getValue()))
            .filter(c -> SSOCommonConfig.SSO_TOKEN_HEADER.getValue().equals(c.getName()))
            .collect(Collectors.toList());
    Cookie cookie = null;
    if (!cookieList.isEmpty()) {
      cookie = cookieList.get(0);
      logger.info("SSO_COOKIE_NAME {}", cookie.getName());
      logger.info("SSO_COOKIE_VALUE {}", cookie.getValue());
      logger.info("SSO_COOKIE_MAX_AGE {}", cookie.getMaxAge());
      logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
    if (cookie == null) {
      return;
    }
    String token = cookie.getValue();
    logger.info("token={}", token);
    if (StringUtils.isBlank(token)) {
      return;
    }
    Map<String, Object> body = new HashMap<String, Object>();
    body.put("systemKey", SSOCommonConfig.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    String requestBody = JSON.toJSONString(body);
    Map<String, String> getLogoutHeaders = new HashMap<String, String>();
    getLogoutHeaders.put("Service-Id", SSOCommonConfig.BIG_DATA_MENHU_SERVICE_ID.getValue());
    getLogoutHeaders.put("Content-Type", "application/json");
    getLogoutHeaders.put("Authorization", Constants.TOKEN_PREFIX + token);
    // 请求参数包含body和header内容
    String responseLogout =
        HttpUtils.sendSSLPost(
            SSOCommonConfig.BIG_DATA_MENHU_LOGOUT_URL.getValue(),
            getLogoutHeaders,
            requestBody,
            null,
            "POST");
    logger.info("response.entity={}", responseLogout);
    // 按照接口返回类型转化成实体类
    if (StringUtils.isNotBlank(responseLogout)) {
      ResponseBase responseBody = JSON.parseObject(responseLogout, ResponseBase.class);
      logger.info("menhu logout ={}", JSON.toJSONString(responseBody));
    } else {
      logger.warn("门户退出接口异常");
    }
  }
}
