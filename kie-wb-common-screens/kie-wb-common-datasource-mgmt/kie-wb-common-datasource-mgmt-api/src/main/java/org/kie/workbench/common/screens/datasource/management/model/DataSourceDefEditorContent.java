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
public class DataSourceDefEditorContent {

    private DataSourceDef dataSourceDef;

    public DataSourceDefEditorContent() {
    }

    public DataSourceDef getDataSourceDef() {
        return dataSourceDef;
    }

    public void setDataSourceDef( DataSourceDef dataSourceDef ) {
        this.dataSourceDef = dataSourceDef;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        DataSourceDefEditorContent that = ( DataSourceDefEditorContent ) o;

        return !( dataSourceDef != null ? !dataSourceDef.equals( that.dataSourceDef ) : that.dataSourceDef != null );

    }

    @Override
    public int hashCode() {
        return dataSourceDef != null ? dataSourceDef.hashCode() : 0;
    }
}
