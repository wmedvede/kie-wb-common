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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.screens.datasource.management.type.DataSourceDefType;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.ext.editor.commons.client.BaseEditor;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.ext.editor.commons.client.menu.MenuItems.*;

@Dependent
@WorkbenchEditor( identifier = "DataSourceDefEditor",
        supportedTypes = { DataSourceDefType.class } )
public class DataSourceDefEditor
        extends BaseEditor
        implements DataSourceDefEditorView.Presenter {

    DataSourceDefEditorView view;

    DataSourceDefType type;

    @Inject
    public DataSourceDefEditor( final DataSourceDefEditorView view,
            final DataSourceDefType type ) {
        super( view );
        this.view = view;
        this.type = type;
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
        //TODO use the proper content hash
        return super.mayClose( -1 );
    }

    @Override
    protected void loadContent() {

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
    protected void onSave() {
        Window.alert( "Save DataSource" );
    }
}
