/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.definition;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.bpmn.definition.property.background.BackgroundSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.font.FontSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.AdHocSubprocessTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.morph.Morph;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;
import org.kie.workbench.common.stunner.core.rule.annotation.CanContain;
import org.kie.workbench.common.stunner.core.util.HashUtil;

@Portable
@Bindable
@CanContain(roles = {"cm_activity"})
@Definition(graphFactory = NodeFactory.class, builder = AdHocSubprocess.AdHocSubprocessBuilder.class)
@Morph(base = BaseSubprocess.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class AdHocSubprocess
        extends BaseSubprocess {

    @PropertySet
    @FormField(afterElement = "general")
    @Valid
    protected AdHocSubprocessTaskExecutionSet executionSet;

    @PropertySet
    @FormField(afterElement = "executionSet")
    @Valid
    private ProcessData processData;

    @NonPortable
    public static class AdHocSubprocessBuilder implements Builder<AdHocSubprocess> {

        @Override
        public AdHocSubprocess build() {
            return new AdHocSubprocess(new BPMNGeneralSet("Sub-process"),
                                       new BackgroundSet(),
                                       new FontSet(),
                                       new RectangleDimensionsSet(),
                                       new SimulationSet(),
                                       new AdHocSubprocessTaskExecutionSet(),
                                       new ProcessData());
        }
    }

    public AdHocSubprocess() {
    }

    public AdHocSubprocess(final @MapsTo("general") BPMNGeneralSet general,
                           final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                           final @MapsTo("fontSet") FontSet fontSet,
                           final @MapsTo("dimensionsSet") RectangleDimensionsSet dimensionsSet,
                           final @MapsTo("simulationSet") SimulationSet simulationSet,
                           final @MapsTo("executionSet") AdHocSubprocessTaskExecutionSet executionSet,
                           final @MapsTo("processData") ProcessData processData) {
        super(general,
              backgroundSet,
              fontSet,
              dimensionsSet,
              simulationSet);
        this.executionSet = executionSet;
        this.processData = processData;
    }

    public AdHocSubprocessTaskExecutionSet getExecutionSet() {
        return executionSet;
    }

    public void setExecutionSet(final AdHocSubprocessTaskExecutionSet executionSet) {
        this.executionSet = executionSet;
    }

    public ProcessData getProcessData() {
        return processData;
    }

    public void setProcessData(final ProcessData processData) {
        this.processData = processData;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(super.hashCode(),
                                         executionSet.hashCode(),
                                         processData.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AdHocSubprocess) {
            AdHocSubprocess other = (AdHocSubprocess) o;
            return super.equals(other) &&
                    executionSet.equals(other.executionSet) &&
                    processData.equals(other.processData);
        }
        return false;
    }
}
