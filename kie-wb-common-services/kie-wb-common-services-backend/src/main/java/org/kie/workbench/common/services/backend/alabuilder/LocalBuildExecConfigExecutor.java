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

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.config.Config;
import org.guvnor.ala.pipeline.BiFunctionConfigExecutor;
import org.kie.workbench.common.services.backend.alabuilder.impl.LocalBuildBinaryImpl;
import org.kie.workbench.common.services.backend.builder.BuildServiceHelper;

@ApplicationScoped
public class LocalBuildExecConfigExecutor
        implements BiFunctionConfigExecutor< LocalBuildConfigInternal, LocalBuildExecConfig, LocalBinaryConfig > {

    private BuildServiceHelper serviceHelper;

    public LocalBuildExecConfigExecutor( ) {
    }

    @Inject
    public LocalBuildExecConfigExecutor( BuildServiceHelper serviceHelper ) {
        this.serviceHelper = serviceHelper;
    }

    @Override
    public Optional< LocalBinaryConfig > apply( LocalBuildConfigInternal localBuildConfigInternal, LocalBuildExecConfig localBuildExecConfig ) {
        Optional< LocalBinaryConfig > result = Optional.empty( );

        switch ( localBuildConfigInternal.getBuildType( ) ) {
            case FULL_BUILD:
                result = Optional.of( new LocalBuildBinaryImpl(
                        serviceHelper.build( localBuildConfigInternal.getProject( ) ) ) );
                break;
            case ADD_RESOURCE:
                result = Optional.of( new LocalBuildBinaryImpl(
                        serviceHelper.addPackageResource( localBuildConfigInternal.getResource( ) ) ) );
                break;
            case UPDATE_RESOURCE:
                result = Optional.of( new LocalBuildBinaryImpl(
                        serviceHelper.updatePackageResource( localBuildConfigInternal.getResource( ) ) ) );
                break;
            case DELETE_RESOURCE:
                result = Optional.of( new LocalBuildBinaryImpl(
                        serviceHelper.deletePackageResource( localBuildConfigInternal.getResource( ) ) ) );
                break;
            case BATCH_CHANGES:
                result = Optional.of(
                        new LocalBuildBinaryImpl( serviceHelper.applyBatchResourceChanges(
                                localBuildConfigInternal.getProject( ),
                                localBuildConfigInternal.getResourceChanges( ) ) ) );
                break;
            case FULL_BUILD_AND_DEPLOY:
                result = Optional.of(
                        new LocalBuildBinaryImpl( serviceHelper.buildAndDeploy(
                                localBuildConfigInternal.getProject( ),
                                localBuildConfigInternal.isSuppressHandlers( ),
                                localBuildConfigInternal.getDeploymentMode( ) ) ) );
        }
        return result;
    }


    @Override
    public Class< ? extends Config > executeFor( ) {
        return LocalBuildExecConfig.class;
    }

    @Override
    public String outputId( ) {
        return "local-binary";
    }

}