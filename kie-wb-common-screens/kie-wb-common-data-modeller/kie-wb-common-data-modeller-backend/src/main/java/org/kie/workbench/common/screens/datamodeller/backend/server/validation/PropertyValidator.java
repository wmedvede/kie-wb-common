/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.screens.datamodeller.backend.server.validation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.guvnor.common.services.shared.validation.model.ValidationMessage;

/**
 * Class for validating if a <property, value> can be considered as valid in the context of the persistence descriptor
 * configuration.
 */
public class PropertyValidator {

    public PropertyValidator() {
    }

    /**
     * Validates if a <property, value> in the context of the persistence descriptor.
     * @param name the name of the property.
     * @param value the value for the property.
     * @return a list list of validation messages.
     */
    public List<ValidationMessage> validate( String name, String value ) {
        List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        if ( name == null || value.trim().isEmpty() ) {
            //uncommon case
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PROPERTY_NAME_EMPTY_ID,
                    ValidationMessages.PROPERTY_NAME_EMPTY ) );
        }
        if ( value == null || value.trim().isEmpty() ) {
            messages.add( ValidationMessages.newWarningMessage( ValidationMessages.PROPERTY_VALUE_EMPTY_ID,
                    MessageFormat.format( ValidationMessages.PROPERTY_VALUE_EMPTY, name ) ) );
        }
        return messages;
    }
}