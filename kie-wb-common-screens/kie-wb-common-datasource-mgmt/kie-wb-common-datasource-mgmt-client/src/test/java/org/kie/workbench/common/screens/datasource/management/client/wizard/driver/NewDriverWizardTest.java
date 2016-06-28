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

package org.kie.workbench.common.screens.datasource.management.client.wizard.driver;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datasource.management.client.editor.driver.DriverDefEditorHelper;
import org.kie.workbench.common.screens.datasource.management.client.editor.driver.DriverDefMainPanel;
import org.kie.workbench.common.screens.datasource.management.client.editor.driver.DriverDefTestConstants;
import org.kie.workbench.common.screens.datasource.management.client.util.ClientValidationServiceMock;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.mockito.Mock;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;
import org.uberfire.ext.widgets.core.client.wizards.WizardView;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.workbench.events.NotificationEvent;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class NewDriverWizardTest
        implements DriverDefTestConstants {

    private DriverDefPage driverDefPage;

    @Mock
    private DriverDefEditorService driverDefService;

    private Caller<DriverDefEditorService> driverDefServiceCaller;

    @GwtMock
    private DriverDefPageView driverDefPageView;

    @GwtMock
    private DriverDefMainPanel mainPanel;

    @Mock
    private TranslationService translationService;

    private DriverDefEditorHelper editorHelper;

    @Mock
    private EventSourceMock<WizardPageStatusChangeEvent> statusChangeEvent;

    NewDriverDefWizard driverDefWizard;

    @Mock
    private EventSourceMock<NotificationEvent> notificationEvent;

    @Mock
    Project project;

    @Mock
    Path path;

    @GwtMock
    WizardView wizardView;

    @Before
    public void setup() {
        editorHelper = new DriverDefEditorHelper( translationService, new ClientValidationServiceMock() );
        driverDefServiceCaller = new CallerMock<>( driverDefService );

        driverDefPage = new DriverDefPage( driverDefPageView, mainPanel, editorHelper, statusChangeEvent );
        driverDefWizard = new NewDriverDefWizard( driverDefPage, driverDefServiceCaller, notificationEvent ) {
            {
                this.view = wizardView;
            }
        };
    }

    @Test
    public void testCreateProjectDriver() {
        testCreate( project  );
    }

    @Test
    public void testCreateGlobalDriver() {
        testCreate( null );
    }

    /**
     * Emulates a sequence of valid data entering and the wizard completion.
     */
    private void testCreate( final Project project ) {

        when( mainPanel.getName() ).thenReturn( NAME );
        when( mainPanel.getGroupId() ).thenReturn( GROUP_ID );
        when( mainPanel.getArtifactId() ).thenReturn( ARTIFACT_ID );
        when( mainPanel.getVersion() ).thenReturn( VERSION );
        when( mainPanel.getDriverClass() ).thenReturn( DRIVER_CLASS );

        if ( project != null ) {
            when( driverDefService.create( any( DriverDef.class ), eq( project ) ) ).thenReturn( path );
            driverDefWizard.setProject( project );
        } else {
            when( driverDefService.createGlobal( any( DriverDef.class ) ) ).thenReturn( path );
        }
        when( path.toString() ).thenReturn( "target_driver_path" );

        driverDefWizard.start();

        editorHelper.onNameChange();
        editorHelper.onGroupIdChange();
        editorHelper.onArtifactIdChange();
        editorHelper.onDriverClassChange();
        editorHelper.onVersionIdChange();

        DriverDef expectedDriverDef = new DriverDef();
        expectedDriverDef.setName( NAME );
        expectedDriverDef.setGroupId( GROUP_ID );
        expectedDriverDef.setArtifactId( ARTIFACT_ID );
        expectedDriverDef.setVersion( VERSION );
        expectedDriverDef.setDriverClass( DRIVER_CLASS );

        driverDefWizard.complete();

        if ( project != null ) {
            verify( driverDefService, times( 1 ) ).create( expectedDriverDef, project );

        } else {
            verify( driverDefService, times( 1 ) ).createGlobal( expectedDriverDef );
        }
        verify( notificationEvent, times( 1 ) ).fire(
                new NotificationEvent( "Driver: " + path.toString() + " was successfully created." ) );
    }
}

