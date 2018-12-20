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

package com.yealink.tcc.core.service.handler;

import com.yealink.tcc.annotation.Tcc;
import com.yealink.tcc.common.bean.context.TccTransactionContext;
import com.yealink.tcc.common.bean.entity.Participant;
import com.yealink.tcc.common.bean.entity.TccInvocation;
import com.yealink.tcc.common.enums.TccActionEnum;
import com.yealink.tcc.core.service.TccTransactionHandler;
import com.yealink.tcc.core.service.executor.TccTransactionExecutor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * InlineTccTransactionHandler.
 * This is the method annotated by TCC within an actor.
 *
 * @author yl1997
 */
@Component
public class LocalTccTransactionHandler implements TccTransactionHandler {

    private final TccTransactionExecutor tccTransactionExecutor;

    @Autowired
    public LocalTccTransactionHandler(final TccTransactionExecutor tccTransactionExecutor) {
        this.tccTransactionExecutor = tccTransactionExecutor;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final TccTransactionContext context) throws Throwable {
        if (TccActionEnum.TRYING.getCode() == context.getAction()) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Class<?> clazz = point.getTarget().getClass();
            Object[] args = point.getArgs();
            final Tcc tcc = method.getAnnotation(Tcc.class);
            TccInvocation confirmInvocation = null;
            String confirmMethodName = tcc.confirmMethod();
            String cancelMethodName = tcc.cancelMethod();
            if (StringUtils.isNoneBlank(confirmMethodName)) {
                confirmInvocation = new TccInvocation(clazz, confirmMethodName, method.getParameterTypes(), args);
            }
            TccInvocation cancelInvocation = null;
            if (StringUtils.isNoneBlank(cancelMethodName)) {
                cancelInvocation = new TccInvocation(clazz, cancelMethodName, method.getParameterTypes(), args);
            }
            final Participant participant = new Participant(context.getTransId(),
                    confirmInvocation, cancelInvocation);
            tccTransactionExecutor.registerByNested(context.getTransId(), participant);
        }
        return point.proceed();
    }

}
