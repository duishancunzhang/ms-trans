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

package com.yealink.tcc.demo.springcloud.order;

import com.yealink.tcc.spring.boot.starter.parent.config.TccConfigProperties;
import feign.Logger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpringCloudTccOrderApplication.
 * @author yl1997
 */
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.yealink.tcc.demo.springcloud.order.mapper")
public class SpringCloudTccOrderApplication {

    @Autowired
    TccConfigProperties tccConfigProperties;
    /**
     * main.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        SpringApplication.run(SpringCloudTccOrderApplication.class, args);
    }
    /**
     * feign日志级别
     *
     * @return
     */
//    @Bean
//    public Logger.Level feignLoggerLevel() {
//
//        return Logger.Level.FULL;
//    }
}
