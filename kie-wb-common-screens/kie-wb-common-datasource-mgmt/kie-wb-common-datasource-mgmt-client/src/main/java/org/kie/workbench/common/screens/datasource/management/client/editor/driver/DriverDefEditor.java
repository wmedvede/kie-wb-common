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

package org.kie.workbench.common.screens.datasource.management.client.editor.driver;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.kie.workbench.common.screens.datasource.management.client.type.DriverDefType;
import org.kie.workbench.common.screens.datasource.management.client.util.PopupsUtil;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DriverManagementService;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.ext.editor.commons.client.BaseEditor;
import org.uberfire.ext.editor.commons.client.file.DeletePopup;
import org.uberfire.ext.editor.commons.client.file.SaveOperationService;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.resources.i18n.CommonConstants;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.ext.editor.commons.client.menu.MenuItems.*;

@Dependent
@WorkbenchEditor( identifier = "DriverDefEditor",
        supportedTypes = { DriverDefType.class } )
public class DriverDefEditor
        extends BaseEditor
        implements DriverDefEditorView.Presenter {

    private DriverDefEditorView view;

    private DriverDefMainPanel mainPanel;

    private DriverDefEditorHelper editorHelper;

    private PopupsUtil popupsUtil;

    private DriverDefType type;

    private Caller<DriverDefEditorService> editorService;

    private Caller<DriverManagementService> driverService;

    private Caller<DataSourceManagementService> dataSourceManagement;

    private DriverDefEditorContent editorContent;

    @Inject
    public DriverDefEditor( final DriverDefEditorView view,
            final DriverDefMainPanel mainPanel,
            final DriverDefEditorHelper editorHelper,
            final PopupsUtil popupsUtil,
            final DriverDefType type,
            final Caller<DriverDefEditorService> editorService,
            final Caller<DriverManagementService> driverService,
            final Caller<DataSourceManagementService> dataSourceManagement ) {
        super( view );
        this.view = view;
        this.mainPanel = mainPanel;
        this.editorHelper = editorHelper;
        this.popupsUtil = popupsUtil;
        this.type = type;
        this.editorService = editorService;
        this.driverService = driverService;
        this.dataSourceManagement = dataSourceManagement;
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
                SAVE );
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
                validate();
            }
        };
    }

    @Override
    protected void save() {
        executeSafeUpdateCommand( DataSourceManagementConstants.DriverDefEditor_DriverHasRunningDependantsForSave,
                new Command() {
                    @Override public void execute() {
                        save( false );
                    }
                },
                new Command() {
                    @Override public void execute() {
                        save( true );
                    }
                },
                new Command() {
                    @Override public void execute() {
                        //do nothing;
                    }
                } );
    }

    protected void executeSafeUpdateCommand( String onDependantsMessageKey,
            Command defaultCommand, Command yesCommand, Command noCommand ) {
        dataSourceManagement.call( new RemoteCallback<DriverRuntimeInfo>() {
            @Override
            public void callback( DriverRuntimeInfo driverRuntimeInfo ) {

                if ( driverRuntimeInfo.hasRunningDependants() ) {
                    popupsUtil.showYesNoPopup( CommonConstants.INSTANCE.Warning(),
                            editorHelper.getMessage( onDependantsMessageKey ),
                            yesCommand,
                            CommonConstants.INSTANCE.YES(),
                            ButtonType.WARNING,
                            noCommand,
                            CommonConstants.INSTANCE.NO(),
                            ButtonType.DEFAULT );
                } else {
                    defaultCommand.execute();
                }
            }
        } ).getDriverRuntimeInfo( getContent().getDriverDef().getUuid() );
    }

    protected void save( boolean forceSave ) {
        new SaveOperationService().save( versionRecordManager.getCurrentPath(),
                new ParameterizedCommand<String>() {
                    @Override
                    public void execute( final String commitMessage ) {
                        editorService.call( getSaveSuccessCallback( getContent().hashCode() ),
                                new HasBusyIndicatorDefaultErrorCallback( view ) ).save( versionRecordManager.getCurrentPath(),
                                getContent(),
                                commitMessage,
                                forceSave );
                    }
                }
        );
        concurrentUpdateSessionInfo = null;
    }

    @Override
    public boolean mayClose( Integer currentHash ) {
        return super.mayClose( currentHash );
    }

    @Override
    protected void makeMenuBar() {
        super.makeMenuBar();
        menuBuilder.addDelete( onDelete( versionRecordManager.getCurrentPath() ) );
        menuBuilder.addValidate( onValidate() );
    }

    protected Command onDelete( ObservablePath currentPath ) {
        return new Command() {
            @Override
            public void execute() {
                executeSafeUpdateCommand( DataSourceManagementConstants.DriverDefEditor_DriverHasRunningDependantsForDelete,
                        new Command() {
                            @Override public void execute() {
                                delete( currentPath, false );
                            }
                        },
                        new Command() {
                            @Override public void execute() {
                                delete( currentPath, true );
                            }
                        },
                        new Command() {
                            @Override public void execute() {
                                //do nothing.
                            }
                        }
                );
            }
        };
    }

    private void delete( ObservablePath currentPath, boolean forceDelete ) {

        final DeletePopup popup = new DeletePopup( new ParameterizedCommand<String>() {
            @Override
            public void execute( final String comment ) {
                view.showBusyIndicator( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.Deleting() );
                editorService.call( new RemoteCallback<Void>() {
                    @Override public void callback( Void aVoid ) {
                        view.hideBusyIndicator();
                        notification.fire( new NotificationEvent( org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemDeletedSuccessfully(),
                                NotificationEvent.NotificationType.SUCCESS ) );
                    }
                } , new HasBusyIndicatorDefaultErrorCallback( view ) ).delete( currentPath, comment, forceDelete );
            }
        } );
        popup.show();
    }

    private RemoteCallback<DriverDefEditorContent> getLoadContentSuccessCallback() {
        return new RemoteCallback<DriverDefEditorContent>() {
            @Override
            public void callback( DriverDefEditorContent editorContent ) {
                view.hideBusyIndicator();
                onContentLoaded( editorContent );
            }
        };
    }

    protected void onContentLoaded( final DriverDefEditorContent editorContent ) {
        //Path is set to null when the Editor is closed (which can happen before async calls complete).
        if ( versionRecordManager.getCurrentPath() == null ) {
            return;
        }
        setContent( editorContent );
        setOriginalHash( editorContent.hashCode() );
    }

    protected DriverDefEditorContent getContent() {
        return editorContent;
    }

    protected void setContent( final DriverDefEditorContent editorContent ) {
        this.editorContent = editorContent;
        this.editorHelper.setDriverDef( editorContent.getDriverDef() );
        editorHelper.setValid( true );
    }

    private void validate() {
        editorService.call(
                getValidationSuccessCallback(), new DefaultErrorCallback() ).validate( getContent().getDriverDef() );
    }

    private RemoteCallback<List<ValidationMessage>> getValidationSuccessCallback() {
        return new RemoteCallback<List<ValidationMessage>>() {
            @Override
            public void callback( List<ValidationMessage> messages ) {

                if ( messages == null || messages.isEmpty() ) {
                    notification.fire( new NotificationEvent(
                            org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants.INSTANCE.ItemValidatedSuccessfully(),
                            NotificationEvent.NotificationType.SUCCESS ) );
                } else {
                    popupsUtil.showValidationMessages( messages );
                }
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
                        onDeployDriver();
                    }
                } )
                .endMenu()
                .build().getItems().get( 0 ) );

        menuBuilder.addNewTopLevelMenu( MenuFactory.newTopLevelMenu( "Test-UnDeploy" )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        onUnDeployDriver();
                    }
                } )
                .endMenu()
                .build().getItems().get( 0 ) );
    }

    private void onCheckDeploymentStatus() {
        //Experimental method for development purposes.
        driverService.call(
                new RemoteCallback<DriverDeploymentInfo>() {
                    @Override
                    public void callback( DriverDeploymentInfo deploymentInfo ) {
                        if ( deploymentInfo != null ) {
                            popupsUtil.showInformationPopup( "driver is deployed as: " + deploymentInfo.getDeploymentId() );
                        } else {
                            popupsUtil.showInformationPopup( "driver is not deployed" );
                        }
                    }
                }, new DefaultErrorCallback() ).getDeploymentInfo( getContent().getDriverDef().getUuid() );
    }

    private void onDeployDriver() {
        //Experimental method for development purposes.
        driverService.call(
                new RemoteCallback<DriverDeploymentInfo>() {
                    @Override
                    public void callback( DriverDeploymentInfo deploymentInfo ) {
                        popupsUtil.showInformationPopup( "driver successfully deployed: " + deploymentInfo.getDeploymentId() );
                    }
                }, new DefaultErrorCallback() ).deploy( getContent().getDriverDef() );
    }

    private void onUnDeployDriver() {
        //Experimental method for development purposes.
        driverService.call(
                new RemoteCallback<DriverDeploymentInfo>() {
                    @Override
                    public void callback( DriverDeploymentInfo deploymentInfo ) {

                        if ( deploymentInfo == null ) {
                            popupsUtil.showInformationPopup( "driver is not deployed in current server" );
                        } else {
                            driverService.call( new RemoteCallback<Void>() {
                                @Override
                                public void callback( Void aVoid ) {
                                    popupsUtil.showInformationPopup( "driver was successfully un-deployed" );
                                }
                            }, new DefaultErrorCallback() ).undeploy( deploymentInfo );
                        }
                    }
                }, new DefaultErrorCallback() ).getDeploymentInfo( getContent().getDriverDef().getUuid() );
    }
}