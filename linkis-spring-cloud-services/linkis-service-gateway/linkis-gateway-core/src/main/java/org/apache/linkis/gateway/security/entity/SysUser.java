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

package org.apache.linkis.gateway.security.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author colourness
 * @description 用户对象 sys_user
 * @date 2023/3/13
 */
public class SysUser extends BaseEntity {
  private static final long serialVersionUID = 1L;

  /** 用户ID */
  private Long userId;

  /** 部门ID */
  private Long deptId;

  /** 用户账号 */
  private String userName;

  public String getFullName() {
    // return fullName;
    return getRemark();
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  private String fullName;

  /** 用户昵称 */
  private String nickName;

  /** 用户邮箱 */
  private String email;

  /** 手机号码 */
  private String phonenumber;

  /** 用户性别 */
  private String sex;

  /** 用户头像 */
  private String avatar;

  /** 密码 */
  private String password;

  /** 盐加密 */
  private String salt;

  /** 帐号状态（0正常 1停用） */
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  private String delFlag;

  /** 最后登录IP */
  private String loginIp;

  /** 最后登录时间 */
  private Date loginDate;

  /** 部门对象 */
  private SysDept dept;

  /** 角色对象 */
  private List<SysRole> roles;

  /** 角色组 */
  private Long[] roleIds;

  /** 岗位组 */
  private Long[] postIds;

  /** 数据来源 1：本系统 2：吉星 */
  private String dataFrom;

  public String getDataFrom() {
    return dataFrom;
  }

  public void setDataFrom(String dataFrom) {
    this.dataFrom = dataFrom;
  }

  public SysUser() {}

  public SysUser(Long userId) {
    this.userId = userId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public boolean isAdmin() {
    return isAdmin(this.userId);
  }

  public static boolean isAdmin(Long userId) {
    return userId != null && 1L == userId;
  }

  public Long getDeptId() {
    return deptId;
  }

  public void setDeptId(Long deptId) {
    this.deptId = deptId;
  }

  @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  @NotBlank(message = "用户账号不能为空")
  @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Email(message = "邮箱格式不正确")
  @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
  public String getPhonenumber() {
    return phonenumber;
  }

  public void setPhonenumber(String phonenumber) {
    this.phonenumber = phonenumber;
  }

  public String getSex() {
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  @JsonIgnore
  @JsonProperty
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDelFlag() {
    return delFlag;
  }

  public void setDelFlag(String delFlag) {
    this.delFlag = delFlag;
  }

  public String getLoginIp() {
    return loginIp;
  }

  public void setLoginIp(String loginIp) {
    this.loginIp = loginIp;
  }

  public Date getLoginDate() {
    return loginDate;
  }

  public void setLoginDate(Date loginDate) {
    this.loginDate = loginDate;
  }

  public SysDept getDept() {
    return dept;
  }

  public void setDept(SysDept dept) {
    this.dept = dept;
  }

  public List<SysRole> getRoles() {
    return roles;
  }

  public void setRoles(List<SysRole> roles) {
    this.roles = roles;
    this.roleIds =
        roles.stream().map(o -> o.getRoleId()).collect(Collectors.toList()).toArray(new Long[0]);
  }

  public Long[] getPostIds() {
    return postIds;
  }

  public void setPostIds(Long[] postIds) {
    this.postIds = postIds;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("userId", getUserId())
        .append("deptId", getDeptId())
        .append("userName", getUserName())
        .append("nickName", getNickName())
        .append("email", getEmail())
        .append("phonenumber", getPhonenumber())
        .append("sex", getSex())
        .append("avatar", getAvatar())
        .append("password", getPassword())
        .append("salt", getSalt())
        .append("status", getStatus())
        .append("delFlag", getDelFlag())
        .append("loginIp", getLoginIp())
        .append("loginDate", getLoginDate())
        .append("createBy", getCreateBy())
        .append("createTime", getCreateTime())
        .append("updateBy", getUpdateBy())
        .append("updateTime", getUpdateTime())
        .append("remark", getRemark())
        .append("dept", getDept())
        .toString();
  }
}
