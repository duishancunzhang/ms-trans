/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yealink.tcc.common.config;

import lombok.Data;

/**
 * mongo 保存事务日志配置.
 * @author yl1997
 */
@Data
public class TccMongoConfig {

    /**
     * mongo数据库设置.
     */
    private String mongoDbName;

    /**
     * mongo数据库URL.
     */
    private String mongoDbUrl;

    /**
     * mongo数据库用户名.
     */
    private String mongoUserName;

    /**
     * mongo数据库密码.
     */
    private String mongoUserPwd;

}
