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

import com.yealink.tcc.common.bean.context.TccTransactionContext;
import com.yealink.tcc.common.bean.entity.TccTransaction;
import com.yealink.tcc.common.enums.TccActionEnum;
import com.yealink.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.yealink.tcc.core.concurrent.threadpool.TccThreadFactory;
import com.yealink.tcc.core.service.TccTransactionHandler;
import com.yealink.tcc.core.service.executor.TccTransactionExecutor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * this is transaction starter.
 *
 * @author yl1997
 */
@Component
public class StarterTccTransactionHandler implements TccTransactionHandler {

    private static final int MAX_THREAD = Runtime.getRuntime().availableProcessors() << 1;

    private final TccTransactionExecutor tccTransactionExecutor;

    private final Executor executor = new ThreadPoolExecutor(MAX_THREAD, MAX_THREAD, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            TccThreadFactory.create("tcc-execute", false),
            new ThreadPoolExecutor.AbortPolicy());

    @Autowired
    public StarterTccTransactionHandler(final TccTransactionExecutor tccTransactionExecutor) {
        this.tccTransactionExecutor = tccTransactionExecutor;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final TccTransactionContext context)
            throws Throwable {
        Object returnValue;
        try {
            TccTransaction tccTransaction = tccTransactionExecutor.begin(point);
            try {
                //execute try
                returnValue = point.proceed();
                tccTransaction.setStatus(TccActionEnum.TRYING.getCode());
                tccTransactionExecutor.updateStatus(tccTransaction);
            } catch (Throwable throwable) {
                //if exception ,execute cancel
                final TccTransaction currentTransaction = tccTransactionExecutor.getCurrentTransaction();
                executor.execute(() -> tccTransactionExecutor
                        .cancel(currentTransaction));
                throw throwable;
            }
            //execute confirm
            final TccTransaction currentTransaction = tccTransactionExecutor.getCurrentTransaction();
            executor.execute(() -> tccTransactionExecutor.confirm(currentTransaction));
        } finally {
            TransactionContextLocal.getInstance().remove();
            tccTransactionExecutor.remove();
        }
        return returnValue;
    }
}
