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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistry;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

@ApplicationScoped
public class DriverDefRegistryImpl
        implements DriverDefRegistry {

    private Map<String, DriverDef> driverDefs = new HashMap<>( );

    @Override
    public void register( DriverDef driverDef ) {
        driverDefs.put( driverDef.getUuid(), driverDef );
    }

    @Override
    public DriverDef deregister( String uuid ) {
        return driverDefs.remove( uuid );
    }

    @Override
    public DriverDef get( String uuid ) {
        return driverDefs.get( uuid );
    }
}
