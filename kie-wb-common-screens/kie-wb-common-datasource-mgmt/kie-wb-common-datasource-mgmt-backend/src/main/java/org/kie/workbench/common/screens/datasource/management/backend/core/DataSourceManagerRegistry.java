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

import java.util.Collection;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

/**
 * Main entry point for managing the registering/de-registering of data sources and drivers in the data source management
 * system.
 */
public interface DataSourceManagerRegistry {

    //validate( XXX );

    //checkStatus();


    /**
     * Registers the driver definition with a SOFT registration.
     *
     * @throws If there are REFERENCED data sources that depends on this driver definition an exception is thrown.
     */
    void registerDriverDef( DriverDef driverDef ) throws Exception;

    void registerDriverDef( DriverDef driverDef, RegistrationMode registrationMode ) throws Exception;

    /**
     * De-registers the driver definition with a SOFT registration.
     *
     * @throws If there are REFERENCED data sources that depends on this driver an exception is thrown.
     */
    void deRegisterDriverDef( String driverDefUuid ) throws Exception;

    void deRegisterDriverDef( String uuid, RegistrationMode registrationMode ) throws Exception;

    DriverDef getDriverDef( String uuid );

    /**
     * Gets the lists data source definitions that depends on a driver definition.
     *
     * @param uuid Identifier for a given driver definition.
     *
     * @return A collection with the dependant data source definitions.
     */
    Collection<DataSourceDef> getDependants( String uuid );

    /**
     * Registers the data source definition with a SOFT registration by using the required provider according
     * to the DataSourceDefType.
     *
     * It's up to the provider to decide if the underlying data source instance is created at registration time
     * or it's postponed until a later moment, typically when the first lookup is executed.
     */
    void registerDataSourceDef( DataSourceDef dataSourceDef ) throws Exception;

    void registerDataSourceDef( DataSourceDef dataSourceDef, RegistrationMode registrationMode ) throws Exception;

    /**
     * De-registers the data source definition.
     */
    void deRegisterDataSourceDef( String uuid ) throws Exception;

    void deRegisterDataSourceDef( String uuid, RegistrationMode registrationMode ) throws Exception;

    DataSourceDef getDataSourceDef( String uuid );

}
