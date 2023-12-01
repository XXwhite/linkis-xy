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

package org.apache.linkis.entrance.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.stat.TableStat;

/**
 * @description: 解析sql工具类
 * @create: 2023/06/13
 * @author: colourness
 */
public class SqlParserUtils {
  /**
   * 解析sql语句中所有表名
   *
   * @param sql
   * @return
   */
  public static List<String> getAllTableNameBySQL(String sql) {
    SQLStatementParser parser = new MySqlStatementParser(sql);
    // 使用Parser解析生成AST，这里SQLStatement就是AST
    SQLStatement sqlStatement = parser.parseStatement();
    MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
    sqlStatement.accept(visitor);
    Map<TableStat.Name, TableStat> tables = visitor.getTables();
    List<String> allTableName = new ArrayList<>();
    for (TableStat.Name t : tables.keySet()) {
      allTableName.add(t.getName());
    }
    return allTableName;
  }

  public static void main(String[] args) {
    String s = "select a.id from hs.test001 a left join hm.test002 b on a.id=b.id";
    List<String> tableNameList = getAllTableNameBySQL(s);
    System.out.println(tableNameList);
  }
}
