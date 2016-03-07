/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.screens.projecteditor.backend.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.guvnor.common.services.backend.metadata.MetadataServerSideService;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.backend.server.utils.POMContentHandler;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.MavenRepositoryMetadata;
import org.guvnor.common.services.project.model.MavenRepositorySource;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.ProjectRepositories;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.GAVAlreadyExistsException;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.defaulteditor.service.DefaultEditorContent;
import org.kie.workbench.common.screens.defaulteditor.service.DefaultEditorService;
import org.kie.workbench.common.screens.projecteditor.service.PomEditorService;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileSystem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PomEditorServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private DefaultEditorService defaultEditorService;

    @Mock
    private MetadataServerSideService metadataService;

    @Mock
    private CommentedOptionFactory commentedOptionFactory;

    @Mock
    private KieProjectService projectService;

    @Mock
    private ProjectRepositoryResolver repositoryResolver;

    @Mock
    private ProjectRepositoriesService projectRepositoriesService;

    @Mock
    private Path pomPath;

    @Mock
    private Metadata metaData;

    @Mock
    private KieProject project;

    @Mock
    private POM pom;

    @Mock
    private Path projectRepositoriesPath;

    private PomEditorService service;

    private String pomPathUri = "default://p0/pom.xml";

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private DefaultEditorContent content = new DefaultEditorContent();

    private POMContentHandler pomContentHandler = new POMContentHandler();

    private String pomXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "<modelVersion>4.0.0</modelVersion>\n" +
            "<groupId>groupId</groupId>\n" +
            "<artifactId>artifactId</artifactId>\n" +
            "<version>0.0.1</version>\n" +
            "<name>name</name>\n" +
            "<description>description</description>\n" +
            "</project>";

    private String comment = "comment";

    @BeforeClass
    public static void setupSystemProperties() {
        //These are not needed for the tests
        System.setProperty( "org.uberfire.nio.git.daemon.enabled",
                            "false" );
        System.setProperty( "org.uberfire.nio.git.ssh.enabled",
                            "false" );
        System.setProperty( "org.uberfire.sys.repo.monitor.disabled",
                            "true" );
    }

    @Before
    public void setup() {
        service = new PomEditorServiceImpl( ioService,
                                            defaultEditorService,
                                            metadataService,
                                            commentedOptionFactory,
                                            projectService,
                                            pomContentHandler,
                                            repositoryResolver,
                                            projectRepositoriesService );

        when( pomPath.toURI() ).thenReturn( pomPathUri );
        when( defaultEditorService.loadContent( pomPath ) ).thenReturn( content );
        when( metadataService.setUpAttributes( eq( pomPath ),
                                               any( Metadata.class ) ) ).thenReturn( attributes );
        when( projectService.resolveProject( pomPath ) ).thenReturn( project );
        when( project.getRepositoriesPath() ).thenReturn( projectRepositoriesPath );
        when( project.getPom() ).thenReturn( pom );
    }

    @Test
    public void testLoad() {
        final DefaultEditorContent content = service.loadContent( pomPath );
        assertNotNull( content );
        assertEquals( this.content,
                      content );
    }

    @Test
    public void testSaveNonClashingGAVChangeToGAV() {
        final Set<ProjectRepositories.ProjectRepository> projectRepositoriesMetadata = new HashSet<ProjectRepositories.ProjectRepository>();
        final ProjectRepositories projectRepositories = new ProjectRepositories( projectRepositoriesMetadata );
        when( projectRepositoriesService.load( projectRepositoriesPath ) ).thenReturn( projectRepositories );

        final ArgumentCaptor<MavenRepositoryMetadata> resolvedRepositoriesCaptor = ArgumentCaptor.forClass( MavenRepositoryMetadata.class );
        when( repositoryResolver.getRepositoriesResolvingArtifact( eq( pomXml ),
                                                                   resolvedRepositoriesCaptor.capture() ) ).thenReturn( Collections.EMPTY_SET );
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.2" ) );

        service.save( pomPath,
                      pomXml,
                      metaData,
                      comment,
                      DeploymentMode.VALIDATED );

        verify( projectService,
                times( 1 ) ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                times( 1 ) ).load( projectRepositoriesPath );
        verify( repositoryResolver,
                times( 1 ) ).getRepositoriesResolvingArtifact( eq( pomXml ) );
        final List<MavenRepositoryMetadata> resolvedRepositories = resolvedRepositoriesCaptor.getAllValues();
        assertNotNull( resolvedRepositories );
        assertEquals( 0,
                      resolvedRepositories.size() );

        verify( ioService,
                times( 1 ) ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                times( 1 ) ).write( any( org.uberfire.java.nio.file.Path.class ),
                                    eq( pomXml ),
                                    eq( attributes ),
                                    any( CommentedOption.class ) );
        verify( ioService,
                times( 1 ) ).endBatch();
    }

    @Test
    public void testSaveNonClashingGAVNoChangeToGAV() {
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.1" ) );

        service.save( pomPath,
                      pomXml,
                      metaData,
                      comment,
                      DeploymentMode.VALIDATED );

        verify( projectService,
                times( 1 ) ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                never() ).load( projectRepositoriesPath );
        verify( repositoryResolver,
                never() ).getRepositoriesResolvingArtifact( eq( pomXml ) );

        verify( ioService,
                times( 1 ) ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                times( 1 ) ).write( any( org.uberfire.java.nio.file.Path.class ),
                                    eq( pomXml ),
                                    eq( attributes ),
                                    any( CommentedOption.class ) );
        verify( ioService,
                times( 1 ) ).endBatch();
    }

    @Test
    public void testSaveNonClashingGAVFilteredChangeToGAV() {
        final Set<ProjectRepositories.ProjectRepository> projectRepositoriesMetadata = new HashSet<ProjectRepositories.ProjectRepository>() {{
            add( new ProjectRepositories.ProjectRepository( true,
                                                            new MavenRepositoryMetadata( "local-id",
                                                                                         "local-url",
                                                                                         MavenRepositorySource.LOCAL ) ) );
        }};
        final ProjectRepositories projectRepositories = new ProjectRepositories( projectRepositoriesMetadata );
        when( projectRepositoriesService.load( projectRepositoriesPath ) ).thenReturn( projectRepositories );

        final ArgumentCaptor<MavenRepositoryMetadata> resolvedRepositoriesCaptor = ArgumentCaptor.forClass( MavenRepositoryMetadata.class );
        when( repositoryResolver.getRepositoriesResolvingArtifact( eq( pomXml ),
                                                                   resolvedRepositoriesCaptor.capture() ) ).thenReturn( Collections.EMPTY_SET );
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.2" ) );

        service.save( pomPath,
                      pomXml,
                      metaData,
                      comment,
                      DeploymentMode.VALIDATED );

        verify( projectService,
                times( 1 ) ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                times( 1 ) ).load( projectRepositoriesPath );
        verify( repositoryResolver,
                times( 1 ) ).getRepositoriesResolvingArtifact( eq( pomXml ),
                                                               any( MavenRepositoryMetadata.class ) );
        final List<MavenRepositoryMetadata> resolvedRepositories = resolvedRepositoriesCaptor.getAllValues();
        assertNotNull( resolvedRepositories );
        assertEquals( 1,
                      resolvedRepositories.size() );
        final MavenRepositoryMetadata repositoryMetadata = resolvedRepositories.get( 0 );
        assertEquals( "local-id",
                      repositoryMetadata.getId() );
        assertEquals( "local-url",
                      repositoryMetadata.getUrl() );
        assertEquals( MavenRepositorySource.LOCAL,
                      repositoryMetadata.getSource() );

        verify( ioService,
                times( 1 ) ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                times( 1 ) ).write( any( org.uberfire.java.nio.file.Path.class ),
                                    eq( pomXml ),
                                    eq( attributes ),
                                    any( CommentedOption.class ) );
        verify( ioService,
                times( 1 ) ).endBatch();
    }

    @Test
    public void testSaveNonClashingGAVFilteredNoChangeToGAV() {
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.1" ) );

        service.save( pomPath,
                      pomXml,
                      metaData,
                      comment,
                      DeploymentMode.VALIDATED );

        verify( projectService,
                times( 1 ) ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                never() ).load( projectRepositoriesPath );
        verify( repositoryResolver,
                never() ).getRepositoriesResolvingArtifact( eq( pomXml ),
                                                            any( MavenRepositoryMetadata.class ) );

        verify( ioService,
                times( 1 ) ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                times( 1 ) ).write( any( org.uberfire.java.nio.file.Path.class ),
                                    eq( pomXml ),
                                    eq( attributes ),
                                    any( CommentedOption.class ) );
        verify( ioService,
                times( 1 ) ).endBatch();
    }

    @Test
    public void testSaveClashingGAVChangeToGAV() {
        final Set<ProjectRepositories.ProjectRepository> projectRepositoriesMetadata = new HashSet<ProjectRepositories.ProjectRepository>() {{
            add( new ProjectRepositories.ProjectRepository( true,
                                                            new MavenRepositoryMetadata( "local-id",
                                                                                         "local-url",
                                                                                         MavenRepositorySource.LOCAL ) ) );
        }};
        final ProjectRepositories projectRepositories = new ProjectRepositories( projectRepositoriesMetadata );
        when( projectRepositoriesService.load( projectRepositoriesPath ) ).thenReturn( projectRepositories );

        final Set<MavenRepositoryMetadata> clashingRepositories = new HashSet<MavenRepositoryMetadata>() {{
            add( new MavenRepositoryMetadata( "local-id",
                                              "local-url",
                                              MavenRepositorySource.LOCAL ) );
        }};
        final ArgumentCaptor<MavenRepositoryMetadata> resolvedRepositoriesCaptor = ArgumentCaptor.forClass( MavenRepositoryMetadata.class );
        when( repositoryResolver.getRepositoriesResolvingArtifact( eq( pomXml ),
                                                                   resolvedRepositoriesCaptor.capture() ) ).thenReturn( clashingRepositories );
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.2" ) );

        try {
            service.save( pomPath,
                          pomXml,
                          metaData,
                          comment,
                          DeploymentMode.VALIDATED );

        } catch ( GAVAlreadyExistsException e ) {
            // This is expected! We catch here rather than let JUnit handle it with
            // @Test(expected = GAVAlreadyExistsException.class) so we can verify
            // that only the expected methods have been invoked.

        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

        verify( projectService,
                times( 1 ) ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                times( 1 ) ).load( projectRepositoriesPath );
        verify( repositoryResolver,
                times( 1 ) ).getRepositoriesResolvingArtifact( eq( pomXml ),
                                                               any( MavenRepositoryMetadata.class ) );
        final List<MavenRepositoryMetadata> resolvedRepositories = resolvedRepositoriesCaptor.getAllValues();
        assertNotNull( resolvedRepositories );
        assertEquals( 1,
                      resolvedRepositories.size() );
        final MavenRepositoryMetadata repositoryMetadata = resolvedRepositories.get( 0 );
        assertEquals( "local-id",
                      repositoryMetadata.getId() );
        assertEquals( "local-url",
                      repositoryMetadata.getUrl() );
        assertEquals( MavenRepositorySource.LOCAL,
                      repositoryMetadata.getSource() );

        verify( ioService,
                never() ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                never() ).write( any( org.uberfire.java.nio.file.Path.class ),
                                 eq( pomXml ),
                                 eq( attributes ),
                                 any( CommentedOption.class ) );
        verify( ioService,
                never() ).endBatch();
    }

    @Test
    public void testSaveClashingGAVNoChangeToGAV() {
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.1" ) );

        try {
            service.save( pomPath,
                          pomXml,
                          metaData,
                          comment,
                          DeploymentMode.VALIDATED );

        } catch ( GAVAlreadyExistsException e ) {
            // This is should not be thrown if the GAV has not changed.
            fail( e.getMessage() );
        }

        verify( projectService,
                times( 1 ) ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                never() ).load( projectRepositoriesPath );
        verify( repositoryResolver,
                never() ).getRepositoriesResolvingArtifact( eq( pomXml ),
                                                            any( MavenRepositoryMetadata.class ) );

        verify( ioService,
                times( 1 ) ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                times( 1 ) ).write( any( org.uberfire.java.nio.file.Path.class ),
                                    eq( pomXml ),
                                    eq( attributes ),
                                    any( CommentedOption.class ) );
        verify( ioService,
                times( 1 ) ).endBatch();
    }

    @Test
    public void testSaveClashingGAVForced() {
        final Set<ProjectRepositories.ProjectRepository> projectRepositoriesMetadata = new HashSet<ProjectRepositories.ProjectRepository>() {{
            add( new ProjectRepositories.ProjectRepository( true,
                                                            new MavenRepositoryMetadata( "local-id",
                                                                                         "local-url",
                                                                                         MavenRepositorySource.LOCAL ) ) );
        }};
        final ProjectRepositories projectRepositories = new ProjectRepositories( projectRepositoriesMetadata );
        when( projectRepositoriesService.load( projectRepositoriesPath ) ).thenReturn( projectRepositories );

        final Set<MavenRepositoryMetadata> clashingRepositories = new HashSet<MavenRepositoryMetadata>() {{
            add( new MavenRepositoryMetadata( "local-id",
                                              "local-url",
                                              MavenRepositorySource.LOCAL ) );
        }};
        when( repositoryResolver.getRepositoriesResolvingArtifact( eq( pomXml ),
                                                                   any( MavenRepositoryMetadata.class ) ) ).thenReturn( clashingRepositories );
        when( pom.getGav() ).thenReturn( new GAV( "groupId",
                                                  "artifactId",
                                                  "0.0.1" ) );

        try {
            service.save( pomPath,
                          pomXml,
                          metaData,
                          comment,
                          DeploymentMode.FORCED );

        } catch ( GAVAlreadyExistsException e ) {
            fail( e.getMessage() );
        }

        verify( projectService,
                never() ).resolveProject( pomPath );
        verify( projectRepositoriesService,
                never() ).load( pomPath );
        verify( repositoryResolver,
                never() ).getRepositoriesResolvingArtifact( eq( pomXml ),
                                                            any( MavenRepositoryMetadata.class ) );

        verify( ioService,
                times( 1 ) ).startBatch( any( FileSystem.class ) );
        verify( ioService,
                times( 1 ) ).write( any( org.uberfire.java.nio.file.Path.class ),
                                    eq( pomXml ),
                                    eq( attributes ),
                                    any( CommentedOption.class ) );
        verify( ioService,
                times( 1 ) ).endBatch();
    }

}
