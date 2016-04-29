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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@ApplicationScoped
@WorkbenchScreen( identifier = "DataSourceDefExplorer" )
public class DataSourceDefExplorerScreen {

    private DataSourceDefExplorer explorer;

    private NewDataSourcePopup newDataSourcePopup;

    private Caller<DataSourceDefEditorService> editorService;

    private PlaceRequest placeRequest;

    private PlaceManager placeManager;

    private Menus menu;

    private Path globalDataSourcesContext;

    @Inject
    public DataSourceDefExplorerScreen( DataSourceDefExplorer explorer,
            NewDataSourcePopup newDataSourcePopup,
            Caller<DataSourceDefEditorService> editorService,
            PlaceManager placeManager ) {
        this.explorer = explorer;
        this.newDataSourcePopup = newDataSourcePopup;
        this.editorService = editorService;
        this.placeManager = placeManager;
    }

    @PostConstruct
    private void init() {
        newDataSourcePopup.addPopupHandler( new NewDataSourcePopupPresenter.NewDataSourcePopupHandler() {
            @Override
            public void onOk() {
                onCreateDataSource();
            }

            @Override
            public void onCancel() {
                newDataSourcePopup.hide();
            }
        } );
    }

    @OnStartup
    public void onStartup( PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
        this.menu = makeMenuBar();

        editorService.call( new RemoteCallback<Path>() {
            @Override
            public void callback( Path path ) {
                globalDataSourcesContext = path;
            }
        }, new DefaultErrorCallback() ).getGlobalDataSourcesContext();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "DataSource Explorer";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return explorer.asWidget();
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return menu;
    }

    private Menus makeMenuBar() {
        return MenuFactory
                .newTopLevelMenu( "Load data sources" )
                .respondsWith( getLoadCommand() )
                .endMenu()
                .newTopLevelMenu( "New" )
                .respondsWith( getNewCommand() )
                .endMenu()
                .build();
    }

    private Command getLoadCommand() {
        return new Command() {
            @Override
            public void execute() {
                explorer.loadDataSources();
            }
        };
    }

    private Command getNewCommand() {
        return new Command() {
            @Override
            public void execute() {
                onNewDataSource();
            }
        };
    }

    public void onNewDataSource() {
        newDataSourcePopup.clear();
        newDataSourcePopup.show();
    }

    public void onCreateDataSource() {
        String name = newDataSourcePopup.getName();
        if ( globalDataSourcesContext != null ) {
            BusyPopup.close();
            editorService.call( getCreateDataSourceSucessCallback(),
                    new DefaultErrorCallback( ) {
                        @Override
                        public boolean error( Message message, Throwable throwable ) {
                            BusyPopup.close();
                            return super.error( message, throwable );
                        }
                    }  ).create( globalDataSourcesContext,
                    name,
                    name + ".datasource" );
        }

    }

    private RemoteCallback<Path> getCreateDataSourceSucessCallback() {
        return new RemoteCallback<Path>() {
            @Override
            public void callback( Path path ) {
                BusyPopup.close();
                placeManager.goTo( new PathPlaceRequest( path ) );
                newDataSourcePopup.hide();
                getLoadCommand().execute();
            }
        };
    }

}