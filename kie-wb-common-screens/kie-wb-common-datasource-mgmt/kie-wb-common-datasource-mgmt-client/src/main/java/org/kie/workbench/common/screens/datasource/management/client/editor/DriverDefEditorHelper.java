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

    private boolean nameValid = false;

    private boolean driverClassValid = false;

    private boolean groupIdValid = false;

    private boolean artifactIdValid = false;

    private boolean versionValid = false;

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

            @Override
            public void onGroupIdChange() {
                DriverDefEditorHelper.this.onGroupIdChange();
            }

            @Override
            public void onArtifactIdChange() {
                DriverDefEditorHelper.this.onArtifactIdChange();
            }

            @Override
            public void onVersionChange() {
                DriverDefEditorHelper.this.onVersionIdChange();
            }
        } );
    }


    public void setHandler( DriverDefMainPanelView.Handler handler ) {
        this.handler = handler;
    }

    private void onNameChange() {
        driverDef.setName( mainPanel.getName().trim() );
        nameValid = validateName( driverDef.getName() );
        if ( handler != null ) {
            handler.onNameChange();
        }
    }

    private void onDriverClassChange() {
        driverDef.setDriverClass( mainPanel.getDriverClass().trim() );
        driverClassValid = validateClassName( driverDef.getDriverClass() );
        if ( handler != null ) {
            handler.onDriverClassChange();
        }
    }

    private void onGroupIdChange() {
        driverDef.setGroupId( mainPanel.getGroupId().trim() );
        groupIdValid = validateGroupId( driverDef.getGroupId() );
        if ( handler != null ) {
            handler.onGroupIdChange();
        }
    }


    private void onArtifactIdChange() {
        driverDef.setArtifactId( mainPanel.getArtifactId().trim() );
        artifactIdValid = validateArtifactId( driverDef.getArtifactId() );
        if ( handler != null ) {
            handler.onArtifactIdChange();
        }
    }

    private void onVersionIdChange() {
        driverDef.setVersion( mainPanel.getVersion().trim() );
        versionValid = validateVersion( driverDef.getVersion() );
        if ( handler != null ) {
            handler.onVersionChange();
        }
    }

    public boolean isNameValid() {
        return nameValid;
    }

    public boolean isDriverClassValid() {
        return driverClassValid;
    }

    public boolean isGroupIdValid() {
        return groupIdValid;
    }

    public boolean isArtifactIdValid() {
        return artifactIdValid;
    }

    public boolean isVersionValid() {
        return versionValid;
    }

    public void setValid( boolean valid ) {
        this.nameValid = valid;
        this.driverClassValid = valid;
        this.groupIdValid = valid;
        this.artifactIdValid = valid;
        this.versionValid = valid;
    }
    public boolean validateClassName( String driverClass ) {
        return !isEmpty( driverClass );
    }

    public boolean validateName( String name ) {
        return !isEmpty( name );
    }

    public boolean validateGroupId( String groupId ) {
        return !isEmpty( groupId );
    }

    public boolean validateArtifactId( String artifactId ) {
        return !isEmpty( artifactId );
    }

    private boolean validateVersion( String version ) {
        return !isEmpty( version );
    }

    public boolean isEmpty( String value ) {
        return value == null || value.trim().isEmpty();
    }

    public void setDriverDef( DriverDef driverDef ) {
        this.driverDef = driverDef;
        mainPanel.clear();
        mainPanel.setName( driverDef.getName() );
        mainPanel.setDriverClass( driverDef.getDriverClass() );
        mainPanel.setGroupId( driverDef.getGroupId() );
        mainPanel.setArtifactId( driverDef.getArtifactId() );
        mainPanel.setVersion( driverDef.getVersion() );
    }
}
