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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain;

import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain.FieldEditor;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;

public class AdvancedDataObjectFieldEditor
        extends FieldEditor
        implements AdvancedDataObjectFieldEditorView.Presenter {

    private AdvancedDataObjectFieldEditorView view;

    @Inject
    public AdvancedDataObjectFieldEditor( AdvancedDataObjectFieldEditorView view ) {
        this.view = view;
        view.setPresenter( this );
        initWidget( view.asWidget() );
    }

    @Override
    public String getName() {
        return "ADVANCED_FIELD_EDITOR";
    }

    @Override
    public String getDomainName() {
        return AdvancedDomainEditor.ADVANCED_DOMAIN;
    }

    @Override
    protected void loadDataObjectField( DataObject dataObject, ObjectProperty objectField ) {
        clean();
        setReadonly( true );
        if ( dataObject != null && objectField != null ) {
            view.loadAnnotations( objectField.getAnnotations() );
        }
    }

    @Override
    public void onDeleteAnnotation( Annotation annotation ) {
        if ( Window.confirm(" Are you sure that you want to remove annotation: " +
                annotation.getClassName() + " from field: " + objectField.getName() ) ) {
            commandBuilder.buildFieldAnnotationRemoveCommand( getContext(),
                    getName(),
                    getDataObject(),
                    getObjectField(),
                    annotation.getClassName() ).execute();
            view.removeAnnotation( annotation );
        }
    }

    public void clean() {
        view.clean();
    }
}