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

package org.kie.workbench.common.screens.datasource.management.backend.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistryEntry;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

@ApplicationScoped
public class DriverDefRegistryImpl
        implements DriverDefRegistry {

    private Map<String, DriverDefRegistryEntry> driverDefEntriesMap = new HashMap<>();

    public DriverDefRegistryImpl() {
    }

    @Override
    public DriverDefRegistryEntry registerDriverDef( DriverDef driverDef ) {
        driverDefEntriesMap.put( driverDef.getUuid(), new DriverDefRegistryEntry( driverDef ) );
        return driverDefEntriesMap.get( driverDef.getUuid() );
    }

    @Override
    public void deRegisterDriverDef( String uuid ) throws Exception {
        driverDefEntriesMap.remove( uuid );
    }

    @Override
    public DriverDefRegistryEntry getDriverDefEntry( String uuid ) {
        return driverDefEntriesMap.get( uuid );
    }

    @Override
    public DriverDef getDriverDef( String uuid ) {
        DriverDefRegistryEntry entry = driverDefEntriesMap.get( uuid );
        return entry != null ? entry.getDriverDef() : null;
    }

    /**
     * Finds all the registries where the given dataSourceUuid is set as dependant.
     */
    public List<DriverDefRegistryEntry> findReferencedEntries( String dataSourceUuid ) {
        List<DriverDefRegistryEntry> result = new ArrayList<>(  );
        for ( DriverDefRegistryEntry entry : driverDefEntriesMap.values() ) {
            if ( entry.hasDependant( dataSourceUuid ) ) {
                result.add( entry );
            }
        }
        return result;
    }
}
