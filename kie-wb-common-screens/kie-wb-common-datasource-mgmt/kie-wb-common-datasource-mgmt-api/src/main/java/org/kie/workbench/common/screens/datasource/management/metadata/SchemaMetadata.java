/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datasource.management.metadata;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class SchemaMetadata {

    private String catalogName;

    private String schemaName;

    public SchemaMetadata( ) {
    }

    public SchemaMetadata( String catalogName, String schemaName ) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

    public SchemaMetadata( String schemaName ) {
        this.schemaName = schemaName;
    }

    public String getCatalogName( ) {
        return catalogName;
    }

    public void setCatalogName( String catalogName ) {
        this.catalogName = catalogName;
    }

    public String getSchemaName( ) {
        return schemaName;
    }

    public void setSchemaName( String schemaName ) {
        this.schemaName = schemaName;
    }
}
