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
import javax.inject.Inject;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.backend.jboss.JBossDataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.backend.jboss.JBossDataSourceService;

@Service
@ApplicationScoped
public class DataSourceManagementServiceImpl
        implements DataSourceManagementService {

    JBossDataSourceService dataSourceManager = new JBossDataSourceService();

    @Inject
    DataSourceDeploymentService deploymentService;

    @Override
    public List<DataSourceDef> getDataSources() {

        List<JBossDataSourceDef> serverSources = null;
        List<DataSourceDef> dataSourceDefs = new ArrayList<DataSourceDef>( );
        DataSourceDef dataSourceDef;

        try {
            serverSources =  dataSourceManager.getDataSources();
            for ( JBossDataSourceDef ds : serverSources ) {
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

    @Override
    public List<DataSourceDeploymentInfo> getSystemDataSources() {

        List<JBossDataSourceDef> serverSources = null;
        List<DataSourceDeploymentInfo> result = new ArrayList<DataSourceDeploymentInfo>( );
        DataSourceDeploymentInfo deploymentInfo;

        try {
            serverSources =  dataSourceManager.getDataSources();
            for ( JBossDataSourceDef ds : serverSources ) {
                deploymentInfo = new DataSourceDeploymentInfo();
                deploymentInfo.setUuid( ds.getName() );
                deploymentInfo.setJndi( ds.getJndi() );
                deploymentInfo.setManaged( true );
                result.add( deploymentInfo );
            }

            return result;
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage() );
        }
    }

    @Override
    public DataSourceDeploymentInfo getDeploymentInfo( String uuid ) {
        try {
            return deploymentService.getDeploymentInfo( uuid );
        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void deploy( DataSourceDef dataSourceDef ) {
        try {
            deploymentService.deploy( dataSourceDef );
        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void undeploy( String uuid ) {
        try {
            deploymentService.undeploy( uuid );
        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }
}
