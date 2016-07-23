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

import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefCache;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefCacheEntry;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

@ApplicationScoped
public class DriverDefCacheImpl
        implements DriverDefCache {

    private Map<String, DriverDefCacheEntry> entries = new HashMap<>();

    @Override
    public DriverDefCacheEntry putDriverDef( DriverDef driverDef ) {
        entries.put( driverDef.getUuid(), new DriverDefCacheEntry( driverDef ) );
        return entries.get( driverDef.getUuid() );
    }

    @Override
    public void remove( String uuid ) {
        entries.remove( uuid );
    }

    @Override
    public DriverDefCacheEntry get( String uuid ) {
        return entries.get( uuid );
    }

    @Override
    public DriverDef getDriverDef( String uuid ) {
        DriverDefCacheEntry entry = entries.get( uuid );
        return entry != null ? entry.getDriverDef() : null;
    }

    /**
     * Finds all the registries where the given dataSourceUuid is set as dependant.
     */
    public List<DriverDefCacheEntry> findReferencedEntries( String dataSourceUuid ) {
        List<DriverDefCacheEntry> result = new ArrayList<>(  );
        for ( DriverDefCacheEntry entry : entries.values() ) {
            if ( entry.hasDependant( dataSourceUuid ) ) {
                result.add( entry );
            }
        }
        return result;
    }
}
