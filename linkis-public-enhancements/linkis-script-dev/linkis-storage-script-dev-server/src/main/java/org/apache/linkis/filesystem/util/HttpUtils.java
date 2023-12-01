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

package org.apache.linkis.filesystem.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: http请求工具类
 * @create: 2023/06/20
 * @author: colourness
 */
public class HttpUtils {
  private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

  public static final String DEFAULT_CHARSET = "UTF-8";
  public static final int CONNECTTIMEOUT = 35000;
  public static final int CONNECTREQUESTTIMEOUT = 35000;
  public static final int SOCKETTIMEOUT = 5000;

  /**
   * 发送get请求
   *
   * @param url 请求地址
   * @param headers 请求头
   * @param params 请求参数
   * @return 响应结果
   */
  public static String doGet(String url, Map<String, String> headers, Map<String, String> params)
      throws IOException {
    return doGet(url, headers, params, DEFAULT_CHARSET);
  }

  /**
   * 发送get请求
   *
   * @param url 请求地址
   * @param headers 请求头
   * @param params 请求参数
   * @param charset 字符集
   * @return 响应结果
   */
  public static String doGet(
      String url, Map<String, String> headers, Map<String, String> params, String charset)
      throws IOException {
    if (StringUtils.isBlank(url)) {
      return null;
    } else {
      if (params != null) {
        url = url + "?" + buildQuery(params, charset);
      }

      CloseableHttpClient httpClient = HttpClients.createDefault();
      RequestConfig requestConfig =
          RequestConfig.custom()
              .setConnectTimeout(35000)
              .setConnectionRequestTimeout(35000)
              .setSocketTimeout(60000)
              .build();
      CloseableHttpResponse response = null;
      HttpGet httpGet = new HttpGet(url);
      if (headers != null) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          httpGet.setHeader(entry.getKey(), entry.getValue());
        }
      }
      try {
        httpGet.setConfig(requestConfig);
        response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
      } finally {
        close(httpClient, response);
      }
    }
  }

  /**
   * 发送post请求
   *
   * @param urlStr 请求地址
   * @param headers 请求头
   * @param params 请求参数
   * @return 响应结果
   */
  public static String doPost(String urlStr, Map<String, String> headers, Object params)
      throws IOException {
    return doPost(urlStr, headers, params, DEFAULT_CHARSET);
  }

  /**
   * 发送post请求
   *
   * @param urlStr 请求地址
   * @param headers 请求头
   * @param params 请求参数
   * @param charset 字符集，为空时取默认值UTF-8
   * @return 响应结果
   */
  public static String doPost(
      String urlStr, Map<String, String> headers, Object params, String charset)
      throws IOException {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectTimeout(CONNECTTIMEOUT)
            .setConnectionRequestTimeout(CONNECTREQUESTTIMEOUT)
            .setSocketTimeout(SOCKETTIMEOUT)
            .build();
    HttpPost httpPost = new HttpPost(urlStr);
    httpPost.setConfig(requestConfig);
    CloseableHttpResponse httpResponse = null;
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        httpPost.setHeader(entry.getKey(), entry.getValue());
      }
    }
    try {
      if (params != null) {
        httpPost.setEntity(new StringEntity(JSON.toJSONString(params), charset));
      }
      httpResponse = httpClient.execute(httpPost);
      HttpEntity entity = httpResponse.getEntity();
      return EntityUtils.toString(entity);
    } finally {
      close(httpClient, httpResponse);
    }
  }

  private static String buildQuery(Map<String, String> params, String charset) {
    if (params != null && !params.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      Iterator var4 = params.entrySet().iterator();

      while (var4.hasNext()) {
        Map.Entry<String, String> entry = (Map.Entry) var4.next();
        if (first) {
          first = false;
        } else {
          sb.append("&");
        }

        String key = (String) entry.getKey();
        String value = (String) entry.getValue();
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
          try {
            sb.append(key).append("=").append(URLEncoder.encode(value, charset));
          } catch (UnsupportedEncodingException var9) {
          }
        }
      }

      return sb.toString();
    } else {
      return null;
    }
  }

  public static void close(CloseableHttpClient httpClient, CloseableHttpResponse response) {
    if (null != response) {
      try {
        response.close();
      } catch (IOException var23) {
        var23.printStackTrace();
      }
    }

    if (null != httpClient) {
      try {
        httpClient.close();
      } catch (IOException var22) {
        var22.printStackTrace();
      }
    }
  }

  public static String sendSSLPost(String url, String param) {
    StringBuilder result = new StringBuilder();
    String urlNameString = url + "?" + param;
    try {
      log.info("sendSSLPost - {}", urlNameString);
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(
          null, new TrustManager[] {new TrustAnyTrustManager()}, new java.security.SecureRandom());
      URL console = new URL(urlNameString);
      HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
      conn.setRequestProperty("accept", "*/*");
      conn.setRequestProperty("connection", "Keep-Alive");
      conn.setRequestProperty(
          "user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
      conn.setRequestProperty("Accept-Charset", "utf-8");
      conn.setRequestProperty("contentType", "utf-8");
      conn.setDoOutput(true);
      conn.setDoInput(true);

      conn.setSSLSocketFactory(sc.getSocketFactory());
      conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
      conn.connect();
      InputStream is = conn.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String ret = "";
      while ((ret = br.readLine()) != null) {
        if (ret != null && !"".equals(ret.trim())) {
          result.append(
              new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        }
      }
      log.info("recv - {}", result);
      conn.disconnect();
      br.close();
    } catch (ConnectException e) {
      log.error("调用HttpUtils.sendSSLPost ConnectException, url=" + url + ",param=" + param, e);
    } catch (SocketTimeoutException e) {
      log.error(
          "调用HttpUtils.sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param, e);
    } catch (IOException e) {
      log.error("调用HttpUtils.sendSSLPost IOException, url=" + url + ",param=" + param, e);
    } catch (Exception e) {
      log.error("调用HttpsUtil.sendSSLPost Exception, url=" + url + ",param=" + param, e);
    }
    return result.toString();
  }

  public static String sendSSLPost(
      String urlString,
      Map<String, String> header,
      String bodyString,
      String param,
      String requestMethod) {
    StringBuffer buffer = null;
    try {
      // 创建SSLContext
      SSLContext sslContext = SSLContext.getInstance("SSL");
      TrustManager[] tm = {new TrustAnyTrustManager()};
      // 初始化
      sslContext.init(null, tm, new java.security.SecureRandom());
      // 获取SSLSocketFactory对象
      SSLSocketFactory ssf = sslContext.getSocketFactory();
      URL url = null;
      if (param != null) {
        url = new URL(urlString + param);
      } else {
        url = new URL(urlString);
      }
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setUseCaches(false);
      conn.setRequestMethod(requestMethod);
      // header
      header.forEach(
          (key, value) -> {
            conn.setRequestProperty(key, value);
          });
      // 设置当前实例使用的SSLSoctetFactory
      conn.setSSLSocketFactory(ssf);
      conn.connect();
      // body
      if (null != bodyString) {
        OutputStream os = conn.getOutputStream();
        os.write(bodyString.getBytes(DEFAULT_CHARSET));
        os.close();
      }
      // 读取服务器端返回的内容
      InputStream is = conn.getInputStream();
      InputStreamReader isr = new InputStreamReader(is, DEFAULT_CHARSET);
      BufferedReader br = new BufferedReader(isr);
      buffer = new StringBuffer();
      String line = null;
      while ((line = br.readLine()) != null) {
        log.info("接口返回流={}", line);
        buffer.append(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return buffer.toString();
  }

  private static class TrustAnyTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[] {};
    }
  }

  private static class TrustAnyHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  }
}
