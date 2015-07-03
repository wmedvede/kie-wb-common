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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.properties;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.IOC;
import org.kie.workbench.common.screens.datamodeller.client.model.DataModelerPropertyEditorFieldInfo;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationeditor.AnnotationEditor;
import org.kie.workbench.common.screens.datamodeller.client.widgets.common.properties.PropertyEditionPopup;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

public class AnnotationEditionDialog
        extends BaseModal implements PropertyEditionPopup {

    interface Binder
            extends
            UiBinder<Widget, AnnotationEditionDialog> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    SimplePanel contentPanel;

    @Inject
    AnnotationEditor annotationEditor;

    PropertyEditorFieldInfo property;

    public AnnotationEditionDialog() {

        setTitle( "Annotation Editor" );
        setMaxHeigth( "450px" );
        setWidth( "600px" );

        // para esconder otro pop upsetHideOthers( true );
        add( uiBinder.createAndBindUi( this ) );

        add( new ModalFooterOKCancelButtons(
                        new Command() {
                            @Override
                            public void execute() {
                                okButton();
                            }
                        },
                        new Command() {
                            @Override
                            public void execute() {
                                cancelButton();
                            }
                        }
                )
        );
        annotationEditor = IOC.getBeanManager().lookupBean( AnnotationEditor.class ).getInstance();
        contentPanel.add( annotationEditor );

    }

    @Override
    public void show() {
        DataModelerPropertyEditorFieldInfo fieldInfo = (DataModelerPropertyEditorFieldInfo) property;
        annotationEditor.loadAnnotation( fieldInfo.getCurrentValue() );
        super.show();
    }

    private void okButton() {

    }

    private void cancelButton() {

    }

    @Override
    public void setOkCommand( Command command ) {

    }

    @Override public String getStringValue() {
        return null;
    }

    @Override public void setStringValue( String value ) {

    }

    @Override
    public void setProperty( PropertyEditorFieldInfo property ) {
        this.property = property;
    }
}
