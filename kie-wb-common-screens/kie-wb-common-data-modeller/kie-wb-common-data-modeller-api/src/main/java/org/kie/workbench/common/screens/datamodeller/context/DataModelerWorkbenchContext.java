/*
 * Copyright 2015 JBoss Inc
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

package org.kie.workbench.common.screens.datamodeller.context;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;

@ApplicationScoped
public class DataModelerWorkbenchContext {

    private KieProject activeProject;

    private DataObject activeDataObject;

    private DataObject activeField;

    @Inject
    private Event<DataModelerWorkbenchContextChangeEvent> contextChangeEvent;

    public DataModelerWorkbenchContext() {
    }

    public void updateContext( KieProject activeProject, DataObject activeDataObject ) {
        this.activeProject = activeProject;
        this.activeDataObject = activeDataObject;

        contextChangeEvent.fire( new DataModelerWorkbenchContextChangeEvent( activeProject, activeDataObject, null ) );
    }

    public void updateContext( KieProject activeProject, DataObject activeDataObject, DataObject activeField ) {
        this.activeProject = activeProject;
        this.activeDataObject = activeDataObject;
        this.activeField = activeField;

        contextChangeEvent.fire( new DataModelerWorkbenchContextChangeEvent( activeProject, activeDataObject, activeField ) );
    }

    public void clearContext() {
        this.activeProject = null;
        this.activeProject = null;
        this.activeField = null;
    }
}
