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
import org.kie.workbench.common.screens.datasource.management.client.validation.ClientValidationService;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
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
        implements DriverDefTestConstants {

    @GwtMock
    private DriverDefEditorView view;

    @GwtMock
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

    private DriverDefEditor editor;

    @Mock
    protected ObservablePath path;

    @GwtMock
    protected VersionRecordManager versionRecordManager;

    @Mock
    protected PlaceRequest placeRequest;

    @Before
    public void setup() {

        clientValidationService = new ClientValidationServiceMock( );
        editorHelper = new DriverDefEditorHelper( translationService, clientValidationService );
        editorServiceCaller = new CallerMock<>( editorService );
        driverServiceCaller = new CallerMock<>( driverService );

        editor = new DriverDefEditor( view,
                mainPanel, editorHelper, type, editorServiceCaller, driverServiceCaller ) {
            {
                this.versionRecordManager = DriverDefEditorTest.this.versionRecordManager;
                this.menuBuilder = mock( BasicFileMenuBuilder.class );
            }
        };

        verify( view, times( 1 ) ).init( editor );
        verify( view, times( 1 ) ).setMainPanel( mainPanel );
    }

    @Test
    public void testLoadFileSuccessFull() {

        DriverDefEditorContent content = createContent();

        when( versionRecordManager.getCurrentPath() ).thenReturn( path );
        when( editorService.loadContent( path ) ).thenReturn( content );

        editor.onStartup( path, placeRequest );

        verify( view, times( 1 ) ).showLoading();
        verify( view, times( 1 ) ).hideBusyIndicator();
        assertEquals( content, editor.getContent() );
    }

    @Test
    public void testEditorChanges() {

        //open the editor with a valid content.
        DriverDefEditorContent content = createContent();
        when( versionRecordManager.getCurrentPath() ).thenReturn( path );
        when( editorService.loadContent( path ) ).thenReturn( content );
        editor.onStartup( path, placeRequest );

        //emulates some valid changes in the editor and the user finally saving the driver definition.
        when( mainPanel.getName() ).thenReturn( NAME );
        when( mainPanel.getDriverClass() ).thenReturn( DRIVER_CLASS );
        when( mainPanel.getGroupId() ).thenReturn( GROUP_ID );
        when( mainPanel.getArtifactId() ).thenReturn( ARTIFACT_ID );
        when( mainPanel.getVersion() ).thenReturn( VERSION );

        editorHelper.onNameChange();
        editorHelper.onDriverClassChange();
        editorHelper.onGroupIdChange();
        editorHelper.onArtifactIdChange();
        editorHelper.onVersionIdChange();

        //the content of the editor should have been properly modified.
        assertEquals( NAME, editor.getContent().getDriverDef().getName() );
        assertEquals( GROUP_ID, editor.getContent().getDriverDef().getGroupId() );
        assertEquals( ARTIFACT_ID, editor.getContent().getDriverDef().getArtifactId() );
        assertEquals( VERSION, editor.getContent().getDriverDef().getVersion() );
        assertEquals( DRIVER_CLASS, editor.getContent().getDriverDef().getDriverClass() );
    }

    private DriverDefEditorContent createContent() {
        DriverDefEditorContent content = new DriverDefEditorContent();
        content.setDriverDef( new DriverDef() );
        return content;
    }
}