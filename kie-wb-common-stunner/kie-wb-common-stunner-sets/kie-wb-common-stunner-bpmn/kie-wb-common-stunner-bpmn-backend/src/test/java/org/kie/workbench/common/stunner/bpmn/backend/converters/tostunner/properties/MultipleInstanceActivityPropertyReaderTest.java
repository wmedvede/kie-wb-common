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
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultipleInstanceActivityPropertyReaderTest {

    private static final String ITEM_ID = "ITEM_ID";
    private static final String PROPERTY_ID = "PROPERTY_ID";
    private static final String EXPRESSION = "EXPRESSION";

    private MultipleInstanceActivityPropertyReader reader;

    @Mock
    private Activity activity;

    @Mock
    private BPMNDiagram diagram;

    @Mock
    private DefinitionResolver definitionResolver;

    @Mock
    private MultiInstanceLoopCharacteristics miloop;

    @Before
    public void setUp() {
        reader = new MultipleInstanceActivityPropertyReader(activity, diagram, definitionResolver);
        when(activity.getLoopCharacteristics()).thenReturn(miloop);
    }

    @Test
    public void testGetCollectionInput() {
        ItemAwareElement item = mockItemAwareElement(ITEM_ID);
        when(miloop.getLoopDataInputRef()).thenReturn(item);
        List<DataInputAssociation> inputAssociations = Collections.singletonList(mockDataInputAssociation(ITEM_ID, PROPERTY_ID));
        when(activity.getDataInputAssociations()).thenReturn(inputAssociations);
        assertEquals(PROPERTY_ID, reader.getCollectionInput());
    }

    @Test
    public void testGetDataInput() {
        DataInput item = mockDataInput(ITEM_ID, PROPERTY_ID);
        when(miloop.getInputDataItem()).thenReturn(item);
        assertEquals(PROPERTY_ID, reader.getDataInput());
    }

    @Test
    public void testGetCollectionOutput() {
        ItemAwareElement item = mockItemAwareElement(ITEM_ID);
        when(miloop.getLoopDataOutputRef()).thenReturn(item);
        List<DataOutputAssociation> outputAssociations = Collections.singletonList(mockDataOutputAssociation(ITEM_ID, PROPERTY_ID));
        when(activity.getDataOutputAssociations()).thenReturn(outputAssociations);
        assertEquals(PROPERTY_ID, reader.getCollectionOutput());
    }

    @Test
    public void testGetDataOutput() {
        DataOutput item = mockDataOutput(ITEM_ID, PROPERTY_ID);
        when(miloop.getOutputDataItem()).thenReturn(item);
        assertEquals(PROPERTY_ID, reader.getDataOutput());
    }

    @Test
    public void getGetCompletionCondition() {
        FormalExpression expression = mock(FormalExpression.class);
        when(expression.getBody()).thenReturn(EXPRESSION);
        when(miloop.getCompletionCondition()).thenReturn(expression);
        assertEquals(EXPRESSION, reader.getCompletionCondition());
    }

    private static ItemAwareElement mockItemAwareElement(String id) {
        ItemAwareElement item = mock(ItemAwareElement.class);
        when(item.getId()).thenReturn(id);
        return item;
    }

    private static Property mockProperty(String id) {
        Property property = mock(Property.class);
        when(property.getId()).thenReturn(id);
        return property;
    }

    public static DataInputAssociation mockDataInputAssociation(String targetRef, String sourceRef) {
        DataInputAssociation inputAssociation = mock(DataInputAssociation.class);
        ItemAwareElement targetRefItem = mockItemAwareElement(targetRef);
        when(inputAssociation.getTargetRef()).thenReturn(targetRefItem);

        ItemAwareElement sourceRefItem = mockProperty(sourceRef);
        List<ItemAwareElement> sourceRefs = Collections.singletonList(sourceRefItem);
        when(inputAssociation.getSourceRef()).thenReturn(sourceRefs);
        return inputAssociation;
    }

    public static DataOutputAssociation mockDataOutputAssociation(String sourceRef, String targetRef) {
        DataOutputAssociation outputAssociation = mock(DataOutputAssociation.class);
        ItemAwareElement sourceRefItem = mockItemAwareElement(sourceRef);
        List<ItemAwareElement> sourceRefs = Collections.singletonList(sourceRefItem);
        when(outputAssociation.getSourceRef()).thenReturn(sourceRefs);

        ItemAwareElement targetRefItem = mockProperty(targetRef);
        when(outputAssociation.getTargetRef()).thenReturn(targetRefItem);
        return outputAssociation;
    }

    public static DataOutput mockDataOutput(String id, String name) {
        DataOutput dataOutput = mock(DataOutput.class);
        when(dataOutput.getId()).thenReturn(id);
        when(dataOutput.getName()).thenReturn(name);
        return dataOutput;
    }

    public static DataInput mockDataInput(String id, String name) {
        DataInput dataInput = mock(DataInput.class);
        when(dataInput.getId()).thenReturn(id);
        when(dataInput.getName()).thenReturn(name);
        return dataInput;
    }
}
