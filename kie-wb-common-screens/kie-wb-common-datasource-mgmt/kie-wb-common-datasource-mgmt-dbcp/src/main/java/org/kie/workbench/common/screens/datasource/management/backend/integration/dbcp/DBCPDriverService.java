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

package org.kie.workbench.common.screens.datasource.management.backend.integration.dbcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.kie.workbench.common.screens.datasource.management.backend.integration.DriverService;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.uberfire.io.IOService;

@Dependent
@Named(value = "DBCPDriverService" )
public class DBCPDriverService
    implements DriverService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Override
    public void deploy( DriverDef driverDef ) throws Exception {

    }

    @Override
    public void undeploy( String uuid ) throws Exception {

    }

    @Override
    public DriverDeploymentInfo getDeploymentInfo( String uuid ) throws Exception {
        return null;
    }

    @Override
    public List<DriverDeploymentInfo> getAllDeploymentInfo() throws Exception {
        return new ArrayList<>(  );
    }

    @Override public List<DriverDef> getDrivers() throws Exception {
        return new ArrayList<>(  );
    }

    @Override
    public void loadConfig( Properties properties ) {

    }
}
