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
import com.yealink.tcc.core.helper.SpringBeanUtils;
import com.yealink.tcc.core.service.TccTransactionAspectService;
import com.yealink.tcc.core.service.TccTransactionFactoryService;
import com.yealink.tcc.core.service.TccTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * TccTransactionAspectServiceImpl.
 *
 * @author yl1997
 */
@Service("tccTransactionAspectService")
@SuppressWarnings("unchecked")
public class TccTransactionAspectServiceImpl implements TccTransactionAspectService {

    private final TccTransactionFactoryService tccTransactionFactoryService;

    @Autowired
    public TccTransactionAspectServiceImpl(final TccTransactionFactoryService tccTransactionFactoryService) {
        this.tccTransactionFactoryService = tccTransactionFactoryService;
    }

    /**
     * tcc transaction aspect.
     *
     * @param tccTransactionContext {@linkplain  TccTransactionContext}
     * @param point                {@linkplain ProceedingJoinPoint}
     * @return object  return value
     * @throws Throwable  exception
     */
    @Override
    public Object invoke(final TccTransactionContext tccTransactionContext, final ProceedingJoinPoint point) throws Throwable {
        final Class clazz = tccTransactionFactoryService.factoryOf(tccTransactionContext);
        final TccTransactionHandler txTransactionHandler = (TccTransactionHandler) SpringBeanUtils.getInstance().getBean(clazz);
        return txTransactionHandler.handler(point, tccTransactionContext);
    }
}
