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

package com.yealink.tcc.common.bean.context;

import com.yealink.tcc.common.enums.TccActionEnum;
import com.yealink.tcc.common.enums.TccRoleEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * TccTransactionContext.
 * @author yl1997
 */
@Data
public class TccTransactionContext implements Serializable {

    private static final long serialVersionUID = -5289080166922118073L;

    /**
     * transId.
     */
    private String transId;

    /**
     * this tcc action. {@linkplain TccActionEnum}
     */
    private int action;

    /**
     * 事务参与的角色. {@linkplain TccRoleEnum}
     */
    private int role;

}
