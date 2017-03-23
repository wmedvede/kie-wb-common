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

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.guvnor.ala.build.Project;
import org.guvnor.ala.build.maven.model.MavenBuild;
import org.guvnor.ala.build.maven.model.impl.MavenProjectBinaryBuildImpl;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.registry.BuildRegistry;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class LocalMavenBuildExecConfigExecutorTest {

    @Mock
    private BuildRegistry buildRegistry;

    @Mock
    private ExtendedM2RepoService m2RepoService;

    @Mock
    private Project project;

    @Mock
    private MavenBuild mavenBuild;

    @Mock
    private LocalMavenBuildExecConfig localMavenBuildExecConfig;

    private LocalMavenBuildExecConfigExecutor executor;

    private Path tempDir;

    private Path targetProjectPath;

    @Before
    public void setUp( ) throws Exception {
        final URL pomUrl = this.getClass( ).getResource( "/MavenBuildTest/pom.xml" );
        final Path testProjectPath = Paths.get( pomUrl.toURI( ) ).getParent( );
        tempDir = Files.createTempDirectory( "LocalMavenBuildExecConfigExecutorTest" );
        targetProjectPath = tempDir.resolve( "MavenBuildTest" );
        FileUtils.copyDirectory( testProjectPath.toFile( ), targetProjectPath.toFile( ) );
        executor = new LocalMavenBuildExecConfigExecutor( buildRegistry, m2RepoService );
    }

    @Test
    public void testApplyForProjectBuild( ) throws Exception {
        doTestBuild( );
        verify( m2RepoService, never( ) ).deployJar( any( InputStream.class ), any( GAV.class ) );
        clearTempDir( );
    }

    @Test
    public void testApplyForProjectBuildAndDeploy( ) throws Exception {
        when( localMavenBuildExecConfig.getDeployIntoKieM2Repository( ) ).thenReturn( Boolean.toString( true ) );
        doTestBuild( );
        verify( m2RepoService, times( 1 ) ).deployJar( any( InputStream.class ),
                eq( new GAV( "org.kie.workbench.common.services.builder.tests", "maven-build-test", "1.0.0" ) ) );
        clearTempDir( );
    }

    private void doTestBuild( ) {
        ArrayList< String > goals = new ArrayList<>( );
        goals.add( "clean" );
        goals.add( "package" );

        when( mavenBuild.getGoals( ) ).thenReturn( goals );
        when( mavenBuild.getProperties( ) ).thenReturn( new Properties( ) );
        when( mavenBuild.getProject( ) ).thenReturn( project );

        when( project.getTempDir( ) ).thenReturn( targetProjectPath.toString( ) );
        when( project.getExpectedBinary( ) ).thenReturn( "maven-build-test-1.0.0.jar" );

        Optional< BinaryConfig > result = executor.apply( mavenBuild, localMavenBuildExecConfig );

        assertTrue( result.get( ) instanceof MavenProjectBinaryBuildImpl );
        MavenProjectBinaryBuildImpl mavenProjectBinaryBuild = ( MavenProjectBinaryBuildImpl ) result.get( );
        assertFalse( mavenProjectBinaryBuild.getMavenBuildResult( ).hasExceptions( ) );
        assertTrue( Files.exists( targetProjectPath.resolve( "target/maven-build-test-1.0.0.jar" ) ) );
        assertEquals( 1, mavenProjectBinaryBuild.getMavenBuildResult( )
                .getBuildMessages( )
                .stream( )
                .filter( buildMessage -> "BUILD SUCCESS".equals( buildMessage.getMessage( ) ) )
                .count( ) );
    }

    private void clearTempDir( ) throws Exception {
        FileUtils.deleteDirectory( tempDir.toFile( ) );
    }
}
