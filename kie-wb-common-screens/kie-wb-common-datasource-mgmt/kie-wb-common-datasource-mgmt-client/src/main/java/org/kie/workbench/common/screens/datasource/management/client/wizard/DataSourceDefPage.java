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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefEditorHelper;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefMainPanel;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefMainPanelView;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

@Dependent
public class DataSourceDefPage
        implements WizardPage,
                    DataSourceDefPageView.Presenter {

    private DataSourceDefPageView view;

    private DataSourceDefMainPanel mainPanel;

    private DataSourceDefEditorHelper editorHelper;

    private Event<WizardPageStatusChangeEvent> statusChangeEvent;

    @Inject
    public DataSourceDefPage( final DataSourceDefPageView view,
            final DataSourceDefMainPanel mainPanel,
            final DataSourceDefEditorHelper editorHelper,
            final  Event<WizardPageStatusChangeEvent> statusChangeEvent ) {
        this.view = view;
        this.mainPanel = mainPanel;
        this.editorHelper = editorHelper;
        this.statusChangeEvent = statusChangeEvent;
        view.init( this );
        editorHelper.init( mainPanel );

        editorHelper.setHandler( new DataSourceDefMainPanelView.Handler() {
            @Override
            public void onNameChange() {
                DataSourceDefPage.this.notifyChange();
            }

            @Override
            public void onJndiChange() {
                DataSourceDefPage.this.notifyChange();
            }

            @Override
            public void onConnectionURLChange() {
                DataSourceDefPage.this.notifyChange();
            }

            @Override
            public void onUserChange() {
                DataSourceDefPage.this.notifyChange();
            }

            @Override
            public void onPasswordChange() {
                DataSourceDefPage.this.notifyChange();
            }

            @Override
            public void onDriverChange() {
                DataSourceDefPage.this.notifyChange();
            }

        } );
    }

    @PostConstruct
    private void init() {
        view.setMainPanel( mainPanel );
    }

    public void setDataSourceDef( DataSourceDef dataSourceDef ) {
        editorHelper.setDataSourceDef( dataSourceDef );
    }

    @Override
    public String getTitle() {
        return "Data source info";
    }

    @Override
    public void isComplete( Callback<Boolean> callback ) {
        boolean complete = editorHelper.isDriverValid() &&
                editorHelper.isJndiValid() &&
                editorHelper.isConnectionURLValid() &&
                editorHelper.isUserValid() &&
                editorHelper.isPasswordValid() &&
                editorHelper.isDriverValid();

        callback.callback( complete );
    }

    @Override
    public void initialise() {

    }

    @Override
    public void prepareView() {

    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void loadDrivers( final List<DriverDefInfo> driverDefs ) {
        editorHelper.loadDrivers( driverDefs );
    }

    public void notifyChange() {
        final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( this );
        statusChangeEvent.fire( event );
    }

    public void clear() {
        mainPanel.clear();
    }
}
