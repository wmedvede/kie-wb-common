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

import java.util.ArrayList;
import java.util.List;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

/**
 * Represents a registration record in the DriverDeploymentCach
 */
public class DriverDeploymentCacheEntry {

    private DriverDef driverDef;

    private List<DataSourceDeploymentInfo> dependants = new ArrayList<>( );

    public DriverDeploymentCacheEntry( DriverDef driverDef ) {
        this.driverDef = driverDef;
    }

    public String getUuid() {
        return driverDef.getUuid();
    }

    public DriverDef getDriverDef() {
        return driverDef;
    }

    public void addDependant( DataSourceDeploymentInfo deploymentInfo ) {
        dependants.add( deploymentInfo );
    }

    public boolean hasDependants( ) {
        return !dependants.isEmpty();
    }

    public boolean hasDependant( DataSourceDeploymentInfo deploymentInfo ) {
        return dependants.contains( deploymentInfo );
    }

    public void removeDependant( DataSourceDeploymentInfo deploymentInfo ) {
        dependants.remove( deploymentInfo );
    }

    public List<DataSourceDeploymentInfo> getDependants() {
        return dependants;
    }
}
