/*
 * Copyright 2015 JBoss Inc
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

import java.util.Date;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.resources.i18n.Constants;
import org.kie.workbench.common.screens.datamodeller.client.widgets.refactoring.ShowUsagesPopup;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldDeletedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldSelectedEvent;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.datamodeller.core.impl.ObjectPropertyImpl;
import org.uberfire.backend.vfs.Path;
import org.uberfire.mvp.Command;

@Dependent
public class DataObjectFieldBrowser
    implements IsWidget,
    DataObjectFieldBrowserView.Presenter {


    private DataObjectFieldBrowserView view;

    private DataObject dataObject;

    private DataModelerContext context;

    @Inject
    private Caller<DataModelerService> modelerService;

    @Inject
    private Event<DataModelerEvent> dataModelerEvent;

    public DataObjectFieldBrowser( ) {
    }

    @Inject
    public DataObjectFieldBrowser( DataObjectFieldBrowserView view ) {
        this.view = view;
        view.setPresenter( this );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public DataModelerContext getContext() {
        return context;
    }

    public void setContext( DataModelerContext context ) {
        this.context = context;
    }

    public DataObject getDataObject() {
        return dataObject;
    }

    public void loadDataObject( DataObject dataObject ) {
        view.clear();
        this.dataObject = dataObject;
        if ( dataObject != null ) {
            loadFields( dataObject.getProperties() );
        }
    }

    private void loadFields( List<ObjectProperty> properties ) {
        for ( ObjectProperty property : properties ) {
            addField( property );
        }
    }

    public void setReadOnly( boolean readOnly ) {
        view.setReadOnly( readOnly );
    }

    @Override
    public void onAddItem() {
        //TODO mock implementation
        ObjectProperty property = new ObjectPropertyImpl(  );
        property.setClassName( "Some type" );
        property.setName( "field_" + new Date() );
        addField( property );
    }

    private void addField( ObjectProperty property ) {
        FieldBrowserItem item = view.addItem();
        item.setFieldName( property.getName() );
        item.setTypeName( property.getClassName(), property.isMultiple() );
    }

    @Override
    public void onSelectItem( String itemId ) {
        FieldBrowserItem item = view.getItem( itemId );
        ObjectProperty field = getDataObject().getProperty( item.getFieldName() );
        if ( field != null ) {
            notifyFieldSelected( field );
        }
    }

    @Override
    public void onRemoveItem( String itemId  ) {
        FieldBrowserItem item = view.getItem( itemId );
        String fieldName = item.getFieldName();
        checkUsageAndDeleteDataObjectProperty( dataObject.getProperty( fieldName ), itemId );
    }

    private void checkUsageAndDeleteDataObjectProperty( final ObjectProperty objectProperty,
            final String itemId ) {

        final String className = dataObject.getClassName();
        final String fieldName = objectProperty.getName();

        if ( getContext() != null ) {

            final Path currentPath = getContext().getEditorModelContent() != null ? getContext().getEditorModelContent().getPath() : null;

            modelerService.call( new RemoteCallback<List<Path>>() {

                @Override
                public void callback( List<Path> paths ) {

                    if ( paths != null && paths.size() > 0 ) {
                        //If usages for this field were detected in project assets
                        //show the confirmation message to the user.

                        ShowUsagesPopup showUsagesPopup = ShowUsagesPopup.newUsagesPopupForDeletion(
                                Constants.INSTANCE.modelEditor_confirm_deletion_of_used_field( objectProperty.getName() ),
                                paths,
                                new Command() {
                                    @Override
                                    public void execute() {
                                        deleteDataObjectProperty( objectProperty, itemId );
                                    }
                                },
                                new Command() {
                                    @Override
                                    public void execute() {
                                        //do nothing.
                                    }
                                }
                        );

                        showUsagesPopup.setCloseVisible( false );
                        showUsagesPopup.show();

                    } else {
                        //no usages, just proceed with the deletion.
                        deleteDataObjectProperty( objectProperty, itemId );
                    }
                }
            } ).findFieldUsages( currentPath, className, fieldName );
        }
    }

    private void deleteDataObjectProperty( final ObjectProperty objectProperty, final String itemId ) {
        if ( dataObject != null ) {
            view.removeItem( itemId );
            dataObject.removeProperty( objectProperty.getName() );

            getContext().getHelper().dataObjectUnReferenced( objectProperty.getClassName(), dataObject.getClassName() );
            notifyFieldDeleted( objectProperty );
        }
    }

    private void notifyFieldSelected( ObjectProperty field ) {
        dataModelerEvent.fire( new DataObjectFieldSelectedEvent(
                getContext().getContextId(),
                DataModelerEvent.DATA_OBJECT_BROWSER,
                getDataObject(),
                field ) );
    }

    private void notifyFieldDeleted( ObjectProperty deletedProperty ) {
        dataModelerEvent.fire( new DataObjectFieldDeletedEvent(
                getContext().getContextId(),
                DataModelerEvent.DATA_OBJECT_BROWSER,
                getDataObject(),
                deletedProperty ) );
    }

}
