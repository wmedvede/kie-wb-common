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
package org.kie.workbench.common.stunner.cm.backend;

import java.io.InputStream;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.backend.definition.factory.TestScopeModelFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.workitem.service.WorkItemDefinitionBackendService;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Id;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Height;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Width;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet;
import org.kie.workbench.common.stunner.cm.backend.converters.fromstunner.properties.CaseManagementPropertyWriterFactory;
import org.kie.workbench.common.stunner.cm.definition.AdHocSubprocess;
import org.kie.workbench.common.stunner.cm.definition.CaseManagementDiagram;
import org.kie.workbench.common.stunner.cm.definition.CaseReusableSubprocess;
import org.kie.workbench.common.stunner.cm.definition.ProcessReusableSubprocess;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.backend.BackendFactoryManager;
import org.kie.workbench.common.stunner.core.backend.definition.adapter.reflect.BackendDefinitionAdapter;
import org.kie.workbench.common.stunner.core.backend.definition.adapter.reflect.BackendDefinitionSetAdapter;
import org.kie.workbench.common.stunner.core.backend.definition.adapter.reflect.BackendPropertyAdapter;
import org.kie.workbench.common.stunner.core.backend.definition.adapter.reflect.BackendPropertySetAdapter;
import org.kie.workbench.common.stunner.core.backend.service.XMLEncoderDiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.definition.adapter.AdapterManager;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.diagram.DiagramImpl;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;
import org.kie.workbench.common.stunner.core.factory.impl.EdgeFactoryImpl;
import org.kie.workbench.common.stunner.core.factory.impl.GraphFactoryImpl;
import org.kie.workbench.common.stunner.core.factory.impl.NodeFactoryImpl;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManagerImpl;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSetImpl;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.BoundsImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewImpl;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.graph.impl.GraphImpl;
import org.kie.workbench.common.stunner.core.graph.impl.NodeImpl;
import org.kie.workbench.common.stunner.core.graph.store.GraphNodeStoreImpl;
import org.kie.workbench.common.stunner.core.registry.definition.AdapterRegistry;
import org.kie.workbench.common.stunner.core.rule.RuleEvaluationContext;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.kie.workbench.common.stunner.core.rule.RuleSet;
import org.kie.workbench.common.stunner.core.rule.violations.DefaultRuleViolations;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.di;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseManagementDirectDiagramMarshallerTest {

    private static final String UUID_REGEX = "_[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12}";

    private static final String CM_FILE = "org/kie/workbench/common/stunner/cm/backend/case.bpmn-cm";

    @Mock
    DefinitionManager definitionManager;

    @Mock
    AdapterManager adapterManager;

    @Mock
    AdapterRegistry adapterRegistry;

    @Mock
    RuleManager rulesManager;

    BackendFactoryManager applicationFactoryManager;

    private CaseManagementDirectDiagramMarshaller tested;

    @Before
    public void setUp() throws Exception {

        // Graph utils.
        when(definitionManager.adapters()).thenReturn(adapterManager);
        when(adapterManager.registry()).thenReturn(adapterRegistry);
        // initApplicationFactoryManagerAlt();
        when(rulesManager.evaluate(any(RuleSet.class),
                                   any(RuleEvaluationContext.class))).thenReturn(new DefaultRuleViolations());

        DefinitionUtils definitionUtils = new DefinitionUtils(definitionManager,
                                                              applicationFactoryManager,
                                                              null);
        TestScopeModelFactory testScopeModelFactory = new TestScopeModelFactory(new CaseManagementDefinitionSet.CaseManagementDefinitionSetBuilder().build());
        // Definition manager.
        final BackendDefinitionAdapter definitionAdapter = new BackendDefinitionAdapter(definitionUtils);
        final BackendDefinitionSetAdapter definitionSetAdapter = new BackendDefinitionSetAdapter(definitionAdapter);
        final BackendPropertySetAdapter propertySetAdapter = new BackendPropertySetAdapter();
        final BackendPropertyAdapter propertyAdapter = new BackendPropertyAdapter();
        mockAdapterManager(definitionAdapter, definitionSetAdapter, propertySetAdapter, propertyAdapter);
        mockAdapterRegistry(definitionAdapter, definitionSetAdapter, propertySetAdapter, propertyAdapter);
        applicationFactoryManager = new MockApplicationFactoryManager(
                definitionManager,
                new GraphFactoryImpl(definitionManager),
                testScopeModelFactory,
                new EdgeFactoryImpl(definitionManager),
                new NodeFactoryImpl(definitionUtils)
        );

        GraphCommandManagerImpl commandManager = new GraphCommandManagerImpl(null,
                                                                             null,
                                                                             null);
        GraphCommandFactory commandFactory = new GraphCommandFactory();

        // The work item definition service.
        WorkItemDefinitionBackendService widService = mock(WorkItemDefinitionBackendService.class);

        // The tested CM marshaller.
        tested = new CaseManagementDirectDiagramMarshaller(
                new XMLEncoderDiagramMetadataMarshaller(),
                definitionManager,
                rulesManager,
                widService,
                applicationFactoryManager,
                commandFactory,
                commandManager);
    }

    private void mockAdapterRegistry(BackendDefinitionAdapter definitionAdapter,
                                     BackendDefinitionSetAdapter definitionSetAdapter,
                                     BackendPropertySetAdapter propertySetAdapter,
                                     BackendPropertyAdapter propertyAdapter) {
        when(adapterRegistry.getDefinitionSetAdapter(any(Class.class))).thenReturn(definitionSetAdapter);
        when(adapterRegistry.getDefinitionAdapter(any(Class.class))).thenReturn(definitionAdapter);
        when(adapterRegistry.getPropertySetAdapter(any(Class.class))).thenReturn(propertySetAdapter);
        when(adapterRegistry.getPropertyAdapter(any(Class.class))).thenReturn(propertyAdapter);
    }

    private void mockAdapterManager(BackendDefinitionAdapter definitionAdapter,
                                    BackendDefinitionSetAdapter definitionSetAdapter,
                                    BackendPropertySetAdapter propertySetAdapter,
                                    BackendPropertyAdapter propertyAdapter) {
        when(adapterManager.forDefinitionSet()).thenReturn(definitionSetAdapter);
        when(adapterManager.forDefinition()).thenReturn(definitionAdapter);
        when(adapterManager.forPropertySet()).thenReturn(propertySetAdapter);
        when(adapterManager.forProperty()).thenReturn(propertyAdapter);
    }

    @Test
    public void testMarshall() throws Exception {

        String result = tested.marshall(createDiagram());

        // all nodes saved
        hasElement(result,
                   "<bpmn2:adHocSubProcess id=\"_BCD8C7E1-9833-407D-9833-E12763A9A63D\" name=\"Stage\" ordering=\"Sequential\">");
        hasElement(result,
                   "<bpmn2:adHocSubProcess id=\"_F0A19BD0-3F42-493A-9A2D-2F4C24ED75D9\" name=\"Stage\" ordering=\"Sequential\">");
        hasElement(result,
                   "<bpmn2:userTask id=\"_E95AD08A-4595-4FA4-8948-3318D8BE7941\" name=\"Task\">");
        hasElement(result,
                   "<bpmn2:callActivity id=\"_C468418F-A1EE-470A-BC30-D85888DF3DF7\" drools:independent=\"false\" drools:waitForCompletion=\"true\" name=\"Subcase\" calledElement=\"\">");
        hasElement(result,
                   "<bpmn2:callActivity id=\"_4DF08597-2D2D-4CEE-B0EF-1AF0ED4ADAC2\" drools:independent=\"false\" drools:waitForCompletion=\"true\" name=\"Subcase\" calledElement=\"\">");
        hasElement(result,
                   "<bpmn2:callActivity id=\"_438D1DB6-4161-43C5-86F5-FC6B0F97BA7B\" drools:independent=\"false\" drools:waitForCompletion=\"true\" name=\"Subprocess\" calledElement=\"\">");

        // case saved
        hasElement(result,
                   "<drools:metaData name=\"case\">(\\s*)" + Pattern.quote("<drools:metaValue><![CDATA[true]]></drools:metaValue>") + "(\\s*)</drools:metaData>",
                   2);

        // start and end event created
        hasElement(result,
                   "<bpmn2:startEvent id=\"" + UUID_REGEX + "\">(\\s*)<bpmn2:outgoing>" + UUID_REGEX + "</bpmn2:outgoing>(\\s*)</bpmn2:startEvent>");
        hasElement(result,
                   "<bpmn2:endEvent id=\"" + UUID_REGEX + "\">(\\s*)<bpmn2:incoming>" + UUID_REGEX + "</bpmn2:incoming>(\\s*)</bpmn2:endEvent>");

        // sequence flow created between stages
        hasElement(result,
                   "<bpmn2:sequenceFlow id=\"(" + UUID_REGEX +  ")\" sourceRef=\"(" + UUID_REGEX +  ")\" targetRef=\"_F0A19BD0-3F42-493A-9A2D-2F4C24ED75D9\"/>");
        hasElement(result,
                   "<bpmn2:sequenceFlow id=\"(" + UUID_REGEX +  ")\" sourceRef=\"_F0A19BD0-3F42-493A-9A2D-2F4C24ED75D9\" targetRef=\"_BCD8C7E1-9833-407D-9833-E12763A9A63D\"/>");
        hasElement(result,
                   "<bpmn2:sequenceFlow id=\"(" + UUID_REGEX +  ")\" sourceRef=\"_BCD8C7E1-9833-407D-9833-E12763A9A63D\" targetRef=\"(" + UUID_REGEX +  ")\"/>");

        // sequence flow created inside stages
        hasElement(result,
                   "<bpmn2:sequenceFlow id=\"(" + UUID_REGEX + ")\" sourceRef=\"_4DF08597-2D2D-4CEE-B0EF-1AF0ED4ADAC2\" targetRef=\"_438D1DB6-4161-43C5-86F5-FC6B0F97BA7B\"/>");
        hasElement(result,
                   "<bpmn2:sequenceFlow id=\"(" + UUID_REGEX + ")\" sourceRef=\"_E95AD08A-4595-4FA4-8948-3318D8BE7941\" targetRef=\"_C468418F-A1EE-470A-BC30-D85888DF3DF7\"/>");
    }

    private DiagramImpl createDiagram() {
        CaseManagementDiagram root = new CaseManagementDiagram();
        root.getDiagramSet().setName(new Name("Case"));
        root.getDiagramSet().setId(new Id("New Case Management diagram"));
        root.getDimensionsSet().setWidth(new Width(2800.0));
        root.getDimensionsSet().setHeight(new Height(1400.0));
        View<CaseManagementDiagram> rootContent = new ViewImpl<>(root, BoundsImpl.build(0.0, 0.0, 2800.0, 1400.0));
        Node<View<CaseManagementDiagram>, Edge> rootNode = new NodeImpl<>("_0E761372-8B3C-4BE1-88BC-808D647D9EFF");
        rootNode.getLabels().addAll(root.getLabels());
        rootNode.getLabels().add("org.kie.workbench.common.stunner.cm.definition.CaseManagementDiagram");
        rootNode.setContent(rootContent);


        AdHocSubprocess stage1 = new AdHocSubprocess();
        View<AdHocSubprocess> stage1Content = new ViewImpl<>(stage1, BoundsImpl.build(0.0, 0.0, 175.0, 50.0));
        Node<View<AdHocSubprocess>, Edge> stage1Node = new NodeImpl<>("_F0A19BD0-3F42-493A-9A2D-2F4C24ED75D9");
        stage1Node.getLabels().addAll(stage1.getLabels());
        stage1Node.getLabels().add("org.kie.workbench.common.stunner.cm.definition.AdHocSubprocess");
        stage1Node.setContent(stage1Content);

        Edge<Child, Node> stage1InEdge = new EdgeImpl<>("_CF1684C2-3D30-4FDE-A5AA-D88B81E08418");
        stage1InEdge.setSourceNode(rootNode);
        stage1InEdge.setTargetNode(stage1Node);
        stage1InEdge.setContent(new Child());
        rootNode.getOutEdges().add(stage1InEdge);
        stage1Node.getInEdges().add(stage1InEdge);


        UserTask task1 = new UserTask();
        View<UserTask> task1Content = new ViewImpl<>(task1, BoundsImpl.build(0.0, 0.0, 153.0, 103.0));
        Node<View<UserTask>, Edge> task1Node = new NodeImpl<>("_E95AD08A-4595-4FA4-8948-3318D8BE7941");
        task1Node.getLabels().addAll(task1.getLabels());
        task1Node.getLabels().add("org.kie.workbench.common.stunner.bpmn.definition.UserTask");
        task1Node.setContent(task1Content);

        Edge<Child, Node> task1InEdge = new EdgeImpl<>("_B24CB4A4-93A0-4BC0-87A5-BD3968CC184F");
        task1InEdge.setSourceNode(stage1Node);
        task1InEdge.setTargetNode(task1Node);
        task1InEdge.setContent(new Child());
        stage1Node.getOutEdges().add(task1InEdge);
        task1Node.getInEdges().add(task1InEdge);


        CaseReusableSubprocess case1 = new CaseReusableSubprocess();
        View<CaseReusableSubprocess> case1Content = new ViewImpl<>(case1, BoundsImpl.build(0.0, 0.0, 153.0, 103.0));
        Node<View<CaseReusableSubprocess>, Edge> case1Node = new NodeImpl<>("_C468418F-A1EE-470A-BC30-D85888DF3DF7");
        case1Node.getLabels().addAll(case1.getLabels());
        case1Node.getLabels().add("org.kie.workbench.common.stunner.cm.definition.CaseReusableSubprocess");
        case1Node.setContent(case1Content);

        Edge<Child, Node> case1InEdge = new EdgeImpl<>("_17571CDC-9736-4110-B7EB-27C0EA959AA0");
        case1InEdge.setSourceNode(stage1Node);
        case1InEdge.setTargetNode(case1Node);
        case1InEdge.setContent(new Child());
        stage1Node.getOutEdges().add(case1InEdge);
        case1Node.getInEdges().add(case1InEdge);


        AdHocSubprocess stage2 = new AdHocSubprocess();
        View<AdHocSubprocess> stage2Content = new ViewImpl<>(stage2, BoundsImpl.build(0.0, 0.0, 175.0, 50.0));
        Node<View<AdHocSubprocess>, Edge> stage2Node = new NodeImpl<>("_BCD8C7E1-9833-407D-9833-E12763A9A63D");
        stage2Node.getLabels().addAll(stage2.getLabels());
        stage2Node.getLabels().add("org.kie.workbench.common.stunner.cm.definition.AdHocSubprocess");
        stage2Node.setContent(stage2Content);

        Edge<Child, Node> stage2InEdge = new EdgeImpl<>("_2FBF1046-A4BA-4407-A68B-20F2A5DEB5A4");
        stage2InEdge.setSourceNode(rootNode);
        stage2InEdge.setTargetNode(stage2Node);
        stage2InEdge.setContent(new Child());
        rootNode.getOutEdges().add(stage2InEdge);
        stage2Node.getInEdges().add(stage2InEdge);


        CaseReusableSubprocess case2 = new CaseReusableSubprocess();
        View<CaseReusableSubprocess> case2Content = new ViewImpl<>(case2, BoundsImpl.build(0.0, 0.0, 153.0, 103.0));
        Node<View<CaseReusableSubprocess>, Edge> case2Node = new NodeImpl<>("_4DF08597-2D2D-4CEE-B0EF-1AF0ED4ADAC2");
        case2Node.getLabels().addAll(case2.getLabels());
        case2Node.getLabels().add("org.kie.workbench.common.stunner.cm.definition.CaseReusableSubprocess");
        case2Node.setContent(case2Content);

        Edge<Child, Node> case2InEdge = new EdgeImpl<>("_A8727CFC-E58C-4876-BCDD-E4C75FDD1252");
        case2InEdge.setSourceNode(stage2Node);
        case2InEdge.setTargetNode(case2Node);
        case2InEdge.setContent(new Child());
        stage2Node.getOutEdges().add(case2InEdge);
        case2Node.getInEdges().add(case2InEdge);


        ProcessReusableSubprocess process2 = new ProcessReusableSubprocess();
        View<ProcessReusableSubprocess> process2Content = new ViewImpl<>(process2, BoundsImpl.build(0.0, 0.0, 153.0, 103.0));
        Node<View<ProcessReusableSubprocess>, Edge> process2Node = new NodeImpl<>("_438D1DB6-4161-43C5-86F5-FC6B0F97BA7B");
        process2Node.getLabels().addAll(process2.getLabels());
        process2Node.getLabels().add("org.kie.workbench.common.stunner.cm.definition.CaseReusableSubprocess");
        process2Node.setContent(process2Content);

        Edge<Child, Node> process2InEdge = new EdgeImpl<>("_8B517D71-EC7A-441C-91EB-5AF86BC11974");
        process2InEdge.setSourceNode(stage2Node);
        process2InEdge.setTargetNode(process2Node);
        process2InEdge.setContent(new Child());
        stage2Node.getOutEdges().add(process2InEdge);
        process2Node.getInEdges().add(process2InEdge);


        DefinitionSet definitionSet = new DefinitionSetImpl("org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet");
        //definitionSet.setBounds(BoundsImpl.build(0.0, 0.0, 2800.0, 1400.0));

        Graph graph = new GraphImpl<>("_E0752AEB-6594-483D-9757-F147960EA60A", new GraphNodeStoreImpl());
        graph.getLabels().add("org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet");
        graph.setContent(definitionSet);
        graph.addNode(rootNode);
        graph.addNode(stage1Node);
        graph.addNode(stage2Node);
        graph.addNode(task1Node);
        graph.addNode(case1Node);
        graph.addNode(case2Node);
        graph.addNode(process2Node);


        MetadataImpl metaData = new MetadataImpl();
        metaData.setDefinitionSetId("org.kie.workbench.common.stunner.cm.CaseManagementDefinitionSet");
        metaData.setTitle("New Case Management diagram");
        metaData.setShapeSetId("org.kie.workbench.common.stunner.cm.client.CaseManagementShapeSet");
        metaData.setCanvasRootUUID("_0E761372-8B3C-4BE1-88BC-808D647D9EFF");

        DiagramImpl diagram = new DiagramImpl("_D518B746-92D2-4BF1-8AD1-1EBA552C5F6F", metaData);
        diagram.setGraph(graph);

        return diagram;
    }

    private void hasElement(String result, String elementPattern) {
        hasElement(result, elementPattern, 1);
    }

    private void hasElement(String result, String elementPattern, int count) {
        Pattern pattern = Pattern.compile(elementPattern);
        Matcher matcher = pattern.matcher(result);

        for (int i = 0; i < count; i++) {
            assertTrue(matcher.find());
        }

        assertFalse(matcher.find());
    }

    @Test
    public void testUnmarshall() throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CM_FILE)) {
            Metadata metadata =
                    new MetadataImpl.MetadataImplBuilder(
                            BindableAdapterUtils.getDefinitionSetId(CaseManagementDefinitionSet.class)).build();

            Graph<DefinitionSet, Node> graph = tested.unmarshall(metadata, inputStream);

            Node<View<CaseManagementDiagram>, Edge> root = StreamSupport.stream(graph.nodes().spliterator(), false)
                    .filter(node -> ((View) node.getContent()).getDefinition() instanceof CaseManagementDiagram)
                    .findAny().get();
            assertEquals(2, root.getOutEdges().size());

            Node<View<AdHocSubprocess>, Edge> stage1 = root.getOutEdges().get(0).getTargetNode();
            assertEquals(1, stage1.getInEdges().size());
            assertEquals(2, stage1.getOutEdges().size());
            assertTrue(stage1.getContent().getDefinition() instanceof AdHocSubprocess);

            Node<View<UserTask>, Edge> task1 = stage1.getOutEdges().get(0).getTargetNode();
            assertEquals(1, task1.getInEdges().size());
            assertEquals(0, task1.getOutEdges().size());
            assertTrue(task1.getContent().getDefinition() instanceof UserTask);

            Node<View<CaseReusableSubprocess>, Edge> case1 = stage1.getOutEdges().get(1).getTargetNode();
            assertEquals(1, case1.getInEdges().size());
            assertEquals(0, case1.getOutEdges().size());
            assertTrue(case1.getContent().getDefinition() instanceof CaseReusableSubprocess);
            assertTrue(case1.getContent().getDefinition().getExecutionSet().getCaze().getValue());

            Node<View<AdHocSubprocess>, Edge> stage2 = root.getOutEdges().get(1).getTargetNode();
            assertEquals(1, stage2.getInEdges().size());
            assertEquals(2, stage2.getOutEdges().size());
            assertTrue(stage2.getContent().getDefinition() instanceof AdHocSubprocess);

            Node<View<CaseReusableSubprocess>, Edge> case2 = stage2.getOutEdges().get(0).getTargetNode();
            assertEquals(1, case2.getInEdges().size());
            assertEquals(0, case2.getOutEdges().size());
            assertTrue(case2.getContent().getDefinition() instanceof CaseReusableSubprocess);
            assertTrue(case2.getContent().getDefinition().getExecutionSet().getCaze().getValue());

            Node<View<ProcessReusableSubprocess>, Edge> process2 = stage2.getOutEdges().get(1).getTargetNode();
            assertEquals(1, process2.getInEdges().size());
            assertEquals(0, process2.getOutEdges().size());
            assertTrue(process2.getContent().getDefinition() instanceof ProcessReusableSubprocess);
            assertFalse(process2.getContent().getDefinition().getExecutionSet().getCaze().getValue());
        }
    }

    @Test
    public void testCreateFromStunnerConverterFactory() throws Exception {
        assertTrue(tested.createFromStunnerConverterFactory(new GraphImpl("x", new GraphNodeStoreImpl()), new CaseManagementPropertyWriterFactory())
                           instanceof org.kie.workbench.common.stunner.cm.backend.converters.fromstunner.CaseManagementConverterFactory);
    }

    @Test
    public void testCreateToStunnerConverterFactory() throws Exception {
        Definitions definitions = bpmn2.createDefinitions();
        definitions.getRootElements().add(bpmn2.createProcess());
        BPMNDiagram bpmnDiagram = di.createBPMNDiagram();
        bpmnDiagram.setPlane(di.createBPMNPlane());
        definitions.getDiagrams().add(bpmnDiagram);

        DefinitionResolver definitionResolver = new DefinitionResolver(definitions, Collections.emptyList());

        FactoryManager factoryManager = mock(FactoryManager.class);

        TypedFactoryManager typedFactoryManager = new TypedFactoryManager(factoryManager);

        assertTrue(tested.createToStunnerConverterFactory(definitionResolver, typedFactoryManager)
                           instanceof org.kie.workbench.common.stunner.cm.backend.converters.tostunner.CaseManagementConverterFactory);
    }

    @Test
    public void testCreatePropertyWriterFactory() throws Exception {
        assertTrue(tested.createPropertyWriterFactory() instanceof CaseManagementPropertyWriterFactory);
    }

    @Test
    public void testGetDefinitionSetClass() throws Exception {
        assertEquals(tested.getDefinitionSetClass(), CaseManagementDefinitionSet.class);
    }
}
