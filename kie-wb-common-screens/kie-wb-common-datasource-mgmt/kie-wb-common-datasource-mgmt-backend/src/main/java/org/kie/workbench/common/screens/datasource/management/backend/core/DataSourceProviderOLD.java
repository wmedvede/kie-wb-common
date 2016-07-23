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

import java.util.Properties;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceStatus;

/**
 * Manages the instantiation and life cycle of a DataSource.
 */
public interface DataSourceProviderOLD {

    /**
     * Used by the data source management system to pass configurations to the provider. Invoked prior
     * to the invocation of the start method.
     */
    void loadConfig( Properties properties );

    /**
     * Invoked by the platform prior to the any invocation of the initialized method.
     */
    void start();

    /**
     * Initializes a data source given a definition. It's up to the provider to decide if the data source is created
     * at initialization time, or it's postponed until the first lookup.
     *
     * @param dataSourceDef The data source definition that it's about to be initialized.
     *
     * @throws Exception is thrown in case of unexpected errors.
     *
     */
    void initialize( DataSourceDef dataSourceDef ) throws Exception;

    /**
     * Gets a reference to a previously initialized data source.
     *
     * @param uuid The uuid for the data source.
     *
     * @return The data source previously initialized for the given uuid, null if the data source wasn't initialized.
     *
     * @throws Exception is thrown in case of unexpected errors.
     */
    DataSource lookup( String uuid ) throws Exception;

    /**
     * Gets the status for a given data source.
     *
     * @param uuid The uuid for the data source.
     *
     * @return The status of the data source or null if no data source has been initialized with the given uuid.
     */
    DataSourceStatus getStatus( String uuid );

    /**
     * Releases a previously initialized data source. When a data source is released it's state is set to STALE,
     * and latter invocations to the getConnection method may fail.
     *
     * @param dataSourceDef The data source definition to be released.
     *
     * @throws Exception is thrown in case of unexpected errors.
     */
    void release( DataSourceDef dataSourceDef ) throws Exception;

}