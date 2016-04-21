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

package org.kie.workbench.common.screens.datasource.management.backend.integration.jboss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DataSourceService;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;

import static org.jboss.as.controller.client.helpers.ClientConstants.*;

public class JBossDataSourceService
        extends JBossBaseService
        implements DataSourceService {

    public JBossDataSourceService() {
    }

    @Override
    public List<DataSourceDef> getDataSources() throws Exception {

        List<JBossDataSourceDef> dataSources;
        List<DataSourceDef> dataSourceDefs = new ArrayList<>( );
        DataSourceDef dataSourceDef;

        dataSources = getInternalDataSources();
        for ( JBossDataSourceDef internalDef : dataSources ) {
            dataSourceDef = new DataSourceDef();
            dataSourceDef.setName( internalDef.getName() );
            dataSourceDef.setJndi( internalDef.getJndi() );
            dataSourceDef.setConnectionURL( internalDef.getConnectionURL() );
            dataSourceDef.setDriverName( internalDef.getDriverName() );
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

    @Override
    public void deploy( DataSourceDef dataSourceDef ) throws Exception {
        createDatasource( dataSourceDef.getUuid(),
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
        deleteDatasource( uuid );
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
        List<JBossDataSourceDef> dataSources = getInternalDataSources();
        List<DataSourceDeploymentInfo> result = new ArrayList<DataSourceDeploymentInfo>( );
        DataSourceDeploymentInfo deploymentInfo;

        for ( JBossDataSourceDef internalDef : dataSources ) {
            deploymentInfo = new DataSourceDeploymentInfo();
            deploymentInfo.setUuid( internalDef.getName() );
            deploymentInfo.setJndi( internalDef.getJndi() );
            deploymentInfo.setManaged( true );
            result.add( deploymentInfo );
        }
        return result;
    }

    private List<JBossDataSourceDef> getInternalDataSources() throws Exception {

        List<JBossDataSourceDef> dataSources = new ArrayList<JBossDataSourceDef>( );
        JBossDataSourceDef dataSource;
        ModelNode response = null;
        ModelControllerClient client = null;

        try {
            client = createControllerClient();
            ModelNode operation = new ModelNode();

            ///subsystem=datasources:read-children-resources(child-type=data-source)
            operation.get( OP ).set( "read-children-resources" );
            operation.get( "child-type" ).set( "data-source" );
            operation.get( OP_ADDR ).add( "subsystem", "datasources" );

            response = client.execute( new OperationBuilder( operation ).build() );
            if ( !isFailure( response ) ) {
                if ( response.hasDefined( RESULT ) ) {
                    List<ModelNode> nodes = response.get( RESULT ).asList();
                    Property property;
                    ModelNode node;
                    for ( ModelNode resultNode : nodes ) {
                        property = resultNode.asProperty();
                        node = property.getValue();
                        dataSource = new JBossDataSourceDef();

                        dataSource.setName( property.getName() );
                        dataSource.setJndi( node.get( JBossDataSourceAttributes.JNDI_NAME ).asString() );
                        dataSource.setConnectionURL( node.get( JBossDataSourceAttributes.CONNECTION_URL ).asString() );
                        dataSource.setDriverName( node.get( JBossDataSourceAttributes.DRIVER_NAME ).asString() );
                        dataSource.setDriverClass( node.get( JBossDataSourceAttributes.DRIVER_CLASS ).asString() );
                        dataSource.setDataSourceClass( node.get( JBossDataSourceAttributes.DATASOURCE_CLASS ).asString() );
                        dataSource.setUser( node.get( JBossDataSourceAttributes.USER_NAME ).asString() );
                        dataSource.setPassword( node.get( JBossDataSourceAttributes.PASSWORD ).asString() );
                        dataSource.setUseJTA( node.get( JBossDataSourceAttributes.JTA ).asBoolean() );
                        dataSource.setUseCCM( node.get( JBossDataSourceAttributes.USE_CCM ).asBoolean() );

                        dataSources.add( dataSource );
                    }
                }
            }
        } finally {
            safeClose( client );
            checkResponse( response );
        }

        return dataSources;
    }

    /**
     * @param name Seems to not be used any more in EAP 6.4.6 (pool-name attribute is used to hold the name value)
     * @param jndi (required) Specifies the JNDI name for the datasource.
     * @param connectionURL (required) The JDBC driver connection URL.
     * @param driverClass The fully qualified name of the JDBC driver class. (seems to be mandatory en EAP 6.4.6)
     * @param datasourceClass The fully qualified name of the JDBC datasource class.
     * @param driverName (required) Defines the JDBC driver the datasource should use. It is a symbolic name matching the the
     * name of installed driver. In case the driver is deployed as jar, the name is the name of deployment unit.
     * @param user
     * @param password
     * @param poolName Seems like EAP 6.4.6 uses the name as the pool name.
     * @throws Exception
     */
    private void createDatasource( String name,
            String jndi,
            String connectionURL,
            String driverClass,
            String datasourceClass,
            String driverName,
            String user,
            String password,
            String poolName,
            Boolean useJTA,
            Boolean useCCM ) throws Exception {

        ModelNode operation = new ModelNode();
        operation.get( OP ).set( ADD );
        operation.get( OP_ADDR ).add( "subsystem", "datasources" );

        if ( name != null ) {
            //Seems to be no longer used in EAP 6.4.6.
            // The name entered in the management console goes directly to the pool-name attribute
            operation.get( OP_ADDR ).add( "data-source", name );
        }
        if ( jndi != null ) {
            operation.get( JBossDataSourceAttributes.JNDI_NAME ).set( jndi );
        }
        if ( connectionURL != null ) {
            operation.get( JBossDataSourceAttributes.CONNECTION_URL ).set( connectionURL );
        }
        if ( driverName != null ) {
            operation.get( JBossDataSourceAttributes.DRIVER_NAME ).set( driverName );
        }
        if ( driverClass != null ) {
            operation.get( JBossDataSourceAttributes.DRIVER_CLASS ).set( driverClass );
        }
        if ( datasourceClass != null ) {
            operation.get( JBossDataSourceAttributes.DATASOURCE_CLASS ).set( datasourceClass );
        }
        if ( user != null ) {
            operation.get( JBossDataSourceAttributes.USER_NAME ).set( user );
        }
        if ( password != null ) {
            operation.get( JBossDataSourceAttributes.PASSWORD ).set( password );
        }
        if ( useJTA != null ) {
            operation.get( JBossDataSourceAttributes.JTA ).set( useJTA );
        }
        if ( useCCM != null ) {
            operation.get( JBossDataSourceAttributes.USE_CCM ).set( useCCM );
        }

        if ( poolName != null ) {
            // not need to be set on EAP 6.4.6, it will be automatically set with the name of the datasource
            // in previous versions request.get( "pool-name" ).set( poolName ); should be used.
        }

        ModelControllerClient client = createControllerClient();
        ModelNode response = client.execute( new OperationBuilder( operation ).build() );

        safeClose( client );
        checkResponse( response );
    }

    private void updateDatasource( String name, Map<String, Object> changeSet ) throws Exception {

        //note: in order to update a datasource it should first be disabled.

        //The operation is a composite operation of multiple attribute changes.
        ModelNode operation = new ModelNode();
        operation.get( OP ).set( COMPOSITE );
        operation.get( OP_ADDR ).setEmptyList();

        //Use a template for copying the datasource address
        ModelNode stepTemplate = new ModelNode();
        stepTemplate.get( OP ).set( "write-attribute" );
        stepTemplate.get( OP_ADDR ).add( "subsystem", "datasources" );
        stepTemplate.get( OP_ADDR ).add( "data-source", name );

        ModelNode step = null;
        ModelNode stepValue;
        List<ModelNode> steps = new ArrayList<ModelNode>();
        Object value;

        for ( String attrName : changeSet.keySet() ) {

            value = changeSet.get( attrName );
            if ( value == null ) {
                continue;
            }
            step = stepTemplate.clone();
            step.get( NAME ).set( attrName );
            stepValue = step.get( "value" );
            //TODO, this works fine for String attributes but
            //should be improved to support other types admitted by the ModelNode
            //by now it's ok to assume strings.
            stepValue.set( value.toString() );

            steps.add( step );
        }

        operation.get( STEPS ).set( steps );

        ModelControllerClient client = createControllerClient();
        ModelNode response = client.execute( new OperationBuilder( operation ).build() );

        safeClose( client );
        checkResponse( response );
    }

    private void enableDatasource( String name, boolean enable ) throws Exception {

        final String opName = enable ? "enable" : "disable";

        ModelNode operation = new ModelNode( );
        operation.get( OP ).set( opName );
        operation.get( OP_ADDR ).add( "subsystem", "datasources");
        operation.get( OP_ADDR ).add( "data-source", name );

        if ( ! enable  ) {
            operation.get( OPERATION_HEADERS ).get( "allow-resource-service-restart" ).set( true );
        }

        ModelControllerClient client = createControllerClient();
        ModelNode result = client.execute( operation );
        safeClose( client );
        checkResponse( result );

    }

    private void deleteDatasource( String name ) throws Exception {

        ModelNode operation = new ModelNode( );
        operation.get( OP ).set( "remove" );
        operation.get( OP_ADDR ).add( "subsystem", "datasources" );
        operation.get( OP_ADDR ).add( "data-source", name );

        ModelControllerClient client = createControllerClient();
        ModelNode result = client.execute( operation );
        safeClose( client );
        checkResponse( result );
    }

}
