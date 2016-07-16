/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datasource.management.backend;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceManager;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceService;

@Service
@ApplicationScoped
public class DataSourceServiceImpl
        implements DataSourceService {

    @Inject
    private DataSourceManager dataSourceManager;

    public DataSourceServiceImpl() {
    }

    @Override
    public DataSourceRuntimeInfo getDataSourceRuntimeInfo( String uuid ) {
        return dataSourceManager.getDataSourceRuntimeInfo( uuid );
    }

    @Override
    public DriverRuntimeInfo getDriverRuntimeInfo( String uuid ) {
        return dataSourceManager.getDriverRuntimeInfo( uuid );
    }
}