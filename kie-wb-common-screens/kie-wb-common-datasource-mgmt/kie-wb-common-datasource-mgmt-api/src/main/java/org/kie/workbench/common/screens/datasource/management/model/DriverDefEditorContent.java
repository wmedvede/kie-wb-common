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

package org.kie.workbench.common.screens.datasource.management.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DriverDefEditorContent {

    private DriverDef driverDef;

    public DriverDefEditorContent() {
    }

    public DriverDef getDriverDef() {
        return driverDef;
    }

    public void setDriverDef( DriverDef driverDef ) {
        this.driverDef = driverDef;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        DriverDefEditorContent that = ( DriverDefEditorContent ) o;

        return !( driverDef != null ? !driverDef.equals( that.driverDef ) : that.driverDef != null );

    }

    @Override
    public int hashCode() {
        return driverDef != null ? ~driverDef.hashCode() : 0;
    }
}
