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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.commons.data.Pair;
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

    @DataField ( value = "driver-form-group" )
    Element driverFormGroup = DOM.createDiv();

    @Inject
    @DataField ( value = "driver-selector" )
    Select driverSelector;

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
    public void init( final DataSourceDefEditorPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setName( final String name ) {
        this.nameTextBox.setText( name );
    }

    @Override
    public String getName() {
        return nameTextBox.getText();
    }

    @Override
    public void setJndi( final String jndi ) {
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
    public void setConnectionURL( final String connectionURL ) {
        this.connectionURLTextBox.setText( connectionURL );
    }

    @Override
    public String getUser() {
        return userTextBox.getText();
    }

    @Override
    public void setUser( final String user ) {
        this.userTextBox.setText( user );
    }

    @Override
    public String getPassword() {
        return passwordTextBox.getText();
    }

    @Override
    public void setPassword( final String password ) {
        this.passwordTextBox.setText( password );
    }

    @Override
    public void enableDeployButton( final boolean enabled ) {
        deployButton.setEnabled( enabled );
    }

    @Override
    public void enableUnDeployButton( final boolean enabled ) {
        undeployButton.setEnabled( enabled );
    }

    @Override
    public void enableTestButton( final boolean enabled ) {
        testButton.setEnabled( enabled );
    }

    @Override
    public void loadDriverOptions( final List<Pair<String, String>> driverOptions, final boolean addEmptyOption ) {
        driverSelector.clear();
        if ( addEmptyOption ) {
            driverSelector.add( newOption( "", "" ) );
        }
        for ( Pair<String, String> optionPair: driverOptions ) {
            driverSelector.add( newOption( optionPair.getK1(), optionPair.getK2() ));
        }
        refreshDriverSelector();
    }

    @Override
    public String getDriver() {
        return driverSelector.getValue();
    }

    @Override
    public void setDriver( final String driver ) {
        driverSelector.setValue( driver );
        refreshDriverSelector();
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

    private Option newOption( final String text, final String value ) {
        final Option option = new Option();
        option.setValue( value );
        option.setText( text );
        return option;
    }

    private void refreshDriverSelector() {
        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                driverSelector.refresh();
            }
        } );
    }
}
