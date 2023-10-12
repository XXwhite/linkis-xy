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

package org.apache.linkis.gateway.security.conf;

import org.apache.linkis.common.conf.CommonVars;

public class SSOCommonConfig {

  public static final CommonVars<String> SSO_TOKEN_HEADER =
      CommonVars.apply("sso.token.header", "x-token");

  public static final CommonVars<String> SSO_TOKEN_SECRET =
      CommonVars.apply("sso.token.secret", "");

  public static final CommonVars<Integer> SSO_TOKEN_EXPIRE_TIME =
      CommonVars.apply("sso.token.expireTime", 30);

  public static final CommonVars<String> BIG_DATA_MENHU_SYSTEMKEY =
      CommonVars.apply("big.data.menhu.systemKey", "");

  public static final CommonVars<String> BIG_DATA_MENHU_TOKEN_HEADER =
      CommonVars.apply("big.data.menhu.token.header", "");

  public static final CommonVars<String> BIG_DATA_MENHU_GETUSERBYTOKEN_URL =
      CommonVars.apply(
          "big.data.menhu.getuserbytoken.url",
          "https://gateway.zsj.jl.cegn.cn:443/thirdLogin/getUserByToken");

  public static final CommonVars<String> BIG_DATA_MENHU_GETTOKEN_URL =
      CommonVars.apply(
          "big.data.menhu.gettoken.url", "https://gateway.zsj.jl.cegn.cn:443/thirdLogin/getToken");

  public static final CommonVars<String> BIG_DATA_MENHU_GETURL_URL =
      CommonVars.apply(
          "big.data.menhu.geturl.url", "https://gateway.zsj.jl.cegn.cn:443/thirdLogin/getUrl");

  public static final CommonVars<String> BIG_DATA_MENHU_THIRD_SYS_BACK_URL =
      CommonVars.apply(
          "big.data.menhu.third.sys.back.url",
          "&thirdSystemBackUrl=http://172.24.5.91:8085/#/checkCode?");

  public static final CommonVars<String> BIG_DATA_MENHU_LOGOUT_URL =
      CommonVars.apply(
          "big.data.menhu.logout.url", "https://gateway.zsj.jl.cegn.cn:443/thirdLogin/logout");
  public static final CommonVars<String> BIG_DATA_MENHU_SERVICE_ID =
      CommonVars.apply("big.data.menhu.service.id", "BkFS2H");

  /*public static final CommonVars<String>  BDP_SSO_SERVER_USER_URI =
  CommonVars.apply("wds.linkis.server.user.sso.restful.uri", "/api/rest_j/" + BDP_SERVER_VERSION + "/SSOUser");*/
}
