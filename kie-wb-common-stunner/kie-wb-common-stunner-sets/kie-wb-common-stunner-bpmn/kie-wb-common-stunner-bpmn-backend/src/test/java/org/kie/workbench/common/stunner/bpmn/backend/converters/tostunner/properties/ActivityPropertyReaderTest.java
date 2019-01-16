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

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeListValue;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivityPropertyReaderTest {

    private static final String JAVA = "java";
    private static final String JAVA_FORMAT = "http://www.java.com/java";
    private static final String SCRIPT = "SCRIPT";

    @Mock
    private Activity activity;

    @Mock
    private BPMNDiagram diagram;

    @Mock
    private DefinitionResolver definitionResolver;

    private ActivityPropertyReader reader;

    @Before
    public void setUp() {
        reader = new ActivityPropertyReader(activity, diagram, definitionResolver);
    }

    @Test
    public void testGetOnEntryScript() {
        OnEntryScriptType onEntryScript = Mockito.mock(OnEntryScriptType.class);
        when(onEntryScript.getScript()).thenReturn(SCRIPT);
        when(onEntryScript.getScriptFormat()).thenReturn(JAVA_FORMAT);
        List<OnEntryScriptType> onEntryScripts = Collections.singletonList(onEntryScript);

        List<ExtensionAttributeValue> extensions = mockExtensions(DroolsPackage.Literals.DOCUMENT_ROOT__ON_ENTRY_SCRIPT, onEntryScripts);
        when(activity.getExtensionValues()).thenReturn(extensions);

        assertScript(JAVA, SCRIPT, reader.getOnEntryAction());
    }

    @Test
    public void testGetOnExitScript() {
        OnExitScriptType onExitScript = Mockito.mock(OnExitScriptType.class);
        when(onExitScript.getScript()).thenReturn(SCRIPT);
        when(onExitScript.getScriptFormat()).thenReturn(JAVA_FORMAT);
        List<OnExitScriptType> onExitScripts = Collections.singletonList(onExitScript);

        List<ExtensionAttributeValue> extensions = mockExtensions(DroolsPackage.Literals.DOCUMENT_ROOT__ON_EXIT_SCRIPT, onExitScripts);
        when(activity.getExtensionValues()).thenReturn(extensions);

        assertScript(JAVA, SCRIPT, reader.getOnExitAction());
    }

    @Test
    public void testGetAssingmentsInfo() {
        //TODO WM, terminar este test si hay tiempo
        /*
        List<DataInput> dataInputs = new ArrayList<>();
        DataInput dataInput = MultipleInstanceActivityPropertyReaderTest.mockDataInput("INPUT_ID", "NAME");
        FeatureMap attributes = mock(FeatureMap.class);

        dataInputs.add(dataInput);
        InputOutputSpecification ioSpec = mock(InputOutputSpecification.class);
        when(ioSpec.getDataInputs()).thenReturn(dataInputs);

        List<DataOutput> dataOutputs = new ArrayList<>();
        when(ioSpec.getDataOutputs()).thenReturn(dataOutputs);

        List<DataInputAssociation> dataInputAssociations = new ArrayList<>();
        DataInputAssociation inputAssociation = mockDataInputAssociation("INPUT_ID", "A_VALUE");
        dataInputAssociations.add(inputAssociation);

        List<DataOutputAssociation> dataOutputAssociations = new ArrayList<>();

        when(activity.getIoSpecification()).thenReturn(ioSpec);
        when(activity.getDataInputAssociations()).thenReturn(dataInputAssociations);
        when(activity.getDataOutputAssociations()).thenReturn(dataOutputAssociations);

        AssignmentsInfo result = reader.getAssignmentsInfo();
        */
        int i = 0;
    }

    private static void assertScript(String expectedLanguage, String expectedScript, ScriptTypeListValue value) {
        assertEquals(1, value.getValues().size());
        assertEquals(expectedLanguage, value.getValues().get(0).getLanguage());
        assertEquals(expectedScript, value.getValues().get(0).getScript());
    }

    private static List<ExtensionAttributeValue> mockExtensions(EStructuralFeature feature, Object value) {
        FeatureMap featureMap = mock(FeatureMap.class);
        when(featureMap.get(feature, true)).thenReturn(value);
        ExtensionAttributeValue attributeValue = Mockito.mock(ExtensionAttributeValue.class);
        when(attributeValue.getValue()).thenReturn(featureMap);
        return Collections.singletonList(attributeValue);
    }
}
