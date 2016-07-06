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

package org.kie.workbench.common.screens.datasource.management.backend.integration.dbcp;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DataSourceService;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DataSourceServicesProvider;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;
import org.kie.workbench.common.screens.datasource.management.util.URLConnectionFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Dependent
@Named(value = "DBCPDataSourceService" )
public class DBCPDataSourceService
    implements DataSourceService {

    //private static final Logger logger = LoggerFactory.getLogger( DBCPDataSourceService.class );

    @Inject
    private DataSourceServicesProvider servicesProvider;

    @Inject
    private MavenArtifactResolver artifactResolver;

    private Map<String, PoolingDataSource<PoolableConnection>> deploymentRegistry = new HashMap<>(  );

    private Map<String, DataSourceDeploymentInfo> deploymentInfos = new HashMap<>(  );

    private Map<String, DataSourceDef> deployedDataSources = new HashMap<>(  );

    @Override
    public DataSourceDeploymentInfo deploy( DataSourceDef dataSourceDef ) throws Exception {

        DriverDef driverDef = null;
        for ( DriverDef _driverDef : servicesProvider.getDriverService().getDeployments() ) {
            if ( _driverDef.getUuid().equals( dataSourceDef.getDriverUuid() ) ) {
                driverDef = _driverDef;
                break;
            }
        }

        if ( driverDef == null ) {
            throw new Exception( "Required driver: " + dataSourceDef.getUuid() + " is not deployed" );
        }

        final URI uri = artifactResolver.resolve( driverDef.getGroupId(),
                driverDef.getArtifactId(), driverDef.getVersion() );
        if ( uri == null ) {
            throw new Exception( "Unable to get driver library artifact for driver: " + driverDef );
        }

        final Properties properties = new Properties(  );
        properties.setProperty( "user", dataSourceDef.getUser() );
        properties.setProperty( "password", dataSourceDef.getPassword() );
        final URLConnectionFactory urlConnectionFactory = new URLConnectionFactory( uri.toURL(),
                driverDef.getDriverClass(),
                dataSourceDef.getConnectionURL(), properties );

        //Connection Factory that the pool will use for creating connections.
        ConnectionFactory connectionFactory = new DBCPConnectionFactory( urlConnectionFactory );

        //Poolable connection factory
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory( connectionFactory, null );

        //The pool to be used by the ConnectionFactory
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>( poolableConnectionFactory );

        //Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool( connectionPool );

        //Finally create DataSource
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>( connectionPool );

        //register the DataSource in the JNDI context:
        //The standard contexts "java:comp", "java:module" and "java:app" are typically read only
        //while the java:global should be r/w
        //wildfly/eap adds to additional r/w directories, java:jboss and java:/

        bindObject( dataSourceDef.getJndi(), dataSource );

        DataSourceDeploymentInfo deploymentInfo = new DataSourceDeploymentInfo( dataSourceDef.getUuid(),
                true, dataSourceDef.getUuid(), dataSourceDef.getJndi() );

        deploymentRegistry.put( deploymentInfo.getDeploymentId(), dataSource );
        deploymentInfos.put( deploymentInfo.getDeploymentId(), deploymentInfo );
        deployedDataSources.put( deploymentInfo.getDeploymentId(), dataSourceDef );

        return deploymentInfo;
    }

    @Override
    public void undeploy( DataSourceDeploymentInfo deploymentInfo ) throws Exception {
        DataSourceDeploymentInfo currentDeploymentInfo = deploymentInfos.get( deploymentInfo.getDeploymentId() );
        if ( currentDeploymentInfo == null ) {
            throw new Exception( "DataSource: " + deploymentInfo.getUuid() + " is not deployed" );
        }

        PoolingDataSource<PoolableConnection> dataSource = deploymentRegistry.remove(
                currentDeploymentInfo.getDeploymentId() );
        if ( dataSource != null ) {
            try {
                dataSource.close();
            } catch ( Exception e ) {
                //logger.warn( "An error was produced during datasource close", e );
            }
        }
        unbindObject( currentDeploymentInfo.getJndi() );
        deploymentRegistry.remove( currentDeploymentInfo.getDeploymentId() );
        deployedDataSources.remove( currentDeploymentInfo.getDeploymentId() );
        deploymentInfos.remove( currentDeploymentInfo.getDeploymentId() );
    }

    @Override
    public void update( DataSourceDef dataSourceDef ) throws Exception {
        throw new Exception( "not yet implemented." );
    }

    @Override
    public DataSourceDeploymentInfo getDeploymentInfo( String uuid ) throws Exception {
        return deploymentInfos.get( uuid );
    }

    @Override
    public List<DataSourceDeploymentInfo> getDeploymentsInfo() throws Exception {
        List<DataSourceDeploymentInfo> result = new ArrayList<>(  );
        result.addAll( deploymentInfos.values() );
        return result;
    }

    @Override
    public List<DataSourceDef> getDeployments() throws Exception {
        List<DataSourceDef> result = new ArrayList<>();
        result.addAll( deployedDataSources.values() );
        return result;
    }

    @Override
    public void loadConfig( Properties properties ) {
    }

    private class DBCPConnectionFactory
            implements ConnectionFactory {

        URLConnectionFactory urlConnectionFactory;

        public DBCPConnectionFactory( URLConnectionFactory urlConnectionFactory ) {
            this.urlConnectionFactory = urlConnectionFactory;
        }

        @Override
        public Connection createConnection() throws SQLException {
            return urlConnectionFactory.createConnection();
        }
    }

    private void bindObject( String namingContext, Object object ) throws Exception {
        final InitialContext context = new InitialContext( );
        try {
            context.bind( namingContext, object );
        } catch ( Exception e ) {
            //logger.error( "unable to bind datasource: {} in namingContext: {}", object, namingContext );
            throw new Exception( "unable to bind datasource in namingContext: " + namingContext, e );
        }
    }

    private void unbindObject( String namingContext ) throws Exception {
        final InitialContext context = new InitialContext( );
        try {
            context.unbind( namingContext );
        } catch ( Exception e ) {
            //logger.error( "unable to unbind datasource from namingContext: " + namingContext );
        }
    }
}