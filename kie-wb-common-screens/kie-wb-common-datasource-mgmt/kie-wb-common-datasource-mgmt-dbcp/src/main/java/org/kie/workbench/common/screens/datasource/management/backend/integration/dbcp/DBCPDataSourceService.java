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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.naming.InitialContext;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DataSourceService;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;

@Dependent
@Named(value = "DBCPDataSourceService" )
public class DBCPDataSourceService
    implements DataSourceService {

    Map<String, PoolingDataSource<PoolableConnection>> deploymentRegistry = new HashMap<>(  );

    @Override
    public void deploy( DataSourceDef dataSourceDef ) throws Exception {
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory( dataSourceDef.getConnectionURL(),
                dataSourceDef.getUser(),
                dataSourceDef.getPassword() );

        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory( connectionFactory, null );

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>( poolableConnectionFactory );

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool( connectionPool );

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>( connectionPool );


        deploymentRegistry.put( dataSourceDef.getUuid(), dataSource );

        InitialContext context = new InitialContext(  );

        //The standard java:comp, java:module and java:app are typically read only

        bindObject( "java:global/UNO", dataSource );
        bindObject( "java:app/DOS", dataSource );
        bindObject( "java:module/TRES", dataSource );
        bindObject( "java:comp/CUATRO", dataSource );

        bindObject( "java:/CINCO", dataSource );

        //register in the JNDI context
    }

    private void bindObject( String namingContext, Object object ) throws Exception {
        final InitialContext context = new InitialContext( );

        try {
            context.bind( namingContext, object );
        } catch ( Exception e ) {
            System.out.println( "Error al hacer el binding en el contexto: " + namingContext +
            " del objeto: " + object );
            e.printStackTrace();
        }
    }


    @Override
    public void undeploy( String uuid ) throws Exception {
        PoolingDataSource<PoolableConnection> dataSource = deploymentRegistry.remove( uuid );
        if ( dataSource != null ) {
            try {
                dataSource.close();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public DataSourceDeploymentInfo getDeploymentInfo( String uuid ) throws Exception {
        PoolingDataSource<PoolableConnection> dataSource = deploymentRegistry.get( uuid );
        DataSourceDeploymentInfo result = null;
        if ( dataSource != null ) {
            result = new DataSourceDeploymentInfo();
            result.setUuid( uuid );
        }
        return result;
    }

    @Override
    public List<DataSourceDeploymentInfo> getAllDeploymentInfo() throws Exception {
        return new ArrayList<>(  );
    }

    @Override
    public List<DataSourceDef> getDataSources() throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void loadConfig( Properties properties ) {
        //bla bla bla
    }
}