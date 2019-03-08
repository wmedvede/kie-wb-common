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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner;

import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.BasePropertyReader;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EndNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.junit.Assert.assertEquals;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.GraphBuilderTest.mockBpmnNode;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.GraphBuilderTest.mockNode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessPostConverterTest {

    @Test
    public void testPostConvert() {

        double laneX = 80;
        double laneY = 100;
        double laneWidth = 500;
        double laneHeight = 200;
        RectangleDimensionsSet laneRectangleDimensionsSet = new RectangleDimensionsSet(laneWidth, laneHeight);
        Lane laneDefinition = mock(Lane.class);
        when(laneDefinition.getDimensionsSet()).thenReturn(laneRectangleDimensionsSet);
        Node<? extends View<? extends BPMNViewDefinition>, ?> lane = mockNode(laneDefinition, laneX, laneY, laneWidth, laneHeight);
        BpmnNode laneNode = mockBpmnNode(lane);

        double startEventX = 180;
        double startEventY = 130;
        double eventWidth = 56d;
        double eventHeight = 56d;
        Node<? extends View<? extends BPMNViewDefinition>, ?> startEvent = mockNode(mock(StartNoneEvent.class), startEventX + laneX, startEventY + laneY, eventWidth, eventHeight);
        BpmnNode startEventNode = mockBpmnNode(startEvent);

        //subprocess is collapsed and contains the task
        double subprocessX = 270;
        double subprocessY = 180;
        double subprocessWidth = 100;
        double subprocessHeight = 60;
        RectangleDimensionsSet subprocessRectangleDimensionsSet = new RectangleDimensionsSet(subprocessWidth, subprocessHeight);
        EmbeddedSubprocess subprocessDefinition = mock(EmbeddedSubprocess.class);
        when(subprocessDefinition.getDimensionsSet()).thenReturn(subprocessRectangleDimensionsSet);
        Node<? extends View<? extends BPMNViewDefinition>, ?> subprocess = mockNode(subprocessDefinition, subprocessX + laneX, subprocessY + laneY, subprocessWidth, subprocessHeight);
        BpmnNode subprocessNode = mockBpmnNode(subprocess);
        BasePropertyReader subprocessPropertyReader = subprocessNode.getPropertyReader();
        when(subprocessPropertyReader.isExpanded()).thenReturn(false);

        double taskX = 10;
        double taskY = 10;
        double taskWidth = 200;
        double taskHeight = 100;
        Node<? extends View<? extends BPMNViewDefinition>, ?> task = mockNode(mock(UserTask.class), taskX, taskY, taskWidth, taskHeight);
        BpmnNode taskNode = mockBpmnNode(task);

        double endEventX = 450;
        double endEventY = 230;
        Node<? extends View<? extends BPMNViewDefinition>, ?> endEvent = mockNode(mock(EndNoneEvent.class), endEventX + laneX, endEventY + laneY, eventWidth, eventHeight);
        BpmnNode endEventNode = mockBpmnNode(endEvent);

        Node<? extends View<? extends BPMNViewDefinition>, ?> diagram = mockNode(mock(BPMNDiagramImpl.class), 0, 0, 1000, 1000);
        BpmnNode rootNode = mockBpmnNode(diagram);

        rootNode.addChild(laneNode);
        laneNode.addChild(startEventNode);
        laneNode.addChild(subprocessNode);
        subprocessNode.addChild(taskNode);
        laneNode.addChild(endEventNode);

        ProcessPostConverter postConverter = new ProcessPostConverter();
        DefinitionResolver definitionResolver = mock(DefinitionResolver.class);
        when(definitionResolver.getResolutionFactor()).thenReturn(2d);
        postConverter.postConvert(rootNode, definitionResolver);

        Bounds startEventBounds = startEventNode.value().getContent().getBounds();
        //preserves original position
        assertEquals(laneX + startEventX, startEventBounds.getUpperLeft().getX(), 0);
        assertEquals(laneY + startEventY, startEventBounds.getUpperLeft().getY(), 0);
        assertEquals(eventWidth, startEventBounds.getWidth(), 0);
        assertEquals(eventHeight, startEventBounds.getHeight(), 0);

        Bounds subProcessBounds = subprocessNode.value().getContent().getBounds();
        //was properly resized and preserves original position
        assertEquals(laneX + subprocessX, subProcessBounds.getUpperLeft().getX(), 0);
        assertEquals(laneY + subprocessY, subProcessBounds.getUpperLeft().getY(), 0);
        assertEquals(200 + 10 + 10, subProcessBounds.getWidth(), 0);
        assertEquals(100 + 10 + 10, subProcessBounds.getHeight(), 0);

        Bounds taskBounds = taskNode.value().getContent().getBounds();
        //was properly positioned and preserves size
        assertEquals(laneX + subprocessX + taskX, taskBounds.getUpperLeft().getX(), 0);
        assertEquals(laneY + subprocessY + taskY, taskBounds.getUpperLeft().getY(), 0);
        assertEquals(200, taskBounds.getWidth(), 0);
        assertEquals(100, taskBounds.getHeight(), 0);

        Bounds endEventBounds = endEventNode.value().getContent().getBounds();
        double subprocessDeltaX = subProcessBounds.getWidth() - subprocessWidth;
        double subprocessDeltaY = subProcessBounds.getHeight() - subprocessHeight;
        //was properly moved after the subprocess resize
        assertEquals(laneX + endEventX + subprocessDeltaX, endEventBounds.getUpperLeft().getX(), 0);
        assertEquals(laneY + endEventY + subprocessDeltaY, endEventBounds.getUpperLeft().getY(), 0);
    }
}
