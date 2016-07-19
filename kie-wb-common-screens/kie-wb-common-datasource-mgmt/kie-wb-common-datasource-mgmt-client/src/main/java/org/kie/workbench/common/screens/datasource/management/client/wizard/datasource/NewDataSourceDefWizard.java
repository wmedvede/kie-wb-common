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

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.kie.workbench.common.screens.datasource.management.client.util.PopupsUtil;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefType;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.core.client.wizards.AbstractWizard;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class NewDataSourceDefWizard
        extends AbstractWizard {

    private final List<WizardPage> pages = new ArrayList<>(  );

    private DataSourceDefPage dataSourceDefPage;

    private DataSourceDef dataSourceDef;

    private Caller<DataSourceDefEditorService> dataSourceDefService;

    private TranslationService translationService;

    private PopupsUtil popupsUtil;

    private Event<NotificationEvent> notification;

    private Project project;

    @Inject
    public NewDataSourceDefWizard( final DataSourceDefPage dataSourceDefPage,
            final Caller<DataSourceDefEditorService> dataSourceDefService,
            final TranslationService translationService,
            final PopupsUtil popupsUtil,
            final Event<NotificationEvent> notification ) {
        this.dataSourceDefPage = dataSourceDefPage;
        this.dataSourceDefService = dataSourceDefService;
        this.translationService = translationService;
        this.popupsUtil = popupsUtil;
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
        dataSourceDef.setType( DataSourceDefType.CONTAINER );
        dataSourceDefPage.setDataSourceDef( dataSourceDef );
        dataSourceDefPage.setProject( project );
        dataSourceDefPage.loadDrivers( getLoadDriversSuccessCommand(), getLoadDriversFailureCommand() );
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
        return translationService.getTranslation( DataSourceManagementConstants.NewDataSourceDefWizard_title );
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
        //TODO REMOVE THIS type setting
        if ( dataSourceDef.getName().toLowerCase().contains( DataSourceDefType.CONTAINER.name().toLowerCase() ) ) {
            dataSourceDef.setType( DataSourceDefType.CONTAINER );
        } else if ( dataSourceDef.getName().toLowerCase().contains( DataSourceDefType.REFERRED.name().toLowerCase() ) ) {
            dataSourceDef.setType( DataSourceDefType.REFERRED );
        } else {
            dataSourceDef.setType( DataSourceDefType.CUSTOM );
        }

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
                        translationService.format(
                                DataSourceManagementConstants.NewDataSourceDefWizard_DataSourceCreatedMessage,
                                path.toString() ) ) );
                NewDataSourceDefWizard.super.complete();
            }
        };
    }

    private ErrorCallback<?> getCreateErrorCallback() {
        return new DefaultErrorCallback() {
            @Override
            public boolean error( Message message, Throwable throwable ) {
                popupsUtil.showErrorPopup( translationService.format(
                        DataSourceManagementConstants.NewDataSourceDefWizard_DataSourceCreateErrorMessage,
                        buildOnCreateErrorMessage( throwable ) ) );
                return false;
            }
        };
    }

    private Command getLoadDriversSuccessCommand() {
        return new Command() {
            @Override
            public void execute() {
                NewDataSourceDefWizard.super.start();
            }
        };
    }

    private ParameterizedCommand<Throwable> getLoadDriversFailureCommand() {
        return new ParameterizedCommand<Throwable>() {
            @Override
            public void execute( Throwable parameter ) {
                popupsUtil.showErrorPopup(
                        translationService.format(
                                DataSourceManagementConstants.NewDataSourceDefWizard_WizardStartErrorMessage,
                                parameter.getMessage() ) );
            }
        };
    }

    private String buildOnCreateErrorMessage( Throwable t ) {
        if ( t instanceof FileAlreadyExistsException ) {
            return translationService.format(
                    DataSourceManagementConstants.NewDataSourceDefWizard_FileExistsErrorMessage,
                    ((FileAlreadyExistsException )t).getFile() );
        } else {
            return t.getMessage();
        }
    }

    private boolean isGlobal() {
        return project == null;
    }

}