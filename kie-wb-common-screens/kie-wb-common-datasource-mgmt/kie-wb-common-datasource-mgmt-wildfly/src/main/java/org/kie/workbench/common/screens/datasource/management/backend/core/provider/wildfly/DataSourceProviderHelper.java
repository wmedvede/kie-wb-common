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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDataSourceDef;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDataSourceManagementClient;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDriverDef;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDriverManagementClient;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;

/**
 * Helper class used by the WildflyDataSourceProvider to interact with the Wildfly server.
 */
@ApplicationScoped
public class DataSourceProviderHelper {

    private WildflyDataSourceManagementClient dataSourceMgmtService = new WildflyDataSourceManagementClient();

    private WildflyDriverManagementClient driverMgmtService = new WildflyDriverManagementClient();

    @Inject
    private MavenArtifactResolver artifactResolver;

    public void loadConfig( Properties properties ) {
        dataSourceMgmtService.loadConfig( properties );
        driverMgmtService.loadConfig( properties );
    }

    /**
     * Gets the deployment information about a driver definition.
     *
     * @param uuid the driver definition identifier.
     *
     * @return the deployment information for the driver definition of null if the driver wasn't deployed.
     *
     * @throws Exception exceptions may be thrown if e.g. communication with the Wildfly server fails, etc.
     */
    public DriverDeploymentInfo getDriverDeploymentInfo( final String uuid ) throws Exception {
        for ( DriverDeploymentInfo deploymentInfo : getDriversDeploymentInfo() ) {
            if ( uuid.equals( deploymentInfo.getUuid() ) ) {
                return deploymentInfo;
            }
        }
        return null;
    }

    /**
     * Gets the deployment information for all the drivers currently deployed on the Wildfly server.
     *
     * @return a list with the deployment information for all the drivers.
     *
     * @throws Exception exceptions may be thrown if e.g. communication with the Wildfly server fails, etc.
     */
    public List<DriverDeploymentInfo> getDriversDeploymentInfo() throws Exception {

        List<DriverDeploymentInfo> deploymentsInfo = new ArrayList<>(  );
        DriverDeploymentInfo deploymentInfo;
        String uuid;
        boolean managed;

        for ( WildflyDriverDef internalDef : driverMgmtService.getDeployedDrivers() ) {
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

    /**
     * Deploys a driver definition on the Wildfly server.
     *
     * @param driverDef A driver definition to be deployed.
     *
     * @return The deployment information for the just deployed driver.
     *
     * @throws Exception exceptions may be thrown if was not possible to deployDataSource the driver.
     */
    public DriverDeploymentInfo deployDriver( final DriverDef driverDef ) throws Exception {

        final URI uri = artifactResolver.resolve( driverDef.getGroupId(),
                driverDef.getArtifactId(), driverDef.getVersion() );
        if ( uri == null ) {
            throw new Exception( "Unable to get driver library artifact for gav: " + driverDef.getGroupId() +
                    ":" + driverDef.getArtifactId() +
                    ":" + driverDef.getVersion() );
        }

        String deploymentId = DeploymentIdGenerator.generateDeploymentId( driverDef );
        driverMgmtService.deploy( deploymentId, uri );
        return new DriverDeploymentInfo( deploymentId, true, driverDef.getUuid(), driverDef.getDriverClass() );
    }

    /**
     * Un-deploys a previously deployed driver definition.
     *
     * @param uuid identifier of the driver definition to be deployed.
     *
     * @throws Exception exceptions may be thrown if was not possible to deployDataSource the driver.
     */
    public void undeployDriver( final String uuid ) throws Exception {
        DriverDeploymentInfo deploymentInfo = getDriverDeploymentInfo( uuid );
        if ( deploymentInfo != null ) {
            driverMgmtService.undeploy( deploymentInfo.getDeploymentId() );
        }
    }

    /**
     * Gets the list of driver definitions for the currently deployed drivers.
     *
     * @return list with the definitions for the deployed drivers.
     *
     * @throws Exception exceptions may be thrown if e.g. communication with the Wildfly server fails, etc.
     */
    public List<DriverDef> getDeployedDrivers() throws Exception {

        List<DriverDef> driverDefs = new ArrayList<>(  );
        DriverDef driverDef;
        String uuid;

        for ( WildflyDriverDef internalDef : driverMgmtService.getDeployedDrivers() ) {
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

    /**
     * Gets the deployment information about a data source definition.
     *
     * @param uuid the data source definition identifier.
     *
     * @return the deployment information for the data source definition of null if no data source has been created
     * with the given uuid.
     *
     * @throws Exception exceptions may be thrown if e.g. communication with the Wildfly server fails, etc.
     */
    public DataSourceDeploymentInfo getDataSourceDeploymentInfo( final String uuid ) throws Exception {
        for ( DataSourceDeploymentInfo deploymentInfo : getDataSourcesDeploymentInfo() ) {
            if ( uuid.equals( deploymentInfo.getUuid() ) ) {
                return deploymentInfo;
            }
        }
        return null;
    }

    /**
     * Gets the deployment information for all the data sources currently defined on the Wildfly server.
     *
     * @return a list with the deployment information for all the data sources.
     *
     * @throws Exception exceptions may be thrown if e.g. communication with the Wildfly server fails, etc.
     */
    public List<DataSourceDeploymentInfo> getDataSourcesDeploymentInfo() throws Exception {
        List<WildflyDataSourceDef> dataSources = dataSourceMgmtService.getDataSources();
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

    /**
     * Gets the list of data source definitions for the currently defined data sources in the Wildfly server.
     *
     * @return list with the definitions for the defined data sources.
     *
     * @throws Exception exceptions may be thrown if e.g. communication with the Wildfly server fails, etc.
     */
    public List<DataSourceDef> getDefinedDataSources() throws Exception {

        List<WildflyDataSourceDef> dataSources;
        List<DataSourceDef> dataSourceDefs = new ArrayList<>( );
        DataSourceDef dataSourceDef;
        String dataSourceUuid;
        String driverUuid;

        dataSources = dataSourceMgmtService.getDataSources();
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

    /**
     * Creates a data source in the Wildfly server.
     *
     * @param dataSourceDef Data source definition to be created.
     *
     * @param jndi jndi name to be use the Wildly server to bound the data source in the jndi context.
     *
     * @return returns the deployment information for the created data source.
     *
     * @throws Exception exceptions may be thrown if the data source couldn't be created.
     */
    public DataSourceDeploymentInfo deployDataSource( final DataSourceDef dataSourceDef, final String jndi ) throws Exception {
        String deploymentId = DeploymentIdGenerator.generateDeploymentId( dataSourceDef );
        deployDataSource( dataSourceDef, jndi, deploymentId );
        return deployDataSource( dataSourceDef, jndi, deploymentId );
    }

    /**
     * Creates a data source in the Wildfly server.
     *
     * @param dataSourceDef Data source definition to be created.
     *
     * @param jndi jndi name to be use the Wildly server to bound the data source in the jndi context.
     *
     * @return returns the deployment information for the created data source.
     *
     * @throws Exception exceptions may be thrown if the data source couldn't be created.
     */
    public DataSourceDeploymentInfo deployDataSource( final DataSourceDef dataSourceDef, final String jndi, String deploymentId ) throws Exception {
        DriverDeploymentInfo driverDeploymentInfo = getDriverDeploymentInfo( dataSourceDef.getDriverUuid() );
        if ( driverDeploymentInfo == null ) {
            throw new Exception( "Required driver: " + dataSourceDef.getDriverUuid() + " has not been deployed." );
        }

        WildflyDataSourceDef wfDataSourceDef = buildWFDataSource( deploymentId,
                jndi, dataSourceDef, driverDeploymentInfo.getDeploymentId() );

        dataSourceMgmtService.createDataSource( wfDataSourceDef );
        return new DataSourceDeploymentInfo( dataSourceDef.getName(), true, dataSourceDef.getUuid() );
    }

    /**
     * Deletes a data source in the Widlfy server.
     *
     * @param uuid Identifier of the data source definiton to be deleted.
     *
     * @throws Exception exceptions may be thrown if the data source couldn't be deleted.
     */
    public void undeployDataSource( final String uuid ) throws Exception {
        DataSourceDeploymentInfo deploymentInfo = getDataSourceDeploymentInfo( uuid );
        if ( deploymentInfo != null ) {
            dataSourceMgmtService.deleteDataSource( deploymentInfo.getDeploymentId() );
        }
    }

    private WildflyDataSourceDef buildWFDataSource( String deploymentId,
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
    }

}
