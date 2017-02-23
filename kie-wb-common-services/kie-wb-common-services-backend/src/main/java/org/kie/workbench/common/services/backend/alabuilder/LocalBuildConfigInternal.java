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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.guvnor.ala.config.BuildConfig;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.uberfire.backend.vfs.Path;
import org.uberfire.workbench.events.ResourceChange;

public class LocalBuildConfigInternal implements BuildConfig {

    private LocalBuildConfig.BuildType buildType;

    private Project project;

    private Path resource;

    private Map< Path, Collection< ResourceChange > > resourceChanges = new HashMap<>( );

    private DeploymentMode deploymentMode;

    private boolean suppressHandlers;

    public LocalBuildConfigInternal( ) {
    }

    public LocalBuildConfigInternal( Project project ) {
        this.project = project;
        this.buildType = LocalBuildConfig.BuildType.FULL_BUILD;
    }

    public LocalBuildConfigInternal( LocalBuildConfig.BuildType buildType, Project project, Path resource ) {
        this.buildType = buildType;
        this.project = project;
        this.resource = resource;
    }

    public LocalBuildConfigInternal( Project project, Map< Path, Collection< ResourceChange > > resourceChanges ) {
        this.project = project;
        this.resourceChanges = resourceChanges;
        this.buildType = LocalBuildConfig.BuildType.BATCH_CHANGES;
    }

    public LocalBuildConfig.BuildType getBuildType( ) {
        return buildType;
    }

    public Project getProject( ) {
        return project;
    }

    public Path getResource( ) {
        return resource;
    }

    public Map< Path, Collection< ResourceChange > > getResourceChanges( ) {
        return resourceChanges;
    }

    public DeploymentMode getDeploymentMode( ) {
        return deploymentMode;
    }

    public boolean isSuppressHandlers( ) {
        return suppressHandlers;
    }
}