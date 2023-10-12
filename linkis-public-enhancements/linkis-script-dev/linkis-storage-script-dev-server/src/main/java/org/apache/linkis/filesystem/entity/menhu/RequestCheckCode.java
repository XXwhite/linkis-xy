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

package org.apache.linkis.filesystem.entity.menhu;

import java.io.Serializable;

/**
 * @description: 获取门户checkCode信息
 * @create: 2023/04/19
 * @author: colourness
 */
public class RequestCheckCode implements Serializable {
  // checkCode
  private String checkCode;

  public String getCheckCode() {
    return checkCode;
  }

  public void setCheckCode(String checkCode) {
    this.checkCode = checkCode;
  }
}