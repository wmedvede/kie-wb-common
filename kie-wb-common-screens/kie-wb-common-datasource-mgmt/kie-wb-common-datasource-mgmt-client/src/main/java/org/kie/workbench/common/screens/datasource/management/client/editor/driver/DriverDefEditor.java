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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.client.type.DriverDefType;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DriverManagementService;
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

    private DriverDefType type;

    private Caller<DriverDefEditorService> editorService;

    private Caller<DriverManagementService> driverService;

    private DriverDefEditorContent editorContent;

    @Inject
    public DriverDefEditor( final DriverDefEditorView view,
            final DriverDefMainPanel mainPanel,
            final DriverDefEditorHelper editorHelper,
            final DriverDefType type,
            final Caller<DriverDefEditorService> editorService,
            final Caller<DriverManagementService> driverService ) {
        super( view );
        this.view = view;
        this.mainPanel = mainPanel;
        this.editorHelper = editorHelper;
        this.type = type;
        this.editorService = editorService;
        this.driverService = driverService;
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
                validate();
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
                                new HasBusyIndicatorDefaultErrorCallback( view ) ).save( versionRecordManager.getCurrentPath(),
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

    @Override
    protected void makeMenuBar() {
        super.makeMenuBar();
        addDevelopMenu();
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
                    mainPanel.showValidationMessages( messages );
                }
            }
        };
    }

    private void addDevelopMenu() {
        //for development purposes menu entries, will be removed.
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

    private void onDeployDriver() {
        //Experimental method for development purposes.
        driverService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback( Void aVoid ) {
                        Window.alert( "driver successfully deployed" );
                    }
                }, new DefaultErrorCallback() ).deploy( getContent().getDriverDef() );
    }

    private void onUnDeployDriver() {
        //Experimental method for development purposes.
        driverService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback( Void aVoid ) {
                        Window.alert( "driver successfully un deployed" );
                    }
                }, new DefaultErrorCallback() ).undeploy( getContent().getDriverDef().getUuid() );
    }
}