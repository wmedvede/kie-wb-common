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

import org.kie.workbench.common.screens.datasource.management.model.DataSourceRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverRuntimeInfo;

/**
 * Main entry point for server side client components of the data sources management system.
 */
public interface DataSourceManager {

    /**
     * Retrieves a data source instance for the given uuid.
     *
     * @param uuid The identifier of an already registered data source definition.
     *
     * @return a reference to the data source previously registered with uuid.
     *
     * @throws Exception if no data source definition was registered with the given uuid, no data source provider
     * is configured, or the data source couldn't be initialized.
     */
    DataSource lookup( String uuid ) throws Exception;

    /**
     * Gets the available runtime information for a given data source.
     *
     * @param uuid a data source identifier.
     *
     * @return the available runtime information, null if the data source is not registered.
     */
    DataSourceRuntimeInfo getDataSourceRuntimeInfo( String uuid );

    /**
     * Gets the available runtime information for a given driver.
     *
     * @param uuid a driver identifier.
     *
     * @return the available runtime information, null if the data source is not registered.
     */
    DriverRuntimeInfo getDriverRuntimeInfo( String uuid );

}