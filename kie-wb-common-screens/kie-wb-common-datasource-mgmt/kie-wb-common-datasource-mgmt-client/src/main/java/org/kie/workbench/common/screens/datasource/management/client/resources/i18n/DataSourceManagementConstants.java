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

package org.kie.workbench.common.screens.datasource.management.client.resources.i18n;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

public interface DataSourceManagementConstants {

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_InvalidNameMessage = "DataSourceDefEditor.InvalidNameMessage";

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_InvalidJndiMessage = "DataSourceDefEditor.InvalidJndiMessage";

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_InvalidConnectionURLMessage = "DataSourceDefEditor.InvalidConnectionURLMessage";

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_InvalidUserMessage = "DataSourceDefEditor.InvalidUserMessage";

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_InvalidPasswordMessage = "DataSourceDefEditor.InvalidPasswordMessage";

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_DriverRequiredMessage = "DataSourceDefEditor.DriverRequiredMessage";

    @TranslationKey( defaultValue = "" )
    String DataSourceDefEditor_AllFieldsRequiresValidation = "DataSourceDefEditor.AllFieldsRequiresValidation";

    @TranslationKey( defaultValue = "" )
    String DriverDefEditor_InvalidNameMessage = "DriverDefEditor.InvalidNameMessage";

    @TranslationKey( defaultValue = "" )
    String DriverDefEditor_InvalidDriverClassMessage = "DriverDefEditor.InvalidDriverClassMessage";

    @TranslationKey( defaultValue = "" )
    String DriverDefEditor_InvalidGroupIdMessage = "DriverDefEditor.InvalidGroupIdMessage";

    @TranslationKey( defaultValue = "" )
    String DriverDefEditor_InvalidArtifactIdMessage = "DriverDefEditor.InvalidArtifactIdMessage";

    @TranslationKey( defaultValue = "" )
    String DriverDefEditor_InvalidVersionMessage = "DriverDefEditor.InvalidVersionMessage";

}