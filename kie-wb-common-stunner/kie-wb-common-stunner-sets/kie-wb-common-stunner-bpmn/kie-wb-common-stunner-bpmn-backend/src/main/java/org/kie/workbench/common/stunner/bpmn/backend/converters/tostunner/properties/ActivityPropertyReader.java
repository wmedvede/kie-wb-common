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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;

public class ActivityPropertyReader extends FlowElementPropertyReader {

    protected final Activity activity;
    protected final DefinitionResolver definitionResolver;

    public ActivityPropertyReader(Activity activity, BPMNPlane plane, DefinitionResolver definitionResolver) {
        super(activity, plane, definitionResolver.getShape(activity.getId()));
        this.activity = activity;
        this.definitionResolver = definitionResolver;
    }

    public SimulationSet getSimulationSet() {
        return definitionResolver.resolveSimulationParameters(activity.getId())
                .map(SimulationSets::of)
                .orElse(new SimulationSet());
    }

    public AssignmentsInfo getAssignmentsInfo() {
        AssignmentsInfo info = AssignmentsInfos.of(getDataInputs(),
                                   getDataInputAssociations(),
                                   getDataOutputs(),
                                   getDataOutputAssociations(),
                                   getIOSpecification().isPresent());

        if (info.getValue().isEmpty()) {
            info.setValue("||||");
        }
        return info;
    }

    protected Optional<InputOutputSpecification> getIOSpecification() {
        return Optional.ofNullable(activity.getIoSpecification());
    }

    protected List<DataInput> getDataInputs() {
        return getIOSpecification().map(InputOutputSpecification::getDataInputs).orElse(Collections.emptyList());
    }

    protected List<DataOutput> getDataOutputs() {
        return getIOSpecification().map(InputOutputSpecification::getDataOutputs).orElse(Collections.emptyList());
    }

    protected List<DataInputAssociation> getDataInputAssociations() {
        return activity.getDataInputAssociations();
    }

    protected List<DataOutputAssociation> getDataOutputAssociations() {
        return activity.getDataOutputAssociations();
    }
}
