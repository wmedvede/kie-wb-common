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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FieldMetadata;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.TypeMetadata;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.TypeMetadataQueryResult;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchCallback;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchResults;
import org.uberfire.mocks.CallerMock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor.FunctionSearchServiceTest.verifyContains;
import static org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor.FunctionSearchServiceTest.verifyNotContains;
import static org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor.VariableSearchService.unboxDefaultType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VariableSearchServiceTest {

    private static final String CANVAS_ROOT_ID = "CANVAS_ROOT_ID";
    private static final String SELECTED_ITEM = "SELECTED_ITEM";
    private static final String SOURCE_NODE = "SOURCE_NODE";
    private static final String PARENT_NODE1 = "PARENT_NODE1";
    private static final String PARENT_NODE2 = "PARENT_NODE2";
    private static final String PARENT_NODE3 = "PARENT_NODE3";
    private static final String PARENT_NODE4 = "PARENT_NODE4";

    private static final String MULTIPLE_INSTANCE_SUBPROCESS = "MultipleInstanceSubprocess";
    private static final String EMBEDDED_SUBPROCESS = "EmbeddedSubprocess";
    private static final String ADHOC_SUBPROCESS = "AdHocSubprocess";
    private static final String EVENT_SUBPROCESS = "EventSubprocess";
    private static final String MAIN_PROCESS = "MainProcess";

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

    @Mock
    private LiveSearchCallback<String> searchCallback;

    @Captor
    private ArgumentCaptor<LiveSearchResults<String>> searchResultsCaptor;

    private Set<String> mockedVariableNames = new HashSet<>();

    private Map<String, String> mockedVariableTypes = new HashMap<>();

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
        prepareAndInitSession();
        mockedVariableNames.forEach(variable -> verifyVariable(variable, variable));
    }

    @Test
    public void testSearchWithResults() {
        prepareAndInitSession();
        searchService.search(MULTIPLE_INSTANCE_SUBPROCESS, 20, searchCallback);
        verify(searchCallback).afterSearch(searchResultsCaptor.capture());
        LiveSearchResults<String> results = searchResultsCaptor.getValue();
        List<Pair<String, String>> expectedVariables1 = buildExpectedVariableNames(MULTIPLE_INSTANCE_SUBPROCESS, 17);

        verifyResultsContains(results, expectedVariables1);

        searchService.search("MultipleIns", 20, searchCallback);
        verify(searchCallback, times(2)).afterSearch(searchResultsCaptor.capture());
        results = searchResultsCaptor.getValue();
        verifyResultsContains(results, expectedVariables1);

        searchService.search(ADHOC_SUBPROCESS, 20, searchCallback);
        verify(searchCallback, times(3)).afterSearch(searchResultsCaptor.capture());
        results = searchResultsCaptor.getValue();
        List<Pair<String, String>> expectedVariables2 = buildExpectedVariableNames(ADHOC_SUBPROCESS, 17);
        verifyResultsContains(results, expectedVariables2);
        verifyResultsNotContains(results, expectedVariables1);
    }

    @Test
    public void testSearchWithoutResults() {
        prepareAndInitSession();
        searchService.search("non-existing", 20, searchCallback);
        verify(searchCallback).afterSearch(searchResultsCaptor.capture());
        LiveSearchResults<String> results = searchResultsCaptor.getValue();
        assertEquals(0, results.size());
    }

    @Test
    public void testSearchEntryWithResults() {
        prepareAndInitSession();
        List<Pair<String, String>> expectedVariables = buildExpectedVariableNames(MULTIPLE_INSTANCE_SUBPROCESS, 17);
        for (int i = 0; i < expectedVariables.size(); i++) {
            searchService.searchEntry(expectedVariables.get(i).getK1(), searchCallback);
            verify(searchCallback, times(i + 1)).afterSearch(searchResultsCaptor.capture());
            verifyContains(searchResultsCaptor.getValue(), expectedVariables.get(i));
        }
    }

    @Test
    public void testSearchEntryWithoutResults() {
        prepareAndInitSession();
        List<String> checkedVariables = Arrays.asList("not-existing1", "not-existing2", "not-existing3", "and_so_on");
        for (int i = 0; i < checkedVariables.size(); i++) {
            searchService.searchEntry(checkedVariables.get(i), searchCallback);
            verify(searchCallback, times(i + 1)).afterSearch(searchResultsCaptor.capture());
            verifyNotContains(searchResultsCaptor.getValue(), new Pair<>(checkedVariables.get(i), checkedVariables.get(i)));
        }
    }

    @Test
    public void testGetOptionTypeWithResults() {
        prepareAndInitSession();
        List<Pair<String, String>> checkedVariables = buildExpectedVariableNames(MULTIPLE_INSTANCE_SUBPROCESS, 17);
        checkedVariables.forEach(variable -> assertEquals("Option type wasn't properly calculated for variable: " + variable,
                                                          mockedVariableTypes.get(variable.getK1()), searchService.getOptionType(variable.getK1())));
    }

    @Test
    public void testGetOptionTypeWithoutResults() {
        prepareAndInitSession();
        prepareAndInitSession();
        List<String> checkedVariables = Arrays.asList("not-existing1", "not-existing2", "not-existing3", "and_so_on");
        String type;
        for (int i = 0; i < checkedVariables.size(); i++) {
            type = searchService.getOptionType(checkedVariables.get(i));
            assertNotEquals(mockedVariableTypes, type);
        }
    }

    @Test
    public void testUnboxDefaultTypes() {
        assertEquals(Short.class.getName(), unboxDefaultType("short"));
        assertEquals(Short.class.getName(), unboxDefaultType("Short"));
        assertEquals(Integer.class.getName(), unboxDefaultType("int"));
        assertEquals(Integer.class.getName(), unboxDefaultType("Integer"));
        assertEquals(Long.class.getName(), unboxDefaultType("long"));
        assertEquals(Long.class.getName(), unboxDefaultType("Long"));
        assertEquals(Float.class.getName(), unboxDefaultType("float"));
        assertEquals(Float.class.getName(), unboxDefaultType("Float"));
        assertEquals(Double.class.getName(), unboxDefaultType("double"));
        assertEquals(Double.class.getName(), unboxDefaultType("Double"));
        assertEquals(Character.class.getName(), unboxDefaultType("char"));
        assertEquals(Character.class.getName(), unboxDefaultType("Character"));
        assertEquals(String.class.getName(), unboxDefaultType("String"));
        assertEquals(Object.class.getName(), unboxDefaultType("Object"));
        assertEquals("Other_value", unboxDefaultType("Other_value"));
        assertEquals(null, unboxDefaultType(null));
    }

    @Test
    public void testClear() {
        prepareAndInitSession();
        List<Pair<String, String>> expectedVariables = buildExpectedVariableNames(MULTIPLE_INSTANCE_SUBPROCESS, 17);
        for (int i = 0; i < expectedVariables.size(); i++) {
            searchService.searchEntry(expectedVariables.get(i).getK1(), searchCallback);
            verify(searchCallback, times(i + 1)).afterSearch(searchResultsCaptor.capture());
            verifyContains(searchResultsCaptor.getValue(), expectedVariables.get(i));
            assertEquals(mockedVariableTypes.get(expectedVariables.get(i).getK1()), searchService.getOptionType(expectedVariables.get(i).getK1()));
        }
        searchService.clear();
        int testedSize = expectedVariables.size();
        for (int i = 0; i < expectedVariables.size(); i++) {
            searchService.searchEntry(expectedVariables.get(i).getK1(), searchCallback);
            verify(searchCallback, times(i + 1 + testedSize)).afterSearch(searchResultsCaptor.capture());
            assertEquals(0, searchResultsCaptor.getValue().size());
            assertNull(searchService.getOptionType(expectedVariables.get(i).getK1()));
        }
    }

    private void prepareAndInitSession() {
        prepareSelectedItem();
        Node sourceNode = mockSourceNode(SOURCE_NODE, SELECTED_ITEM);

        Node parentNode1 = mockNode(PARENT_NODE1, mockMultipleInstanceSubprocess(mockVariables(MULTIPLE_INSTANCE_SUBPROCESS)));
        setParentNode(sourceNode, parentNode1);

        Node parentNode2 = mockNode(PARENT_NODE2, mockEmbeddedSubprocess(mockVariables(EMBEDDED_SUBPROCESS)));
        setParentNode(parentNode1, parentNode2);

        Node parentNode3 = mockNode(PARENT_NODE3, mockAdHocSubProcess(mockVariables(ADHOC_SUBPROCESS)));
        setParentNode(parentNode2, parentNode3);

        Node parentNode4 = mockNode(PARENT_NODE4, mockEventSubProcess(mockVariables(EVENT_SUBPROCESS)));
        setParentNode(parentNode3, parentNode4);

        Node canvasRoot = mockNode(CANVAS_ROOT_ID, mockBPMNDiagram(mockVariables(MAIN_PROCESS)));
        setParentNode(parentNode4, canvasRoot);

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(sourceNode);
        nodes.add(parentNode1);
        nodes.add(parentNode2);
        nodes.add(parentNode3);
        nodes.add(parentNode4);
        nodes.add(canvasRoot);

        when(graph.nodes()).thenReturn(nodes);
        Set<TypeMetadata> typeMetadatas = new HashSet<>();
        typeMetadatas.add(mockBean1Metadata());
        TypeMetadataQueryResult queryResult = new TypeMetadataQueryResult(typeMetadatas, new HashSet<>());
        when(editorService.findMetadata(any())).thenReturn(queryResult);

        searchService.init(clientSession);
    }

    private void verifyVariable(String variableKey, String variableValue) {
        searchService.searchEntry(variableKey, results -> assertTrue(results.stream()
                                                                             .filter(entry -> variableKey.equals(entry.getKey()) && variableValue.equals(entry.getValue()))
                                                                             .findFirst()
                                                                             .isPresent()));
    }

    private void verifyResultsContains(LiveSearchResults<String> results, List<Pair<String, String>> expectedVariables) {
        verifyContains(results, expectedVariables.stream().map(expectedVariable -> new Pair<>(expectedVariable.getK1(), expectedVariable.getK2())).collect(Collectors.toList()));
    }

    private void verifyResultsNotContains(LiveSearchResults<String> results, List<Pair<String, String>> expectedVariables) {
        verifyNotContains(results, expectedVariables.stream().map(expectedVariable -> new Pair<>(expectedVariable.getK1(), expectedVariable.getK2())).collect(Collectors.toList()));
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

    private Node mockNode(String UUID, Object definition) {
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

    private AdHocSubprocess mockAdHocSubProcess(String variables) {
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

    private String mockVariables(String prefix) {
        StringBuilder variables = new StringBuilder();
        int index = 0;
        variables.append(mockVariable(prefix, index++, Short.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "short"));
        variables.append("," + mockVariable(prefix, index++, Integer.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "int"));
        variables.append("," + mockVariable(prefix, index++, Long.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "long"));
        variables.append("," + mockVariable(prefix, index++, Float.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "float"));
        variables.append("," + mockVariable(prefix, index++, Double.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "double"));
        variables.append("," + mockVariable(prefix, index++, Boolean.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "boolean"));
        variables.append("," + mockVariable(prefix, index++, Character.class.getName()));
        variables.append("," + mockVariable(prefix, index++, "char"));
        variables.append("," + mockVariable(prefix, index++, String.class.getName()));
        variables.append("," + mockVariable(prefix, index++, Object.class.getName()));
        variables.append("," + mockVariable(prefix, index++, Bean1.class.getName()));
        return variables.toString();
    }

    private String mockVariable(String prefix, int index, String type) {
        String variableName = mockVariableName(prefix, index);
        mockedVariableTypes.put(variableName, unboxDefaultType(type));
        return variableName + ":" + type;
    }

    private String mockVariableName(String prefix, int index) {
        String variable = prefix + "Variable" + index;
        mockedVariableNames.add(variable);
        return variable;
    }

    private List<Pair<String, String>> buildExpectedVariableNames(String prefix, int count) {
        List<Pair<String, String>> result = new ArrayList<>();
        String varName;
        for (int i = 0; i < count; i++) {
            varName = mockVariableName(prefix, i);
            result.add(new Pair<>(varName, varName));
        }
        String bean1Variable = result.get(count - 1).getK1();
        Pair<String, String> name = new Pair<>(bean1Variable + ".getName()", bean1Variable + ".name");
        Pair<String, String> surname = new Pair<>(bean1Variable + ".getSurname()", bean1Variable + ".surname");
        Pair<String, String> age = new Pair<>(bean1Variable + ".getAge()", bean1Variable + ".age");
        result.add(name);
        result.add(surname);
        result.add(age);
        mockedVariableNames.add(name.getK1());
        mockedVariableTypes.put(name.getK1(), String.class.getName());
        mockedVariableNames.add(surname.getK1());
        mockedVariableTypes.put(surname.getK1(), String.class.getName());
        mockedVariableNames.add(age.getK1());
        mockedVariableTypes.put(age.getK1(), Integer.class.getName());
        return result;
    }

    private TypeMetadata mockBean1Metadata() {
        List<FieldMetadata> fieldMetadatas = new ArrayList<>();
        fieldMetadatas.add(new FieldMetadata("name", String.class.getName(), "getName", "setName"));
        fieldMetadatas.add(new FieldMetadata("surname", String.class.getName(), "getSurname", "setSurname"));
        fieldMetadatas.add(new FieldMetadata("age", "int", "getAge", "setAge"));
        return new TypeMetadata(Bean1.class.getName(), fieldMetadatas);
    }
}
