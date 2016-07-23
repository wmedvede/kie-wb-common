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
import java.util.Properties;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;

public interface DataSourceProvider {

    DataSourceDeploymentInfo deploy( final DataSourceDef dataSourceDef ) throws Exception;

    DataSourceDeploymentInfo resync( final DataSourceDef dataSourceDef, final DataSourceDeploymentInfo deploymentInfo ) throws Exception;

    void undeploy( final DataSourceDeploymentInfo deploymentInfo ) throws Exception;

    DataSourceDeploymentInfo getDeploymentInfo( final String uuid ) throws Exception;

    List<DataSourceDeploymentInfo> getDeploymentsInfo() throws Exception;

    List<DataSourceDef> getDeployments() throws Exception;

    void loadConfig( Properties properties );

    void update( DataSourceDef dataSourceDef ) throws Exception;

    DataSource lookupDataSource( DataSourceDeploymentInfo deploymentInfo ) throws Exception;
}
