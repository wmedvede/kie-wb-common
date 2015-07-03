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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.services.datamodeller.core.Annotation;

public class AdvancedDataObjectEditorViewImpl
        extends Composite
        implements AdvancedDataObjectEditorView {

    interface AdvancedDataObjectEditorViewImplUiBinder
            extends UiBinder<Widget, AdvancedDataObjectEditorViewImpl> {

    }

    private static AdvancedDataObjectEditorViewImplUiBinder uiBinder = GWT.create( AdvancedDataObjectEditorViewImplUiBinder.class );

    @UiField
    SimplePanel annotationEditorPanel;

    @Inject
    AdvancedAnnotationListEditor annotationEditor;

    private Presenter presenter;

    public AdvancedDataObjectEditorViewImpl( ) {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    @PostConstruct
    private void init() {
        //annotationEditor.asWidget().setSize( "320px", "380px" );
        annotationEditorPanel.add( annotationEditor );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    public void loadAnnotations( List<Annotation> annotations ) {
        annotationEditor.loadAnnotations( annotations );
    }

}
