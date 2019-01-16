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

import java.util.List;

import bpsim.ElementParameters;
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
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.InitializedVariable.InitializedInputVariable;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.InitializedVariable.InitializedOutputVariable;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.ParsedAssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Ids;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.SimulationSets;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.AssignmentsInfos.isReservedIdentifier;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.Scripts.asCData;

public class ActivityPropertyWriter extends PropertyWriter {

    protected final Activity activity;
    private ElementParameters simulationParameters;

    public ActivityPropertyWriter(Activity activity, VariableScope variableScope) {
        super(activity, variableScope);
        this.activity = activity;
    }

    @Override
    public Activity getFlowElement() {
        return activity;
    }

    public void setSimulationSet(SimulationSet simulationSet) {
        this.simulationParameters = SimulationSets.toElementParameters(simulationSet);
        simulationParameters.setElementRef(activity.getId());
    }

    public ElementParameters getSimulationParameters() {
        return simulationParameters;
    }

    public void setAssignmentsInfo(AssignmentsInfo info) {
        final ParsedAssignmentsInfo assignmentsInfo = ParsedAssignmentsInfo.of(info);
        final InputOutputSpecification ioSpec = getIoSpecification();

        List<InitializedInputVariable> inputs =
                assignmentsInfo.createInitializedInputVariables(getId(), variableScope);

        for (InitializedInputVariable input : inputs) {
            if (isReservedIdentifier(input.getIdentifier())) {
                continue;
            }

            DataInput dataInput = input.getDataInput();
            getInputSet(ioSpec).getDataInputRefs().add(dataInput);
            ioSpec.getDataInputs().add(dataInput);

            this.addItemDefinition(input.getItemDefinition());
            DataInputAssociation dataInputAssociation = input.getDataInputAssociation();
            if (dataInputAssociation != null) {
                activity.getDataInputAssociations().add(dataInputAssociation);
            }
        }

        List<InitializedOutputVariable> outputs =
                assignmentsInfo.createInitializedOutputVariables(getId(), variableScope);

        for (InitializedOutputVariable output : outputs) {
            DataOutput dataOutput = output.getDataOutput();
            getOutputSet(ioSpec).getDataOutputRefs().add(dataOutput);
            ioSpec.getDataOutputs().add(dataOutput);

            this.addItemDefinition(output.getItemDefinition());
            DataOutputAssociation dataOutputAssociation = output.getDataOutputAssociation();
            if (dataOutputAssociation != null) {
                activity.getDataOutputAssociations().add(dataOutputAssociation);
            }
        }
    }

    public void setMICollectionInput(String collectionInput) {
        // ignore empty input
        if (collectionInput == null) {
            return;
        }

        InputOutputSpecification ioSpec = getIoSpecification();
        DataInput dataInputElement = createMIDataInput("IN_COLLECTION");
        Property prop = findPropertyById(collectionInput); // check whether this exist or throws
        getLoopCharacteristics().setLoopDataInputRef(dataInputElement);
        getInputSet(ioSpec).getDataInputRefs().add(dataInputElement);

        DataInputAssociation dia = Bpmn2Factory.eINSTANCE.createDataInputAssociation();
        dia.getSourceRef().add(prop);
        dia.setTargetRef(dataInputElement);
        activity.getDataInputAssociations().add(dia);
    }

    public void setMIInput(String value) {
        DataInput dataInput = createMIDataInput(value);
        getLoopCharacteristics().setInputDataItem(dataInput);
    }

    public void setMICollectionOutput(String collectionOutput) {
        // ignore empty input
        if (collectionOutput == null) {
            return;
        }

        //TODO WM, revisar porque aun en el codigo de eduardo el input collection tiene menos cosas que el output collection.
        InputOutputSpecification ioSpec = getIoSpecification();
        DataOutput dataOutputElement = createMIDataOutput("OUT_COLLECTION");
        Property prop = findPropertyById(collectionOutput); // check whether this exist or throws
        getLoopCharacteristics().setLoopDataOutputRef(dataOutputElement);
        ItemDefinition item = bpmn2.createItemDefinition();
        item.setId(Ids.multiInstanceItemType(activity.getId(), "OUT_COLLECTION"));
        dataOutputElement.setItemSubjectRef(item);
        addItemDefinition(item);
        getOutputSet(ioSpec).getDataOutputRefs().add(dataOutputElement);

        DataOutputAssociation doa = Bpmn2Factory.eINSTANCE.createDataOutputAssociation();
        doa.getSourceRef().add(dataOutputElement);
        doa.setTargetRef(prop);
        activity.getDataOutputAssociations().add(doa);
    }

    public void setMIOutput(String name) {
        DataOutput dataOutput = createMIDataOutput(name);
        getLoopCharacteristics().setOutputDataItem(dataOutput);
        ItemDefinition item = bpmn2.createItemDefinition();
        item.setId(Ids.multiInstanceItemType(activity.getId(), name));
        dataOutput.setItemSubjectRef(item);
        addItemDefinition(item);
    }

    private DataInput createMIDataInput(String name) {
        DataInput dataInput = bpmn2.createDataInput();
        dataInput.setId(Ids.dataInput(activity.getId(), name));
        dataInput.setName(name);

        getIoSpecification().getDataInputs().add(dataInput);
        return dataInput;
    }

    private DataOutput createMIDataOutput(String value) {
        DataOutput dataOutput = bpmn2.createDataOutput();
        dataOutput.setId(Ids.dataOutput(activity.getId(), value));
        dataOutput.setName(value);

        getIoSpecification().getDataOutputs().add(dataOutput);
        return dataOutput;
    }

    public void setMICompletionCondition(String expression) {
        FormalExpression formalExpression = bpmn2.createFormalExpression();
        formalExpression.setBody(asCData(expression));
        getLoopCharacteristics().setCompletionCondition(formalExpression);
    }

    protected InputOutputSpecification getIoSpecification() {
        InputOutputSpecification ioSpecification = activity.getIoSpecification();
        if (ioSpecification == null) {
            ioSpecification = bpmn2.createInputOutputSpecification();
            activity.setIoSpecification(ioSpecification);
        }
        return ioSpecification;
    }

    protected InputSet getInputSet(InputOutputSpecification ioSpecification) {
        List<InputSet> inputSets = ioSpecification.getInputSets();
        InputSet inputSet;
        if (inputSets.isEmpty()) {
            inputSet = bpmn2.createInputSet();
            inputSets.add(inputSet);
        } else {
            inputSet = inputSets.get(0);
        }
        return inputSet;
    }

    protected OutputSet getOutputSet(InputOutputSpecification ioSpecification) {
        List<OutputSet> outputSets = ioSpecification.getOutputSets();
        OutputSet outputSet;
        if (outputSets.isEmpty()) {
            outputSet = bpmn2.createOutputSet();
            outputSets.add(outputSet);
        } else {
            outputSet = outputSets.get(0);
        }
        return outputSet;
    }

    protected MultiInstanceLoopCharacteristics getLoopCharacteristics() {
        //TODO WM, ver si este cast correspnde o resuelveo tl tipo de loopcharacteristics más arriba en la jerarquia
        MultiInstanceLoopCharacteristics loopCharacteristics = (MultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
        if (loopCharacteristics == null) {
            loopCharacteristics = bpmn2.createMultiInstanceLoopCharacteristics();
            activity.setLoopCharacteristics(loopCharacteristics);
        }
        return loopCharacteristics;
    }

    private Property findPropertyById(String id) {
        return variableScope.lookup(id).getTypedIdentifier();
    }
}
