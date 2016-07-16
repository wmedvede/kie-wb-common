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

package org.kie.workbench.common.screens.datasource.management.backend.core.provider.dbcp;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceProvider;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.impl.AbstractDataSource;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSource;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefType;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceStatus;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;
import org.kie.workbench.common.screens.datasource.management.util.URLConnectionFactory;

/**
 * This class implements a DataSourceProvider for the CUSTOM type data sources.
 */
@ApplicationScoped
public class DBCPDataSourceProvider implements DataSourceProvider {

    private MavenArtifactResolver artifactResolver = new MavenArtifactResolver();

    private Map<String, DBCPDataSource> initializedDataSources = new HashMap<>(  );

    @Inject
    private DriverDefRegistry driverDefRegistry;

    public DBCPDataSourceProvider() {
    }

    @Override
    public void initialize( DataSourceDef dataSourceDef ) throws Exception {

        DriverDef driverDef = driverDefRegistry.getDriverDef( dataSourceDef.getDriverUuid() );
        if ( driverDef == null ) {
            throw new Exception( "Required driver: " + dataSourceDef.getDriverUuid() + " is not registered" );
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

        initializedDataSources.put( dataSourceDef.getUuid(), new DBCPDataSource( dataSource ) );
    }

    @Override
    public DataSource lookup( String uuid ) throws Exception {

        DBCPDataSource dataSource = initializedDataSources.get( uuid );
        if ( dataSource != null ) {
            if ( dataSource.isNew() ) {
                //first access to the data source
                dataSource.setStatus( DataSourceStatus.RUNNING );
            }
            return  dataSource;
        } else {
            throw new Exception( "Data source uuid: " + uuid + " was not initialized." );
        }
    }

    @Override
    public DataSourceStatus getStatus( String uuid ) {
        DBCPDataSource dataSource = initializedDataSources.get( uuid );
        return dataSource != null ? dataSource.getStatus() : null;
    }

    @Override
    public void release( DataSourceDef dataSourceDef ) throws Exception {
        DBCPDataSource dataSource = initializedDataSources.remove( dataSourceDef.getUuid() );
        if ( dataSource != null ) {
            dataSource.setStatus( DataSourceStatus.STALE );
        }
    }

    @Override
    public boolean accepts( DataSourceDefType type ) {
        return DataSourceDefType.CUSTOM.equals( type );
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

    private class DBCPDataSource extends AbstractDataSource {

        javax.sql.DataSource dataSource;

        public DBCPDataSource( javax.sql.DataSource dataSource ) {
            this.dataSource = dataSource;
        }

        @Override
        public Connection getConnection() throws Exception {
            return dataSource.getConnection();
        }

        public void setStatus( DataSourceStatus status ) {
            this.status = status;
            notifyStatusChange( status );
        }
    }
}