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

package com.yealink.tcc.springcloud.feign;

import com.yealink.tcc.annotation.Tcc;
import com.yealink.tcc.common.bean.context.TccTransactionContext;
import com.yealink.tcc.common.bean.entity.Participant;
import com.yealink.tcc.common.bean.entity.TccInvocation;
import com.yealink.tcc.common.enums.TccActionEnum;
import com.yealink.tcc.common.enums.TccRoleEnum;
import com.yealink.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.yealink.tcc.core.helper.SpringBeanUtils;
import com.yealink.tcc.core.service.executor.TccTransactionExecutor;

import feign.InvocationHandlerFactory.MethodHandler;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * yealinkFeignHandler.
 *
 * @author yl1997
 */
public class TccFeignHandler implements InvocationHandler {

    private Map<Method, MethodHandler> handlers;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            final Tcc tcc = method.getAnnotation(Tcc.class);
            if (Objects.isNull(tcc)) {
                return this.handlers.get(method).invoke(args);
            }
            try {
                final TccTransactionContext tccTransactionContext = TransactionContextLocal.getInstance().get();
                if (Objects.nonNull(tccTransactionContext)) {
                    if (tccTransactionContext.getRole() == TccRoleEnum.LOCAL.getCode()) {
                        tccTransactionContext.setRole(TccRoleEnum.INLINE.getCode());
                    }
                }
                final TccTransactionExecutor TccTransactionExecutor =
                        SpringBeanUtils.getInstance().getBean(TccTransactionExecutor.class);
                final Object invoke = this.handlers.get(method).invoke(args);
                final Participant participant = buildParticipant(tcc, method, args, tccTransactionContext);
                if (tccTransactionContext.getRole() == TccRoleEnum.INLINE.getCode()) {
                    TccTransactionExecutor.registerByNested(tccTransactionContext.getTransId(),
                            participant);
                } else {
                    TccTransactionExecutor.enlistParticipant(participant);
                }
                return invoke;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw throwable;
            }
        }
    }

    private Participant buildParticipant(final Tcc tcc, final Method method, final Object[] args,
                                         final TccTransactionContext tccTransactionContext) {
        if (Objects.isNull(tccTransactionContext)
                || (TccActionEnum.TRYING.getCode() != tccTransactionContext.getAction())) {
            return null;
        }
        //获取协调方法
        String confirmMethodName = tcc.confirmMethod();
        if (StringUtils.isBlank(confirmMethodName)) {
            confirmMethodName = method.getName();
        }
        String cancelMethodName = tcc.cancelMethod();
        if (StringUtils.isBlank(cancelMethodName)) {
            cancelMethodName = method.getName();
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        TccInvocation confirmInvocation = new TccInvocation(declaringClass, confirmMethodName, method.getParameterTypes(), args);
        TccInvocation cancelInvocation = new TccInvocation(declaringClass, cancelMethodName, method.getParameterTypes(), args);
        //封装调用点
        return new Participant(tccTransactionContext.getTransId(), confirmInvocation, cancelInvocation);
    }

    /**
     * set handlers.
     *
     * @param handlers handlers
     */
    public void setHandlers(final Map<Method, MethodHandler> handlers) {
        this.handlers = handlers;
    }

}
