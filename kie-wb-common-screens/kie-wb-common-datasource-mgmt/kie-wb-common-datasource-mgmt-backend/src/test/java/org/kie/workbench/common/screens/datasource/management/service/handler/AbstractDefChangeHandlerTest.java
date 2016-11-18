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

package org.kie.workbench.common.screens.datasource.management.service.handler;

import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceRuntimeManager;
import org.kie.workbench.common.screens.datasource.management.backend.core.DeploymentOptions;
import org.kie.workbench.common.screens.datasource.management.backend.core.UnDeploymentOptions;
import org.kie.workbench.common.screens.datasource.management.backend.service.DataSourceServicesHelper;
import org.kie.workbench.common.screens.datasource.management.backend.service.DefRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.service.handler.AbstractDefChangeHandler;
import org.kie.workbench.common.screens.datasource.management.events.DeleteDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.events.NewDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.events.UpdateDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.Def;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.TestDriver;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceDefSerializer;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceEventHelper;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.rpc.SessionInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class AbstractDefChangeHandlerTest {

    private static final String FILE_URI = "default://master@datasources/MockUri.file";

    private static final String SESSION_ID = "SESSION_ID";

    private static final String IDENTIFIER = "IDENTIFIER";

    @Mock
    protected DataSourceRuntimeManager runtimeManager;

    @Mock
    protected DataSourceServicesHelper serviceHelper;

    @Mock
    protected IOService ioService;

    @Mock
    protected KieProjectService projectService;

    @Mock
    protected DataSourceEventHelper eventHelper;

    @Mock
    protected DefRegistry defRegistry;

    @Mock
    protected Path path;

    @Mock
    protected Path originalPath;

    @Mock
    protected SessionInfo sessionInfo;

    @Mock
    protected User identity;

    @Mock
    protected KieProject project;

    protected DataSourceDef dataSourceDef;

    @Mock
    protected DataSourceDef registeredDataSourceDef;

    protected DriverDef driverDef;

    @Mock
    protected DriverDef registeredDriverDef;

    @Mock
    protected DataSourceDeploymentInfo dataSourceDeploymentInfo;

    @Mock
    protected DriverDeploymentInfo driverDeploymentInfo;

    AbstractDefChangeHandler changeHandler;

    @Before
    public void setup() {

        changeHandler = new AbstractDefChangeHandler( runtimeManager,
                serviceHelper, ioService, projectService, eventHelper ) {

        };
        when( serviceHelper.getDefRegistry() ).thenReturn( defRegistry );
        when( projectService.resolveProject( path ) ).thenReturn( project );

        when( sessionInfo.getId() ).thenReturn( SESSION_ID );
        when( sessionInfo.getIdentity() ).thenReturn( identity );
        when( identity.getIdentifier() ).thenReturn( IDENTIFIER );

        dataSourceDef = new DataSourceDef();
        dataSourceDef.setUuid( "uuid" );
        dataSourceDef.setName( "dataSourceName" );
        dataSourceDef.setConnectionURL( "connectionURL" );
        dataSourceDef.setUser( "user" );
        dataSourceDef.setPassword( "password" );

        driverDef = new DriverDef();
        driverDef.setUuid( "uuid" );
        driverDef.setName( "driverName" );
        driverDef.setDriverClass( TestDriver.class.getName() );
        driverDef.setGroupId( "groupId" );
        driverDef.setArtifactId( "artifactId" );
        driverDef.setVersion( "version" );
    }

    /**
     * Tests the case when the file added is a datasource that wasn't previously registered.
     */
    @Test
    public void testAddDataSourceNotRegistered() throws Exception {
        prepareDataSourceDef();
        changeHandler.processResourceAdd( path, sessionInfo );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyAddEvent( path, dataSourceDef );
    }

    /**
     * Tests the case when the file added is a datasource that was already registered and the definition didn't change.
     */
    @Test
    public void testAddDataSourceRegisteredNotChanged() throws Exception {
        prepareDataSourceDef();
        //emulates that the definition was already registered.
        prepareRegisteredResource( path, dataSourceDef, false );
        changeHandler.processResourceAdd( path, sessionInfo );
        verifyNoActions();
    }

    /**
     * Tests the case when the file added is a datasource that was already registered, the definition has changed,
     * and the previous definition was not deployed.
     */
    @Test
    public void testAddDataSourceRegisteredChangedNotDeployed() throws Exception {
        prepareDataSourceDef();
        //emulates that a different definition is registered.
        prepareRegisteredResource( path, registeredDataSourceDef, false );
        changeHandler.processResourceAdd( path, sessionInfo );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyAddEvent( path, dataSourceDef );
    }

    /**
     * Tests the case when the file added is a datasource that was already registered, the definition has changed,
     * and the previous definition was deployed.
     */
    @Test
    public void testAddDataSourceRegisteredChangedDeployed() throws Exception {
        prepareDataSourceDef();
        //emulates that a different definition is registered and also was deployed.
        prepareRegisteredResource( path, registeredDataSourceDef, true );
        changeHandler.processResourceAdd( path, sessionInfo );
        verifyUnDeployed( registeredDataSourceDef );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyAddEvent( path, dataSourceDef );
    }

    /**
     * Tests the case when the file updated is a datasource that wasn't previously registered.
     */
    @Test
    public void testUpdateDataSourceNotRegistered() throws Exception {
        prepareDataSourceDef();
        changeHandler.processResourceUpdate( path, sessionInfo );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyUpdateEvent( path, dataSourceDef, null );
    }

    /**
     * Tests the case when the file updated is a datasource that was already registered and the definition didn't change.
     */
    @Test
    public void testUpdateDataSourceRegisteredNotChanged() throws Exception {
        prepareDataSourceDef();
        //emulates that the definition was already registered.
        prepareRegisteredResource( path, dataSourceDef, false );
        changeHandler.processResourceUpdate( path, sessionInfo );
        verifyNoActions();
    }

    /**
     * Tests the case when the file updated is a datasource that was already registered, the definition has changed,
     * and the previous definition was not deployed.
     */
    @Test
    public void testUpdateDataSourceRegisteredChangedNotDeployed() throws Exception {
        prepareDataSourceDef();
        //emulates that a different definition is registered, but not deployed.
        prepareRegisteredResource( path, registeredDataSourceDef, false );
        changeHandler.processResourceUpdate( path, sessionInfo );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyUpdateEvent( path, dataSourceDef, registeredDataSourceDef );
    }

    /**
     * Tests the case when the file updated is a datasource that was already registered, the definition has changed,
     * and the previous definition was deployed.
     */
    @Test
    public void testUpdateDataSourceRegisteredChangedDeployed() throws Exception {
        prepareDataSourceDef();
        //emulates that a different definition is registered and also was deployed.
        prepareRegisteredResource( path, registeredDataSourceDef, true );
        changeHandler.processResourceUpdate( path, sessionInfo );
        verifyUnDeployed( registeredDataSourceDef );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyUpdateEvent( path, dataSourceDef, registeredDataSourceDef );
    }

    /**
     * Tests the case where the file in the original path wasn't registered, and the target path is not registered.
     */
    @Test
    public void testRenameDataSourceOriginalPathNotRegisteredTargetPathNotRegistered() throws Exception {
        prepareDataSourceDef();
        changeHandler.processResourceRename( originalPath, path, sessionInfo );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyUpdateEvent( path, dataSourceDef, null );
    }

    /**
     * Tests the case where the file in the original path was registered, and the target path is not registered.
     */
    @Test
    public void testRenameDataSourceOriginalPathRegisteredTargetPathNotRegistered() throws Exception {
        prepareDataSourceDef();
        prepareRegisteredResource( originalPath, registeredDataSourceDef, true );
        changeHandler.processResourceRename( originalPath, path, sessionInfo );
        verifyUnDeployed( registeredDataSourceDef );
        verifyRegisteredAndDeployed( path, dataSourceDef );
        verifyUpdateEvent( path, dataSourceDef, null );
    }

    //TODO continue tests here
    
    /**
     * Verifies that the given definition has been properly registered and deployed.
     */
    private void verifyRegisteredAndDeployed( Path path, Def def ) throws Exception {
        // the definition should have been registered and deployed
        verify( defRegistry, times( 1 ) ).setEntry( path, def );
        if ( def instanceof DataSourceDef ) {
            verify( runtimeManager, times( 1 ) ).deployDataSource( (DataSourceDef) def, DeploymentOptions.create( ) );
        } else {
            verify( runtimeManager, times( 1 ) ).deployDriver( (DriverDef) def, DeploymentOptions.create() );
        }
    }

    /**
     * Verifies that the given definition has been un-deployed.
     */
    private void verifyUnDeployed( Def def ) throws Exception {
        // the definition should have been un-deployed.
        if ( def instanceof DataSourceDef ) {
            DataSourceDeploymentInfo deploymentInfo = runtimeManager.getDataSourceDeploymentInfo( def.getUuid() );
            // is deployed by construction
            assertNotNull( deploymentInfo );
            verify( runtimeManager, times( 1 ) ).unDeployDataSource( deploymentInfo,
                    UnDeploymentOptions.forcedUnDeployment( ) );
        } else {
            DriverDeploymentInfo deploymentInfo = runtimeManager.getDriverDeploymentInfo( def.getUuid() );
            // is deployed by construction
            assertNotNull( deploymentInfo );
            verify( runtimeManager, times( 1 ) ).unDeployDriver( deploymentInfo,
                    UnDeploymentOptions.forcedUnDeployment( ) );
        }
    }

    /**
     * verifies that no actions has been invoked on the main components.
     */
    private void verifyNoActions() throws Exception {
        verify( defRegistry, never() ).setEntry( any( Path.class ), any( Def.class ) );
        verify( defRegistry, never() ).invalidateCache( any( Path.class) );

        verify( runtimeManager, never() ).unDeployDataSource( any( DataSourceDeploymentInfo.class ), any( UnDeploymentOptions.class ) );
        verify( runtimeManager, never() ).deployDataSource( any( DataSourceDef.class ), any( DeploymentOptions.class ) );
        verify( eventHelper, never() ).fireCreateEvent( any( NewDataSourceEvent.class ) );
        verify( eventHelper, never() ).fireUpdateEvent( any( UpdateDataSourceEvent.class ) );
        verify( eventHelper, never() ).fireDeleteEvent( any( DeleteDataSourceEvent.class ) ) ;

        verify( runtimeManager, never() ).unDeployDriver( any( DriverDeploymentInfo.class ), any( UnDeploymentOptions.class ) );
        verify( runtimeManager, never() ).deployDriver( any( DriverDef.class ), any( DeploymentOptions.class ) );
        verify( eventHelper, never() ).fireCreateEvent( any( NewDataSourceEvent.class ) );
        verify( eventHelper, never() ).fireUpdateEvent( any( UpdateDataSourceEvent.class ) );
        verify( eventHelper, never() ).fireDeleteEvent( any( DeleteDataSourceEvent.class ) ) ;
    }

    private void verifyAddEvent( Path path, DataSourceDef addedDataSourceDef ) {
        verify( eventHelper, times( 1 ) ).fireCreateEvent( new NewDataSourceEvent( addedDataSourceDef,
                projectService.resolveProject( path ), SESSION_ID, IDENTIFIER ) );
    }

    private void verifyUpdateEvent( Path path, DataSourceDef dataSourceDef, DataSourceDef originalDataSourceDef ) {
        verify( eventHelper, times( 1 ) ).fireUpdateEvent( new UpdateDataSourceEvent( dataSourceDef,
                projectService.resolveProject( path ), SESSION_ID, IDENTIFIER, originalDataSourceDef ) );
    }

    private void prepareRegisteredResource( Path path, Def registeredDef, boolean isDeployed ) throws Exception {
        when ( defRegistry.getEntry( path ) ).thenReturn( registeredDef );
        if ( registeredDef != null && isDeployed ) {
            if ( registeredDef instanceof DataSourceDef ) {
                when( runtimeManager.getDataSourceDeploymentInfo( registeredDef.getUuid() ) ).thenReturn( dataSourceDeploymentInfo );
            } else {
                when( runtimeManager.getDriverDeploymentInfo( registeredDef.getUuid() ) ).thenReturn( driverDeploymentInfo );
            }
        }
    }

    private void prepareDataSourceDef() {
        when( path.toURI() ).thenReturn( FILE_URI );
        when( path.getFileName( ) ).thenReturn( "File.datasource" );
        String content = DataSourceDefSerializer.serialize( dataSourceDef );
        when( ioService.readAllString( Paths.convert( path ) ) ).thenReturn( content );
    }


    private void testAddOrUpdateDataSource( DataSourceDef addedDataSourceDef,
                                            DataSourceDef registeredDef,
                                            boolean isDeployed,
                                            boolean add ) throws Exception {
        prepareDataSourceDef();
        prepareRegisteredResource( path, registeredDef, isDeployed );

        if ( add ) {
            changeHandler.processResourceAdd( path, sessionInfo );
        } else {
            changeHandler.processResourceUpdate( path, sessionInfo );
        }

        if ( registeredDef == null ) {
            // the definition should have been registered, deployed, and the notification should have been sent.
            verify( defRegistry, times( 1 ) ).setEntry( path, addedDataSourceDef );
            verify( runtimeManager, times( 1 ) ).deployDataSource( addedDataSourceDef, DeploymentOptions.create( ) );
            if ( add ) {
                verifyAddEvent( path, addedDataSourceDef );
            } else {
                verifyUpdateEvent( path, addedDataSourceDef, registeredDef );
            }
        } else if ( !registeredDef.equals( addedDataSourceDef ) ) {
            // the new definition is different from the already registered.
            // the definition should have been registered, deployed, and the notification should have been sent.
            verify( defRegistry, times( 1 ) ).invalidateCache( path );
            verify( defRegistry, times( 1 ) ).setEntry( path, addedDataSourceDef );
            if ( isDeployed ) {
                // if the existing definition was deployed it should also have been un-deployed.
                verify( runtimeManager, times( 1 ) ).unDeployDataSource( dataSourceDeploymentInfo, UnDeploymentOptions.forcedUnDeployment( ) );
            }
            verify( runtimeManager, times( 1 ) ).deployDataSource( addedDataSourceDef, DeploymentOptions.create( ) );
            if ( add ) {
                verifyAddEvent( path, addedDataSourceDef );
            } else {
                verifyUpdateEvent( path, addedDataSourceDef, registeredDef );
            }
        } else {
            // the registered definition is the same as the new definition.
            verify( defRegistry, never() ).setEntry( any( Path.class ), any( DataSourceDef.class ) );
            verify( runtimeManager, never() ).unDeployDataSource( any( DataSourceDeploymentInfo.class ), any( UnDeploymentOptions.class ) );
            verify( runtimeManager, never() ).deployDataSource( any( DataSourceDef.class ), any( DeploymentOptions.class ) );
            verify( eventHelper, never() ).fireCreateEvent( any( NewDataSourceEvent.class ) );
        }
    }

}