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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;

import org.kie.workbench.common.screens.datasource.management.backend.core.DataSource;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceProvider;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly.WildflyDriverManagementService;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceStatus;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the DataSourceProvider contract the CONTAINER type data sources that will be created in a
 * Wildfly server.
 */
@ApplicationScoped
@Named( value = "WildflyDataSourceProvider" )
public class WildflyDataSourceProvider
        implements DataSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger( WildflyDataSourceProvider.class );

    @Inject
    private DriverDefRegistry driverDefRegistry;

    @Inject
    private WildflyDataSourceManagementService dataSourceService;

    @Inject
    private WildflyDriverManagementService driverService;

    @Inject
    private DataSourceProviderHelper helper;

    private Map<String, WildlfyDataSource> initializedDataSources = new HashMap<>( );

    @Inject
    private MavenArtifactResolver artifactResolver;

    @Override
    public void loadConfig( Properties properties ) {
        dataSourceService.loadConfig( properties );
        driverService.loadConfig( properties );
    }

    @Override
    public void initialize( DataSourceDef dataSourceDef ) throws Exception {
        DriverDef driverDef = driverDefRegistry.getDriverDef( dataSourceDef.getDriverUuid() );
        if ( driverDef == null ) {
            throw new Exception( "No driver definition has been registered for driver uuid: " +
                    dataSourceDef.getDriverUuid() + " data source: " + dataSourceDef.getName() + " can not be initialized." );
        }

        DriverDeploymentInfo driverDeploymentInfo = helper.getDriverDeploymentInfo( dataSourceDef.getDriverUuid() );
        if ( driverDeploymentInfo == null ) {
            helper.deployDriver( driverDef );
        }

        unDeployExistingDataSources( dataSourceDef.getUuid() );

        //This random identifiers calculation should be removed when WF supports deletion
        //of data sources without letting them published on server until next restart.
        String random = "-" + System.currentTimeMillis();
        String deploymentId = DeploymentIdGenerator.generateDeploymentId( dataSourceDef ) + random;
        String deploymentJndi = JndiNameGenerator.generateJNDIName( dataSourceDef ) + random;

        helper.deployDataSource( dataSourceDef, deploymentJndi, deploymentId );

        javax.sql.DataSource dataSource = (javax.sql.DataSource) lookupObject( deploymentJndi );
        WildlfyDataSource wfDataSource = new WildlfyDataSource( dataSource );
        initializedDataSources.put( dataSourceDef.getUuid(), wfDataSource );
    }

    @Override
    public DataSource lookup( String uuid ) throws Exception {
        WildlfyDataSource dataSource = initializedDataSources.get( uuid );
        if ( dataSource != null ) {
            if ( dataSource.isNew() ) {
                //first access to the data source
                dataSource.setStatus( DataSourceStatus.RUNNING );
            }
            return dataSource;
        } else {
            throw new Exception( "Data source uuid: " + uuid + " was not initialized." );
        }
    }

    @Override
    public DataSourceStatus getStatus( String uuid ) {
        DataSource dataSource = initializedDataSources.get( uuid );
        return dataSource != null ? dataSource.getStatus() : null;
    }

    @Override
    public void release( DataSourceDef dataSourceDef ) throws Exception {
        unDeployExistingDataSources( dataSourceDef.getUuid() );
        WildlfyDataSource wfDataSource = initializedDataSources.remove( dataSourceDef.getUuid() );
        if ( wfDataSource != null ) {
            wfDataSource.setStatus( DataSourceStatus.STALE );
        }
    }

    private void unDeployExistingDataSources( String uuid ) {
        List<DataSourceDeploymentInfo> deploymentInfos = null;
        try {
            deploymentInfos = helper.getDataSourcesDeploymentInfo();
        } catch ( Exception e ) {
            logger.error( "un-deployment of existing data sources for uuid: " + uuid + " failed.", e );
            return;
        }

        if ( deploymentInfos != null ) {
            for ( DataSourceDeploymentInfo deploymentInfo : deploymentInfos ) {
                if ( deploymentInfo.getDeploymentId().contains( uuid ) ) {
                    try {
                        helper.undeployDataSource( deploymentInfo.getUuid() );
                    } catch ( Exception e ) {
                        logger.error( "un-deployment of: " + deploymentInfo + " failed", e );
                    }
                }
            }
        }
    }

    private Object lookupObject( String jndi ) {
        try {
            InitialContext context = new InitialContext();
            return context.lookup( jndi );
        } catch ( Exception e ) {
            logger.error( "an error was produced during object lookp jndi: " + jndi, e );
            return null;
        }
    }

    private void bindObject( String jndi, Object object ) {
        try {
            InitialContext context = new InitialContext();
            context.bind( jndi, object );
        } catch ( Exception e ) {
            logger.error( "an error was produced during object binding, jndi: " + jndi, e );
        }
    }
}