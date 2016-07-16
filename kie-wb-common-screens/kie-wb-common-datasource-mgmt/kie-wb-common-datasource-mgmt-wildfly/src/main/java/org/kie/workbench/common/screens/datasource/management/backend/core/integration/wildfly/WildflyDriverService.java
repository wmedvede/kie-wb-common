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

package org.kie.workbench.common.screens.datasource.management.backend.core.integration.wildfly;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;

import static org.jboss.as.controller.client.helpers.ClientConstants.*;

@ApplicationScoped
public class WildflyDriverService
        extends WildflyBaseService {

    @Inject
    private MavenArtifactResolver artifactResolver;

    @Inject
    private WildflyDeploymentService deploymentService;

    public void deploy( final String deploymentId,
            final String groupId, final String artifactId, final String versionId ) throws Exception {

        final URI uri = artifactResolver.resolve( groupId, artifactId, versionId );

        if ( uri == null ) {
            throw new Exception( "Unable to get driver library artifact for gav: " + groupId + ":" + artifactId + ":" + versionId );
        }

        final Path path = java.nio.file.Paths.get( uri );
        byte[] libContent = Files.readAllBytes( path );
        deploymentService.deployContent( deploymentId, deploymentId, libContent, true );
    }

    public void undeploy( final String deploymentId ) throws Exception {
        deploymentService.removeDeployment( deploymentId );
    }

    public List<WildflyDriverDef> getDeployedDrivers() throws Exception {

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
}