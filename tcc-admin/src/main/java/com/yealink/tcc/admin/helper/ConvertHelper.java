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

package com.yealink.tcc.admin.helper;

import com.yealink.tcc.admin.vo.TccCompensationVO;
import com.yealink.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.yealink.tcc.common.utils.DateUtils;

/**
 * ConvertHelper.
 *
 * @author yl1997()
 */
public class ConvertHelper {

    /**
     * build  TccCompensationVO.
     *
     * @param adapter {@linkplain CoordinatorRepositoryAdapter}
     * @return {@linkplain TccCompensationVO}
     */
    public static TccCompensationVO buildVO(final CoordinatorRepositoryAdapter adapter) {
        TccCompensationVO vo = new TccCompensationVO();
        vo.setTransId(adapter.getTransId());
        vo.setCreateTime(DateUtils.parseDate(adapter.getCreateTime()));
        vo.setRetriedCount(adapter.getRetriedCount());
        vo.setLastTime(DateUtils.parseDate(adapter.getLastTime()));
        vo.setVersion(adapter.getVersion());
        vo.setTargetClass(adapter.getTargetClass());
        vo.setTargetMethod(adapter.getTargetMethod());
        vo.setConfirmMethod(adapter.getConfirmMethod());
        vo.setCancelMethod(adapter.getCancelMethod());
        return vo;
    }

}
