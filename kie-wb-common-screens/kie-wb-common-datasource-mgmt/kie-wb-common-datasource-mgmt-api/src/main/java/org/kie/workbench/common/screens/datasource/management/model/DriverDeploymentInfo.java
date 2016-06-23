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

package org.kie.workbench.common.screens.datasource.management.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DriverDeploymentInfo
        extends DeploymentInfo {

    String uuid;

    String driverClass;

    public DriverDeploymentInfo() {
    }

    public DriverDeploymentInfo( String deploymentId, boolean managed, String uuid, String driverClass ) {
        super( deploymentId, managed );
        this.uuid = uuid;
        this.driverClass = driverClass;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDriverClass() {
        return driverClass;
    }

    @Override
    public String toString() {
        return "DriverDeploymentInfo{" +
                "deploymentId='" + deploymentId + '\'' +
                ", managed=" + managed +
                "uuid='" + uuid + '\'' +
                ", driverClass='" + driverClass + '\'' +
                "} " + super.toString();
    }
}
