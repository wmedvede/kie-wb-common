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

package org.kie.workbench.common.screens.datasource.management.service;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;

@Remote
public interface DataSourceManagementService {

    boolean isEnabled();

    List<DataSourceDef> getDeployments();

    DataSourceDeploymentInfo getDeploymentInfo( final String uuid );

    DataSourceDeploymentInfo deploy( final DataSourceDef dataSourceDef );

    void update( final DataSourceDef dataSourceDef );

    void undeploy( final DataSourceDeploymentInfo deploymentInfo );

}