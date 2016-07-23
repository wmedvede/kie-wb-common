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

package org.kie.workbench.common.screens.datasource.management.client.editor.driver;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datasource.management.client.type.DriverDefType;
import org.kie.workbench.common.screens.datasource.management.client.util.ClientValidationServiceMock;
import org.kie.workbench.common.screens.datasource.management.client.util.DataSourceManagementTestConstants;
import org.kie.workbench.common.screens.datasource.management.client.util.PopupsUtil;
import org.kie.workbench.common.screens.datasource.management.client.validation.ClientValidationService;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DriverManagementService;
import org.mockito.Mock;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.ext.editor.commons.client.history.VersionRecordManager;
import org.uberfire.ext.editor.commons.client.menu.BasicFileMenuBuilder;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.PlaceRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class DriverDefEditorTest
        implements DataSourceManagementTestConstants {

    @GwtMock
    private DriverDefEditorView view;

    @GwtMock
    private DriverDefMainPanelView mainPanelView;

    private DriverDefMainPanel mainPanel;

    private DriverDefEditorHelper editorHelper;

    @Mock
    private TranslationService translationService;

    @Mock
    private ClientValidationService clientValidationService;

    @GwtMock
    private DriverDefType type;

    @Mock
    private DriverDefEditorService editorService;

    @Mock
    private Caller<DriverDefEditorService> editorServiceCaller;

    @Mock
    private DriverManagementService driverService;

    private Caller<DriverManagementService> driverServiceCaller;

    @Mock
    private DataSourceManagementService managementService;

    private Caller<DataSourceManagementService> managementServiceCaller;

    private DriverDefEditor editor;

    @Mock
    private ObservablePath path;

    @GwtMock
    private VersionRecordManager versionRecordManager;

    @Mock
    private PlaceRequest placeRequest;

    @GwtMock
    private PopupsUtil popupsUtil;

    private DriverDefEditorContent content;

    @Before
    public void setup() {

        mainPanel = new DriverDefMainPanel( mainPanelView );
        clientValidationService = new ClientValidationServiceMock( );
        editorHelper = new DriverDefEditorHelper( translationService, clientValidationService );
        editorServiceCaller = new CallerMock<>( editorService );
        driverServiceCaller = new CallerMock<>( driverService );
        managementServiceCaller = new CallerMock<>( managementService );

        editor = new DriverDefEditor( view,
                mainPanel, editorHelper, popupsUtil, type, editorServiceCaller, driverServiceCaller, managementServiceCaller ) {
            {
                this.versionRecordManager = DriverDefEditorTest.this.versionRecordManager;
                this.menuBuilder = mock( BasicFileMenuBuilder.class );
            }
        };

        verify( view, times( 1 ) ).init( editor );
        verify( view, times( 1 ) ).setMainPanel( mainPanel );
    }

    private void prepareLoadFileSuccessful() {
        //opens the editor with a valid content.
        content = createContent();
        when( versionRecordManager.getCurrentPath() ).thenReturn( path );
        when( editorService.loadContent( path ) ).thenReturn( content );
        editor.onStartup( path, placeRequest );
    }

    @Test
    public void testLoadFileSuccessFul() {

        prepareLoadFileSuccessful();

        //verifies the content was properly loaded and properly set on the UI
        verify( view, times( 1 ) ).showLoading();
        verify( view, times( 1 ) ).hideBusyIndicator();
        assertEquals( content, editor.getContent() );

        verify( mainPanelView, times( 1 ) ).setName( content.getDriverDef().getName() );
        verify( mainPanelView, times( 1 ) ).setGroupId( content.getDriverDef().getGroupId() );
        verify( mainPanelView, times( 1 ) ).setArtifactId( content.getDriverDef().getArtifactId() );
        verify( mainPanelView, times( 1 ) ).setVersion( content.getDriverDef().getVersion() );
        verify( mainPanelView, times( 1 ) ).setDriverClass( content.getDriverDef().getDriverClass() );
    }

    @Test
    public void testEditorChanges() {

        //open the editor with a valid content.
        prepareLoadFileSuccessful();

        //emulates some valid changes in the editor.
        when( mainPanelView.getName() ).thenReturn( NAME_2 );
        when( mainPanelView.getDriverClass() ).thenReturn( DRIVER_CLASS_2 );
        when( mainPanelView.getGroupId() ).thenReturn( GROUP_ID_2 );
        when( mainPanelView.getArtifactId() ).thenReturn( ARTIFACT_ID_2 );
        when( mainPanelView.getVersion() ).thenReturn( VERSION_2 );

        mainPanel.onNameChange();
        mainPanel.onDriverClassChange();
        mainPanel.onGroupIdChange();
        mainPanel.onArtifactIdChange();
        mainPanel.onVersionChange();

        //the content of the editor should have been properly modified.
        assertEquals( NAME_2, editor.getContent().getDriverDef().getName() );
        assertEquals( GROUP_ID_2, editor.getContent().getDriverDef().getGroupId() );
        assertEquals( ARTIFACT_ID_2, editor.getContent().getDriverDef().getArtifactId() );
        assertEquals( VERSION_2, editor.getContent().getDriverDef().getVersion() );
        assertEquals( DRIVER_CLASS_2, editor.getContent().getDriverDef().getDriverClass() );
    }

    private DriverDefEditorContent createContent() {
        DriverDefEditorContent content = new DriverDefEditorContent();
        content.setDriverDef( new DriverDef() );
        content.getDriverDef().setUuid( DRIVER_UUID );
        content.getDriverDef().setName( NAME );
        content.getDriverDef().setGroupId( GROUP_ID );
        content.getDriverDef().setArtifactId( ARTIFACT_ID );
        content.getDriverDef().setVersion( VERSION );
        content.getDriverDef().setDriverClass( DRIVER_CLASS );
        return content;
    }
}