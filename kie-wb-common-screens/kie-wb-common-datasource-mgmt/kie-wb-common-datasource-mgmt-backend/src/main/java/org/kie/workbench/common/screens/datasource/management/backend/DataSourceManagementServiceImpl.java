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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;

import org.kie.workbench.integration.DataSourceManager;

@Service
@ApplicationScoped
public class DataSourceManagementServiceImpl
        implements DataSourceManagementService {

    DataSourceManager dataSourceManager;

    @Override
    public List<DataSourceDef> getDataSources() {

        List<org.kie.workbench.integration.DataSourceDef> serverSources = null;
        List<DataSourceDef> dataSourceDefs = new ArrayList<DataSourceDef>( );
        DataSourceDef dataSourceDef;

        try {
            serverSources =  dataSourceManager.getDataSources();
            for ( org.kie.workbench.integration.DataSourceDef ds : serverSources ) {
                dataSourceDef = new DataSourceDef();
                dataSourceDef.setName( ds.getName() );
                dataSourceDef.setJndi( ds.getJndi() );
                dataSourceDef.setConnectionURL( ds.getConnectionURL() );
                dataSourceDef.setDriverName( ds.getDriverName() );
                dataSourceDef.setDriverClass( ds.getDriverClass() );
                dataSourceDef.setDataSourceClass( ds.getDataSourceClass() );
                dataSourceDef.setUser( ds.getUser() );
                dataSourceDef.setPassword( ds.getPassword() );
                dataSourceDef.setUseJTA( ds.isUseJTA() );
                dataSourceDef.setUseCCM( ds.isUseCCM() );
                dataSourceDefs.add( dataSourceDef );
            }

            return dataSourceDefs;
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage() );
        }
    }
}
