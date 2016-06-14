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

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

@Dependent
public class DriverDefEditorHelper {

    private TranslationService translationService;

    private DriverDef driverDef;

    private DriverDefMainPanel mainPanel;

    private DriverDefMainPanelView.Handler handler;

    private boolean nameValid;

    private boolean driverClassValid;

    @Inject
    public DriverDefEditorHelper( TranslationService translationService ) {
        this.translationService = translationService;
    }

    public void init( DriverDefMainPanel mainPanel ) {
        this.mainPanel = mainPanel;

        mainPanel.setHandler( new DriverDefMainPanelView.Handler() {
            @Override
            public void onNameChange() {
                DriverDefEditorHelper.this.onNameChange();
            }

            @Override
            public void onDriverClassChange() {
                DriverDefEditorHelper.this.onDriverClassChange();
            }
        } );
    }

    public void setHandler( DriverDefMainPanelView.Handler handler ) {
        this.handler = handler;
    }

    private void onNameChange() {
        driverDef.setName( mainPanel.getName().trim() );
        nameValid = validateName( driverDef.getName() );
        if ( !nameValid ) {
            //TODO
            //SET error message
        } else {
            //TODO
            //CLEAR error message
        }
        if ( handler != null ) {
            handler.onNameChange();
        }
    }

    private void onDriverClassChange() {
        driverDef.setDriverClass( mainPanel.getDriverClass().trim() );
        driverClassValid = validateClassName( driverDef.getDriverClass() );
        if ( !driverClassValid ) {
            //TODO
            //SET error message
        } else {
            //TODO
            //CLEAR error message
        }
        if ( handler != null ) {
            handler.onDriverClassChange();
        }
    }

    public boolean validateClassName( String driverClass ) {
        return !isEmpty( driverClass );
    }

    public boolean validateName( String name ) {
        return !isEmpty( name );
    }

    public boolean isEmpty( String value ) {
        return value == null || value.trim().isEmpty();
    }

    public void setDriverDef( DriverDef driverDef ) {
        this.driverDef = driverDef;
        mainPanel.setName( driverDef.getName() );
        mainPanel.setDriverClass( driverDef.getDriverClass() );
    }
}
