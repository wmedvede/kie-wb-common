/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.services.backend.alabuilder;

import org.guvnor.ala.config.BuildConfig;

public interface LocalBuildConfig extends BuildConfig {

    enum BuildType {
        FULL_BUILD, FULL_BUILD_AND_DEPLOY, ADD_RESOURCE, DELETE_RESOURCE, UPDATE_RESOURCE, BATCH_CHANGES
    }

    default String getBuildType( ) {
        return "${input.build-type}";
    }

    default String getResource( ) {
        return "${input.resource}";
    }

    default String getResourceChanges( ) {
        return "${input.resource-changes}";
    }
}