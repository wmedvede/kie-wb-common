/*
 * Copyright 2016 JBoss Inc
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

package org.kie.workbench.common.screens.datamodeller.backend.server.validation;

import org.guvnor.common.services.shared.message.Level;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;

public class ValidationMessages {

    private static long ID_SEQUENCE = 1000;

    public static final long DESCRIPTOR_NOT_BELONG_TO_PROJECT_ID = ID_SEQUENCE++;

    public static final String DESCRIPTOR_NOT_BELONG_TO_PROJECT = "Persistence descriptor do not belong to a valid project";

    public static final long CLASS_NOT_FOUND_ID = ID_SEQUENCE++;

    public static final String CLASS_NOT_FOUND = "Class {0} was not found in current project class path";

    public static final long CLASS_NOT_PERSISTABLE_ID = ID_SEQUENCE++;

    public static final String CLASS_NOT_PERSISTABLE = "Class {0} must be a persistable class";

    public static final long PERSISTABLE_CLASS_NAME_EMPTY_ID = ID_SEQUENCE++;

    public static final String PERSISTABLE_CLASS_NAME_EMPTY = "Empty persistable class name was found";

    public static final long PERSISTENCE_UNIT_NOT_FOUND_ID = ID_SEQUENCE++;

    public static final String PERSISTENCE_UNIT_NOT_FOUND = "Persistence descriptor must have a persistence unit";

    public static final long PERSISTENCE_UNIT_NAME_EMPTY_ID = ID_SEQUENCE++;

    public static final String PERSISTENCE_UNIT_NAME_EMPTY = "Persistence unit name must have a non empty String value";

    public static final long PERSISTENCE_UNIT_PROVIDER_ID = ID_SEQUENCE++;

    public static final String PERSISTENCE_UNIT_PROVIDER_EMPTY = "Persistence unit provider must have a non empty String value";

    public static final long PERSISTENCE_UNIT_TRANSACTION_TYPE_EMPTY_ID = ID_SEQUENCE++;

    public static final String PERSISTENCE_UNIT_TRANSACTION_TYPE_EMPTY = "Persistence must have a Transaction Type";

    public static final long PERSISTENCE_UNIT_JTA_DATASOURCE_EMPTY_ID = ID_SEQUENCE++;

    public static final String PERSISTENCE_UNIT_JTA_DATASOURCE_EMPTY = "JTA transaction type must have a non empty Data Source configured";

    public static ValidationMessage newValidationMessage( long id, Level level, String text ) {
        ValidationMessage validationMessage = new ValidationMessage();
        validationMessage.setId( id );
        validationMessage.setLevel( level );
        validationMessage.setText( text );
        return validationMessage;
    }

    public static ValidationMessage newErrorMessage( long id, String text ) {
        return newValidationMessage( id, Level.ERROR, text );
    }

    public static ValidationMessage newWarningMessage( long id, String text ) {
        return newValidationMessage( id, Level.WARNING, text );
    }

}

