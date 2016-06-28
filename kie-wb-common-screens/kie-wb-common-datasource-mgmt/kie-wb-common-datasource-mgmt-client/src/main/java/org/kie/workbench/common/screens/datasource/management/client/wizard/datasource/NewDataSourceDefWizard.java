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

package org.kie.workbench.common.screens.datasource.management.client.wizard.datasource;

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
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.core.client.wizards.AbstractWizard;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class NewDataSourceDefWizard
        extends AbstractWizard {

    private final List<WizardPage> pages = new ArrayList<>(  );

    private DataSourceDefPage dataSourceDefPage;

    private DataSourceDef dataSourceDef;

    private Caller<DataSourceDefEditorService> dataSourceDefService;

    private final Caller<DataSourceDefQueryService> driverDefService;

    private Event<NotificationEvent> notification;

    private Project project;

    @Inject
    public NewDataSourceDefWizard( final DataSourceDefPage dataSourceDefPage,
            final Caller<DataSourceDefEditorService> dataSourceDefService,
            final Caller<DataSourceDefQueryService> driverDefService,
            final Event<NotificationEvent> notification ) {
        this.dataSourceDefPage = dataSourceDefPage;
        this.dataSourceDefService = dataSourceDefService;
        this.driverDefService = driverDefService;
        this.notification = notification;
    }

    @PostConstruct
    public void init() {
        pages.add( dataSourceDefPage );
    }

    @Override
    public void start() {
        dataSourceDefPage.clear();
        dataSourceDefPage.setComplete( false );
        dataSourceDef = new DataSourceDef();
        dataSourceDefPage.setDataSourceDef( dataSourceDef );
        dataSourceDefPage.setProject( project );

        if ( isGlobal() ) {
            driverDefService.call(
                    getLoadDriversSuccessCallback(),
                    getLoadDriversErrorCallback() ).findGlobalDrivers();
        } else {
            driverDefService.call(
                    getLoadDriversSuccessCallback(),
                    getLoadDriversErrorCallback() ).findProjectDrivers( project.getRootPath() );
        }
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
        return "New data source";
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
        dataSourceDefPage.isComplete( callback );
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
        if ( isGlobal() ) {
            dataSourceDefService.call( getCreateSuccessCallback(), getCreateErrorCallback() ).createGlobal(
                    dataSourceDef );
        } else {
            dataSourceDefService.call( getCreateSuccessCallback(), getCreateErrorCallback() ).create(
                    dataSourceDef, project );
        }
    }

    private RemoteCallback<Path> getCreateSuccessCallback() {
        return new RemoteCallback<Path>() {
            @Override
            public void callback( Path path ) {
                notification.fire( new NotificationEvent(
                        "Data source : " + path.toString() + " was successfully created." ) );
                NewDataSourceDefWizard.super.complete();
            }
        };
    }

    private ErrorCallback<?> getCreateErrorCallback() {
        return new DefaultErrorCallback() {
            @Override
            public boolean error( Message message, Throwable throwable ) {
                Window.alert( "Data source was not created due to the following error: " +
                        buildOnCreateErrorMessage( throwable )  );
                return false;
            }
        };
    }

    private RemoteCallback<List<DriverDefInfo>> getLoadDriversSuccessCallback() {
        return new RemoteCallback<List<DriverDefInfo>>() {
            @Override
            public void callback( List<DriverDefInfo> response ) {
                dataSourceDefPage.loadDrivers( response );
                NewDataSourceDefWizard.super.start();
            }
        };
    }

    private ErrorCallback<?> getLoadDriversErrorCallback() {
        return new ErrorCallback<Object>() {
            @Override
            public boolean error( Object o, Throwable throwable ) {
                Window.alert( "Wizard initialization failed, it was not possible to load driver definitions. "
                        + throwable.getMessage() );
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