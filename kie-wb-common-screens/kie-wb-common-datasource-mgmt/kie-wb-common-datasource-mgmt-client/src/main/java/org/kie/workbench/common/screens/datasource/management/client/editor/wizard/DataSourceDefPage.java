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

package org.kie.workbench.common.screens.datasource.management.client.editor.wizard;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefMainPanelPresenter;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;

@Dependent
public class DataSourceDefPage
        implements WizardPage,
        DataSourceDefMainPanelPresenter {

    private DataSourceDefMainPanelView view;

    @Inject
    public DataSourceDefPage( final DataSourceDefMainPanelView view ) {
        this.view = view;
        view.init( this );
    }

    @Override
    public String getTitle() {
        return "Data source info";
    }

    @Override
    public void isComplete( Callback<Boolean> callback ) {
        callback.callback( true );
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

    @Override public void onNameChange() {

    }

    @Override public void onJndiChange() {

    }

    @Override public void onConnectionURLChange() {

    }

    @Override public void onUserChange() {

    }

    @Override public void onPasswordChange() {

    }

    @Override public void onDriverChange() {

    }

    @Override public void onDeployDataSource() {

    }

    @Override public void onUnDeployDataSource() {

    }

    @Override public void onUnTestDataSource() {

    }
}
