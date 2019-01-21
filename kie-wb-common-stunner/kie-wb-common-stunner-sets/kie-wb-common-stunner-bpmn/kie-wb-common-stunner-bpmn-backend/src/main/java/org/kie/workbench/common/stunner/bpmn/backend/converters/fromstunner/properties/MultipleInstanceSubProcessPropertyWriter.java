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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties;

import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.SubProcess;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Ids;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class MultipleInstanceSubProcessPropertyWriter extends SubProcessPropertyWriter {

    public MultipleInstanceSubProcessPropertyWriter(SubProcess process, VariableScope variableScope) {
        super(process, variableScope);
        setUpLoopCharacteristics();
    }

    @Override
    public void setInput(String name) {
        if (isEmpty(name)) {
            return;
        }
        DataInput miDataInputElement = createDataInput(name, name);
        ItemDefinition item = bpmn2.createItemDefinition();
        item.setId(Ids.multiInstanceItemType(process.getId(), name));
        addItemDefinition(item);
        miDataInputElement.setItemSubjectRef(item);
        getMiloop().setInputDataItem(miDataInputElement);
    }

    @Override
    public void setOutput(String name) {
        if (isEmpty(name)) {
            return;
        }
        DataOutput miDataOutputElement = createDataOutput(name, name);
        ItemDefinition item = bpmn2.createItemDefinition();
        item.setId(Ids.multiInstanceItemType(process.getId(), name));
        addItemDefinition(item);
        miDataOutputElement.setItemSubjectRef(item);
        getMiloop().setOutputDataItem(miDataOutputElement);
    }
}
