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
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datasource.management.client.editor.driver.DriverDefEditorHelper;
import org.kie.workbench.common.screens.datasource.management.client.editor.driver.DriverDefMainPanel;
import org.kie.workbench.common.screens.datasource.management.client.editor.driver.DriverDefMainPanelView;
import org.kie.workbench.common.screens.datasource.management.client.util.DataSourceManagementTestConstants;
import org.kie.workbench.common.screens.datasource.management.client.util.ClientValidationServiceMock;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.mockito.Mock;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class DriverDefPageTest
        implements DataSourceManagementTestConstants {

    @GwtMock
    private DriverDefPageView view;

    @GwtMock
    private DriverDefMainPanelView mainPanelView;

    private DriverDefMainPanel mainPanel;

    @Mock
    private TranslationService translationService;

    private DriverDefEditorHelper editorHelper;

    @Mock
    private EventSourceMock<WizardPageStatusChangeEvent> statusChangeEvent;

    DriverDefPage defPage;

    DriverDef driverDef;

    @Before
    public void setup() {
        mainPanel = new DriverDefMainPanel( mainPanelView );
        driverDef = new DriverDef();
        editorHelper = new DriverDefEditorHelper( translationService, new ClientValidationServiceMock( ) );
        defPage = new DriverDefPage( view, mainPanel, editorHelper, statusChangeEvent );
        defPage.setDriverDef( driverDef );
    }

    //@Test
    public void testValidChanges() {
        //emulates the user completing the page by typing valid values in all fields.
        when( mainPanelView.getName() ).thenReturn( NAME );
        when( mainPanelView.getGroupId() ).thenReturn( GROUP_ID );
        when( mainPanelView.getArtifactId() ).thenReturn( ARTIFACT_ID );
        when( mainPanelView.getVersion() ).thenReturn( VERSION );
        when( mainPanelView.getDriverClass() ).thenReturn( DRIVER_CLASS );

        mainPanel.onNameChange();
        mainPanel.onGroupIdChange();
        mainPanel.onArtifactIdChange();
        mainPanel.onVersionChange();
        mainPanel.onDriverClassChange();

        //modification event should have been fired.
        verify( statusChangeEvent, times( 5 ) ).fire( any( WizardPageStatusChangeEvent.class ) );

        //the DriverDef should have been populated
        assertEquals( NAME, driverDef.getName() );
        assertEquals( GROUP_ID, driverDef.getGroupId() );
        assertEquals( ARTIFACT_ID, driverDef.getArtifactId() );
        assertEquals( VERSION, driverDef.getVersion() );
        assertEquals( DRIVER_CLASS, driverDef.getDriverClass() );

        //the page should be in completed state.
        defPage.isComplete( new Callback<Boolean>() {
            @Override
            public void callback( Boolean result ) {
                assertTrue( result );
            }
        } );
    }

    @Test
    public void testInvalidChanges() {
        //emulates the completion of a field with an invalid value. The page should be set automatically in un-completed
        //state.

        //first force the editor to be in valid state.
        editorHelper.setValid( true );
        //now the page should be in completed state.
        defPage.isComplete( new Callback<Boolean>() {
            @Override
            public void callback( Boolean result ) {
                assertTrue( result );
            }
        } );

        //now emulates the entering of a wrong value e.g. for the driver class name
        when( mainPanelView.getDriverClass() ).thenReturn( "SomeWrongClassName" );
        editorHelper.onDriverClassChange();

        //now the page should be in un-completed state.
        defPage.isComplete( new Callback<Boolean>() {
            @Override
            public void callback( Boolean result ) {
                assertFalse( result );
            }
        } );
    }
}
