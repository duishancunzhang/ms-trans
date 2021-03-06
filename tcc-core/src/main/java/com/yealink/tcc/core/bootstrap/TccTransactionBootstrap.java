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

package com.yealink.tcc.core.bootstrap;

import com.yealink.tcc.common.config.TccConfig;
import com.yealink.tcc.core.helper.SpringBeanUtils;
import com.yealink.tcc.core.service.TccInitService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * tcc bootstrap.
 * @author yl1997
 */
public class TccTransactionBootstrap extends TccConfig implements ApplicationContextAware {

    private final TccInitService tccInitService;

    @Autowired
    public TccTransactionBootstrap(final TccInitService tccInitService) {
        this.tccInitService = tccInitService;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        if(SpringBeanUtils.getInstance().getCfgContext() == null){

            SpringBeanUtils.getInstance().setCfgContext((ConfigurableApplicationContext) applicationContext);
            start(this);
        }
    }

    private void start(final TccConfig tccConfig) {
        tccInitService.initialization(tccConfig);
    }
}
