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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.kie.workbench.common.screens.datasource.management.model.TestConnectionResult;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.uberfire.commons.data.Pair;

@Dependent
public class DataSourceDefEditorHelper {

    private TranslationService translationService;

    private Caller<DataSourceDefEditorService> editorService;

    private DataSourceDef dataSourceDef;

    private DataSourceDefMainPanel mainPanel;

    private Map<String, DriverDefInfo> driverDefMap = new HashMap<>(  );

    private DataSourceDefMainPanelView.Handler handler;

    private Project project;

    private boolean nameValid = false;
    private boolean jndiValid = false;
    private boolean connectionURLValid = false;
    private boolean userValid = false;
    private boolean passwordValid = false;
    private boolean driverValid = false;

    @Inject
    public DataSourceDefEditorHelper( final TranslationService translationService,
            final Caller<DataSourceDefEditorService> editorService ) {
        this.translationService = translationService;
        this.editorService = editorService;
    }

    public void init( final DataSourceDefMainPanel mainPanel ) {
        this.mainPanel = mainPanel;

        mainPanel.setHandler( new DataSourceDefMainPanelView.Handler() {
            @Override
            public void onNameChange() {
                DataSourceDefEditorHelper.this.onNameChange();
            }

            @Override
            public void onJndiChange() {
                DataSourceDefEditorHelper.this.onJndiChange();
            }

            @Override
            public void onConnectionURLChange() {
                DataSourceDefEditorHelper.this.onConnectionURLChange();
            }

            @Override
            public void onUserChange() {
                DataSourceDefEditorHelper.this.onUserChange();
            }

            @Override
            public void onPasswordChange() {
                DataSourceDefEditorHelper.this.onPasswordChange();
            }

            @Override
            public void onDriverChange() {
                DataSourceDefEditorHelper.this.onDriverChange();
            }

            @Override
            public void onTestConnection() {
                DataSourceDefEditorHelper.this.onTestConnection();
            }
        } );
    }

    public void setDataSourceDef( final DataSourceDef dataSourceDef ) {
        this.dataSourceDef = dataSourceDef;
        mainPanel.clear();
        mainPanel.setName( dataSourceDef.getName() );
        mainPanel.setJndi( dataSourceDef.getJndi() );
        mainPanel.setConnectionURL( dataSourceDef.getConnectionURL() );
        mainPanel.setUser( dataSourceDef.getUser() );
        mainPanel.setPassword( dataSourceDef.getPassword() );
        mainPanel.setDriver( dataSourceDef.getDriverUuid() );
    }

    public void setProject( Project project ) {
        this.project = project;
    }

    public void setHandler( final DataSourceDefMainPanelView.Handler handler ) {
        this.handler = handler;
    }

    public void loadDrivers( final List<DriverDefInfo> driverDefs ) {
        List<Pair<String, String>> driverOptions = buildDriverOptions( driverDefs );
        mainPanel.loadDriverOptions( driverOptions, true );
    }

    private List<Pair<String, String>> buildDriverOptions( final List<DriverDefInfo> driverDefs ) {
        List<Pair<String, String>> options = new ArrayList<>(  );
        driverDefMap.clear();
        for ( DriverDefInfo driverDef : driverDefs ) {
            options.add( new Pair<String, String>( driverDef.getName(), driverDef.getUuid() ) );
            driverDefMap.put( driverDef.getUuid(), driverDef );
        }
        return options;
    }

    public void onNameChange() {
        dataSourceDef.setName( mainPanel.getName().trim() );
        nameValid = validateName( dataSourceDef.getName() );
        if ( !nameValid ) {
            mainPanel.setNameErrorMessage(
                    getMessage( DataSourceManagementConstants.DataSourceDefEditor_InvalidNameMessage ) );
        } else {
            mainPanel.clearNameErrorMessage();
        }
        if ( handler != null ) {
            handler.onNameChange();
        }
    }

    public void onJndiChange() {
        dataSourceDef.setJndi( mainPanel.getJndi().trim() );
        jndiValid = validateJndiName( dataSourceDef.getJndi() );
        if ( !jndiValid ) {
            mainPanel.setJndiErrorMessage(
                    getMessage( DataSourceManagementConstants.DataSourceDefEditor_InvalidJndiMessage ) );
        } else {
            mainPanel.clearJndiErrorMessage();
        }
        if ( handler != null ) {
            handler.onJndiChange();
        }
    }

    public void onConnectionURLChange() {
        dataSourceDef.setConnectionURL( mainPanel.getConnectionURL().trim() );
        connectionURLValid = validateConnectionURL( dataSourceDef.getConnectionURL() );
        if ( !connectionURLValid ) {
            mainPanel.setConnectionURLErrorMessage(
                    getMessage( DataSourceManagementConstants.DataSourceDefEditor_InvalidConnectionURLMessage ) );
        } else {
            mainPanel.clearConnectionURLErrorMessage();
        }
        if ( handler != null ) {
            handler.onConnectionURLChange();
        }
    }

    public void onUserChange() {
        dataSourceDef.setUser( mainPanel.getUser().trim() );
        userValid = validateUser( dataSourceDef.getUser() );
        if ( !userValid ) {
            mainPanel.setUserErrorMessage(
                    getMessage( DataSourceManagementConstants.DataSourceDefEditor_InvalidUserMessage ) );
        } else {
            mainPanel.clearUserErrorMessage();
        }
        if ( handler != null ) {
            handler.onUserChange();
        }
    }

    public void onPasswordChange() {
        dataSourceDef.setPassword( mainPanel.getPassword().trim() );
        passwordValid = validatePassword( dataSourceDef.getPassword() );
        if ( !passwordValid ) {
            mainPanel.setPasswordErrorMessage(
                    getMessage( DataSourceManagementConstants.DataSourceDefEditor_InvalidPasswordMessage ) );
        } else {
            mainPanel.clearPasswordErrorMessage();
        }
        if ( handler != null ) {
            handler.onPasswordChange();
        }
    }

    public void onDriverChange() {
        DriverDefInfo driverDef = driverDefMap.get( mainPanel.getDriver() );
        driverValid = driverDef != null;
        if ( !driverValid ) {
            mainPanel.setDriverErrorMessage(
                    getMessage( DataSourceManagementConstants.DataSourceDefEditor_DriverRequiredMessage ) );
            dataSourceDef.setDriverUuid( null );
        } else {
            mainPanel.clearDriverErrorMessage();
            dataSourceDef.setDriverUuid( driverDef.getUuid() );
        }
        if ( handler != null ) {
            handler.onDriverChange();
        }
    }

    public void onTestConnection() {
        editorService.call(
                getTestConnectionSuccessCallback(),
                getTestConnectionErrorCallback() ).testConnection( dataSourceDef, project );
    }


    private RemoteCallback<TestConnectionResult> getTestConnectionSuccessCallback() {
        return new RemoteCallback<TestConnectionResult>() {
            @Override
            public void callback( TestConnectionResult response ) {
                onTestConnectionSuccess( response );
            }
        };
    }

    public void onTestConnectionSuccess( TestConnectionResult response ) {
        Window.alert( "Connection test " + ( response.isTestPassed() ? "Successful" : "Failed" ) + "\n" + response.getMessage() );
    }

    private ErrorCallback<?> getTestConnectionErrorCallback() {
        return new ErrorCallback<Object>() {
            @Override
            public boolean error( Object message, Throwable throwable ) {
                onTestConnectionError( message, throwable );
                return false;
            }
        };
    }

    public void onTestConnectionError( Object message, Throwable throwable ) {
        Window.alert( "An error was produced during connection testing." );
    }

    public void setValid( boolean valid ) {
        this.nameValid = valid;
        this.jndiValid = valid;
        this.connectionURLValid = valid;
        this.userValid = valid;
        this.passwordValid = valid;
        this.driverValid = valid;
    }

    public boolean isDriverValid() {
        return driverValid;
    }

    public boolean isNameValid() {
        return nameValid;
    }

    public boolean validateName( String name ) {
        return !isEmpty( name );
    }

    public boolean isJndiValid() {
        return jndiValid;
    }

    public boolean validateJndiName( String jndiName ) {
        return !isEmpty( jndiName );
    }

    public boolean isConnectionURLValid() {
        return connectionURLValid;
    }

    public boolean validateConnectionURL( String connectionURL ) {
        return !isEmpty( connectionURL );
    }

    public boolean isUserValid() {
        return userValid;
    }

    public boolean validateUser( String user ) {
        return !isEmpty( user );
    }

    public boolean isPasswordValid() {
        return passwordValid;
    }

    public boolean validatePassword( String password ) {
        return !isEmpty( password );
    }

    public boolean isEmpty( String value ) {
        return value == null || value.trim().isEmpty();
    }

    public String getMessage( String messageKey ) {
        return translationService.getTranslation( messageKey );
    }

    public String getMessage( String messageKey, Object... args ) {
        return translationService.format( messageKey, args );
    }
}