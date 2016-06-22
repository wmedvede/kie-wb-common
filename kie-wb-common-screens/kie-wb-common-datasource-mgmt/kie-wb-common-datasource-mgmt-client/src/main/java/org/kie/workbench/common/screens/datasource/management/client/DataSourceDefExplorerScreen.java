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

package org.kie.workbench.common.screens.datasource.management.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@ApplicationScoped
@WorkbenchScreen( identifier = "DataSourceDefExplorer" )
public class DataSourceDefExplorerScreen {

    private DataSourceDefExplorer explorer;

    private Menus menu;

    @Inject
    public DataSourceDefExplorerScreen( DataSourceDefExplorer explorer ) {
        this.explorer = explorer;
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
                .build();
    }

    private Command getLoadCommand() {
        return new Command() {
            @Override public void execute() {
                explorer.loadDataSources();
            }
        };
    }

}
