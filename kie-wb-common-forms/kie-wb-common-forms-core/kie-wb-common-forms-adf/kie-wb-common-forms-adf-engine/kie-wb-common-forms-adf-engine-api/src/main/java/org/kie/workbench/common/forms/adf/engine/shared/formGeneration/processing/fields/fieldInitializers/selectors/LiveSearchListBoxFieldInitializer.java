/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.adf.engine.shared.formGeneration.processing.fields.fieldInitializers.selectors;

import javax.enterprise.context.Dependent;

import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider;
import org.kie.workbench.common.forms.adf.engine.shared.formGeneration.FormGenerationContext;
import org.kie.workbench.common.forms.adf.engine.shared.formGeneration.processing.fields.FieldInitializer;
import org.kie.workbench.common.forms.adf.service.definitions.elements.FieldElement;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.lsListBox.definition.LiveSearchListBoxFieldDefinition;
import org.kie.workbench.common.forms.model.FieldDefinition;

@Dependent
public class LiveSearchListBoxFieldInitializer implements FieldInitializer<LiveSearchListBoxFieldDefinition> {

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        return fieldDefinition instanceof LiveSearchListBoxFieldDefinition;
    }

    @Override
    public void initialize(LiveSearchListBoxFieldDefinition fieldDefinition,
                           FieldElement fieldElement,
                           FormGenerationContext context) {
        String dataProvider = fieldElement.getParams().get(SelectorDataProvider.class.getName());
        String paramMaxResults = fieldElement.getParams().getOrDefault("maxResults", Integer.toString(LiveSearchListBoxFieldDefinition.MAX_RESULTS));
        int maxResults = LiveSearchListBoxFieldDefinition.MAX_RESULTS;
        if (dataProvider != null && !dataProvider.isEmpty()) {
            fieldDefinition.setDataProvider(dataProvider);
        }

        try {
            maxResults = Integer.valueOf(paramMaxResults);
        } catch (NumberFormatException e) {
            //Wrong number, let the execution continue with the by default value.
        }
        fieldDefinition.setMaxResults(maxResults);
    }
}