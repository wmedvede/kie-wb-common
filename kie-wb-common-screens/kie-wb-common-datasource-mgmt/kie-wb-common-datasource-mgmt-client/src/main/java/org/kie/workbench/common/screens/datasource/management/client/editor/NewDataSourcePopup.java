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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class NewDataSourcePopup
        implements NewDataSourcePopupView.Presenter {

    private NewDataSourcePopupView view;

    private NewDataSourcePopupView.NewDataSourcePopupHandler handler;

    @Inject
    public NewDataSourcePopup( NewDataSourcePopupView view ) {
        this.view = view;
        view.init( this );
    }

    @Override
    public String getName() {
        return view.getName();
    }

    @Override
    public void setName( String name ) {
        view.setName( name );
    }

    @Override
    public void show() {
        view.show();
    }

    @Override
    public void hide() {
        view.hide();
    }

    @Override
    public void onOk() {
        if ( handler != null) {
            handler.onOk();
        }
    }

    @Override
    public void onCancel() {
        if ( handler != null ) {
            handler.onCancel();
        }
    }

    @Override
    public void addPopupHandler( NewDataSourcePopupView.NewDataSourcePopupHandler handler ) {
        this.handler = handler;
    }

    public void clear() {
        view.setName( null );
    }
}
