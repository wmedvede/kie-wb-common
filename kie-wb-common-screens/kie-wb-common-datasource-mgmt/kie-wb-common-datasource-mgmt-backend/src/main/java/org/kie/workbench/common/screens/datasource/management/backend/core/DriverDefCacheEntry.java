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
import java.util.HashMap;
import java.util.Map;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

/**
 * Represents a registration record in the DriverDefCache.
 */
public class DriverDefCacheEntry {

    private DriverDef driverDef;

    private Map<String, DataSourceDef> dependants = new HashMap<>(  );

    public DriverDefCacheEntry( DriverDef driverDef ) {
        this.driverDef = driverDef;
    }

    public String getUuid() {
        return driverDef.getUuid();
    }

    public DriverDef getDriverDef() {
        return driverDef;
    }

    public void addDependant( DataSourceDef dataSourceDef ) {
        dependants.put( dataSourceDef.getUuid(), dataSourceDef );
    }

    public boolean hasDependants( ) {
        return !dependants.isEmpty();
    }

    public boolean hasDependant( String dataSourceUuid ) {
        return dependants.containsKey( dataSourceUuid );
    }

    public Collection<DataSourceDef> getDependants() {
        return dependants.values();
    }

    public void removeDependant( String uuid ) {
        dependants.remove( uuid );
    }
}
