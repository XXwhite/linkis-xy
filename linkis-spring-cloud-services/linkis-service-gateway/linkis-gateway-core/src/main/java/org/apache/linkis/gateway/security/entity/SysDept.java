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

import java.util.ArrayList;
import java.util.List;

/**
 * @author colourness
 * @description 部门
 * @date 2023/3/13
 */
public class SysDept extends BaseEntity {
  private static final long serialVersionUID = 1L;

  /** 部门ID */
  private Long deptId;

  /** 父部门ID */
  private Long parentId;

  /** 祖级列表 */
  private String ancestors;

  /** 部门名称 */
  private String deptName;

  /** 显示顺序 */
  private String orderNum;

  /** 负责人 */
  private String leader;

  /** 联系电话 */
  private String phone;

  /** 邮箱 */
  private String email;

  /** 部门状态:0正常,1停用 */
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  private String delFlag;

  /** 父部门名称 */
  private String parentName;

  private Long approvalDept;

  private String approvalDeptName;

  /** 部门编码，记录的是（统一社会信用代码） */
  private String deptCode;
  /** 是否含有子集 */
  private boolean hasChildren;
  /** 是否选中标识 */
  private boolean isDisabled;
  /** 部门类型部门类型（0目录，1部门，2科室） */
  private String deptType;

  private String leaderName;

  public String getLeaderName() {
    return leaderName;
  }

  public void setLeaderName(String leaderName) {
    this.leaderName = leaderName;
  }

  public boolean isDisabled() {
    return isDisabled;
  }

  public void setDisabled(boolean disabled) {
    isDisabled = disabled;
  }

  public String getDeptType() {
    return deptType;
  }

  public void setDeptType(String deptType) {
    this.deptType = deptType;
  }

  public String getApprovalDeptName() {
    return approvalDeptName;
  }

  public void setApprovalDeptName(String approvalDeptName) {
    this.approvalDeptName = approvalDeptName;
  }

  public boolean isHasChildren() {
    return hasChildren;
  }

  public void setHasChildren(boolean hasChildren) {
    this.hasChildren = hasChildren;
  }

  public Long getApprovalDept() {
    return approvalDept;
  }

  public void setApprovalDept(Long approvalDept) {
    this.approvalDept = approvalDept;
  }

  public String getDeptCode() {
    return deptCode;
  }

  public void setDeptCode(String deptCode) {
    this.deptCode = deptCode;
  }

  /** 子部门 */
  private List<SysDept> children = new ArrayList<SysDept>();

  public Long getDeptId() {
    return deptId;
  }

  public void setDeptId(Long deptId) {
    this.deptId = deptId;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public String getAncestors() {
    return ancestors;
  }

  public void setAncestors(String ancestors) {
    this.ancestors = ancestors;
  }

  @NotBlank(message = "部门名称不能为空")
  @Size(min = 0, max = 30, message = "部门名称长度不能超过30个字符")
  public String getDeptName() {
    return deptName;
  }

  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  @NotBlank(message = "显示顺序不能为空")
  public String getOrderNum() {
    return orderNum;
  }

  public void setOrderNum(String orderNum) {
    this.orderNum = orderNum;
  }

  public String getLeader() {
    return leader;
  }

  public void setLeader(String leader) {
    this.leader = leader;
  }

  @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Email(message = "邮箱格式不正确")
  @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public String getParentName() {
    return parentName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  public List<SysDept> getChildren() {
    return children;
  }

  public void setChildren(List<SysDept> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("deptId", getDeptId())
        .append("parentId", getParentId())
        .append("ancestors", getAncestors())
        .append("deptName", getDeptName())
        .append("orderNum", getOrderNum())
        .append("leader", getLeader())
        .append("phone", getPhone())
        .append("email", getEmail())
        .append("status", getStatus())
        .append("delFlag", getDelFlag())
        .append("createBy", getCreateBy())
        .append("createTime", getCreateTime())
        .append("updateBy", getUpdateBy())
        .append("updateTime", getUpdateTime())
        .append("deptCode", getDeptCode())
        .append("hasChildren", isHasChildren())
        .append("approvalDeptName", getApprovalDeptName())
        .append("deptType", getDeptType())
        .append("isDisabled", isDisabled())
        .toString();
  }
}
