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

package org.kie.workbench.common.screens.datamodeller.client.widgets.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.event.Event;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchContextChangeEvent;
import org.kie.workbench.common.screens.datamodeller.client.widgets.DomainEditorBaseTest;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerEvent;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.datamodeller.core.impl.ObjectPropertyImpl;
import org.mockito.Mock;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.LockRequiredEvent;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class DataObjectBrowserTest
        extends DomainEditorBaseTest {

    @Mock
    DataObjectBrowserView view;

    Event<DataModelerWorkbenchContextChangeEvent> dataModelerWBContextEvent = mock( EventSourceMock.class );

    Event<LockRequiredEvent> lockRequiredEvent = mock( EventSourceMock.class );

    @Mock
    PlaceManager placeManager;

    @Mock
    NewFieldPopupView newFieldPopupView;

    NewFieldPopup newFieldPopup;

    @Mock
    Path dummyPath;

    Event<DataModelerEvent> dataModelerEvent = mock( EventSourceMock.class );

    protected DataObjectBrowser createBrowser() {

        newFieldPopup = new NewFieldPopup( newFieldPopupView );

        DataObjectBrowser objectBrowser = new DataObjectBrowser( handlerRegistry,
                commandBuilder,
                modelerServiceCaller,
                validatorService,
                dataModelerEvent,
                dataModelerWBContextEvent,
                lockRequiredEvent,
                placeManager,
                newFieldPopup,
                view );

        //emulate the @PostConstruct method invocation.
        objectBrowser.init();

        return objectBrowser;
    }

    @Test
    public void loadContextTest() {

        DataObjectBrowser objectBrowser = createBrowser();
        DataModelerContext context = createContext();

        objectBrowser.setContext( context );

        verify( view, times( 1 ) ).setReadonly( context.isReadonly() );
        verify( view, times( 1 ) ).setObjectSelectorLabel( "TestObject1Label (TestObject1)", context.getDataObject().getClassName() );
    }

    @Test
    public void removePropertyTest() {

        DataObjectBrowser objectBrowser = createBrowser();
        DataModelerContext context = createContext();

        //the dataObject has fields: field1, field2 and field3
        DataObject dataObject = context.getDataObject();
        ObjectProperty objectProperty = dataObject.getProperty( "field3" );
        int count = dataObject.getProperties().size();

        context.getEditorModelContent().setPath( dummyPath );

        objectBrowser.setContext( context );

        when( modelerService.findFieldUsages( dummyPath, dataObject.getClassName(), objectProperty.getName() ) )
                .thenReturn( new ArrayList<Path>() );

        //field3 is on position 2 by construction.
        objectBrowser.onDeleteProperty( objectProperty, 2 );

        //if field3 was removed, then field2 should have been selected.
        verify( view ).setSelectedRow( dataObject.getProperty( "field2" ), true );
        //an even should have been fired with the notification of the just removed property.
        verify( dataModelerEvent, times( 1 ) ).fire( any( DataModelerEvent.class ) );
        //the dataObject should now have one less property.
        assertEquals( (count - 1), dataObject.getProperties().size() );
    }

    @Test
    public void addValidPropertyAndContinueTest( ) {
        addValidPropertyTest( true );
    }

    @Test
    public void addValidPropertyAndCloseTest( ) {
        addValidPropertyTest( false );
    }

    private void addValidPropertyTest( boolean createAndContinue ) {

        DataObjectBrowser objectBrowser = createBrowser();
        DataModelerContext context = createContext();
        objectBrowser.setContext( context );

        //the dataObject has fields: field1, field2 and field3
        DataObject dataObject = context.getDataObject();

        //open the new property dialog.
        objectBrowser.onNewProperty();

        //check the new field popup is shown
        verify( newFieldPopupView, times( 1 ) ).show();

        //emulate the user data entering in the new field popup
        when( newFieldPopupView.getFieldName() ).thenReturn( "field4" );
        when( newFieldPopupView.getSelectedType() ).thenReturn( "java.lang.String" );
        when( newFieldPopupView.getIsMultiple() ).thenReturn( false );

        //emulate that the provided field name is correct
        Map<String, Boolean> validationResult = new HashMap<String, Boolean>(  );
        validationResult.put( "field4", true );
        when( validationService.evaluateJavaIdentifiers( new String[] { "field4" } ) ).thenReturn( validationResult );

        //emulate the user pressing the create button in the new field popup
        newFieldPopup.onCreate();

        //the new field popup should have been closed and the new property shoud have been added o the data object.
        ObjectProperty expectedProperty = new ObjectPropertyImpl( "field4", "java.lang.String", false );

        if ( createAndContinue ) {
            verify( newFieldPopupView, times( 1 ) ).clear();
        } else {
            verify( newFieldPopupView, times( 1 ) ).hide();
        }
        assertEquals( 4, dataObject.getProperties().size() );
        assertEquals( expectedProperty, dataObject.getProperties().get( 3 ) );
    }

    @Test
    public void addInvalidPropertyTest() {

        DataObjectBrowser objectBrowser = createBrowser();
        DataModelerContext context = createContext();
        objectBrowser.setContext( context );

        //the dataObject has fields: field1, field2 and field3
        DataObject dataObject = context.getDataObject();

        //open the new property dialog.
        objectBrowser.onNewProperty();

        //check the new field popup is shown
        verify( newFieldPopupView, times( 1 ) ).show();

        //emulate the user data entering in the new field popup
        when( newFieldPopupView.getFieldName() ).thenReturn( "field4" );
        when( newFieldPopupView.getSelectedType() ).thenReturn( "java.lang.String" );
        when( newFieldPopupView.getIsMultiple() ).thenReturn( false );

        //emulate that the provided field name is NOT correct
        Map<String, Boolean> validationResult = new HashMap<String, Boolean>(  );
        validationResult.put( "field4", false );
        when( validationService.evaluateJavaIdentifiers( new String[] { "field4" } ) ).thenReturn( validationResult );

        //emulate the user pressing the create button in the new field popup
        newFieldPopup.onCreate();

        //the error message should have been set
        verify( newFieldPopupView, times( 1 ) ).setErrorMessage( anyString() );

        //no property should have been added.
        assertEquals( 3, dataObject.getProperties().size() );
    }

}
