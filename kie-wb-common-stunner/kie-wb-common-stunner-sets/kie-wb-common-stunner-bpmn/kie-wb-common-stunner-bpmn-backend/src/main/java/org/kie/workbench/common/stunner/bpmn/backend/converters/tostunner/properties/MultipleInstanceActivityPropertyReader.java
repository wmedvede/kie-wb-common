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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.DataAssociation;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;

public class MultipleInstanceActivityPropertyReader extends ActivityPropertyReader {

    public MultipleInstanceActivityPropertyReader(Activity activity, BPMNPlane plane, DefinitionResolver definitionResolver) {
        super(activity, plane, definitionResolver);
    }

    public String getCollectionInput() {
        String ieDataInputId = getLoopDataInputRefId();
        return super.getDataInputAssociations().stream()
                .filter(dia -> hasTargetRef(dia, ieDataInputId))
                .filter(MultipleInstanceActivityPropertyReader::hasSourceRefs)
                .map(dia -> getVariableName((Property) dia.getSourceRef().get(0)))
                .findFirst()
                .orElse(null);
    }

    public String getCollectionOutput() {
        String ieDataOutputId = getLoopDataOutputRefId();
        return super.getDataOutputAssociations().stream()
                .filter(doa -> hasSourceRef(doa, ieDataOutputId))
                .map(doa -> getVariableName((Property) doa.getTargetRef()))
                .findFirst()
                .orElse(null);
    }

    public String getDataInput() {
        return getMultiInstanceLoopCharacteristics()
                .map(MultiInstanceLoopCharacteristics::getInputDataItem)
                .map(d -> Optional.ofNullable(d.getName()).orElse(d.getId()))
                .orElse("");
    }

    public String getDataOutput() {
        return getMultiInstanceLoopCharacteristics()
                .map(MultiInstanceLoopCharacteristics::getOutputDataItem)
                .map(d -> Optional.ofNullable(d.getName()).orElse(d.getId()))
                .orElse("");
    }

    public String getCompletionCondition() {
        return getMultiInstanceLoopCharacteristics()
                .map(miloop -> (FormalExpression) miloop.getCompletionCondition())
                .map(FormalExpression::getBody)
                .orElse("");
    }

    private Optional<MultiInstanceLoopCharacteristics> getMultiInstanceLoopCharacteristics() {
        return Optional.ofNullable((MultiInstanceLoopCharacteristics) activity.getLoopCharacteristics());
    }

    private static String getVariableName(Property property) {
        return ProcessVariableReader.getProcessVariableName(property);
    }

    @Override
    protected List<DataInput> getDataInputs() {
        String dataInputIdForInputVariable = getDataInputIdForDataInputVariable();
        String dataInputIdForInputCollection = getLoopDataInputRefId();
        return super.getDataInputs().stream()
                .filter(di -> !di.getId().equals(dataInputIdForInputVariable))
                .filter(di -> !di.getId().equals(dataInputIdForInputCollection))
                .collect(Collectors.toList());
    }

    @Override
    protected List<DataOutput> getDataOutputs() {
        String dataOuputIdForOutputVariable = getDataOutputIdForDataOutputVariable();
        String dataOutputIdForCollection = getLoopDataOutputRefId();
        return super.getDataOutputs().stream()
                .filter(dout -> !dout.getId().equals(dataOuputIdForOutputVariable))
                .filter(dout -> !dout.getId().equals(dataOutputIdForCollection))
                .collect(Collectors.toList());
    }

    @Override
    protected List<DataInputAssociation> getDataInputAssociations() {
        String dataInputIdForInputVariable = getDataInputIdForDataInputVariable();
        String dataInputIdForInputCollection = getLoopDataInputRefId();
        return super.getDataInputAssociations().stream()
                .filter(dia -> !hasTargetRef(dia, dataInputIdForInputVariable))
                .filter(dia -> !hasTargetRef(dia, dataInputIdForInputCollection))
                .collect(Collectors.toList());
    }

    @Override
    protected List<DataOutputAssociation> getDataOutputAssociations() {
        String dataOutputIdForOutputVariable = getDataOutputIdForDataOutputVariable();
        String dataOutputIdForOutputCollection = getLoopDataOutputRefId();
        return super.getDataOutputAssociations().stream()
                .filter(doa -> !hasSourceRef(doa, dataOutputIdForOutputVariable))
                .filter(doa -> !hasSourceRef(doa, dataOutputIdForOutputCollection))
                .collect(Collectors.toList());
    }

    protected String getDataInputIdForDataInputVariable() {
        String dataInputVariable = getDataInput();
        String dataInputVariableId = super.getDataInputAssociations().stream()
                .filter(dia -> hasSourceRef(dia, dataInputVariable))
                .map(dia -> dia.getTargetRef().getId())
                .findFirst().orElse(null);
        return dataInputVariableId;
    }

    protected String getDataOutputIdForDataOutputVariable() {
        String dataOutputVariable = getDataOutput();
        String dataOutputVariableId = super.getDataOutputAssociations().stream()
                .filter(doa -> hasTargetRef(doa, dataOutputVariable))
                .map(doa -> doa.getSourceRef().get(0).getId())
                .findFirst().orElse(null);
        return dataOutputVariableId;
    }

    protected String getLoopDataInputRefId() {
        return getMultiInstanceLoopCharacteristics()
                .map(MultiInstanceLoopCharacteristics::getLoopDataInputRef)
                .map(ItemAwareElement::getId)
                .orElse(null);
    }

    protected String getLoopDataOutputRefId() {
        return getMultiInstanceLoopCharacteristics()
                .map(MultiInstanceLoopCharacteristics::getLoopDataOutputRef)
                .map(ItemAwareElement::getId)
                .orElse(null);
    }

    static boolean hasSourceRefs(DataAssociation dataAssociation) {
        return dataAssociation.getSourceRef() != null && !dataAssociation.getSourceRef().isEmpty();
    }

    static boolean hasSourceRef(DataAssociation dataAssociation, String id) {
        return hasSourceRefs(dataAssociation) && Objects.equals(dataAssociation.getSourceRef().get(0).getId(), id);
    }

    static boolean hasTargetRef(DataAssociation dataAssociation, String id) {
        return dataAssociation.getTargetRef() != null && Objects.equals(dataAssociation.getTargetRef().getId(), id);
    }
}
