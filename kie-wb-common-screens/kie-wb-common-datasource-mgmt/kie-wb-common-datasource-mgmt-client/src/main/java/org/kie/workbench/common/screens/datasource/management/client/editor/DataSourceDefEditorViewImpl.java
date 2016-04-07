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

@Dependent
@Templated
public class DataSourceDefEditorViewImpl
        extends BaseEditorViewImpl
        implements DataSourceDefEditorPresenter.DataSourceDefEditorView {

    @DataField ( value = "name-form-group" )
    Element nameFormGroup =  DOM.createDiv();

    @DataField ( value = "name" )
    TextBox nameTextBox = GWT.create( TextBox.class );

    @DataField ( value = "jndi-form-group" )
    Element jndiFormGroup =  DOM.createDiv();

    @DataField ( value = "jndi" )
    TextBox jndiTextBox = GWT.create( TextBox.class );

    @DataField ( value = "connection-url-form-group" )
    Element connectionURLFormGroup =  DOM.createDiv();

    @DataField ( value = "connection-url" )
    TextBox connectionURLTextBox = GWT.create( TextBox.class );

    @DataField ( value = "user-form-group" )
    Element userFormGroup =  DOM.createDiv();

    @DataField ( value = "user" )
    TextBox userTextBox = GWT.create( TextBox.class );

    @DataField ( value = "password-form-group" )
    Element passwordFormGroup =  DOM.createDiv();

    @DataField ( value = "password" )
    TextBox passwordTextBox = GWT.create( TextBox.class );

    @Inject
    @DataField("deploy-btn")
    Button deployButton;

    @Inject
    @DataField("undeploy-btn")
    Button undeployButton;

    @Inject
    @DataField("test-btn")
    Button testButton;

    private DataSourceDefEditorPresenter presenter;

    private TranslationService translationService;

    @Inject
    public DataSourceDefEditorViewImpl( final TranslationService translationService ) {
        super();
        this.translationService = translationService;
    }

    @PostConstruct
    private void init() {
        //UI initializations
        enableDeployButton( false );
        enableUnDeployButton( false );
        enableTestButton( false );
    }

    @Override
    public void init( DataSourceDefEditorPresenter presenter ) {
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
    public void setJndi( String jndi ) {
        this.jndiTextBox.setText( jndi );
    }

    @Override
    public String getJndi() {
        return jndiTextBox.getText();
    }

    @Override
    public String getConnectionURL() {
        return connectionURLTextBox.getText();
    }

    @Override
    public void setConnectionURL( String connectionURL ) {
        this.connectionURLTextBox.setText( connectionURL );
    }

    @Override
    public String getUser() {
        return userTextBox.getText();
    }

    @Override
    public void setUser( String user ) {
        this.userTextBox.setText( user );
    }

    @Override
    public String getPassword() {
        return passwordTextBox.getText();
    }

    @Override
    public void setPassword( String password ) {
        this.passwordTextBox.setText( password );
    }

    @Override
    public void enableDeployButton( boolean enabled ) {
        deployButton.setEnabled( enabled );
    }

    @Override
    public void enableUnDeployButton( boolean enabled ) {
        undeployButton.setEnabled( enabled );
    }

    @Override
    public void enableTestButton( boolean enabled ) {
        testButton.setEnabled( enabled );
    }

    @EventHandler("deploy-btn")
    public void onDeploy( final ClickEvent event ) {
        presenter.onDeployDataSource();
    }

    @EventHandler("undeploy-btn")
    public void onUnDeploy( final ClickEvent event ) {
        presenter.onUnDeployDataSource();
    }

    @EventHandler("test-btn")
    public void onTest( final ClickEvent event ) {
        presenter.onUnTestDataSource();
    }
}
