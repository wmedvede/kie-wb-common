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

package org.kie.workbench.common.screens.datasource.management.backend.core.impl;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.screens.datasource.management.backend.DataSourceManagementBootstrap;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSource;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceManager;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceManagerRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceProviderOLD;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefCache;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefCacheEntry;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverRuntimeInfo;

@ApplicationScoped
public class DataSourceManagerImpl
        implements DataSourceManager {

    private DataSourceManagerRegistry dataSourceManagerRegistry;

    private DriverDefCache driverDefCache;

    private DataSourceManagementBootstrap dataSourceManagementConfig;

    public DataSourceManagerImpl() {
    }

    @Inject
    public DataSourceManagerImpl( DataSourceManagerRegistry dataSourceManagerRegistry,
            DriverDefCache driverDefCache,
            DataSourceManagementBootstrap dataSourceManagementConfig ) {
        this.dataSourceManagerRegistry = dataSourceManagerRegistry;
        this.driverDefCache = driverDefCache;
        this.dataSourceManagementConfig = dataSourceManagementConfig;
    }

    @Override
    public DataSource lookup( String uuid ) throws Exception {

        DataSourceDef dataSourceDef = dataSourceManagerRegistry.getDataSourceDef( uuid );
        if ( dataSourceDef == null ) {
            throw new Exception( "No data source definition has been registered for uuid : " + uuid );
        }
        DataSourceProviderOLD dataSourceProviderOLD = dataSourceManagementConfig.getDataSourceProviderOLD();
        if ( dataSourceProviderOLD == null ) {
            throw new Exception( "No data source provider has been registered for data source: " + dataSourceDef );
        }

        return dataSourceProviderOLD.lookup( uuid );
    }

    @Override
    public DataSourceRuntimeInfo getDataSourceRuntimeInfo( String uuid ) {
        DataSourceDef dataSourceDef = dataSourceManagerRegistry.getDataSourceDef( uuid );
        if ( dataSourceDef == null ) {
            return null;
        }

        DataSourceProviderOLD provider = dataSourceManagementConfig.getDataSourceProviderOLD();
        if ( provider == null ) {
            return null;
        }

        return new DataSourceRuntimeInfo( provider.getStatus( uuid ) );
    }

    @Override
    public DriverRuntimeInfo getDriverRuntimeInfo( String uuid ) {
        DriverDefCacheEntry entry = driverDefCache.get( uuid );
        if ( entry == null ) {
            return null;
        }

        List<DataSourceRuntimeInfo> dependantsInfo = new ArrayList<>();
        for ( DataSourceDef dataSourceDef : entry.getDependants() ) {
            dependantsInfo.add( getDataSourceRuntimeInfo( dataSourceDef.getUuid() ) );
        }

        return new DriverRuntimeInfo( dependantsInfo );
    }
}
