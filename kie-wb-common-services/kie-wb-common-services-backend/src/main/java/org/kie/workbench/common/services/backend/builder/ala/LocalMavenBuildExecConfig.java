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

package org.kie.workbench.common.services.backend.builder.ala;

import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;

/**
 * Interface that represents a maven build execution that will be executed locally.
 */
public interface LocalMavenBuildExecConfig
        extends MavenBuildExecConfig {

    String DEPLOY_INTO_KIE_M2_REPOSITORY = "deployInKieM2Repository";

    default String getDeployIntoKieM2Repository() {
        return "${input." + DEPLOY_INTO_KIE_M2_REPOSITORY + "}";
    }
}