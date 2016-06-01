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

package org.kie.workbench.common.screens.datasource.management.client.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefMainPanel;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefMainPanelView;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

@Dependent
public class DataSourceDefPage
        implements WizardPage,
                    DataSourceDefPageView.Presenter {

    private DataSourceDefPageView view;

    private DataSourceDefMainPanel mainPanel;

    private DataSourceDef dataSourceDef;

    private Map<String, DriverDef> driverDefMap = new HashMap<>(  );

    private Event<WizardPageStatusChangeEvent> statusChangeEvent;

    private boolean nameValid = false;

    private boolean jndiValid = false;

    private boolean connectionURLValid = false;

    private boolean userValid = false;

    private boolean passwordValid = false;

    private boolean driverValid = false;

    @Inject
    public DataSourceDefPage( final DataSourceDefPageView view,
            final DataSourceDefMainPanel mainPanel,
            final  Event<WizardPageStatusChangeEvent> statusChangeEvent ) {
        this.view = view;
        this.mainPanel = mainPanel;
        this.statusChangeEvent = statusChangeEvent;
        view.init( this );
        mainPanel.setHandler( new DataSourceDefMainPanelView.Handler() {
            @Override
            public void onNameChange() {
                DataSourceDefPage.this.onNameChange();
            }

            @Override
            public void onJndiChange() {
                DataSourceDefPage.this.onJndiChange();
            }

            @Override
            public void onConnectionURLChange() {
                DataSourceDefPage.this.onConnectionURLChange();
            }

            @Override
            public void onUserChange() {
                DataSourceDefPage.this.onUserChange();
            }

            @Override
            public void onPasswordChange() {
                DataSourceDefPage.this.onPasswordChange();
            }

            @Override
            public void onDriverChange() {
                DataSourceDefPage.this.onDriverChange();
            }

        } );
    }

    @PostConstruct
    private void init() {
        view.setMainPanel( mainPanel );
    }

    public void setDataSourceDef( DataSourceDef dataSourceDef ) {
        this.dataSourceDef = dataSourceDef;
        nameValid = false;
        jndiValid = false;
        connectionURLValid = false;
        userValid = false;
        passwordValid = false;
        driverValid = false;
    }

    @Override
    public String getTitle() {
        return "Data source info";
    }

    @Override
    public void isComplete( Callback<Boolean> callback ) {
        boolean complete = nameValid && jndiValid && connectionURLValid && userValid && passwordValid && driverValid;
        callback.callback( complete );
    }

    @Override
    public void initialise() {

    }

    @Override
    public void prepareView() {

    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void onNameChange() {
        dataSourceDef.setName( mainPanel.getName().trim() );
        nameValid = isValidName( dataSourceDef.getName() );
        if ( !nameValid ) {
            mainPanel.setNameErrorMessage( "A data source name is required" );
        } else {
            mainPanel.clearNameErrorMessage();
        }
        notifyChange();
    }

    public void onJndiChange() {
        dataSourceDef.setJndi( mainPanel.getJndi().trim() );
        jndiValid = isValidJndiName( dataSourceDef.getJndi() );
        if ( !jndiValid ) {
            mainPanel.setJndiErrorMessage( "A valid jndi name is required" );
        } else {
            mainPanel.clearJndiErrorMessage();
        }
        notifyChange();
    }

    public void onConnectionURLChange() {
        dataSourceDef.setConnectionURL( mainPanel.getConnectionURL().trim() );
        connectionURLValid = isValidConnectionURL( dataSourceDef.getConnectionURL() );
        if ( !connectionURLValid ) {
            mainPanel.setConnectionURLErrorMessage( "A valid connection url is required" );
        } else {
            mainPanel.clearConnectionURLErrorMessage();
        }
        notifyChange();
    }

    public void onUserChange() {
        dataSourceDef.setUser( mainPanel.getUser().trim() );
        userValid = isValidUser( dataSourceDef.getUser() );
        if ( !userValid ) {
            mainPanel.setUserErrorMessage( "A user name is required" );
        } else {
            mainPanel.clearUserErrorMessage();
        }
        notifyChange();
    }

    public void onPasswordChange() {
        dataSourceDef.setPassword( mainPanel.getPassword().trim() );
        passwordValid = isValidPassword( dataSourceDef.getPassword() );
        if ( !passwordValid ) {
            mainPanel.setPasswordErrorMessage( "A password is required" );
        } else {
            mainPanel.clearPasswordErrorMessage();
        }
        notifyChange();
    }

    public void onDriverChange() {
        DriverDef driverDef = driverDefMap.get( mainPanel.getDriver() );
        driverValid = driverDef != null;
        if ( !driverValid ) {
            mainPanel.setDriverErrorMessage( "A driver is required" );
            dataSourceDef.setDriverUuid( null );
            dataSourceDef.setDriverClass( null );
        } else {
            mainPanel.clearDriverErrorMessage();
            dataSourceDef.setDriverUuid( driverDef.getUuid() );
            dataSourceDef.setDriverClass( driverDef.getDriverClass() );
        }
        notifyChange();
    }

    public void onDeployDataSource() {

    }

    public void onUnDeployDataSource() {

    }

    public void onUnTestDataSource() {

    }

    public void loadDrivers( final List<DriverDef> driverDefs ) {
        List<Pair<String, String>> driverOptions = buildDriverOptions( driverDefs );
        mainPanel.loadDriverOptions( driverOptions, true );
    }

    private List<Pair<String, String>> buildDriverOptions( final List<DriverDef> driverDefs ) {
        List<Pair<String, String>> options = new ArrayList<>(  );
        driverDefMap.clear();
        for ( DriverDef driverDef : driverDefs ) {
            options.add( new Pair<String, String>( driverDef.getDriverClass(), driverDef.getUuid() ) );
            driverDefMap.put( driverDef.getUuid(), driverDef );
        }
        return options;
    }

    private boolean isValidName( String name ) {
        return !isEmpty( name );
    }

    private boolean isValidJndiName( String jndiName ) {
        return !isEmpty( jndiName );
    }

    private boolean isValidConnectionURL( String connectionURL ) {
        return !isEmpty( connectionURL );
    }

    private boolean isValidUser( String user ) {
        return !isEmpty( user );
    }

    private boolean isValidPassword( String password ) {
        return !isEmpty( password );
    }

    private boolean isEmpty( String value ) {
        return value == null || value.trim().isEmpty();
    }

    public void notifyChange() {
        final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( this );
        statusChangeEvent.fire( event );
    }

    public void clear() {
        mainPanel.clear();
    }
}
