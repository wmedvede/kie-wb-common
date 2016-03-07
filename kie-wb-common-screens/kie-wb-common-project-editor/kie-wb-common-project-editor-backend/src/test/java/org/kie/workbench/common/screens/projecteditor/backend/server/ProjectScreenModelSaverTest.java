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

package org.kie.workbench.common.screens.projecteditor.backend.server;

import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.ProjectImports;
import org.guvnor.common.services.project.model.ProjectRepositories;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.guvnor.test.TestFileSystem;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.projecteditor.model.ProjectScreenModel;
import org.kie.workbench.common.services.shared.kmodule.KModuleModel;
import org.kie.workbench.common.services.shared.kmodule.KModuleService;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.kie.workbench.common.services.shared.project.ProjectImportsService;
import org.kie.workbench.common.services.shared.whitelist.PackageNameWhiteListService;
import org.kie.workbench.common.services.shared.whitelist.WhiteList;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.rpc.SessionInfo;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectScreenModelSaverTest {

    @Mock
    private POMService pomService;

    @Mock
    private KModuleService kModuleService;

    @Mock
    private ProjectImportsService importsService;

    @Mock
    private ProjectRepositoriesService repositoriesService;

    @Mock
    private PackageNameWhiteListService whiteListService;

    @Mock
    private KieProjectService projectService;

    @Mock
    private ProjectRepositoryResolver repositoryResolver;

    @Mock
    private User identity;

    @Mock
    private SessionInfo sessionInfo;

    @Mock
    private CommentedOptionFactory commentedOptionFactory;

    @Mock
    private IOService ioService;

    private Path pathToPom;

    private ProjectScreenModelSaver saver;

    private TestFileSystem testFileSystem;

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
    public void setUp() throws Exception {
        testFileSystem = new TestFileSystem();

        saver = new ProjectScreenModelSaver( pomService,
                                             kModuleService,
                                             importsService,
                                             repositoriesService,
                                             whiteListService,
                                             ioService,
                                             projectService,
                                             repositoryResolver,
                                             commentedOptionFactory );

        pathToPom = testFileSystem.createTempFile( "testproject/pom.xml" );
    }

    @After
    public void tearDown() throws Exception {
        testFileSystem.tearDown();
    }

    @Test
    public void testPatchSave() throws Exception {
        final CommentedOption commentedOption = new CommentedOption( "hello" );
        when( commentedOptionFactory.makeCommentedOption( "message" ) ).thenReturn( commentedOption );

        saver.save( pathToPom,
                    new ProjectScreenModel(),
                    DeploymentMode.FORCED,
                    "message" );

        verify( ioService ).startBatch( any( FileSystem.class ),
                                        eq( commentedOption ) );

        verify( ioService ).endBatch();
    }

    @Test
    public void testPOMSave() throws Exception {
        final ProjectScreenModel model = new ProjectScreenModel();
        final POM pom = new POM();
        model.setPOM( pom );
        final Metadata pomMetaData = new Metadata();
        model.setPOMMetaData( pomMetaData );

        saver.save( pathToPom,
                    model,
                    DeploymentMode.FORCED,
                    "message" );

        verify( pomService ).save( eq( pathToPom ),
                                   eq( pom ),
                                   eq( pomMetaData ),
                                   eq( "message" ) );
    }

    @Test
    public void testKModuleSave() throws Exception {
        final ProjectScreenModel model = new ProjectScreenModel();
        final KModuleModel kModule = new KModuleModel();
        model.setKModule( kModule );
        final Path pathToKModule = mock( Path.class );
        model.setPathToKModule( pathToKModule );
        final Metadata metadata = new Metadata();
        model.setKModuleMetaData( metadata );

        saver.save( pathToPom,
                    model,
                    DeploymentMode.FORCED,
                    "message kmodule" );

        verify( kModuleService ).save( eq( pathToKModule ),
                                       eq( kModule ),
                                       eq( metadata ),
                                       eq( "message kmodule" ) );
    }

    @Test
    public void testImportsSave() throws Exception {
        final ProjectScreenModel model = new ProjectScreenModel();
        final ProjectImports projectImports = new ProjectImports();
        model.setProjectImports( projectImports );
        final Path pathToImports = mock( Path.class );
        model.setPathToImports( pathToImports );
        final Metadata metadata = new Metadata();
        model.setProjectImportsMetaData( metadata );

        saver.save( pathToPom,
                    model,
                    DeploymentMode.FORCED,
                    "message imports" );

        verify( importsService ).save( eq( pathToImports ),
                                       eq( projectImports ),
                                       eq( metadata ),
                                       eq( "message imports" ) );
    }

    @Test
    public void testRepositoriesSave() throws Exception {
        final ProjectScreenModel model = new ProjectScreenModel();
        final ProjectRepositories projectRepositories = new ProjectRepositories();
        model.setRepositories( projectRepositories );
        final Path pathToRepositories = mock( Path.class );
        model.setPathToRepositories( pathToRepositories );

        saver.save( pathToPom,
                    model,
                    DeploymentMode.FORCED,
                    "message repositories" );

        verify( repositoriesService ).save( eq( pathToRepositories ),
                                            eq( projectRepositories ),
                                            eq( "message repositories" ) );
    }

    @Test
    public void testWhiteListSave() throws Exception {
        final ProjectScreenModel model = new ProjectScreenModel();
        final WhiteList whiteList = new WhiteList();
        model.setWhiteList( whiteList );
        final Path pathToWhiteList = mock( Path.class );
        model.setPathToWhiteList( pathToWhiteList );
        final Metadata metadata = new Metadata();
        model.setWhiteListMetaData( metadata );

        saver.save( pathToPom,
                    model,
                    DeploymentMode.FORCED,
                    "message white list" );

        verify( whiteListService ).save( eq( pathToWhiteList ),
                                         eq( whiteList ),
                                         eq( metadata ),
                                         eq( "message white list" ) );
    }

}