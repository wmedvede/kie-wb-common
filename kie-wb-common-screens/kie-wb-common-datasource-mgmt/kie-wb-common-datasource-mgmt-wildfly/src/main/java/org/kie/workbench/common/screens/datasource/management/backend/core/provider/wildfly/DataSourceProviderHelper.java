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

package org.kie.workbench.common.screens.datasource.management.backend.core.provider.wildfly;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.DeploymentIdGenerator;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDataSourceDef;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDataSourceService;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDriverDef;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDriverService;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;

@ApplicationScoped
public class DataSourceProviderHelper {

    @Inject
    private WildflyDataSourceService dataSourceService;

    @Inject
    private WildflyDriverService driverService;

    public DriverDeploymentInfo getDriverDeploymentInfo( final String uuid ) throws Exception {
        for ( DriverDeploymentInfo deploymentInfo : getDriversDeploymentInfo() ) {
            if ( uuid.equals( deploymentInfo.getUuid() ) ) {
                return deploymentInfo;
            }
        }
        return null;
    }

    public List<DriverDeploymentInfo> getDriversDeploymentInfo() throws Exception {

        List<DriverDeploymentInfo> deploymentsInfo = new ArrayList<>(  );
        DriverDeploymentInfo deploymentInfo;
        String uuid;
        boolean managed;

        for ( WildflyDriverDef internalDef : driverService.getDeployedDrivers() ) {
            try {
                uuid = DeploymentIdGenerator.extractUuid( internalDef.getDriverName() );
                managed = true;
            } catch ( Exception e ) {
                uuid = internalDef.getDriverName();
                managed = false;
            }

            deploymentInfo = new DriverDeploymentInfo( internalDef.getDriverName(),
                    managed, uuid, internalDef.getDriverClass() );

            deploymentsInfo.add( deploymentInfo );
        }

        return deploymentsInfo;
    }

    public DriverDeploymentInfo deployDriver( final DriverDef driverDef ) throws Exception {

        String deploymentId = DeploymentIdGenerator.generateDeploymentId( driverDef );
        driverService.deploy( deploymentId, driverDef.getGroupId(), driverDef.getArtifactId(), driverDef.getVersion() );
        return new DriverDeploymentInfo( deploymentId, true, driverDef.getUuid(), driverDef.getDriverClass() );
    }

    public void undeployDriver( final String uuid ) throws Exception {
        DriverDeploymentInfo deploymentInfo = getDriverDeploymentInfo( uuid );
        if ( deploymentInfo != null ) {
            driverService.undeploy( deploymentInfo.getDeploymentId() );
        }
    }

    public List<DriverDef> getDeployedDrivers() throws Exception {

        List<DriverDef> driverDefs = new ArrayList<>(  );
        DriverDef driverDef;
        String uuid;

        for ( WildflyDriverDef internalDef : driverService.getDeployedDrivers() ) {
            driverDef = new DriverDef();
            try {
                uuid = DeploymentIdGenerator.extractUuid( internalDef.getDriverName() );
            } catch ( Exception e ) {
                uuid = internalDef.getDriverName();
            }
            driverDef.setUuid( uuid );
            driverDef.setName( internalDef.getDeploymentName() );
            driverDef.setDriverClass( internalDef.getDriverClass() );
            driverDefs.add( driverDef );
        }

        return driverDefs;
    }

    public DataSourceDeploymentInfo getDataSourceDeploymentInfo( final String uuid ) throws Exception {
        for ( DataSourceDeploymentInfo deploymentInfo : getDataSourcesDeploymentInfo() ) {
            if ( uuid.equals( deploymentInfo.getUuid() ) ) {
                return deploymentInfo;
            }
        }
        return null;
    }

    public List<DataSourceDeploymentInfo> getDataSourcesDeploymentInfo() throws Exception {
        List<WildflyDataSourceDef> dataSources = dataSourceService.getDeployedDataSources();
        List<DataSourceDeploymentInfo> result = new ArrayList<>( );
        DataSourceDeploymentInfo deploymentInfo;
        String uuid;
        boolean managed;

        for ( WildflyDataSourceDef internalDef : dataSources ) {
            try {
                uuid = DeploymentIdGenerator.extractUuid( internalDef.getName() );
                managed = true;
            } catch ( Exception e ) {
                uuid = internalDef.getName();
                managed = false;
            }
            deploymentInfo = new DataSourceDeploymentInfo( internalDef.getName(),
                    managed, uuid );
            result.add( deploymentInfo );
        }
        return result;
    }

    public List<DataSourceDef> getDeployments() throws Exception {

        List<WildflyDataSourceDef> dataSources;
        List<DataSourceDef> dataSourceDefs = new ArrayList<>( );
        DataSourceDef dataSourceDef;
        String dataSourceUuid;
        String driverUuid;

        dataSources = dataSourceService.getDeployedDataSources();
        for ( WildflyDataSourceDef internalDef : dataSources ) {
            dataSourceDef = new DataSourceDef();
            try {
                dataSourceUuid = DeploymentIdGenerator.extractUuid( internalDef.getName() );
            } catch ( Exception e ) {
                dataSourceUuid = internalDef.getName();
            }
            try {
                driverUuid = DeploymentIdGenerator.extractUuid( internalDef.getDriverName() );
            } catch ( Exception e ) {
                driverUuid = internalDef.getDriverName();
            }

            dataSourceDef.setUuid( dataSourceUuid );
            dataSourceDef.setName( internalDef.getName() );
            dataSourceDef.setJndi( internalDef.getJndi() );
            dataSourceDef.setConnectionURL( internalDef.getConnectionURL() );
            dataSourceDef.setDriverUuid( driverUuid );
            dataSourceDef.setDriverClass( internalDef.getDriverClass() );
            dataSourceDef.setDataSourceClass( internalDef.getDataSourceClass() );
            dataSourceDef.setUser( internalDef.getUser() );
            dataSourceDef.setPassword( internalDef.getPassword() );
            dataSourceDef.setUseJTA( internalDef.isUseJTA() );
            dataSourceDef.setUseCCM( internalDef.isUseCCM() );
            dataSourceDefs.add( dataSourceDef );
        }

        return dataSourceDefs;
    }

    public DataSourceDeploymentInfo deploy( final DataSourceDef dataSourceDef, final String jndi ) throws Exception {
        String deploymentId = DeploymentIdGenerator.generateDeploymentId( dataSourceDef );
        deploy( dataSourceDef, jndi, deploymentId );
        return deploy( dataSourceDef, jndi, deploymentId );
    }

    public DataSourceDeploymentInfo deploy( final DataSourceDef dataSourceDef, final String jndi, String deploymentId ) throws Exception {
        DriverDeploymentInfo driverDeploymentInfo = getDriverDeploymentInfo( dataSourceDef.getDriverUuid() );
        if ( driverDeploymentInfo == null ) {
            throw new Exception( "Required driver: " + dataSourceDef.getDriverUuid() + " has not been deployed." );
        }

        WildflyDataSourceDef wfDataSourceDef = buildWFDataSource( deploymentId,
                jndi, dataSourceDef, driverDeploymentInfo.getDeploymentId() );

        dataSourceService.deploy( wfDataSourceDef );
        return new DataSourceDeploymentInfo( dataSourceDef.getName(), true, dataSourceDef.getUuid() );
    }

    public void undeployDataSource( final String uuid ) throws Exception {
        DataSourceDeploymentInfo deploymentInfo = getDataSourceDeploymentInfo( uuid );
        if ( deploymentInfo != null ) {
            dataSourceService.deleteDatasource( deploymentInfo.getDeploymentId() );
        }
    }

    public WildflyDataSourceDef buildWFDataSource( String deploymentId,
            String jndi, DataSourceDef dataSourceDef, String driverDeploymentId ) {
        WildflyDataSourceDef wfDataSourceDef = new WildflyDataSourceDef();
        wfDataSourceDef.setName( deploymentId );
        wfDataSourceDef.setDriverName( driverDeploymentId );
        wfDataSourceDef.setJndi( jndi );

        wfDataSourceDef.setConnectionURL( dataSourceDef.getConnectionURL() );
        wfDataSourceDef.setDriverClass( dataSourceDef.getDriverClass() );
        wfDataSourceDef.setDataSourceClass( dataSourceDef.getDataSourceClass() );
        wfDataSourceDef.setUser( dataSourceDef.getUser() );
        wfDataSourceDef.setPassword( dataSourceDef.getPassword() );
        wfDataSourceDef.setUseJTA( true );

        return wfDataSourceDef;
    };

}
