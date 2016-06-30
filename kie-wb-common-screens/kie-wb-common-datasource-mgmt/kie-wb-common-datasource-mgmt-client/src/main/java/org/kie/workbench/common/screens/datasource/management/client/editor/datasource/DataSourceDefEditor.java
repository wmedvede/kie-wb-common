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

package org.kie.workbench.common.screens.datasource.management.client.editor.datasource;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.kie.workbench.common.screens.datasource.management.client.type.DataSourceDefType;
import org.kie.workbench.common.screens.datasource.management.client.util.PopupsUtil;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
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
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.ext.editor.commons.client.menu.MenuItems.*;

@Dependent
@WorkbenchEditor( identifier = "DataSourceDefEditor",
        supportedTypes = { DataSourceDefType.class } )
public class DataSourceDefEditor
        extends BaseEditor
        implements DataSourceDefEditorView.Presenter {

    private DataSourceDefEditorView view;

    private DataSourceDefMainPanel mainPanel;

    private DataSourceDefEditorHelper editorHelper;

    private PopupsUtil popupsUtil;

    private DataSourceDefType type;

    private Caller<DataSourceDefEditorService> editorService;

    private Caller<DataSourceManagementService> dataSourceService;

    private DataSourceDefEditorContent editorContent;

    @Inject
    public DataSourceDefEditor( final DataSourceDefEditorView view,
            final DataSourceDefMainPanel mainPanel,
            final DataSourceDefEditorHelper editorHelper,
            final PopupsUtil popupsUtil,
            final DataSourceDefType type,
            final Caller<DataSourceDefEditorService> editorService,
            final Caller<DataSourceManagementService> dataSourceService ) {
        super( view );
        this.view = view;
        this.mainPanel = mainPanel;
        this.editorHelper = editorHelper;
        this.popupsUtil = popupsUtil;
        this.type = type;
        this.editorService = editorService;
        this.dataSourceService = dataSourceService;
        view.init( this );
        view.setMainPanel( mainPanel );
        editorHelper.init( mainPanel );
    }

    @OnStartup
    public void onStartup( final ObservablePath path, final PlaceRequest place ) {
        init( path,
                place,
                type,
                true,
                false,
                SAVE,
                DELETE,
                VALIDATE );
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return super.getTitleText();
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

    @Override
    protected Command onValidate() {
        return new Command() {
            @Override
            public void execute() {
                Window.alert( "Validate DataSource, not yet implemented.");
            }
        };
    }

    @Override
    protected void save() {
        if ( !editorHelper.isNameValid() ||
                !editorHelper.isJndiValid() ||
                !editorHelper.isConnectionURLValid() ||
                !editorHelper.isUserValid() ||
                !editorHelper.isPasswordValid() ||
                !editorHelper.isDriverValid() ) {
            popupsUtil.showInformationPopup( editorHelper.getMessage(
                    DataSourceManagementConstants.DataSourceDefEditor_AllFieldsRequiresValidation ) );

        } else {

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
    }

    @Override
    protected Caller<? extends SupportsDelete> getDeleteServiceCaller() {
        return editorService;
    }

    @Override
    public boolean mayClose( Integer currentHash ) {
        return super.mayClose( currentHash );
    }

    @Override
    protected void makeMenuBar() {
        super.makeMenuBar();
        addDevelopMenu();
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

    protected void onContentLoaded( final DataSourceDefEditorContent editorContent ) {
        //Path is set to null when the Editor is closed (which can happen before async calls complete).
        if ( versionRecordManager.getCurrentPath() == null ) {
            return;
        }
        setContent( editorContent );
        setOriginalHash( editorContent.hashCode() );
        editorHelper.loadDrivers( getLoadDriversSuccessCommand(), getLoadDriversErrorCommand() );
    }

    protected DataSourceDefEditorContent getContent() {
        return editorContent;
    }

    protected void setContent( final DataSourceDefEditorContent editorContent ) {
        this.editorContent = editorContent;
        editorHelper.setDataSourceDef( editorContent.getDataSourceDef() );
        editorHelper.setProject( editorContent.getProject() );
        editorHelper.setValid( true );
    }

    public Command getLoadDriversSuccessCommand() {
        return new Command() {
            @Override public void execute() {
                mainPanel.setDriver( getContent().getDataSourceDef().getDriverUuid() );
            }
        };
    }

    public ParameterizedCommand<Throwable> getLoadDriversErrorCommand() {
        return new ParameterizedCommand<Throwable>() {
            @Override
            public void execute( Throwable parameter ) {
                popupsUtil.showErrorPopup( editorHelper.getMessage(
                        DataSourceManagementConstants.DataSourceDefEditor_LoadDriversErrorMessage,
                        parameter.getMessage() ) );
            }
        };
    }

    private void addDevelopMenu() {
        //for development purposes menu entries, will be removed.
        menuBuilder.addNewTopLevelMenu( MenuFactory.newTopLevelMenu( "Check-Status" )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        onCheckDeploymentStatus();
                    }
                } )
                .endMenu()
                .build().getItems().get( 0 ) );

        menuBuilder.addNewTopLevelMenu( MenuFactory.newTopLevelMenu( "Test-Deploy" )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        onDeployDataSource();
                    }
                } )
                .endMenu()
                .build().getItems().get( 0 ) );

        menuBuilder.addNewTopLevelMenu( MenuFactory.newTopLevelMenu( "Test-UnDeploy" )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        onUnDeployDataSource();
                    }
                } )
                .endMenu()
                .build().getItems().get( 0 ) );

        menuBuilder.addNewTopLevelMenu( MenuFactory.newTopLevelMenu( "Test-DS" )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        onTestDataSource();
                    }
                } )
                .endMenu()
                .build().getItems().get( 0 ) );
    }

    private void onCheckDeploymentStatus() {
        //Experimental method for development purposes.
        dataSourceService.call(
                new RemoteCallback<DataSourceDeploymentInfo>() {
                    @Override
                    public void callback( DataSourceDeploymentInfo deploymentInfo ) {
                        if ( deploymentInfo != null ) {
                            popupsUtil.showInformationPopup( "datasource is deployed as: " + deploymentInfo.getDeploymentId() );
                        } else {
                            popupsUtil.showInformationPopup( "datasource is not deployed" );
                        }
                    }
                }, new DefaultErrorCallback() ).getDeploymentInfo( getContent().getDataSourceDef().getUuid() );
    }

    private void onDeployDataSource() {
        //Experimental method for development purposes.
        dataSourceService.call(
                new RemoteCallback<DataSourceDeploymentInfo>() {
                    @Override
                    public void callback( DataSourceDeploymentInfo deploymentInfo ) {
                        popupsUtil.showInformationPopup( "datasource successfully deployed: " + deploymentInfo.getDeploymentId() );
                    }
                }, new DefaultErrorCallback() ).deploy( getContent().getDataSourceDef() );
    }

    private void onUnDeployDataSource() {
        //Experimental method for development purposes.
        dataSourceService.call( new RemoteCallback<DataSourceDeploymentInfo>() {
            @Override
            public void callback( DataSourceDeploymentInfo deploymentInfo ) {
                if ( deploymentInfo == null ) {
                    popupsUtil.showInformationPopup( "datasource is not deployed in current server" );
                } else {
                    dataSourceService.call( new RemoteCallback<Void>() {
                        @Override
                        public void callback( Void aVoid ) {
                            popupsUtil.showInformationPopup( "datasource was successfully un-deployed" );
                        }
                    } ).undeploy( deploymentInfo );
                }

            }
        }, new DefaultErrorCallback() ).getDeploymentInfo( getContent().getDataSourceDef().getUuid() );

    }

    private void onTestDataSource() {
        //Experimental method for development purposes.
        editorService.call(
                new RemoteCallback<String>() {
                    @Override
                    public void callback( String result ) {
                        popupsUtil.showInformationPopup( new SafeHtmlBuilder().appendEscapedLines( result ).toSafeHtml().asString() );
                    }
                }, new DefaultErrorCallback() ).test( getContent().getDataSourceDef().getJndi() );
    }
}