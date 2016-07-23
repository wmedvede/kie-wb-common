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

import java.util.List;

import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

/**
 * Specialized cache for the storage of the driver definitions.
 */
public interface DriverDefCache {

    DriverDefCacheEntry putDriverDef( DriverDef driverDef );

    void remove( String uuid );

    DriverDefCacheEntry get( String uuid );

    DriverDef getDriverDef( String uuid );

    /**
     * Finds all the entries where the given dataSourceUuid is set as dependant.
     */
    List<DriverDefCacheEntry> findReferencedEntries( String dataSourceUuid );

}
