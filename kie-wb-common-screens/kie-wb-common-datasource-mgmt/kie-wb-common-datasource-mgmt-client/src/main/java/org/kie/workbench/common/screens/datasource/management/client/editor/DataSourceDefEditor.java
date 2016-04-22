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
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.client.type.DataSourceDefType;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.service.DriverManagementService;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.editor.commons.client.BaseEditor;
import org.uberfire.ext.editor.commons.client.file.SaveOperationService;
import org.uberfire.ext.editor.commons.service.support.SupportsDelete;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.ext.editor.commons.client.menu.MenuItems.*;

@Dependent
@WorkbenchEditor( identifier = "DataSourceDefEditor",
        supportedTypes = { DataSourceDefType.class } )
public class DataSourceDefEditor
        extends BaseEditor
        implements DataSourceDefEditorPresenter {


    private DataSourceDefEditorView view;

    private DataSourceDefType type;

    private Caller<DataSourceDefEditorService> editorService;

    private Caller<DataSourceManagementService> dataSourceService;

    private Caller<DriverManagementService> driverService;

    private DataSourceDefEditorContent editorContent;

    private Map<String, DriverDef> driverDefMap = new HashMap<>(  );

    @Inject
    public DataSourceDefEditor( final DataSourceDefEditorView view,
            final DataSourceDefType type,
            final Caller<DataSourceDefEditorService> editorService,
            final Caller<DataSourceManagementService> dataSourceService,
            final Caller<DriverManagementService> driverService ) {
        super( view );
        this.view = view;
        this.type = type;
        this.editorService = editorService;
        this.dataSourceService = dataSourceService;
        this.driverService = driverService;
        view.init( this );
    }

    @OnStartup
    public void onStartup( final ObservablePath path, final PlaceRequest place ) {
        init( path,
                place,
                type,
                true,
                false,
                SAVE,
                COPY,
                RENAME,
                DELETE );

    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return "DataSourceEditor [" + "todo set title" + "]";
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return view.asWidget();
    }

    @OnMayClose
    public boolean onMayClose() {
        return super.mayClose( getContent().hashCode() );
    }

    @Override
    protected void loadContent() {
        editorService.call( getLoadContentSuccessCallback(),
                new HasBusyIndicatorDefaultErrorCallback( view ) ).loadContent(
                versionRecordManager.getCurrentPath() );

    }

    protected void loadDrivers() {
        driverService.call( getLoadDriversSuccessCallback(),
                new DefaultErrorCallback() ).getDrivers();
    }

    @Override
    protected Command onValidate() {
        return new Command() {
            @Override
            public void execute() {
                Window.alert( "Validate DataSource");
            }
        };
    }

    @Override
    protected void save() {
        new SaveOperationService().save( versionRecordManager.getCurrentPath(),
                new ParameterizedCommand<String>() {
                    @Override
                    public void execute( final String commitMessage ) {
                        editorService.call( getSaveSuccessCallback( getContent().hashCode() ),
                                new HasBusyIndicatorDefaultErrorCallback( view )
                        ).save( versionRecordManager.getCurrentPath(),
                                getContent(),
                                commitMessage );
                    }
                }
        );
        concurrentUpdateSessionInfo = null;
    }

    @Override
    protected Caller<? extends SupportsDelete> getDeleteServiceCaller() {
        return editorService;
    }

    @Override
    public boolean mayClose( Integer currentHash ) {
        return super.mayClose( currentHash );
    }

    private RemoteCallback<DataSourceDefEditorContent> getLoadContentSuccessCallback() {
        return new RemoteCallback<DataSourceDefEditorContent>() {
            @Override
            public void callback( DataSourceDefEditorContent editorContent ) {
                view.hideBusyIndicator();
                onContentLoaded( editorContent );
            }
        };
    }

    private RemoteCallback<List<DriverDef>> getLoadDriversSuccessCallback() {
        return new RemoteCallback<List<DriverDef>>() {
            @Override
            public void callback( List<DriverDef> driverDefs ) {
                onDriversLoaded( driverDefs );
            }
        };
    }

    private void onDriversLoaded( final List<DriverDef> driverDefs ) {
        List<Pair<String, String>> driverOptions = buildDriverOptions( driverDefs );
        view.loadDriverOptions( driverOptions, true );
        view.setDriver( getContent().getDataSourceDef().getDriverUuid()  );
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

    protected void onContentLoaded( final DataSourceDefEditorContent editorContent ) {
        //Path is set to null when the Editor is closed (which can happen before async calls complete).
        if ( versionRecordManager.getCurrentPath() == null ) {
            return;
        }
        setContent( editorContent );
        setOriginalHash( editorContent.hashCode() );
        refreshDeploymentInfo();
        loadDrivers();
    }

    protected DataSourceDefEditorContent getContent() {
        return editorContent;
    }

    protected void setContent( final DataSourceDefEditorContent editorContent ) {
        this.editorContent = editorContent;
        view.setName( editorContent.getDataSourceDef().getName() );
        view.setJndi( editorContent.getDataSourceDef().getJndi() );
        view.setConnectionURL( editorContent.getDataSourceDef().getConnectionURL() );
        view.setUser( editorContent.getDataSourceDef().getUser() );
        view.setPassword( editorContent.getDataSourceDef().getPassword() );
    }

    protected void refreshDeploymentInfo() {
        dataSourceService.call( getRefreshDeploymentInfoSuccessCallback(),
                new DefaultErrorCallback() ).getDeploymentInfo( getContent().getDataSourceDef().getUuid() );
    }

    private RemoteCallback<DataSourceDeploymentInfo> getRefreshDeploymentInfoSuccessCallback() {
        return new RemoteCallback<DataSourceDeploymentInfo>() {
            @Override
            public void callback( DataSourceDeploymentInfo deploymentInfo ) {
                if ( deploymentInfo != null ) {
                    view.enableDeployButton( false );
                    view.enableUnDeployButton( true );
                    view.enableTestButton( true );
                } else {
                    view.enableDeployButton( true );
                    view.enableUnDeployButton( false );
                    view.enableTestButton( false );
                }
            }
        };
    }

    @Override
    public void onNameChange() {
        getContent().getDataSourceDef().setName( view.getName() );
    }

    @Override
    public void onJndiChange() {
        getContent().getDataSourceDef().setJndi( view.getJndi() );
    }

    @Override
    public void onConnectionURLChange() {
        getContent().getDataSourceDef().setConnectionURL( view.getConnectionURL() );
    }

    @Override
    public void onUserChange() {
        getContent().getDataSourceDef().setUser( view.getUser() );
    }

    @Override
    public void onPasswordChange() {
        getContent().getDataSourceDef().setPassword( view.getPassword() );
    }

    @Override
    public void onDriverChange() {
        DriverDef driverDef = driverDefMap.get( view.getDriver() );
        if ( driverDef != null ) {
            getContent().getDataSourceDef().setDriverUuid( driverDef.getUuid() );
            getContent().getDataSourceDef().setDriverClass( driverDef.getDriverClass() );
        } else {
            getContent().getDataSourceDef().setDriverUuid( null );
            getContent().getDataSourceDef().setDriverClass( null );
        }
    }

    @Override
    public void onDeployDataSource() {
        //TODO verify all required parameters are set.
        //TODO add and verify server response.
        dataSourceService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback( Void aVoid ) {
                        Window.alert( "datasource successfully deployed" );
                        view.enableUnDeployButton( true );
                        view.enableDeployButton( false );
                        view.enableTestButton( true );
                    }
                }, new DefaultErrorCallback() ).deploy( getContent().getDataSourceDef() );
    }

    @Override
    public void onUnDeployDataSource() {
        //TODO add and verify server response, etc.
        dataSourceService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback( Void aVoid ) {
                        Window.alert( "datasource successfully un deployed" );
                        view.enableUnDeployButton( false );
                        view.enableDeployButton( true );
                        view.enableTestButton( false );
                    }
                }, new DefaultErrorCallback() ).undeploy( getContent().getDataSourceDef().getUuid() );

    }

    @Override
    public void onUnTestDataSource() {
        editorService.call(
                new RemoteCallback<String>() {
                    @Override
                    public void callback( String result ) {
                        Window.alert( result );
                    }
                }, new DefaultErrorCallback() ).test( getContent().getDataSourceDef().getJndi() );
    }
}