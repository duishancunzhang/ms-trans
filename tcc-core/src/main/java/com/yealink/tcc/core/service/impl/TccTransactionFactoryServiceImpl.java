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

package com.yealink.tcc.core.service.impl;

import com.yealink.tcc.common.bean.context.TccTransactionContext;
import com.yealink.tcc.common.enums.TccRoleEnum;
import com.yealink.tcc.core.service.TccTransactionFactoryService;
import com.yealink.tcc.core.service.handler.ConsumeTccTransactionHandler;
import com.yealink.tcc.core.service.handler.LocalTccTransactionHandler;
import com.yealink.tcc.core.service.handler.ParticipantTccTransactionHandler;
import com.yealink.tcc.core.service.handler.StarterTccTransactionHandler;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * TccTransactionFactoryServiceImpl.
 *
 * @author yl1997
 */
@Service("tccTransactionFactoryService")
public class TccTransactionFactoryServiceImpl implements TccTransactionFactoryService {

    /**
     * acquired TccTransactionHandler.
     *
     * @param context {@linkplain TccTransactionContext}
     * @return Class
     */
    @Override
    public Class factoryOf(final TccTransactionContext context) {
        if (Objects.isNull(context)) {
            return StarterTccTransactionHandler.class;
        } else {
            //why this code?  because spring cloud invoke has proxy.
            if (context.getRole() == TccRoleEnum.SPRING_CLOUD.getCode()) {
                context.setRole(TccRoleEnum.START.getCode());
                return ConsumeTccTransactionHandler.class;
            }
            // if context not null and role is inline  is ParticipantTccTransactionHandler.
            if (context.getRole() == TccRoleEnum.LOCAL.getCode()) {
                return LocalTccTransactionHandler.class;
            } else if (context.getRole() == TccRoleEnum.START.getCode()
                    || context.getRole() == TccRoleEnum.INLINE.getCode()) {
                return ParticipantTccTransactionHandler.class;
            }
            return ConsumeTccTransactionHandler.class;
        }
    }
}
