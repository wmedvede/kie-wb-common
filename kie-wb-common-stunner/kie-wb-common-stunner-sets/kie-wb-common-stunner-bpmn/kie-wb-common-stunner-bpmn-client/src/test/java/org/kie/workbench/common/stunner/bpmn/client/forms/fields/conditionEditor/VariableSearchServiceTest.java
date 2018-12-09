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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.Caller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.bpmn.definition.AdHocSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.MultipleInstanceSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.mocks.CallerMock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VariableSearchServiceTest {

    private static final String CANVAS_ROOT_ID = "CANVAS_ROOT_ID";
    private static final String SELECTED_ITEM = "SELECTED_ITEM";
    private static final String SOURCE_NODE = "SOURCE_NODE";
    private static final String PARENT_NODE1 = "PARENT_NODE1";
    private static final String PARENT_NODE2 = "PARENT_NODE2";
    private static final String PARENT_NODE3 = "PARENT_NODE3";

    @Mock
    private ConditionEditorService editorService;

    private Caller<ConditionEditorService> editorServiceCaller;

    private VariableSearchService searchService;

    @Mock
    private EditorSession clientSession;

    @Mock
    private SelectionControl selectionControl;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private Diagram diagram;

    @Mock
    private Graph graph;

    @Mock
    private Metadata metadata;

    @Mock
    private Path path;

    @Before
    public void setUp() {
        when(clientSession.getCanvasHandler()).thenReturn(canvasHandler);
        when(canvasHandler.getDiagram()).thenReturn(diagram);
        when(diagram.getGraph()).thenReturn(graph);
        when(diagram.getMetadata()).thenReturn(metadata);
        when(metadata.getPath()).thenReturn(path);
        when(metadata.getCanvasRootUUID()).thenReturn(CANVAS_ROOT_ID);
        editorServiceCaller = new CallerMock<>(editorService);
        searchService = new VariableSearchService(editorServiceCaller);
    }

    @Test
    public void testInitSession() {
        prepareSelectedItem();
        Node sourceNode = mockSourceNode(SOURCE_NODE, SELECTED_ITEM);
        Node parentNode1 = mockNode(PARENT_NODE1, null);
        setParentNode(sourceNode, parentNode1);

        Node parentNode2 = mockNode(PARENT_NODE2, null);
        setParentNode(parentNode1, parentNode2);

        Node parentNode3 = mockNode(PARENT_NODE3, null);
        setParentNode(parentNode2, parentNode3);

        Node canvasRoot = mockNode(CANVAS_ROOT_ID, null);
        setParentNode(parentNode3, canvasRoot);

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(sourceNode);
        nodes.add(parentNode1);
        nodes.add(parentNode2);

        when(graph.nodes()).thenReturn(nodes);
        searchService.init(clientSession);
    }

    private void prepareSelectedItem() {
        when(diagram.getGraph()).thenReturn(graph);
        List<String> selectedItems = new ArrayList<>();
        selectedItems.add(SELECTED_ITEM);
        when(selectionControl.getSelectedItems()).thenReturn(selectedItems);
        when(clientSession.getSelectionControl()).thenReturn(selectionControl);
    }

    @SuppressWarnings("unchecked")
    private Node mockSourceNode(String UUID, String edgeId) {
        Node sourceNode = mockNode(UUID, null);
        EdgeImpl edge = mock(EdgeImpl.class);
        when(edge.getUUID()).thenReturn(edgeId);
        List<Edge> outEdges = new ArrayList<>();
        outEdges.add(edge);
        when(sourceNode.getOutEdges()).thenReturn(outEdges);
        return sourceNode;
    }

    private void setParentNode(Node childNode, Node parentNode) {
        List<Edge> inEdges = new ArrayList<>();
        Edge edge = mock(Edge.class);
        String edgeUUID = "from_" + parentNode.getUUID() + "_to_" + childNode;
        when(edge.getUUID()).thenReturn(edgeUUID);
        Child childContent = mock(Child.class);
        when(edge.getContent()).thenReturn(childContent);
        inEdges.add(edge);
        when(childNode.getInEdges()).thenReturn(inEdges);
        when(edge.getSourceNode()).thenReturn(parentNode);
    }

    private Node mockNode(String UUID, Definition definition) {
        Node node = mock(Node.class);
        when(node.getUUID()).thenReturn(UUID);
        when(node.asNode()).thenReturn(node);
        View view = mock(View.class);
        when(view.getDefinition()).thenReturn(definition);
        when(node.getContent()).thenReturn(view);
        return node;
    }

    private EventSubprocess mockEventSubProcess(String variables) {
        EventSubprocess eventSubprocess = mock(EventSubprocess.class);
        ProcessData processData = mockProcessData(variables);
        when(eventSubprocess.getProcessData()).thenReturn(processData);
        return eventSubprocess;
    }

    private AdHocSubprocess mockEventAdHocSubProcess(String variables) {
        AdHocSubprocess adHocSubprocess = mock(AdHocSubprocess.class);
        ProcessData processData = mockProcessData(variables);
        when(adHocSubprocess.getProcessData()).thenReturn(processData);
        return adHocSubprocess;
    }

    private EmbeddedSubprocess mockEmbeddedSubprocess(String variables) {
        EmbeddedSubprocess embeddedSubprocess = mock(EmbeddedSubprocess.class);
        ProcessData processData = mockProcessData(variables);
        when(embeddedSubprocess.getProcessData()).thenReturn(processData);
        return embeddedSubprocess;
    }

    private MultipleInstanceSubprocess mockMultipleInstanceSubprocess(String variables) {
        MultipleInstanceSubprocess multipleInstanceSubprocess = mock(MultipleInstanceSubprocess.class);
        ProcessData processData = mockProcessData(variables);
        when(multipleInstanceSubprocess.getProcessData()).thenReturn(processData);
        return multipleInstanceSubprocess;
    }

    private BPMNDiagramImpl mockBPMNDiagram(String variables) {
        BPMNDiagramImpl bpmnDiagram = mock(BPMNDiagramImpl.class);
        ProcessData processData = mockProcessData(variables);
        when(bpmnDiagram.getProcessData()).thenReturn(processData);
        return bpmnDiagram;
    }

    private ProcessData mockProcessData(String variables) {
        ProcessData processData = mock(ProcessData.class);
        ProcessVariables processVariables = mock(ProcessVariables.class);
        when(processData.getProcessVariables()).thenReturn(processVariables);
        when(processVariables.getValue()).thenReturn(variables);
        return processData;
    }
}
