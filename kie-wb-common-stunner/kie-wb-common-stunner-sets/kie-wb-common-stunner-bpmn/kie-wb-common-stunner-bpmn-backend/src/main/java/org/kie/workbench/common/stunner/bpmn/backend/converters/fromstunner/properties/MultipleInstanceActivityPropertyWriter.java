/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.OutputSet;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Ids;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.Scripts.asCData;
import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class MultipleInstanceActivityPropertyWriter extends ActivityPropertyWriter {

    private MultiInstanceLoopCharacteristics miloop;
    private InputOutputSpecification ioSpec;
    private InputSet inputSet;
    private OutputSet outputSet;

    public MultipleInstanceActivityPropertyWriter(Activity activity, VariableScope variableScope) {
        super(activity, variableScope);
    }

    public void setCollectionInput(String collectionInput) {
        if (isEmpty(collectionInput)) {
            return;
        }

        setUpLoopCharacteristics();
        String suffix = "IN_COLLECTION";
        String id = Ids.dataInput(activity.getId(), suffix);
        DataInput dataInputElement = createDataInput(id, suffix);
        ioSpec.getDataInputs().add(dataInputElement);
        Property prop = findPropertyById(collectionInput); // check whether this exist or throws
        dataInputElement.setItemSubjectRef(prop.getItemSubjectRef());

        miloop.setLoopDataInputRef(dataInputElement);

        inputSet.getDataInputRefs().add(dataInputElement);

        DataInputAssociation dia = Bpmn2Factory.eINSTANCE.createDataInputAssociation();
        dia.getSourceRef().add(prop);
        dia.setTargetRef(dataInputElement);
        activity.getDataInputAssociations().add(dia);
    }

    public void setCollectionOutput(String collectionOutput) {
        if (isEmpty(collectionOutput)) {
            return;
        }

        setUpLoopCharacteristics();
        String suffix = "OUT_COLLECTION";
        String id = Ids.dataOutput(activity.getId(), suffix);
        DataOutput dataOutputElement = createDataOutput(id, suffix);
        ioSpec.getDataOutputs().add(dataOutputElement);

        Property prop = findPropertyById(collectionOutput); // check whether this exist or throws
        dataOutputElement.setItemSubjectRef(prop.getItemSubjectRef());

        miloop.setLoopDataOutputRef(dataOutputElement);

        outputSet.getDataOutputRefs().add(dataOutputElement);

        DataOutputAssociation doa = Bpmn2Factory.eINSTANCE.createDataOutputAssociation();
        doa.getSourceRef().add(dataOutputElement);
        doa.setTargetRef(prop);
        activity.getDataOutputAssociations().add(doa);
    }

    public void setInput(String name) {
        setInput(name, true);
    }

    protected void setInput(String name, boolean addDataInputAssociation) {
        if (isEmpty(name)) {
            return;
        }

        setUpLoopCharacteristics();
        DataInput miDataInputElement = createDataInput(name, name);
        ItemDefinition item = createItemDefinition(name);
        addItemDefinition(item);
        miDataInputElement.setItemSubjectRef(item);
        miloop.setInputDataItem(miDataInputElement);

        String id = Ids.dataInput(activity.getId(), name);
        DataInput dataInputElement = createDataInput(id, name);
        dataInputElement.setItemSubjectRef(item);
        ioSpec.getDataInputs().add(dataInputElement);

        inputSet.getDataInputRefs().add(dataInputElement);

        if (addDataInputAssociation) {
            DataInputAssociation dia = Bpmn2Factory.eINSTANCE.createDataInputAssociation();
            dia.getSourceRef().add(miDataInputElement);
            dia.setTargetRef(dataInputElement);
            activity.getDataInputAssociations().add(dia);
        }
    }

    public void setOutput(String name) {
        setOutput(name, true);
    }

    public void setOutput(String name, boolean addDataOutputAssociation) {
        if (isEmpty(name)) {
            return;
        }

        setUpLoopCharacteristics();
        DataOutput miDataOutputElement = createDataOutput(name, name);
        ItemDefinition item = createItemDefinition(name);
        addItemDefinition(item);
        miDataOutputElement.setItemSubjectRef(item);
        miloop.setOutputDataItem(miDataOutputElement);

        String id = Ids.dataOutput(activity.getId(), name);
        DataOutput dataOutputElement = createDataOutput(id, name);
        dataOutputElement.setItemSubjectRef(item);
        ioSpec.getDataOutputs().add(dataOutputElement);

        outputSet.getDataOutputRefs().add(dataOutputElement);

        if (addDataOutputAssociation) {
            DataOutputAssociation doa = Bpmn2Factory.eINSTANCE.createDataOutputAssociation();
            doa.getSourceRef().add(dataOutputElement);
            doa.setTargetRef(miDataOutputElement);
            activity.getDataOutputAssociations().add(doa);
        }
    }

    public void setCompletionCondition(String expression) {
        if (!isEmpty(expression)) {
            setUpLoopCharacteristics();
            FormalExpression formalExpression = bpmn2.createFormalExpression();
            formalExpression.setBody(asCData(expression));
            miloop.setCompletionCondition(formalExpression);
        }
    }

    protected void setUpLoopCharacteristics() {
        if (miloop == null) {
            miloop = bpmn2.createMultiInstanceLoopCharacteristics();
            activity.setLoopCharacteristics(miloop);
            ioSpec = getIoSpecification();
            inputSet = getInputSet(ioSpec);
            outputSet = getOutputSet(ioSpec);
        }
    }

    protected DataInput createDataInput(String id, String name) {
        DataInput dataInput = bpmn2.createDataInput();
        dataInput.setId(id);
        dataInput.setName(name);
        return dataInput;
    }

    protected DataOutput createDataOutput(String id, String name) {
        DataOutput dataOutput = bpmn2.createDataOutput();
        dataOutput.setId(id);
        dataOutput.setName(name);
        return dataOutput;
    }

    protected ItemDefinition createItemDefinition(String name) {
        ItemDefinition item = bpmn2.createItemDefinition();
        item.setId(Ids.multiInstanceItemType(activity.getId(), name));
        item.setStructureRef("Object");
        return item;
    }

    private Property findPropertyById(String id) {
        return variableScope.lookup(id).getTypedIdentifier();
    }
}
