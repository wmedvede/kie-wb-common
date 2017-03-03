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

package org.kie.workbench.common.services.backend.ala;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.common.services.project.model.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.uberfire.backend.vfs.Path;
import org.uberfire.workbench.events.ResourceChange;
import org.uberfire.workbench.events.ResourceChangeType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class BuildPipelineInvokerTest {

    private static final String ROOT_PATH_URI = "root_path_uri";

    private static final String RESOURCE_PATH_URI = "resource_path_uri";

    @Mock
    private BuildPipelineInitializer pipelineInitializer;

    @Mock
    private PipelineRegistry pipelineRegistry;

    private BuildPipelineInvoker pipelineInvoker;

    @Mock
    private PipelineExecutor pipelineExecutor;

    @Mock
    private Pipeline pipeline;

    @Mock
    private BuildPipelineInvoker.LocalBuildRequest buildRequest;

    @Mock
    private Project project;

    @Mock
    private Path rootPath;

    @Mock
    private Path resource;

    @Mock
    private LocalBinaryConfig localBinaryConfig;

    private Input input;

    private Map< Path, Collection< ResourceChange > > resourceChanges = new HashMap<>( );

    private Path[] resourcePaths;

    @Before
    public void setUp( ) {
        pipelineInvoker = new BuildPipelineInvoker( pipelineInitializer, pipelineRegistry );
        when( pipelineInitializer.getExecutor( ) ).thenReturn( pipelineExecutor );
        when( pipelineRegistry.getPipelineByName( BuildPipelineInitializer.LOCAL_BUILD_PIPELINE ) ).thenReturn( pipeline );

        when( buildRequest.getProject( ) ).thenReturn( project );
        when( project.getRootPath( ) ).thenReturn( rootPath );
        when( rootPath.toURI( ) ).thenReturn( ROOT_PATH_URI );
        when( resource.toURI( ) ).thenReturn( RESOURCE_PATH_URI );

        input = new Input();

        createResourceChanges( );
    }

    @Test
    public void testFullBuildRequest( ) {
        when( buildRequest.getBuildType( ) ).thenReturn( LocalBuildConfig.BuildType.FULL_BUILD );

        // the pipeline should be invoked with this input.
        input.put( LocalSourceConfig.ROOT_PATH, ROOT_PATH_URI );
        input.put( LocalBuildConfig.BUILD_TYPE, LocalBuildConfig.BuildType.FULL_BUILD.name( ) );
        preparePipeline( input );

        LocalBinaryConfig result = pipelineInvoker.invokeLocalBuildPipeLine( buildRequest );
        verifyPipelineInvocation( localBinaryConfig, result );
    }

    @Test
    public void testFullBuildAndDeployValidatedRequest( ) {
        testFullBuildAndDeployRequest( LocalBuildConfig.DeploymentType.VALIDATED );
    }

    @Test
    public void testFullBuildAndDeployForcedRequest( ) {
        testFullBuildAndDeployRequest( LocalBuildConfig.DeploymentType.FORCED );
    }

    private void testFullBuildAndDeployRequest( LocalBuildConfig.DeploymentType deploymentType ) {
        when( buildRequest.getBuildType( ) ).thenReturn( LocalBuildConfig.BuildType.FULL_BUILD_AND_DEPLOY );
        when( buildRequest.getDeploymentType( ) ).thenReturn( deploymentType );
        when( buildRequest.isSuppressHandlers( ) ).thenReturn( false );

        // the pipeline should be invoked with this input.
        input.put( LocalSourceConfig.ROOT_PATH, ROOT_PATH_URI );
        input.put( LocalBuildConfig.BUILD_TYPE, LocalBuildConfig.BuildType.FULL_BUILD_AND_DEPLOY.name( ) );
        input.put( LocalBuildConfig.DEPLOYMENT_TYPE, deploymentType.name( ) );
        input.put( LocalBuildConfig.SUPPRESS_HANDLERS, "false" );
        preparePipeline( input );

        LocalBinaryConfig result = pipelineInvoker.invokeLocalBuildPipeLine( buildRequest );
        verifyPipelineInvocation( localBinaryConfig, result );
    }

    @Test
    public void testIncrementalBuildAddResource( ) {
        testIncrementalBuildResourceRequest( LocalBuildConfig.BuildType.INCREMENTAL_ADD_RESOURCE, resource );
    }

    @Test
    public void testIncrementalBuildDeleteResource( ) {
        testIncrementalBuildResourceRequest( LocalBuildConfig.BuildType.INCREMENTAL_DELETE_RESOURCE, resource );
    }

    @Test
    public void testIncrementalBuildUpdateResource( ) {
        testIncrementalBuildResourceRequest( LocalBuildConfig.BuildType.INCREMENTAL_UPDATE_RESOURCE, resource );
    }

    @Test
    public void testIncrementalBuildResourceChanges( ) {
        when( buildRequest.getBuildType( ) ).thenReturn( LocalBuildConfig.BuildType.INCREMENTAL_BATCH_CHANGES );
        when( buildRequest.isSingleResource( ) ).thenReturn( false );
        when( buildRequest.getResourceChanges( ) ).thenReturn( resourceChanges );

        // the pipeline should be invoked with this input.
        input.put( LocalSourceConfig.ROOT_PATH, ROOT_PATH_URI );
        input.put( LocalBuildConfig.BUILD_TYPE, LocalBuildConfig.BuildType.INCREMENTAL_BATCH_CHANGES.name( ) );
        input.put( LocalBuildConfig.RESOURCE_CHANGE + resourcePaths[ 0 ].toURI( ), "ADD" );
        input.put( LocalBuildConfig.RESOURCE_CHANGE + resourcePaths[ 1 ].toURI( ), "ADD,UPDATE" );
        input.put( LocalBuildConfig.RESOURCE_CHANGE + resourcePaths[ 2 ].toURI( ), "ADD,UPDATE,DELETE" );
        preparePipeline( input );

        LocalBinaryConfig result = pipelineInvoker.invokeLocalBuildPipeLine( buildRequest );
        verifyPipelineInvocation( localBinaryConfig, result );
    }

    private void testIncrementalBuildResourceRequest( LocalBuildConfig.BuildType buildType, Path resource ) {
        when( buildRequest.getBuildType( ) ).thenReturn( buildType );
        when( buildRequest.getResource( ) ).thenReturn( resource );
        when( buildRequest.isSingleResource( ) ).thenReturn( true );

        // the pipeline should be invoked with this input.
        input.put( LocalSourceConfig.ROOT_PATH, ROOT_PATH_URI );
        input.put( LocalBuildConfig.BUILD_TYPE, buildType.name( ) );
        input.put( LocalBuildConfig.RESOURCE, RESOURCE_PATH_URI );
        preparePipeline( input );

        LocalBinaryConfig result = pipelineInvoker.invokeLocalBuildPipeLine( buildRequest );
        verifyPipelineInvocation( localBinaryConfig, result );
    }

    private void preparePipeline( Input input ) {
        doAnswer( new Answer< Void >( ) {
            public Void answer( InvocationOnMock invocation ) {
                Consumer consumer = ( Consumer ) invocation.getArguments( )[ 2 ];
                consumer.accept( localBinaryConfig );
                return null;
            }
        } ).when( pipelineExecutor ).execute( eq( input ), eq( pipeline ), any( Consumer.class ) );
    }

    private void verifyPipelineInvocation( LocalBinaryConfig expectedResult, LocalBinaryConfig result ) {
        assertEquals( expectedResult, result );
        verify( pipelineExecutor, times( 1 ) ).execute( eq( input ), eq( pipeline ), any( Consumer.class ) );
    }

    private void createResourceChanges( ) {
        resourcePaths = new Path[ 3 ];
        for ( int i = 0; i < resourcePaths.length; i++ ) {
            Path resource = mock( Path.class );
            resourcePaths[ i ] = resource;
            when( resource.toURI( ) ).thenReturn( "resource_" + i );
            resourceChanges.put( resource, createChanges( i ) );
        }
    }

    private Collection< ResourceChange > createChanges( int i ) {
        Collection< ResourceChange > changes = new ArrayList<>( );
        ResourceChange resourceChange;
        //resource_0 -> ADD
        //resource_1 -> ADD,UPDATE
        //resource_2 -> ADD,UPDATE,DELETE
        if ( i >= 0 ) {
            resourceChange = mock( ResourceChange.class );
            when( resourceChange.getType( ) ).thenReturn( ResourceChangeType.ADD );
            changes.add( resourceChange );
        }
        if ( i >= 1 ) {
            resourceChange = mock( ResourceChange.class );
            when( resourceChange.getType( ) ).thenReturn( ResourceChangeType.UPDATE );
            changes.add( resourceChange );
        }
        if ( i >= 2 ) {
            resourceChange = mock( ResourceChange.class );
            when( resourceChange.getType( ) ).thenReturn( ResourceChangeType.DELETE );
            changes.add( resourceChange );
        }
        return changes;
    }

}