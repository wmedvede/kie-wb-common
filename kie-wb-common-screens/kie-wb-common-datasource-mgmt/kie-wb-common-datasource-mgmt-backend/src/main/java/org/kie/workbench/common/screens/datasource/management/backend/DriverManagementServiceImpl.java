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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.shared.exceptions.GenericPortableException;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DataSourceServicesProvider;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.DriverManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class DriverManagementServiceImpl
    implements DriverManagementService {

    private static final Logger logger = LoggerFactory.getLogger( DriverManagementServiceImpl.class );

    @Inject
    DataSourceServicesProvider servicesProvider;

    public DriverManagementServiceImpl() {
    }

    @PostConstruct
    public void init() {
        if ( servicesProvider.getDriverService() == null ) {
            logger.warn( "Driver services was not installed in current server." +
                    "Drivers features won't be available.");
        }
    }

    @Override
    public boolean isEnabled() {
        return servicesProvider.getDriverService() != null;
    }

    @Override
    public List<DriverDef> getDeployments() {

        assertDriverServices();
        try {
            return servicesProvider.getDriverService().getDeployments();
        } catch ( Exception e ) {
            logger.error( "getDeployments failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public DriverDeploymentInfo getDeploymentInfo( final String uuid ) {

        assertDriverServices();
        try {
            return servicesProvider.getDriverService().getDeploymentInfo( uuid );
        } catch ( Exception e ) {
            logger.error( "getDeploymentInfo for driver: " + uuid + " failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public DriverDeploymentInfo deploy( final DriverDef driverDef ) {

        assertDriverServices();
        try {
            return servicesProvider.getDriverService().deploy( driverDef );
        } catch ( Exception e ) {
            logger.error( "deployment of driver: " + driverDef + " failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void undeploy( final DriverDeploymentInfo deploymentInfo ) {

        assertDriverServices();
        try {
            servicesProvider.getDriverService().undeploy( deploymentInfo );
        } catch ( Exception e ) {
            logger.error( "undeployment of driver: " + deploymentInfo + " failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private void assertDriverServices() {
        if ( servicesProvider.getDriverService() == null ) {
            throw new GenericPortableException( "Driver services are not provided for current server.");
        }
    }
}