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
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditor;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditorView;

public class ValuePairEditorPageViewImpl
        extends Composite
        implements ValuePairEditorPageView {

    interface ValuePairEditorPageViewImplUiBinder extends UiBinder<Widget, ValuePairEditorPageViewImpl> {

    }

    private static ValuePairEditorPageViewImplUiBinder uiBinder = GWT.create( ValuePairEditorPageViewImplUiBinder.class );

    private Presenter presenter;

    private ValuePairEditor valuePairEditor;

    @UiField
    DivWidget content;

    @Inject
    public ValuePairEditorPageViewImpl( ValuePairEditor valuePairEditor ) {
        this.valuePairEditor = valuePairEditor;
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    @PostConstruct
    private void init() {
        content.add( valuePairEditor );
        valuePairEditor.addEditorHandler( new ValuePairEditorView.ValuePairEditorHandler() {
            @Override
            public void onValidate() {
                presenter.onValidate();
            }

            @Override
            public void onValueChanged( String value ) {
                presenter.onValueChanged();
            }
        } );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setClassName( String className ) {
        valuePairEditor.setAnnotationClassName( className );
    }

    @Override
    public void setNameLabel( String valuePairName ) {
        valuePairEditor.setNameLabel( valuePairName );
    }

    @Override
    public String getValue() {
        return valuePairEditor.getValue();
    }

    @Override
    public void setValue( String value ) {
        valuePairEditor.setValue( value );
    }

    @Override
    public void clearHelpMessage() {
        valuePairEditor.clearErrorMessage();
    }

    @Override
    public void setHelpMessage( String helpMessage ) {
        valuePairEditor.setErrorMessage( helpMessage );
    }
}
