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

import org.eclipse.bpmn2.BaseElement;
import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.definition.BaseAdHocSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.background.BackgroundSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.font.FontSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.BaseAdHocSubprocessTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.BaseProcessData;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Intended only for testing the abstract BasePropertyWriter class methods, other property writer tests must extend
 * AbstractBasePropertyWriterTest.
 */
public class BasePropertyWriterTest extends AbstractBasePropertyWriterTest<BasePropertyWriter, BaseElement> {

    @Override
    protected BasePropertyWriter newPropertyWriter(BaseElement baseElement, VariableScope variableScope) {
        return new BasePropertyWriterMock(baseElement, variableScope);
    }

    @Override
    protected BaseElement mockElement() {
        return mock(BaseElement.class);
    }

    @Test
    public void testSetAbsoluteBoundsForAdHocSubprocess() {
        testSetAbsoluteBoundsForExpandedNode(mockNode(new BaseAdHocSubprocessMock(), org.kie.workbench.common.stunner.core.graph.content.Bounds.create(X1, Y1, X2, Y2)));
    }

    @Test
    public void testSetAbsoluteBoundsForEmbeddedSubprocess() {
        testSetAbsoluteBoundsForExpandedNode(mockNode(mock(EmbeddedSubprocess.class), org.kie.workbench.common.stunner.core.graph.content.Bounds.create(X1, Y1, X2, Y2)));
    }

    @Test
    public void testSetAbsoluteBoundsForEventSubprocess() {
        testSetAbsoluteBoundsForExpandedNode(mockNode(mock(EventSubprocess.class), org.kie.workbench.common.stunner.core.graph.content.Bounds.create(X1, Y1, X2, Y2)));
    }

    private void testSetAbsoluteBoundsForExpandedNode(Node<View, ?> node) {
        testSetAbsoluteBounds(node);
        assertTrue(propertyWriter.getShape().isIsExpanded());
    }

    private class BasePropertyWriterMock extends BasePropertyWriter {

        BasePropertyWriterMock(BaseElement baseElement, VariableScope variableScope) {
            super(baseElement, variableScope);
        }
    }

    private class BaseAdHocSubprocessMock extends BaseAdHocSubprocess {

        BaseAdHocSubprocessMock() {
            this(null, null, null, null, null);
        }

        private BaseAdHocSubprocessMock(BPMNGeneralSet general, BackgroundSet backgroundSet, FontSet fontSet, RectangleDimensionsSet dimensionsSet, SimulationSet simulationSet) {
            super(general, backgroundSet, fontSet, dimensionsSet, simulationSet);
        }

        @Override
        public BaseAdHocSubprocessTaskExecutionSet getExecutionSet() {
            return null;
        }

        @Override
        public void setExecutionSet(BaseAdHocSubprocessTaskExecutionSet executionSet) {

        }

        @Override
        public BaseProcessData getProcessData() {
            return null;
        }

        @Override
        public void setProcessData(BaseProcessData processData) {

        }
    }
}
