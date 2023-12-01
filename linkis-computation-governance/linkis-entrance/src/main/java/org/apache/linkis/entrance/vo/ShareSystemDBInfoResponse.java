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

package org.apache.linkis.entrance.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 共享系統库表信息返回值-对接王彦斌
 * @create: 2023/06/13
 * @author: colourness
 */
public class ShareSystemDBInfoResponse implements Serializable {
  // ip
  private String ip;
  // 端口
  private String port;
  // 数据库名称
  private String databaseName;
  // 表名称
  private List<String> tableName;

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public List<String> getTableName() {
    return tableName;
  }

  public void setTableName(List<String> tableName) {
    this.tableName = tableName;
  }
}
