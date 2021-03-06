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

import com.yealink.tcc.common.config.TccConfig;
import com.yealink.tcc.common.enums.RepositorySupportEnum;
import com.yealink.tcc.common.enums.SerializeEnum;
import com.yealink.tcc.common.serializer.KryoSerializer;
import com.yealink.tcc.common.serializer.ObjectSerializer;
import com.yealink.tcc.common.utils.LogUtil;
import com.yealink.tcc.common.utils.ServiceBootstrap;
import com.yealink.tcc.core.coordinator.CoordinatorService;
import com.yealink.tcc.core.disruptor.publisher.TccTransactionEventPublisher;
import com.yealink.tcc.core.helper.SpringBeanUtils;
import com.yealink.tcc.core.service.TccInitService;
import com.yealink.tcc.core.spi.CoordinatorRepository;
import com.yealink.tcc.core.spi.repository.JdbcCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * tcc tcc init service.
 *
 * @author yl1997
 */
@Service("tccInitService")
public class TccInitServiceImpl implements TccInitService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TccInitServiceImpl.class);

    private final CoordinatorService coordinatorService;

    private final TccTransactionEventPublisher tccTransactionEventPublisher;

    @Autowired
    public TccInitServiceImpl(final CoordinatorService coordinatorService, final TccTransactionEventPublisher tccTransactionEventPublisher) {
        this.coordinatorService = coordinatorService;
        this.tccTransactionEventPublisher = tccTransactionEventPublisher;
    }

    /**
     * tcc initialization.
     *
     * @param tccConfig {@linkplain TccConfig}
     */
    @Override
    public void initialization(final TccConfig tccConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info("tcc shutdown now")));
        try {
            loadSpiSupport(tccConfig);
            tccTransactionEventPublisher.start(tccConfig.getBufferSize(), tccConfig.getConsumerThreads());
            coordinatorService.start(tccConfig);
        } catch (Exception ex) {
            LogUtil.error(LOGGER, " tcc init exception:{}", ex::getMessage);
            System.exit(1);
        }
        LogUtil.info(LOGGER, () -> "tcc init success!");
    }

    /**
     * load spi.
     *
     * @param tccConfig {@linkplain TccConfig}
     */
    private void loadSpiSupport(final TccConfig tccConfig) {
        //spi serialize
        final SerializeEnum serializeEnum = SerializeEnum.acquire(tccConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);
        final ObjectSerializer serializer = StreamSupport.stream(objectSerializers.spliterator(), false)
                .filter(objectSerializer -> Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize()))
                .findFirst().orElse(new KryoSerializer());
        //spi repository
        final RepositorySupportEnum repositorySupportEnum = RepositorySupportEnum.acquire(tccConfig.getRepositorySupport());
        final ServiceLoader<CoordinatorRepository> recoverRepositories = ServiceBootstrap.loadAll(CoordinatorRepository.class);
        final CoordinatorRepository repository = StreamSupport.stream(recoverRepositories.spliterator(), false)
                .filter(recoverRepository -> Objects.equals(recoverRepository.getScheme(), repositorySupportEnum.getSupport()))
                .findFirst().orElse(new JdbcCoordinatorRepository());
        repository.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(CoordinatorRepository.class.getName(), repository);
    }
}
