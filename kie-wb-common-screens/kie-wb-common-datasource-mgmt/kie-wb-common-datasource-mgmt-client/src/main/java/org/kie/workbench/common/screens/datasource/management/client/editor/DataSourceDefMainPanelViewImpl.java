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
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.common.StyleHelper;

@Dependent
@Templated
public class DataSourceDefMainPanelViewImpl
        extends Composite
        implements DataSourceDefMainPanelView {

    @DataField ( "name-form-group" )
    Element nameFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "name" )
    TextBox nameTextBox;

    @DataField("name-help")
    Element nameHelp = DOM.createSpan();

    @DataField ( "jndi-form-group" )
    Element jndiFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "jndi" )
    TextBox jndiTextBox;

    @DataField( "jndi-help" )
    Element jndiHelp = DOM.createSpan();

    @DataField ( "connection-url-form-group" )
    Element connectionURLFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "connection-url" )
    TextBox connectionURLTextBox;

    @DataField("connection-url-help")
    Element connectionURLHelp = DOM.createSpan();

    @DataField ( "user-form-group" )
    Element userFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "user" )
    TextBox userTextBox;

    @DataField( "user-help" )
    Element userHelp = DOM.createSpan();

    @DataField ( "password-form-group" )
    Element passwordFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "password" )
    TextBox passwordTextBox;

    @DataField( "password-help" )
    Element passwordHelp = DOM.createSpan();

    @DataField ( "driver-form-group" )
    Element driverFormGroup = DOM.createDiv();

    @Inject
    @DataField ( "driver-selector" )
    Select driverSelector;

    @DataField( "driver-selector-help" )
    Element driverSelectorHelp = DOM.createSpan();

    @Inject
    @DataField("test-connection-button")
    Button testConnection;

    private DataSourceDefMainPanelView.Presenter presenter;

    public DataSourceDefMainPanelViewImpl( ) {
    }

    @Override
    public void init( final DataSourceDefMainPanelView.Presenter presenter ) {
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

    public void setNameErrorMessage( final String message ) {
        setGroupOnError( nameFormGroup, true );
        setSpanMessage( nameHelp, message );
    }

    public void clearNameErrorMessage() {
        setGroupOnError( nameFormGroup, false );
        clearSpanMessage( nameHelp );
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
    public void setJndiErrorMessage( final String message ) {
        setGroupOnError( jndiFormGroup, true );
        setSpanMessage( jndiHelp, message );
    }

    @Override
    public void clearJndiErrorMessage() {
        setGroupOnError( jndiFormGroup, false );
        clearSpanMessage( jndiHelp );
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
    public void setConnectionURLErrorMessage( String message ) {
        setGroupOnError( connectionURLFormGroup, true );
        setSpanMessage( connectionURLHelp, message );
    }

    @Override
    public void clearConnectionURLErrorMessage() {
        setGroupOnError( connectionURLFormGroup, false );
        clearSpanMessage( connectionURLHelp );
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
    public void setUserErrorMessage( String message ) {
        setGroupOnError( userFormGroup, true );
        setSpanMessage( userHelp, message );
    }

    @Override
    public void clearUserErrorMessage() {
        setGroupOnError( userFormGroup, false );
        clearSpanMessage( userHelp );
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
    public void setPasswordErrorMessage( String message ) {
        setGroupOnError( passwordFormGroup, true );
        setSpanMessage( passwordHelp, message );
    }

    @Override
    public void clearPasswordErrorMessage() {
        setGroupOnError( passwordFormGroup, false );
        clearSpanMessage( passwordHelp );
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

    @Override
    public void setDriverErrorMessage( final String message ) {
        setGroupOnError( driverFormGroup, true );
        setSpanMessage( driverSelectorHelp, message );
    }

    @Override
    public void clearDriverErrorMessage() {
        setGroupOnError( driverFormGroup, false );
        clearSpanMessage( driverSelectorHelp );
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

    @EventHandler( "name" )
    public void onNameChange( final ChangeEvent event ) {
        presenter.onNameChange();
    }

    @EventHandler( "jndi" )
    public void onJndiChange( final ChangeEvent event ) {
        presenter.onJndiChange();
    }

    @EventHandler( "connection-url")
    public void onConnectionURLChange( final ChangeEvent event ) {
        presenter.onConnectionURLChange();
    }

    @EventHandler( "user" )
    public void onUserChange( final ChangeEvent event ) {
        presenter.onUserChange();
    }

    @EventHandler( "password" )
    public void onPasswordChange( final ChangeEvent event ) {
        presenter.onPasswordChange();
    }

    @EventHandler( "driver-selector" )
    public void onDriverChange( final ChangeEvent event ) {
        presenter.onDriverChange();
    }

    @EventHandler( "test-connection-button" )
    public void onTestConnection( final ClickEvent event ) {
        presenter.onTestConnection();
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

    private void setGroupOnError( final Element formGroup, final boolean onError ) {
        StyleHelper.addUniqueEnumStyleName( formGroup, ValidationState.class,
                onError ? ValidationState.ERROR : ValidationState.NONE );
    }

    private void setSpanMessage( final Element span, final String text ) {
        span.getStyle().setVisibility( Style.Visibility.VISIBLE );
        span.setInnerHTML( text );
    }

    private void clearSpanMessage( final Element span ) {
        span.getStyle().setVisibility( Style.Visibility.HIDDEN );
        span.setInnerHTML( "" );
    }
}