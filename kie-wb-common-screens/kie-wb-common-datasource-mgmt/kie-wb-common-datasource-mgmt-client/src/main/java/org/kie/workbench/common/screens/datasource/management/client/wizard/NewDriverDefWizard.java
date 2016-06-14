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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.core.client.wizards.AbstractWizard;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class NewDriverDefWizard
        extends AbstractWizard {

    private final List<WizardPage> pages = new ArrayList<>(  );

    private DriverDefPage driverDefPage;

    private DriverDef driverDef;

    private Caller<DriverDefEditorService> driverDefService;

    private Event<NotificationEvent> notification;

    private Project project;

    private Path driversContext;

    @Inject
    public NewDriverDefWizard( final DriverDefPage driverDefPage,
            final Caller<DriverDefEditorService> driverDefService,
            final Event<NotificationEvent> notification ) {
        this.driverDefPage = driverDefPage;
        this.driverDefService = driverDefService;
        this.notification = notification;
    }

    @PostConstruct
    public void init() {
        pages.add( driverDefPage );
    }

    @Override
    public void start() {
        driverDef = new DriverDef();
        driverDefPage.setDriverDef( driverDef );
        if ( isGlobal() ) {
            driverDefService.call( getLoadDriversContextSuccessCallback(),
                    getLoadDriversContextErrorCallback() ).getGlobalDriversContext();
        } else {
            driverDefService.call( getLoadDriversContextSuccessCallback(),
                    getLoadDriversContextErrorCallback() ).getProjectDriversContext( project );
        }

        super.start();
    }

    private ErrorCallback<?> getLoadDriversContextErrorCallback() {
        return new ErrorCallback<Object>() {
            @Override
            public boolean error( Object o, Throwable throwable ) {
                Window.alert( "Wizard initialization failed, it was not possible to load driver files context. "
                        + throwable.getMessage() );
                return false;
            }
        };
    }

    private RemoteCallback<Path> getLoadDriversContextSuccessCallback() {
        return new RemoteCallback<Path>() {
            @Override
            public void callback( Path path ) {
                NewDriverDefWizard.this.driversContext = path;
                NewDriverDefWizard.super.start();
            }
        };
    }

    @Override
    public List<WizardPage> getPages() {
        return pages;
    }

    @Override
    public Widget getPageWidget( int pageNumber ) {
        return pages.get( pageNumber ).asWidget();
    }

    @Override
    public String getTitle() {
        return "New driver";
    }

    @Override
    public int getPreferredHeight() {
        return 600;
    }

    @Override
    public int getPreferredWidth() {
        return 700;
    }

    @Override
    public void isComplete( Callback<Boolean> callback ) {
        driverDefPage.isComplete( callback );
    }

    @Override
    public void complete() {
        doComplete();
    }

    public void setProject( final Project project ) {
        this.project = project;
    }

    public void setGlobal() {
        this.project = null;
    }

    private void doComplete() {
        //TODO check where to get the file name from
        driverDefPage.setFileName( driverDef.getName() + ".driver.jar" );
        driverDefPage.setPath( driversContext );

        driverDefPage.upload( new Command() {
            @Override
            public void execute() {
                onSuccessUpload();
            }
        }, new Command() {
            @Override
            public void execute() {
                onFailedUpload();
            }
        } );
    }

    private void onFailedUpload() {
        Window.alert( "File Upload Failed" );
        super.complete();
    }

    private void onSuccessUpload() {
        //the file was properly uploaded.
        if ( isGlobal() ) {
            //TODO create the global DS
        } else {
            driverDefService.call( getCreateSuccessCallback(), getCreateErrorCallback() ).create(
                    driverDef, project, true );
        }
    }

    private RemoteCallback<Path> getCreateSuccessCallback() {
        return new RemoteCallback<Path>() {
            @Override
            public void callback( Path path ) {
                notification.fire( new NotificationEvent(
                        "Driver : " + path.toString() + " was successfully created." ) );
                NewDriverDefWizard.super.complete();
            }
        };
    }

    private ErrorCallback<?> getCreateErrorCallback() {
        return new DefaultErrorCallback() {
            @Override
            public boolean error( Message message, Throwable throwable ) {
                Window.alert( "Driver was not created due to the following error: " +
                        buildOnCreateErrorMessage( throwable ) );
                return false;
            }
        };
    }

    private String buildOnCreateErrorMessage( Throwable t ) {
        if ( t instanceof FileAlreadyExistsException ) {
            return "File already exists: " + ((FileAlreadyExistsException )t).getFile();
        } else {
            return t.getMessage();
        }
    }

    private boolean isGlobal() {
        return project == null;
    }
}
