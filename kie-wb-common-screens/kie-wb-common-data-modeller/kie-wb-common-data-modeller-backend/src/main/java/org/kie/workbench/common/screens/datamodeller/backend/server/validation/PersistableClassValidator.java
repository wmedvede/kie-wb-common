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

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.guvnor.common.services.shared.validation.model.ValidationMessage;

public class PersistableClassValidator {

    public PersistableClassValidator() {
    }

    public ValidationMessage validate( String className, ClassLoader classLoader ) {
        ValidationMessage message = null;
        Class<?> clazz;
        try {
            if ( className == null || "".equals( className.trim() ) ) {
                message = ValidationMessages.newErrorMessage( ValidationMessages.PERSISTABLE_CLASS_NAME_EMPTY_ID,
                        ValidationMessages.PERSISTABLE_CLASS_NAME_EMPTY );
            }

            clazz = classLoader.loadClass( className );

            Annotation[] annotations = clazz.getAnnotations();
            boolean persistable = false;
            for ( int i = 0; annotations != null && i < annotations.length; i++ ) {
                if ( Entity.class.equals( annotations[ i ].annotationType() ) ||
                        Embeddable.class.equals( annotations[ i ].annotationType() ) ||
                        MappedSuperclass.class.equals( annotations[ i ].annotationType() ) ) {
                    persistable = true;
                    break;
                }
            }

            if ( !persistable ) {
                message = ValidationMessages.newErrorMessage( ValidationMessages.CLASS_NOT_PERSISTABLE_ID,
                        MessageFormat.format( ValidationMessages.CLASS_NOT_PERSISTABLE, className ) );

            }

        } catch ( ClassNotFoundException e ) {
            message = ValidationMessages.newErrorMessage( ValidationMessages.CLASS_NOT_FOUND_ID,
                    MessageFormat.format( ValidationMessages.CLASS_NOT_FOUND, className ) );
        }
        return message;
    }

}
