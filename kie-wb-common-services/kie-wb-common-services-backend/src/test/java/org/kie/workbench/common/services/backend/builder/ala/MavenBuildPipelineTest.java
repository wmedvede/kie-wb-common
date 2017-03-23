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

import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.guvnor.ala.build.Project;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.build.maven.executor.MavenBuildConfigExecutor;
import org.guvnor.ala.build.maven.executor.MavenProjectConfigExecutor;
import org.guvnor.ala.build.maven.model.MavenBuild;
import org.guvnor.ala.build.maven.model.impl.MavenProjectBinaryBuildImpl;
import org.guvnor.ala.registry.BuildRegistry;
import org.guvnor.ala.registry.SourceRegistry;
import org.guvnor.ala.source.Source;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.ala.source.git.executor.GitConfigExecutor;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class MavenBuildPipelineTest
        extends BuildPipelineExecutionTestBase {

    @Mock
    private SourceRegistry sourceRegistry;

    @Mock
    private BuildRegistry buildRegistry;

    @Mock
    private ExtendedM2RepoService m2RepoService;

    private ArgumentCaptor< GitConfig > gitConfigArgumentCaptor;

    private ArgumentCaptor< Source > sourceArgumentCaptor;

    private ArgumentCaptor< MavenProjectConfig > mavenProjectConfigArgumentCaptor;

    private ArgumentCaptor< Project > projectArgumentCaptor;

    private ArgumentCaptor< MavenBuild > mavenBuildArgumentCaptor;

    private ArgumentCaptor< MavenBuildExecConfig > mavenBuildExecConfigArgumentCaptor;

    private GitConfigExecutor gitConfigExecutor;

    private MavenProjectConfigExecutor mavenProjectConfigExecutor;

    private MavenBuildConfigExecutor mavenBuildConfigExecutor;

    private LocalMavenBuildExecConfigExecutor localMavenBuildExecConfigExecutor;

    @Before
    public void setUp( ) {
        //maven build initialization
        gitConfigExecutor = spy( new GitConfigExecutor( sourceRegistry ) {
            @Override
            public Optional< Source > apply( GitConfig gitConfig ) {
                java.nio.file.Path nioSourcePath = java.nio.file.Paths.get( gitConfig.getOutPath( ) );
                Path sourcePath = PathFactory.newPath( "/", nioSourcePath.toUri( ).toString( ) );
                return Optional.of( new LocalSource( Paths.convert( sourcePath ) ) );
            }
        } );
        mavenProjectConfigExecutor = spy( new MavenProjectConfigExecutor( sourceRegistry ) );
        mavenBuildConfigExecutor = spy( new MavenBuildConfigExecutor( ) );
        localMavenBuildExecConfigExecutor = spy( new LocalMavenBuildExecConfigExecutor( buildRegistry, m2RepoService ) );

        gitConfigArgumentCaptor = ArgumentCaptor.forClass( GitConfig.class );
        sourceArgumentCaptor = ArgumentCaptor.forClass( Source.class );
        mavenProjectConfigArgumentCaptor = ArgumentCaptor.forClass( MavenProjectConfig.class );
        projectArgumentCaptor = ArgumentCaptor.forClass( Project.class );
        mavenBuildArgumentCaptor = ArgumentCaptor.forClass( MavenBuild.class );
        mavenBuildExecConfigArgumentCaptor = ArgumentCaptor.forClass( MavenBuildExecConfig.class );

        configs.add( gitConfigExecutor );
        configs.add( mavenProjectConfigExecutor );
        configs.add( mavenBuildConfigExecutor );
        configs.add( localMavenBuildExecConfigExecutor );

        pipelineInitializer = new BuildPipelineInitializer( pipelineRegistry, configs );
        pipe = pipelineRegistry.getPipelineByName( BuildPipelineInitializer.MAVEN_BUILD_PIPELINE );

        verifyStages( "Git Source",
                "Maven Project",
                "Maven Build Config",
                "Local Maven Build" );
    }

    @Test
    public void testMavenBuildExecution( ) throws Exception {

        final URL pomUrl = this.getClass( ).getResource( "/MavenBuildTest/pom.xml" );
        final java.nio.file.Path testProjectPath = java.nio.file.Paths.get( pomUrl.toURI( ) ).getParent( );
        java.nio.file.Path tempDir = Files.createTempDirectory( "BuildPipelineTest" );
        java.nio.file.Path targetProjectPath = tempDir.resolve( "MavenBuildTest" );
        FileUtils.copyDirectory( testProjectPath.toFile( ), targetProjectPath.toFile( ) );

        input = createMavenBuildInput( "TestRepo", "master", "MavenBuildTest", false );
        input.put( "out-dir", tempDir.toString( ) );

        // execute the pipeline and verify the result
        pipelineInitializer.getExecutor( ).execute( input, pipe, ( Consumer< MavenProjectBinaryBuildImpl > ) localBinaryConfig -> {
            assertFalse( localBinaryConfig.getMavenBuildResult( ).hasExceptions( ) );
            assertEquals( 1, localBinaryConfig
                    .getMavenBuildResult( )
                    .getBuildMessages( )
                    .stream( )
                    .filter( buildMessage -> "BUILD SUCCESS".equals( buildMessage.getMessage( ) ) )
                    .count( ) );
        }, pipelineEventListener );

        // verify that all stages were properly invoked.
        //verifyLocalSourceConfigWasInvoked( );
        //verifyLocalProjectConfigWasInvoked( );
        //verifyLocalBuildConfigExecutorWasInvoked( project, LocalBuildConfig.BuildType.FULL_BUILD.name( ) );
        //verifyLocalBuildExecConfigExecutorWasInvoked( project, LocalBuildConfig.BuildType.FULL_BUILD );

        verifyPipelineEvents( pipe );


    }
}