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

import org.apache.linkis.gateway.http.GatewayHttpRequest;
import org.apache.linkis.gateway.security.conf.SSOCommonConfig;
import org.apache.linkis.gateway.security.constants.Constants;
import org.apache.linkis.gateway.security.entity.menhu.MHToken;
import org.apache.linkis.gateway.security.entity.menhu.ResponseBase;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author colourness
 * @description token
 * @date 2023/3/13
 */
@Component
public class TokenService {
  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
  /**
   * token
   *
   * @param request
   * @return token
   */
  public String getToken(GatewayHttpRequest request, String checkCode) {
    RestTemplate getTokenRestTemplate = new RestTemplate();
    Map<String, Object> getTokenBody = new HashMap<String, Object>();
    getTokenBody.put("systemKey", SSOCommonConfig.BIG_DATA_MENHU_SYSTEMKEY.getValue());
    getTokenBody.put("checkCode", checkCode);
    String getTokenRequestBody = JSON.toJSONString(getTokenBody);
    // 请求参数包含body和header内容
    HttpEntity<String> getTokenRequestEntity = new HttpEntity<String>(getTokenRequestBody, null);

    ResponseEntity<String> getTokenResponseEntity =
        getTokenRestTemplate.postForEntity(
            SSOCommonConfig.BIG_DATA_MENHU_GETTOKEN_URL.getValue(),
            getTokenRequestEntity,
            String.class);
    logger.info("getTokenResponseEntity.entity={}", JSON.toJSONString(getTokenResponseEntity));
    if (getTokenResponseEntity.getStatusCode().is2xxSuccessful()) {
      // 请求接口成功
      logger.info("getTokenResponseEntity.body={}", getTokenResponseEntity.getBody());
      logger.info(
          "getTokenResponseEntity.statusCode={}", getTokenResponseEntity.getStatusCode().value());
      ResponseBase getTokenResponseBody =
          JSON.parseObject(getTokenResponseEntity.getBody(), ResponseBase.class);
      MHToken tokenResult =
          JSON.parseObject(JSON.toJSONString(getTokenResponseBody.getData()), MHToken.class);
      logger.info("token={}", tokenResult.getToken());
      return tokenResult.getToken();
    }
    return null;
  }
  /**
   * @param token
   * @return
   */
  public Claims parseToken(String token) {
    return Jwts.parser()
        .setSigningKey(SSOCommonConfig.SSO_TOKEN_SECRET.getValue())
        .parseClaimsJws(token)
        .getBody();
  }

  public String getTokenKey(String uuid) {
    return Constants.LOGIN_TOKEN_KEY + uuid;
  }
}
