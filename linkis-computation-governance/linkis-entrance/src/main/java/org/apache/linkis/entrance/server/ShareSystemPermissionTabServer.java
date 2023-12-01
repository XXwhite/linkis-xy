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

package org.apache.linkis.entrance.server;

import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.apache.linkis.entrance.parser.SqlParserUtils;
import org.apache.linkis.entrance.vo.ShareSystemDBInfoResponse;
import org.apache.linkis.entrance.vo.ShareSystemResponseBase;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.conf.ServerConfiguration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 共享系统库表权限服务
 * @create: 2023/06/14
 * @author: colourness
 */
@Component(ServiceNameConsts.SHARE_SYS_PERMISSION_SERVER)
public class ShareSystemPermissionTabServer {
  private static final Logger logger =
      LoggerFactory.getLogger(ShareSystemPermissionTabServer.class);

  public Message shareSysPermissionTabCheck(HttpServletRequest req, Map<String, Object> json) {
    String runType = (String) json.get(TaskConstant.RUNTYPE);
    if (StringUtils.isNotBlank(runType)) {
      if ("jdbc".equalsIgnoreCase(runType)) {
        String token = null;
        Cookie[] cookieArray = req.getCookies();
        logger.info("request.饼干 ={}", JSON.toJSONString(cookieArray));
        for (Cookie tmpCookie : cookieArray) {
          if ("x-token".equals(tmpCookie.getName())) {
            token = tmpCookie.getValue();
            break;
          }
        }
        if (StringUtils.isNotBlank(token)) {
          Map<String, String> getShareSysHeaders = new HashMap<String, String>();
          getShareSysHeaders.put(
              "Service-Id", ServerConfiguration.MH_SHARE_SYS_PER_SERVICE_ID().getValue());
          getShareSysHeaders.put("Content-Type", "application/json");
          getShareSysHeaders.put("Host", ServerConfiguration.MH_EOLINKER_HOST().getValue());
          getShareSysHeaders.put("Authorization", ServiceNameConsts.TOKEN_PREFIX + token);
          // 请求参数包含body和header内容
          logger.info(
              "getShareSysRequestEntity.url={}",
              ServerConfiguration.BDP_SHARE_SYS_PERMISSION_URI().getValue());
          logger.info("getShareSysRequestEntity.header={}", JSON.toJSONString(getShareSysHeaders));
          String responseShareSysInfo =
              HttpUtils.sendSSLPost(
                  ServerConfiguration.BDP_SHARE_SYS_PERMISSION_URI().getValue(),
                  getShareSysHeaders,
                  null,
                  null,
                  "GET");
          logger.info("response.entity={}", responseShareSysInfo);
          // 按照接口返回类型转化成实体类
          if (StringUtils.isNotBlank(responseShareSysInfo)) {
            // 请求接口成功
            ShareSystemResponseBase<List<ShareSystemDBInfoResponse>> responseBody =
                JSONObject.parseObject(
                    responseShareSysInfo,
                    new TypeReference<
                        ShareSystemResponseBase<List<ShareSystemDBInfoResponse>>>() {});
            String executionCode = (String) json.get(TaskConstant.EXECUTIONCODE);
            logger.info("控制台输入的sql.jdbc语句={}", executionCode);
            // 解析sql语句中的表名
            List<String> tableNameList = SqlParserUtils.getAllTableNameBySQL(executionCode);
            if (CollectionUtils.isEmpty(responseBody.getData())) {
              // 没有任何表权限
              return Message.error("没有共享平台访问表的权限");
            }
            if (CollectionUtils.isNotEmpty(tableNameList)) {
              // 比对表名是否在权限列表里
              List<ShareSystemDBInfoResponse> permissionTabList = responseBody.getData();
              logger.info("permissionTabList={}", JSON.toJSONString(permissionTabList));
              // 记录sql中用户没有权限的表
              List<String> noPermissionTabList = new ArrayList<>();
              noPermissionTabList.addAll(tableNameList);
              for (String tabName : tableNameList) {
                for (ShareSystemDBInfoResponse dbInfoResponse : permissionTabList) {
                  // 库名.表名写法校验
                  if (tabName.contains(".")) {
                    String[] dbAndTable = tabName.split(".");
                    if (dbInfoResponse.getDatabaseName().equals(dbAndTable[0])
                        && dbInfoResponse.getTableName().contains(dbAndTable[1])) {
                      noPermissionTabList.remove(tabName);
                      break;
                    }
                  } else {
                    // sql语句中没写【库名.表名】格式，目前无法确认

                  }
                }
              }
              if (CollectionUtils.isNotEmpty(noPermissionTabList)) {
                logger.warn("用户没有共享平台 ={} 数据库表权限接口异常", StringUtils.join(noPermissionTabList, ","));
                return Message.error(
                    "用户没有共享平台" + StringUtils.join(noPermissionTabList, ",") + "数据库表权限接口异常");
              }
            }
          } else {
            logger.warn("获取共享平台用户数据库表权限接口异常");
            // 没有任何表权限
            return Message.error("获取共享平台用户数据库表权限接口异常");
          }
        }
      }
    }
    return null;
  }
}
