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
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.Config;
import org.guvnor.ala.pipeline.BiFunctionConfigExecutor;
import org.guvnor.common.services.project.model.Project;
import org.uberfire.backend.vfs.Path;
import org.uberfire.workbench.events.ResourceChange;

@ApplicationScoped
public class LocalBuildConfigExecutor
        implements BiFunctionConfigExecutor< LocalProject, LocalBuildConfig, BuildConfig > {

    @Inject
    public LocalBuildConfigExecutor( ) {
    }

    @Override
    public Optional< BuildConfig > apply( LocalProject localProject, LocalBuildConfig localBuildConfig ) {
        LocalBuildConfigInternal execConfig = null;

        LocalBuildConfig.BuildType buildType = getBuildType( localBuildConfig.getBuildType( ) );
        switch ( buildType ) {
            case FULL_BUILD:
                execConfig = new LocalBuildConfigInternal( localProject.getProject( ) );
                break;
            case ADD_RESOURCE:
            case DELETE_RESOURCE:
            case UPDATE_RESOURCE:
                execConfig = new LocalBuildConfigInternal(
                        buildType, localProject.getProject( ),
                        getResourcePath( localProject.getProject( ), localBuildConfig.getResource( ) ) );
                break;
            case BATCH_CHANGES:
                execConfig = new LocalBuildConfigInternal( localProject.getProject( ),
                        getResourceChanges( localBuildConfig.getResourceChanges( ) ) );
                break;

        }
        return Optional.ofNullable( execConfig );
    }

    @Override
    public Class< ? extends Config > executeFor( ) {
        return LocalBuildConfig.class;
    }

    @Override
    public String outputId( ) {
        return "local-build";
    }

    private LocalBuildConfig.BuildType getBuildType( String value ) {
        return LocalBuildConfig.BuildType.valueOf( value );
    }

    private Path getResourcePath( Project project, String resource ) {
        return null;
    }

    private Map< Path, Collection< ResourceChange > > getResourceChanges( String value ) {
        return null;
    }
}