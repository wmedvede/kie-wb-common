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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.kie.workbench.common.screens.datamodeller.backend.server.DataModelerServiceHelper;
import org.kie.workbench.common.screens.datamodeller.model.persistence.PersistenceDescriptorModel;
import org.kie.workbench.common.screens.datamodeller.model.persistence.PersistenceUnitModel;
import org.kie.workbench.common.screens.datamodeller.model.persistence.Property;
import org.kie.workbench.common.screens.datamodeller.model.persistence.TransactionType;
import org.kie.workbench.common.screens.datamodeller.validation.PersistenceDescriptorValidator;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.backend.vfs.Path;

@ApplicationScoped
public class PersistenceDescriptorValidatorImpl
        implements PersistenceDescriptorValidator {

    private KieProjectService projectService;

    private DataModelerServiceHelper serviceHelper;

    private PersistableClassValidator classValidator = new PersistableClassValidator();

    private PropertyValidator propertyValidator = new PropertyValidator();

    public PersistenceDescriptorValidatorImpl() {
    }

    @Inject
    public PersistenceDescriptorValidatorImpl( KieProjectService projectService,
            DataModelerServiceHelper serviceHelper ) {
        this.projectService = projectService;
        this.serviceHelper = serviceHelper;
    }

    @Override
    public List<ValidationMessage> validate( Path path, PersistenceDescriptorModel model ) {

        List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        List<ValidationMessage> subMessages;
        ValidationMessage message;
        PersistenceUnitModel unitModel;

        KieProject project = projectService.resolveProject( path );

        if ( project == null ) {
            //uncommon scenario, since by construction, the same as with other wb assets, a persistence descriptor
            // belongs to a project
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.DESCRIPTOR_NOT_BELONG_TO_PROJECT_ID,
                    ValidationMessages.DESCRIPTOR_NOT_BELONG_TO_PROJECT ) );
            return messages;
        }

        if ( ( unitModel = model.getPersistenceUnit() ) == null ) {
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PERSISTENCE_UNIT_NOT_FOUND_ID,
                    ValidationMessages.PERSISTENCE_UNIT_NOT_FOUND ) );
            return messages;
        }

        if ( unitModel.getName() == null || "".equals( unitModel.getName().trim() ) ) {
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PERSISTENCE_UNIT_NAME_EMPTY_ID,
                    ValidationMessages.PERSISTENCE_UNIT_NAME_EMPTY ) );
        }

        if ( unitModel.getProvider() == null || "".equals( unitModel.getProvider().trim() ) ) {
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PERSISTENCE_UNIT_PROVIDER_ID,
                    ValidationMessages.PERSISTENCE_UNIT_PROVIDER_EMPTY ) );
        }

        if ( unitModel.getTransactionType() == null ) {
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PERSISTENCE_UNIT_TRANSACTION_TYPE_EMPTY_ID,
                    ValidationMessages.PERSISTENCE_UNIT_TRANSACTION_TYPE_EMPTY ) );
        } else if ( unitModel.getTransactionType() == TransactionType.JTA &&
                ( unitModel.getJtaDataSource() == null || "".equals( unitModel.getJtaDataSource().trim() ) ) ) {
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PERSISTENCE_UNIT_JTA_DATASOURCE_EMPTY_ID,
                    ValidationMessages.PERSISTENCE_UNIT_JTA_DATASOURCE_EMPTY ) );
        } else if ( unitModel.getTransactionType() == TransactionType.RESOURCE_LOCAL &&
                ( unitModel.getNonJtaDataSource() == null || "".equals( unitModel.getNonJtaDataSource().trim() ) ) ) {
            messages.add( ValidationMessages.newErrorMessage( ValidationMessages.PERSISTENCE_UNIT_NON_JTA_DATASOURCE_EMPTY_ID,
                    ValidationMessages.PERSISTENCE_UNIT_NON_JTA_DATASOURCE_EMPTY ) );
        }

        if ( unitModel.getClasses() != null && unitModel.getClasses().size() > 0 ) {
            ClassLoader projectClassLoader = serviceHelper.getProjectClassLoader( project );
            for ( String clazz : unitModel.getClasses() ) {
                if ( ( message = classValidator.validate( clazz, projectClassLoader ) ) != null ) {
                    messages.add( message );
                }
            }
        }

        if ( unitModel.getProperties() != null ) {
            for ( Property property : unitModel.getProperties() ) {
                subMessages = propertyValidator.validate( property.getName(), property.getValue() );
                if ( subMessages.size() > 0 ) {
                    messages.addAll( subMessages );
                }
            }
        }
        return messages;
    }
}
