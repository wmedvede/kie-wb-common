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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.kie.workbench.common.screens.datasource.management.client.validation.ClientValidationService;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.services.shared.validation.ValidationService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.editor.commons.client.validation.ValidatorCallback;
import org.uberfire.mocks.CallerMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class DriverDefEditorHelperTest
        implements DriverDefTestConstants {

    @Mock
    private TranslationService translationService;

    @Mock
    private ValidationService validationService;

    private CallerMock<ValidationService> validationServiceCaller;

    private ClientValidationService clientValidationService;

    @Mock
    private DriverDefMainPanel mainPanel;

    private DriverDefEditorHelper editorHelper;

    @Mock
    private DriverDefMainPanelView.Handler handler;

    private DriverDef driverDef;

    @Before
    public void setup() {
        validationServiceCaller = new CallerMock<>( validationService );
        clientValidationService = new ClientValidationService ( validationServiceCaller ) {
            @Override
            public void isValidDriverName( String driverName, ValidatorCallback callback ) {
                if ( driverName.equals( NAME ) ) {
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
            }
        };

        editorHelper = new DriverDefEditorHelper( translationService, clientValidationService );
        editorHelper.setHandler( handler );
        editorHelper.init( mainPanel );
        driverDef = new DriverDef();
        editorHelper.setDriverDef( driverDef );

        verify( mainPanel, times( 1 ) ).clear();
        verify( mainPanel, times( 1 ) ).setName( driverDef.getName() );
        verify( mainPanel, times( 1 ) ).setDriverClass( driverDef.getDriverClass() );
        verify( mainPanel, times( 1 ) ).setGroupId( driverDef.getGroupId() );
        verify( mainPanel, times( 1 ) ).setArtifactId( driverDef.getArtifactId() );
        verify( mainPanel, times( 1 ) ).setVersion( driverDef.getVersion() );
    }

    @Test
    public void testValidNameChange() {
        testNameChange( true );
    }

    @Test
    public void testInvalidNameChange() {
        testNameChange( false );
    }

    private void testNameChange( boolean isValid ) {
        if ( isValid ) {
            when( mainPanel.getName() ).thenReturn( NAME );
        } else {
            when( mainPanel.getName() ).thenReturn( "InvalidName" );
            when( translationService.getTranslation(
                    DataSourceManagementConstants.DriverDefEditor_InvalidNameMessage ) ).thenReturn( "ErrorMessage" );
        }

        //emulates the helper receiving the change event
        editorHelper.onNameChange();

        if ( isValid ) {
            assertTrue( editorHelper.isNameValid() );
            assertEquals( NAME, driverDef.getName() );
            verify( mainPanel, times( 1 ) ).clearNameErrorMessage();
        } else {
            assertFalse( editorHelper.isNameValid() );
            assertEquals( "InvalidName", driverDef.getName() );
            verify( mainPanel, times( 1 ) ).setNameErrorMessage( "ErrorMessage" );
        }
        verify( handler, times( 1 ) ).onNameChange();
    }

    @Test
    public void testValidGroupIdChange() {
        testGroupIdChange( true );
    }

    @Test
    public void testInvalidGroupIdCChange( ) {
        testGroupIdChange( false );
    }

    private void testGroupIdChange( boolean isValid ) {

        when( mainPanel.getGroupId() ).thenReturn( GROUP_ID );
        when( validationService.validateGroupId( GROUP_ID ) ).thenReturn( isValid );
        when( translationService.getTranslation(
                DataSourceManagementConstants.DriverDefEditor_InvalidGroupIdMessage ) ).thenReturn( "ErrorMessage" );

        //emulates the helper receiving the change event
        editorHelper.onGroupIdChange();

        if ( isValid ) {
            assertTrue( editorHelper.isGroupIdValid() );
            verify( mainPanel, times( 1 ) ).clearGroupIdErrorMessage();
        } else {
            assertFalse( editorHelper.isGroupIdValid() );
            verify( mainPanel, times( 1 ) ).setGroupIdErrorMessage( "ErrorMessage" );
        }
        verify( handler, times( 1 ) ).onGroupIdChange();
    }

    @Test
    public void testValidArtifactIdChange() {
        testArtifactIdChange( true );
    }

    @Test
    public void testInvalidArtifactIdChange() {
        testArtifactIdChange( false );
    }

    private void testArtifactIdChange( boolean isValid ) {

        when( mainPanel.getArtifactId() ).thenReturn( ARTIFACT_ID );
        when( validationService.validateArtifactId( ARTIFACT_ID ) ).thenReturn( isValid );
        when( translationService.getTranslation(
                DataSourceManagementConstants.DriverDefEditor_InvalidArtifactIdMessage ) ).thenReturn( "ErrorMessage" );

        //emulates the helper receiving the change event
        editorHelper.onArtifactIdChange();

        if ( isValid ) {
            assertTrue( editorHelper.isArtifactIdValid() );
            verify( mainPanel, times( 1 ) ).clearArtifactIdErrorMessage();
        } else {
            assertFalse( editorHelper.isArtifactIdValid() );
            verify( mainPanel, times( 1 ) ).setArtifactIdErrorMessage( "ErrorMessage" );
        }
        verify( handler, times( 1 ) ).onArtifactIdChange();
    }

    @Test
    public void testValidVersionChange() {
        testVersionChange( true );
    }

    @Test
    public void testInvalidVersionChange() {
        testVersionChange( false );
    }

    private void testVersionChange( boolean isValid ) {

        when( mainPanel.getVersion() ).thenReturn( VERSION );
        when( validationService.validateGAVVersion( VERSION ) ).thenReturn( isValid );
        when( translationService.getTranslation(
                DataSourceManagementConstants.DriverDefEditor_InvalidVersionMessage ) ).thenReturn( "ErrorMessage" );

        //emulates the helper receiving the change event
        editorHelper.onVersionIdChange();

        if ( isValid ) {
            assertTrue( editorHelper.isVersionValid() );
            verify( mainPanel, times( 1 ) ).clearVersionErrorMessage();
        } else {
            assertFalse( editorHelper.isVersionValid() );
            verify( mainPanel, times( 1 ) ).setVersionErrorMessage( "ErrorMessage" );
        }
        verify( handler, times( 1 ) ).onVersionChange();
    }

    public void testValidDriverClassChange() {
        testDriverClassChange( true );
    }

    public void testInvalidDriverClassChange() {
        testDriverClassChange( false );
    }

    private void testDriverClassChange( boolean isValid ) {

        when( mainPanel.getDriverClass() ).thenReturn( DRIVER_CLASS );
        Map<String, Boolean> validationResult = new HashMap<>(  );
        validationResult.put( DRIVER_CLASS, isValid );
        when( validationService.evaluateJavaIdentifiers( new String[] { DRIVER_CLASS } ) ).thenReturn( validationResult );
        when( translationService.getTranslation(
                DataSourceManagementConstants.DriverDefEditor_InvalidDriverClassMessage ) ).thenReturn( "ErrorMessage" );

        //emulates the helper receiving the change event
        editorHelper.onDriverClassChange();

        if ( isValid ) {
            assertTrue( editorHelper.isDriverClassValid() );
            verify( mainPanel, times( 1 ) ).clearDriverClassErrorMessage();
        } else {
            assertFalse( editorHelper.isDriverClassValid() );
            verify( mainPanel, times( 1 ) ).setDriverClassErrorMessage( "ErrorMessage" );
        }
        verify( handler, times( 1 ) ).onDriverClassChange();
    }

}
