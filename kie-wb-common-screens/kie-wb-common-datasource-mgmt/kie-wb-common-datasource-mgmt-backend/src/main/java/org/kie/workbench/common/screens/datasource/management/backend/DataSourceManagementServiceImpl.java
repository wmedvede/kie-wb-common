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
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.backend.integration.DataSourceServicesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class DataSourceManagementServiceImpl
        implements DataSourceManagementService {

    private static final Logger logger = LoggerFactory.getLogger( DataSourceManagementServiceImpl.class );

    @Inject
    DataSourceServicesProvider servicesProvider;

    @PostConstruct
    public void init() {
        if ( servicesProvider.getDataSourceService() == null ) {
            logger.warn( "Data source services was not initialized in current server. " +
            "Data sources features won't be available.");
        }
    }

    @Override
    public boolean isEnabled() {
        return servicesProvider.getDataSourceService() != null;
    }

    @Override
    public List<DataSourceDef> getDataSources() {

        assertDataSourceSerives();
        try {
            return servicesProvider.getDataSourceService().getDataSources();
        } catch ( Exception e ) {
            logger.error( "getDataSources failed: " + e.getMessage(), e );
            throw new RuntimeException( e.getMessage() );
        }
    }

    @Override
    public List<DataSourceDef> getSystemDataSources() {
        return getDataSources();
    }

    @Override
    public DataSourceDeploymentInfo getDeploymentInfo( final String uuid ) {

        assertDataSourceSerives();
        try {
            return servicesProvider.getDataSourceService().getDeploymentInfo( uuid );
        } catch ( Exception e ) {
            logger.error( "getDeploymentInfo for dataSource: " + uuid + " failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void deploy( final DataSourceDef dataSourceDef ) {

        assertDataSourceSerives();
        try {
            servicesProvider.getDataSourceService().deploy( dataSourceDef );
        } catch ( Exception e ) {
            logger.error( "deployment of dataSourceDef: " + dataSourceDef + " failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void undeploy( final String uuid ) {

        assertDataSourceSerives();
        try {
            servicesProvider.getDataSourceService().undeploy( uuid );
        } catch ( Exception e ) {
            logger.error( "undeployment of dataSource: " + uuid + " failed: " + e.getMessage(), e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private void assertDataSourceSerives() {
        if ( servicesProvider.getDataSourceService() == null ) {
            throw new GenericPortableException( "Data source services are not provided for current server.");
        }
    }

}