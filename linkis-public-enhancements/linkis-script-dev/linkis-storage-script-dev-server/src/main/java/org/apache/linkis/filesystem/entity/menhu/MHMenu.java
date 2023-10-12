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
 * @description: 门户菜单
 * @create: 2023/04/12
 * @author: colourness
 */
public class MHMenu implements Serializable {
  // 菜单ID
  private String menuId;
  // 菜单名称
  private String menuName;
  // 父菜单ID
  private String parentId;
  // 接入系统ID
  private String sysId;
  // 父级列表
  private String menuAncestors;
  // 显示顺序
  private String orderNum;
  // 路由地址
  private String path;
  // 组件路径
  private String component;
  // 路由参数
  private String query;
  // 是否为外链（0是 1否）
  private String isFrame;
  // 是否缓存（0缓存 1不缓存）
  private String isCache;
  // 菜单类型（M目录 C菜单 F按钮）
  private String menuType;
  // 菜单状态（0显示 1隐藏）
  private String visible;
  // 菜单状态（0正常 1停用）
  private String status;
  // 权限标识
  private String perms;
  // 菜单图标
  private String icon;
  // 创建者
  private String createBy;
  // 创建时间
  private String createTime;

  public String getMenuId() {
    return menuId;
  }

  public void setMenuId(String menuId) {
    this.menuId = menuId;
  }

  public String getMenuName() {
    return menuName;
  }

  public void setMenuName(String menuName) {
    this.menuName = menuName;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getSysId() {
    return sysId;
  }

  public void setSysId(String sysId) {
    this.sysId = sysId;
  }

  public String getMenuAncestors() {
    return menuAncestors;
  }

  public void setMenuAncestors(String menuAncestors) {
    this.menuAncestors = menuAncestors;
  }

  public String getOrderNum() {
    return orderNum;
  }

  public void setOrderNum(String orderNum) {
    this.orderNum = orderNum;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getIsFrame() {
    return isFrame;
  }

  public void setIsFrame(String isFrame) {
    this.isFrame = isFrame;
  }

  public String getIsCache() {
    return isCache;
  }

  public void setIsCache(String isCache) {
    this.isCache = isCache;
  }

  public String getMenuType() {
    return menuType;
  }

  public void setMenuType(String menuType) {
    this.menuType = menuType;
  }

  public String getVisible() {
    return visible;
  }

  public void setVisible(String visible) {
    this.visible = visible;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPerms() {
    return perms;
  }

  public void setPerms(String perms) {
    this.perms = perms;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }
}
