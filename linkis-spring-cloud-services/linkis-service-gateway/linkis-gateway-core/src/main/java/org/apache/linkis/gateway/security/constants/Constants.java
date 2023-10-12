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

package org.apache.linkis.gateway.security.constants;

import io.jsonwebtoken.Claims;

/**
 * @author colourness
 * @description
 * @date 2023/3/13
 */
public class Constants {
  /** UTF-8 */
  public static final String UTF8 = "UTF-8";

  /** GBK */
  public static final String GBK = "GBK";

  /** www */
  public static final String WWW = "www.";

  /** http */
  public static final String HTTP = "http://";

  /** https */
  public static final String HTTPS = "https://";

  /** */
  public static final String SUCCESS = "0";

  /** */
  public static final String FAIL = "1";

  /** */
  public static final String LOGIN_SUCCESS = "Success";

  /** */
  public static final String LOGOUT = "Logout";

  /** */
  public static final String REGISTER = "Register";

  /** */
  public static final String LOGIN_FAIL = "Error";

  /** redis key */
  public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

  /** redis key */
  public static final String LOGIN_TOKEN_KEY = "login_tokens:";

  /** redis key */
  public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

  /** */
  public static final Integer CAPTCHA_EXPIRATION = 2;

  /** */
  public static final String TOKEN = "token";

  /** */
  public static final String TOKEN_PREFIX = "Bearer ";

  /** */
  public static final String LOGIN_USER_KEY = "login_user_key";

  /** */
  public static final String JWT_USERID = "userid";

  /** */
  public static final String JWT_USERNAME = Claims.SUBJECT;

  /** */
  public static final String JWT_AVATAR = "avatar";

  /** */
  public static final String JWT_CREATED = "created";

  /** */
  public static final String JWT_AUTHORITIES = "authorities";

  /** cache key */
  public static final String SYS_CONFIG_KEY = "sys_config:";

  /** cache key */
  public static final String SYS_DICT_KEY = "sys_dict:";

  /** */
  public static final String RESOURCE_PREFIX = "/profile";
}
