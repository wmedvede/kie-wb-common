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

/**
 * Helper class for keeping the registration for the different data source definitions. DataSourceDef registrations
 * should always be realised by using the DataSourceManagerRegistry.
 */
package org.kie.workbench.common.screens.datasource.management.backend.core;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;

/**
 * Specialized cache for the storage of the data source definitions.
 */
public interface DataSourceDefCache {

    void put( DataSourceDef dataSourceDef );

    void remove( String uuid );

    DataSourceDef get( String uuid );
}
