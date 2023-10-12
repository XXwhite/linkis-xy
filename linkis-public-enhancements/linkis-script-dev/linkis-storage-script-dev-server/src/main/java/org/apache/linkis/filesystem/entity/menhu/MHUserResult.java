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
import java.util.List;

/**
 * @description: 获取门户用户信息接口返回值
 * @create: 2023/04/12
 * @author: colourness
 */
public class MHUserResult implements Serializable {
  // 用户信息
  private MHUser user;
  // 角色信息
  private List<MHRole> roles;

  public MHUser getUser() {
    return user;
  }

  public void setUser(MHUser user) {
    this.user = user;
  }

  public List<MHRole> getRoles() {
    return roles;
  }

  public void setRoles(List<MHRole> roles) {
    this.roles = roles;
  }
}
