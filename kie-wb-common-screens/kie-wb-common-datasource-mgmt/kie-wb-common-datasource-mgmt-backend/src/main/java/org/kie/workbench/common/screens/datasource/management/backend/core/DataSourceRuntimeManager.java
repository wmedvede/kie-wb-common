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

package org.kie.workbench.common.screens.datasource.management.backend.core;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;

/**
 * Runtime system for the data sources management.
 *
 */
public interface DataSourceRuntimeManager {

    /**
     * Deploys a data source definition in the data sources runtime system.
     *
     * @param dataSourceDef
     *
     * @param options
     *
     * @return
     */
    DataSourceDeploymentInfo deployDataSource( DataSourceDef dataSourceDef, DeploymentOptions options ) throws Exception;

    /**
     * Gets the deployment information for given data source.
     *
     * @param uuid
     *
     * @return
     *
     * @throws Exception
     */
    DataSourceDeploymentInfo getDataSourceDeploymentInfo( String uuid ) throws Exception;

    /**
     * Un-deploys a data source from the data sources runtime system.
     *
     * @param deploymentInfo
     *
     * @param options
     *
     * @throws Exception
     */
    void unDeployDataSource( DataSourceDeploymentInfo deploymentInfo, DeploymentOptions options ) throws Exception;

    /**
     * Deploys a driver in the data sources runtime system.
     */
    DriverDeploymentInfo deployDriver( DriverDef driverDef, DeploymentOptions options ) throws Exception;

    DriverDeploymentInfo getDriverDeploymentInfo( String uuid ) throws Exception;

    /**
     * Un-deploys a driver from data sources runtime system.
     *
     * @param deploymentInfo
     *
     * @param options
     */
    void unDeployDriver( DriverDeploymentInfo deploymentInfo, DeploymentOptions options ) throws Exception;

    DataSource lookupDataSource( String uuid ) throws Exception;

}