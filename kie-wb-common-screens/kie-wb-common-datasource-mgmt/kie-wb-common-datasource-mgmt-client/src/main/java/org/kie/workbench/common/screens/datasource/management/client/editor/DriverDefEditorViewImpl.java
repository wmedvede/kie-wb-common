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

package org.kie.workbench.common.screens.datasource.management.client.editor;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.editor.commons.client.BaseEditorViewImpl;
import org.uberfire.ext.widgets.core.client.editors.defaulteditor.DefaultEditorFileUpload;

@Dependent
@Templated
public class DriverDefEditorViewImpl
        extends BaseEditorViewImpl
        implements DriverDefEditorPresenter.DriverDefEditorView {

    @DataField ( value = "name-form-group" )
    Element nameFormGroup =  DOM.createDiv();

    @DataField ( value = "name" )
    TextBox nameTextBox = GWT.create( TextBox.class );

    @DataField ( value = "driver-class-form-group" )
    Element driverClassFormGroup =  DOM.createDiv();

    @DataField ( value = "driver-class" )
    TextBox driverClassTextBox = GWT.create( TextBox.class );

    @Inject
    @DataField( "deploy-btn" )
    Button deployButton;

    @Inject
    @DataField( "undeploy-btn" )
    Button undeployButton;

    @Inject
    @DataField( "file-upload" )
    DefaultEditorFileUpload fileUpload;

    private DriverDefEditorPresenter presenter;

    private TranslationService translationService;

    @Inject
    public DriverDefEditorViewImpl( final TranslationService translationService ) {
        super();
        this.translationService = translationService;
    }

    @PostConstruct
    private void init() {
        //UI initializations
        enableDeployButton( false );
        enableUnDeployButton( false );
    }

    @Override
    public void init( DriverDefEditorPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setName( String name ) {
        this.nameTextBox.setText( name );
    }

    @Override
    public String getName() {
        return nameTextBox.getText();
    }

    @Override
    public void setDriverClass( String driverClass ) {
        this.driverClassTextBox.setText( driverClass );
    }

    @Override
    public String getDriverClass() {
        return driverClassTextBox.getText();
    }

    @Override
    public void enableDeployButton( boolean enabled ) {
        deployButton.setEnabled( enabled );
    }

    @Override
    public void enableUnDeployButton( boolean enabled ) {
        undeployButton.setEnabled( enabled );
    }

    @EventHandler("deploy-btn")
    public void onDeploy( final ClickEvent event ) {
        presenter.onDeployDriver();
    }

    @EventHandler("undeploy-btn")
    public void onUnDeploy( final ClickEvent event ) {
        presenter.onUnDeployDriver();
    }

}
