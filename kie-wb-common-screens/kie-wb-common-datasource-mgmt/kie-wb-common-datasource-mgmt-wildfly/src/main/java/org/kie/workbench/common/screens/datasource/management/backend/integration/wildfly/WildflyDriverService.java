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

package org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DriverService;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;

import static org.jboss.as.controller.client.helpers.ClientConstants.*;

@Dependent
@Named(value = "WildflyDriverService" )
public class WildflyDriverService
        extends WildflyBaseService
        implements DriverService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private WildflyDeploymentService deploymentService;

    @Override
    public DriverDeploymentInfo getDeploymentInfo( final String uuid ) throws Exception {
        for ( DriverDeploymentInfo deploymentInfo : getAllDeploymentInfo() ) {
            if ( uuid.equals( deploymentInfo.getUuid() ) ) {
                return deploymentInfo;
            }
        }
        return null;
    }

    @Override
    public void deploy( final DriverDef driverDef ) throws Exception {

        byte[] libContent = ioService.readAllBytes( Paths.convert( driverDef.getDriverLib() ) );
        deploymentService.deployContent( driverDef.getUuid(), driverDef.getUuid(), libContent, true );

    }

    @Override
    public void undeploy( final String uuid ) throws Exception {
        deploymentService.removeDeployment( uuid );
    }

    public List<DriverDef> getDrivers() throws Exception {

        List<DriverDef> driverDefs = new ArrayList<>(  );
        DriverDef driverDef;

        for ( WildflyDriverDef internalDef : getInternalDrivers() ) {
            driverDef = new DriverDef();
            driverDef.setUuid( Util.normalizeDriverName( internalDef.getDriverName() ) );
            driverDef.setName( internalDef.getDeploymentName() );
            driverDef.setDriverClass( internalDef.getDriverClass() );
            driverDefs.add( driverDef );
        }

        return driverDefs;
    }

    @Override
    public List<DriverDeploymentInfo> getAllDeploymentInfo() throws Exception {

        List<DriverDeploymentInfo> deploymentInfos = new ArrayList<>(  );
        DriverDeploymentInfo deploymentInfo;

        for ( WildflyDriverDef internalDef : getInternalDrivers() ) {
            deploymentInfo = new DriverDeploymentInfo();
            deploymentInfo.setUuid( Util.normalizeDriverName( internalDef.getDriverName() ) );
            deploymentInfo.setInternalUuid( internalDef.getDriverName() );
            deploymentInfo.setDriverClass( internalDef.getDriverClass() );
            deploymentInfo.setManaged( true );

            deploymentInfos.add( deploymentInfo );
        }

        return deploymentInfos;
    }

    private List<WildflyDriverDef> getInternalDrivers() throws Exception {

        ModelNode operation = new ModelNode();
        operation.get( OP ).set( "installed-drivers-list" );
        operation.get( OP_ADDR ).add( "subsystem", "datasources" );

        ModelControllerClient client = null;
        List<WildflyDriverDef> drivers = new ArrayList<WildflyDriverDef>();

        try {
            client = createControllerClient();
            ModelNode response = client.execute( operation );
            WildflyDriverDef driver;
            String driverName;

            if ( !isFailure( response ) ) {

                if ( response.hasDefined( RESULT ) ) {

                    List<ModelNode> nodes = response.get( RESULT ).asList();

                    for ( ModelNode node : nodes ) {

                        driver = new WildflyDriverDef();
                        driverName = node.get( "driver-name" ).asString();
                        driver.setDriverName( driverName );

                        if ( node.hasDefined( "deployment-name" ) ) {
                            driver.setDeploymentName( node.get( "deployment-name" ).asString() );
                        }
                        if ( node.hasDefined( "driver-module-name" ) ) {
                            driver.setDriverModuleName( node.get( "driver-module-name" ).asString() );
                        }
                        if ( node.hasDefined( "module-slot" ) ) {
                            driver.setModuleSlot( node.get( "module-slot" ).asString() );
                        }
                        if ( node.hasDefined( "driver-class-name" ) ) {
                            driver.setDriverClass( node.get( "driver-class-name" ).asString() );
                        }
                        if ( node.hasDefined( "driver-datasource-class-name" ) ) {
                            driver.setDataSourceClass( node.get( "driver-datasource-class-name" ).asString() );
                        }
                        if ( node.hasDefined( "driver-xa-datasource-class-name" ) ) {
                            driver.setXaDataSourceClass( node.get( "driver-xa-datasource-class-name" ).asString() );
                        }
                        if ( node.has( "driver-major-version" ) ) {
                            driver.setMayorVersion( node.get( "driver-major-version" ).asInt() );
                        }
                        if ( node.has( "driver-minor-version" ) ) {
                            driver.setMayorVersion( node.get( "driver-minor-version" ).asInt() );
                        }
                        if ( node.has( "jdbc-compliant" ) ) {
                            driver.setJdbcCompliant( node.get( "jdbc-compliant" ).asBoolean() );
                        }
                        drivers.add( driver );
                    }

                }
            } else {
                checkResponse( response );
            }
        } finally {
            safeClose( client );
        }

        return drivers;
    }

    @Override
    public void loadConfig( Properties properties ) {
        super.loadConfig( properties );
        deploymentService.loadConfig( properties );
    }
}