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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.kie.workbench.common.screens.datasource.management.service.DriverManagementService;
import org.kie.workbench.common.screens.datasource.management.util.DriverDefSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

@ApplicationScoped
public class DriverDefDeployerImpl
        implements DriverDefDeployer {

    Logger logger = LoggerFactory.getLogger( DriverDefDeployerImpl.class );

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    DataSourceDefQueryService queryService;

    @Inject
    DriverManagementService managementService;

    @Override
    public void deployGlobalDrivers() {
        try {
            logger.debug( "Starting global drivers deployment." );
            for ( DriverDefInfo driverDefInfo : queryService.findGlobalDrivers() ) {
                deployDriver( driverDefInfo );
            }
        } catch ( Exception e ) {
            logger.error( "An error was produced during global drivers deployment.", e );
        }
    }

    private void deployDriver( DriverDefInfo driverDefInfo ) {
        try {
            String source = ioService.readAllString( Paths.convert( driverDefInfo.getPath() ) );
            DriverDef driverDef = DriverDefSerializer.deserialize( source );
            managementService.deploy( driverDef );
        } catch ( Exception e ) {
            logger.error( "Driver deployment failed, driverDefInfo: " + driverDefInfo, e );
        }
    }

    @Override
    public void deployProjectDrivers( Path path ) {

    }
}
