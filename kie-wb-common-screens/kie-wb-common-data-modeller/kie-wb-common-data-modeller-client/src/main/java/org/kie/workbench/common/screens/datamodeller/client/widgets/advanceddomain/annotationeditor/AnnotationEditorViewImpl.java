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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationeditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.AnnotationValuePairDefinition;

public class AnnotationEditorViewImpl
        extends Composite
        implements AnnotationEditorView {

    interface AnnotationEditorViewImplUiBinder
        extends
            UiBinder<Widget, AnnotationEditorViewImpl> {

    }

    private static AnnotationEditorViewImplUiBinder uiBinder = GWT.create( AnnotationEditorViewImplUiBinder.class );

    private Presenter presenter;

    @UiField
    AnnotationEditorItemsWidget itemsWidget;

    public AnnotationEditorViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void loadAnnotation( Annotation annotation ) {
        if ( annotation.getAnnotationDefinition() != null ) {
            AnnotationEditorItemWidget item = GWT.create( AnnotationEditorItemWidget.class );

            for ( AnnotationValuePairDefinition valuePairDefinition : annotation.getAnnotationDefinition().getValuePairs() ) {

                AnnotationItemLabel label = GWT.create( AnnotationItemLabel.class );
                label.setText( valuePairDefinition.getName() );
                item.add( label );


                AnnotationItemTextEditor textEditor = GWT.create( AnnotationItemTextEditor.class );
                //TODO provide a mechanism for painting a value as text, basically we should generate
                //the same value that we generate when we save the annotation code.
                Object value = annotation.getValue( valuePairDefinition.getName() );
                String strValue = value != null ? value.toString() : null;

                textEditor.setText( strValue );
                item.add( textEditor );
            }
            itemsWidget.add( item );
        }
    }
}
