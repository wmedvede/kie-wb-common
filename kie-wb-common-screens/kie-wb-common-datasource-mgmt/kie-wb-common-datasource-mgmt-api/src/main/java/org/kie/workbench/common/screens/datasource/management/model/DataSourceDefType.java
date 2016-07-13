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

public enum DataSourceDefType {

    /**
     * CUSTOM: Custom data sources are purely implemented by the data sources management services and are portable
     * across different different containers.
     */
    CUSTOM,

    /**
     * REFERRED: Referred data sources are basically a jndi reference to an already existing data source in current
     * container, and are portable across different container as long the provided jndi name to lookup for is valid
     * for the target container.
     */
    REFERRED,

    /**
     * CONTAINER: Container data sources are implemented by using the current container services. e.g. the Widlfly 10
     * client controller apis, and will work only if there's a properly configured provider for the desired container.
     */
    CONTAINER

}
