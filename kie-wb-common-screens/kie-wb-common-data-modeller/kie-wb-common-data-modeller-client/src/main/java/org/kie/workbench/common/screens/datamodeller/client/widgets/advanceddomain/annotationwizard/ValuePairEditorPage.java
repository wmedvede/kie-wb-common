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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationwizard;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditorView;
import org.kie.workbench.common.services.datamodeller.core.AnnotationValuePairDefinition;

@Dependent
public class ValuePairEditorPage
        extends CreateAnnotationWizardPage
        implements ValuePairEditorPageView.Presenter {


    @Inject
    private ValuePairEditorPageView view;

    private ValuePairEditorView.ValuePairEditorHandler editorHandler;

    private AnnotationValuePairDefinition valuePairDefinition;

    public ValuePairEditorPage() {
        setTitle( "Configure value pair" );
    }

    @PostConstruct
    private void init( ) {
        view.setPresenter( this );
        content.add( view );
    }

    public String getName() {
        return view.getName();
    }

    public void setName( String name ) {
        view.setName( name );
    }

    public String getValue() {
        return view.getValue();
    }

    public void setValue( String value ) {
        view.setValue( value );
    }

    public AnnotationValuePairDefinition getValuePairDefinition() {
        return valuePairDefinition;
    }

    public void setValuePairDefinition( AnnotationValuePairDefinition valuePairDefinition ) {
        this.valuePairDefinition = valuePairDefinition;
    }

    public void clearHelpMessage() {
        view.clearHelpMessage();
    }

    public void setHelpMessage( String helpMessage ) {
        view.setHelpMessage( helpMessage );
    }

    public void addEditorHandler( ValuePairEditorView.ValuePairEditorHandler editorHandler ) {
        this.editorHandler = editorHandler;
    }

    @Override
    public void onValidate() {
        if ( editorHandler != null ) {
            editorHandler.onValidate();
        }
    }

    @Override
    public void onValueChanged() {
        setStatus( PageStatus.NOT_VALIDATED );
        if ( editorHandler != null ) {
            editorHandler.onValueChanged( view.getValue() );
        }
    }
}
