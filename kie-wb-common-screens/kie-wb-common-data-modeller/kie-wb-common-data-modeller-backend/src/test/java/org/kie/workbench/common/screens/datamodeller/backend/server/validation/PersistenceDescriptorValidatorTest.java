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

import java.util.ArrayList;
import java.util.List;

import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.model.persistence.PersistenceDescriptorModel;
import org.kie.workbench.common.screens.datamodeller.model.persistence.PersistenceUnitModel;
import org.kie.workbench.common.screens.datamodeller.model.persistence.Property;
import org.kie.workbench.common.screens.datamodeller.model.persistence.TransactionType;
import org.kie.workbench.common.screens.datamodeller.validation.PersistenceDescriptorValidator;
import org.kie.workbench.common.services.backend.project.ProjectClassLoaderHelper;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;

import static org.junit.Assert.*;
import static org.kie.workbench.common.screens.datamodeller.backend.server.validation.PersistenceDescriptorValidationMessages.newErrorMessage;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PersistenceDescriptorValidatorTest {

    private static final String VERSION = "version";

    private static final String JTA_DATASOURCE = "datasource";

    private static final String PERSISTENCE_UNIT_NAME = "persistenceUnitName";

    private static final String PERSISTENCE_PROVIDER = "persistenceProvider";

    private PersistenceDescriptorValidator validator;

    @Mock
    private KieProjectService projectService;

    @Mock
    private ProjectClassLoaderHelper classLoaderHelper;

    @Mock
    private Path path;

    @Mock
    private KieProject project;

    @Mock
    private ClassLoader classLoader;

    private PersistenceDescriptorModel descriptor;

    @Before
    public void setUp() {
        validator = new PersistenceDescriptorValidatorImpl( projectService, classLoaderHelper );
        descriptor = new PersistenceDescriptorModel();

        when( projectService.resolveProject( path ) ).thenReturn( project );
        when( classLoaderHelper.getProjectClassLoader( project ) ).thenReturn( classLoader );
    }

    /**
     * Tests the validation of a well formed persistence descriptor.
     */
    @Test
    public void testValidateValidDescriptor() {
        descriptor.setVersion( VERSION );
        PersistenceUnitModel unit = new PersistenceUnitModel();
        descriptor.setPersistenceUnit( unit );
        unit.setJtaDataSource( JTA_DATASOURCE );
        unit.setName( PERSISTENCE_UNIT_NAME );
        unit.setProvider( PERSISTENCE_PROVIDER );
        unit.setTransactionType( TransactionType.JTA );

        List<Property> properties = new ArrayList<>(  );
        properties.add( new Property( "name1", "value1" ) );
        properties.add( new Property( "name2", "value2" ) );
        unit.setProperties( properties );

        List<ValidationMessage> result = validator.validate( path, descriptor );
        assertTrue( result.isEmpty() );
    }

    @Test
    public void testValidateInvalidProject() {
        when( projectService.resolveProject( path ) ).thenReturn( null );
        List<ValidationMessage> result = validator.validate( path, descriptor );
        assertEquals( 1, result.size() );
        ValidationMessage expectedMessage = newErrorMessage( PersistenceDescriptorValidationMessages.DESCRIPTOR_NOT_BELONG_TO_PROJECT_ID,
                PersistenceDescriptorValidationMessages.DESCRIPTOR_NOT_BELONG_TO_PROJECT );
        assertEquals( expectedMessage, result.get( 0 ) );
    }

    @Test
    public void testValidateMissingPersistenceUnit() {
        descriptor.setPersistenceUnit( null );
        List<ValidationMessage> result = validator.validate( path, descriptor );
        assertEquals( 1, result.size() );
        ValidationMessage expectedMessage = newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NOT_FOUND_ID,
                PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NOT_FOUND );
        assertEquals( expectedMessage, result.get( 0 ) );
    }

    @Test
    public void testValidateInvalidDescriptor() {
        //create a descriptor that generates all the possible validation messages we can control.
        descriptor.setPersistenceUnit( new PersistenceUnitModel() );
        List<ValidationMessage> result = validator.validate( path, descriptor );

        List<ValidationMessage> expectedMessages = new ArrayList<>(  );


        //TODO, continue here

/*
        expectedMessages.add(  )

        ValidationMessage expectedMessage = newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NOT_FOUND_ID,
                PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NOT_FOUND );
        assertEquals( expectedMessage, result.get( 0 ) );

*/

        /*

                    messages.add( newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NOT_FOUND_ID,
                    PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NOT_FOUND ) );
            return messages;
        }

        if ( unitModel.getName( ) == null || unitModel.getName( ).trim( ).isEmpty( ) ) {
            messages.add( newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NAME_EMPTY_ID,
                    PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NAME_EMPTY ) );
        }

        if ( unitModel.getProvider( ) == null || unitModel.getProvider( ).trim( ).isEmpty( ) ) {
            messages.add( newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_PROVIDER_ID,
                    PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_PROVIDER_EMPTY ) );
        }

        if ( unitModel.getTransactionType( ) == null ) {
            messages.add( newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_TRANSACTION_TYPE_EMPTY_ID,
                    PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_TRANSACTION_TYPE_EMPTY ) );
        } else if ( unitModel.getTransactionType( ) == TransactionType.JTA &&
                ( unitModel.getJtaDataSource( ) == null || unitModel.getJtaDataSource( ).trim( ).isEmpty( ) ) ) {
            messages.add( newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_JTA_DATASOURCE_EMPTY_ID,
                    PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_JTA_DATASOURCE_EMPTY ) );
        } else if ( unitModel.getTransactionType( ) == TransactionType.RESOURCE_LOCAL &&
                ( unitModel.getNonJtaDataSource( ) == null || unitModel.getNonJtaDataSource( ).trim( ).isEmpty( ) ) ) {
            messages.add( newErrorMessage( PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NON_JTA_DATASOURCE_EMPTY_ID,
                    PersistenceDescriptorValidationMessages.PERSISTENCE_UNIT_NON_JTA_DATASOURCE_EMPTY ) );
        }




         */
    }

}