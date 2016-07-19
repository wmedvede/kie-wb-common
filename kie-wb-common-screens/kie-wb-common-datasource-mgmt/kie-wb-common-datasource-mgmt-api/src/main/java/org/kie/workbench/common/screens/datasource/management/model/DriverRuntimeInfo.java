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

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DriverRuntimeInfo {

    private List<DataSourceRuntimeInfo> dependantsInfo;

    public DriverRuntimeInfo() {
    }

    public DriverRuntimeInfo( List<DataSourceRuntimeInfo> dependantsInfo ) {
        this.dependantsInfo = dependantsInfo;
    }

    public boolean hasDependants() {
        return dependantsInfo != null && !dependantsInfo.isEmpty();
    }

    public boolean hasRunningDependants() {
        if ( hasDependants() ) {
            for ( DataSourceRuntimeInfo dependantInfo : dependantsInfo ) {
                if ( dependantInfo.isRunning() ) {
                    return true;
                }
            }
        }
        return false;
    }

    public int runningDependantsCount() {
        int i = 0;
        if ( hasDependants() ) {
            for ( DataSourceRuntimeInfo dependantInfo : dependantsInfo ) {
                if ( dependantInfo.isRunning() ) {
                    i++;
                }
            }
        }
        return i;
    }

    public int dependantsCount() {
        return hasDependants() ? dependantsInfo.size() : 0;
    }

    public List<DataSourceRuntimeInfo> getDependants() {
        return dependantsInfo;
    }
}
