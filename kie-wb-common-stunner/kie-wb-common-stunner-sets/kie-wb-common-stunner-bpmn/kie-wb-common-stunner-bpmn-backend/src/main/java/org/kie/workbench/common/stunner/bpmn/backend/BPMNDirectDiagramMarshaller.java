/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import bpsim.impl.BpsimPackageImpl;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.impl.DroolsPackageImpl;
import org.kie.workbench.common.stunner.bpmn.BPMNDefinitionSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.processes.ProcessConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceFactoryImpl;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceImpl;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.backend.service.XMLEncoderDiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.EmptyRulesCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.processing.index.map.MapIndexBuilder;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Direct as in "skipping json encoding"
 *
 */
@Dependent
public class BPMNDirectDiagramMarshaller implements DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNDirectDiagramMarshaller.class);

    private final XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller;
    private final DefinitionManager definitionManager;
    private final RuleManager ruleManager;
    private final TypedFactoryManager typedFactoryManager;
    private final GraphCommandFactory commandFactory;
    private final GraphCommandManager commandManager;
    private final BPMNDiagramMarshaller legacyMarshaller;

    public BPMNDirectDiagramMarshaller(
            final DefinitionManager definitionManager,
            final RuleManager ruleManager,
            final FactoryManager factoryManager,
            final GraphCommandFactory commandFactory,
            final GraphCommandManager commandManager) {
        this.definitionManager = definitionManager;
        this.ruleManager = ruleManager;
        this.typedFactoryManager = new TypedFactoryManager(factoryManager);
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;

        // these are not used in tests
        this.legacyMarshaller = null;
        this.diagramMetadataMarshaller = null;
    }

    @Inject
    public BPMNDirectDiagramMarshaller(
            final XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller,
            final DefinitionManager definitionManager,
            final RuleManager ruleManager,
            final FactoryManager factoryManager,
            final GraphCommandFactory commandFactory,
            final GraphCommandManager commandManager,
            final BPMNDiagramMarshaller legacyMarshaller) {
        this.diagramMetadataMarshaller = diagramMetadataMarshaller;
        this.definitionManager = definitionManager;
        this.ruleManager = ruleManager;
        this.typedFactoryManager = new TypedFactoryManager(factoryManager);
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
        this.legacyMarshaller = legacyMarshaller;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String marshall(final Diagram diagram) throws IOException {
        return legacyMarshaller.marshall(diagram);
    }

    @Override
    public Graph<DefinitionSet, Node> unmarshall(final Metadata metadata,
                                                 final InputStream inputStream) throws IOException {
        LOG.debug("Starting diagram unmarshalling...");

        Definitions definitions = parseDefinitions(inputStream);
        String definitionsId = definitions.getId();

        Process process = findProcess(definitions);

        metadata.setCanvasRootUUID(definitionsId);
        metadata.setTitle(process.getName());

        Graph<DefinitionSet, Node> graph =
                typedFactoryManager.newGraph(
                        definitionsId, BPMNDefinitionSet.class);

        GraphBuildingContext context = emptyGraphContext(graph);

        PropertyReaderFactory propertyReaderFactory =
                new PropertyReaderFactory(definitions);

        ProcessConverter processConverter =
                new ProcessConverter(
                        typedFactoryManager,
                        propertyReaderFactory,
                        context);

        processConverter.convert(definitionsId, process);

        LOG.debug("Diagram unmarshalling finished successfully.");
        return graph;
    }

    public Process findProcess(Definitions definitions) {
        return (Process) definitions.getRootElements().stream()
                .filter(el -> el instanceof Process)
                .findFirst().get();
    }

    private GraphBuildingContext emptyGraphContext(Graph<DefinitionSet, Node> graph) {
        EmptyRulesCommandExecutionContext commandExecutionContext =
                new EmptyRulesCommandExecutionContext(
                        definitionManager,
                        typedFactoryManager.untyped(),
                        ruleManager,
                        new MapIndexBuilder().build(graph));

        GraphBuildingContext ctx = new GraphBuildingContext(
                commandExecutionContext, commandFactory, commandManager);

        ctx.clearGraph();

        return ctx;
    }

    @Override
    public DiagramMetadataMarshaller<Metadata> getMetadataMarshaller() {
        return diagramMetadataMarshaller;
    }

    private static Definitions parseDefinitions(final InputStream inputStream) throws IOException {
        DroolsPackageImpl.init();
        BpsimPackageImpl.init();

        final ResourceSet resourceSet = new ResourceSetImpl();
        Resource.Factory.Registry resourceFactoryRegistry = resourceSet.getResourceFactoryRegistry();
        resourceFactoryRegistry.getExtensionToFactoryMap().put(
                Resource.Factory.Registry.DEFAULT_EXTENSION, new JBPMBpmn2ResourceFactoryImpl());

        EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
        packageRegistry.put("http://www.omg.org/spec/BPMN/20100524/MODEL", Bpmn2Package.eINSTANCE);
        packageRegistry.put("http://www.jboss.org/drools", DroolsPackage.eINSTANCE);

        final JBPMBpmn2ResourceImpl resource =
                (JBPMBpmn2ResourceImpl) resourceSet
                        .createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));

        resource.getDefaultLoadOptions()
                .put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
        resource.setEncoding("UTF-8");

        final Map<String, Object> options = new HashMap<String, Object>();
        options.put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
        options.put(JBPMBpmn2ResourceImpl.OPTION_DEFER_IDREF_RESOLUTION, true);
        options.put(JBPMBpmn2ResourceImpl.OPTION_DISABLE_NOTIFY, true);
        options.put(JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF,
                    JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF_RECORD);

        resource.load(inputStream, options);

        final DocumentRoot root = (DocumentRoot) resource.getContents().get(0);

        return root.getDefinitions();
    }
}
