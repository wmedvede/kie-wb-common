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

package org.kie.workbench.common.screens.datasource.management.events;

import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;

@Portable
public class NewDataSourceEvent {

    private DataSourceDef dataSourceDef;

    private Project project;

    private String sessionId;

    private String identity;

    public NewDataSourceEvent() {
    }

    public NewDataSourceEvent( final DataSourceDef dataSourceDef,
            final Project project,
            final String sessionId,
            final String identity ) {
        this.dataSourceDef = dataSourceDef;
        this.project = project;
        this.sessionId = sessionId;
        this.identity = identity;
    }

    public NewDataSourceEvent( final DataSourceDef dataSourceDef,
            final String sessionId,
            final String identity ) {
        this.dataSourceDef = dataSourceDef;
        this.sessionId = sessionId;
        this.identity = identity;
    }

    public DataSourceDef getDataSourceDef() {
        return dataSourceDef;
    }

    public Project getProject() {
        return project;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIdentity() {
        return identity;
    }

    public boolean isGlobal() {
        return project == null;
    }
}
