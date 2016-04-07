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

import org.kie.workbench.common.screens.datasource.management.backend.jboss.JBossDataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.backend.jboss.JBossDataSourceService;

@ApplicationScoped
public class JBossDataSourceDeploymentService
        implements DataSourceDeploymentService {

    JBossDataSourceService dataSourceManager = new JBossDataSourceService();

    @Override
    public void deploy( DataSourceDef dataSourceDef ) throws Exception {
        dataSourceManager.createDatasource( dataSourceDef.getUuid(),
                dataSourceDef.getJndi(),
                dataSourceDef.getConnectionURL(),
                dataSourceDef.getDriverClass(),
                dataSourceDef.getDataSourceClass(),
                dataSourceDef.getDriverName(),
                dataSourceDef.getUser(),
                dataSourceDef.getPassword(),
                null,
                dataSourceDef.isUseJTA(),
                dataSourceDef.isUseCCM() );
    }

    @Override
    public void undeploy( String uuid ) throws Exception {
        dataSourceManager.deleteDatasource( uuid );
    }

    @Override
    public DataSourceDeploymentInfo getDeploymentInfo( String uuid ) throws Exception {
        for ( DataSourceDeploymentInfo deploymentInfo : getAllDeploymentInfo() ) {
            if ( uuid.equals( deploymentInfo.getUuid() ) ) {
                return deploymentInfo;
            }
        }
        return null;
    }

    @Override
    public List<DataSourceDeploymentInfo> getAllDeploymentInfo() throws Exception {
        List<JBossDataSourceDef> dataSources = dataSourceManager.getDataSources();
        List<DataSourceDeploymentInfo> result = new ArrayList<DataSourceDeploymentInfo>( );
        DataSourceDeploymentInfo deploymentInfo;

        for ( JBossDataSourceDef ds : dataSources ) {
            deploymentInfo = new DataSourceDeploymentInfo();
            deploymentInfo.setUuid( ds.getName() );
            deploymentInfo.setJndi( ds.getJndi() );
            deploymentInfo.setManaged( true );
            result.add( deploymentInfo );
        }
        return result;
    }
}