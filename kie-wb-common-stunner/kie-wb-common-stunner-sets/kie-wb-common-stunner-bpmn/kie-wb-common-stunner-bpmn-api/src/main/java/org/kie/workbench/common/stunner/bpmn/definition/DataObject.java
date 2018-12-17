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

package org.kie.workbench.common.stunner.bpmn.definition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FieldParam;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.bpmn.definition.property.background.BackgroundSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataobject.DataObjectExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.font.FontSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Category;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Labels;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;
import org.kie.workbench.common.stunner.core.util.HashUtil;

import static org.kie.workbench.common.forms.adf.engine.shared.formGeneration.processing.fields.fieldInitializers.nestedForms.AbstractEmbeddedFormsInitializer.COLLAPSIBLE_CONTAINER;
import static org.kie.workbench.common.forms.adf.engine.shared.formGeneration.processing.fields.fieldInitializers.nestedForms.AbstractEmbeddedFormsInitializer.FIELD_CONTAINER_PARAM;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class)
//TODO WM, se puede declarar esto sin morph base?
//@Morph(base = BaseSubprocess.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED,
        defaultFieldSettings = {@FieldParam(name = FIELD_CONTAINER_PARAM, value = COLLAPSIBLE_CONTAINER)}
)
public class DataObject implements BPMNViewDefinition {

    @Category
    public static final transient String category = BPMNCategories.DATA_OBJECTS;

    @Labels
    protected final Set<String> labels = new HashSet<>();

    @PropertySet
    @FormField
    @Valid
    protected BPMNGeneralSet general;

    @PropertySet
    @FormField(afterElement = "general")
    @Valid
    private DataObjectExecutionSet executionSet;

    @PropertySet
    @Valid
    protected BackgroundSet backgroundSet;

    @PropertySet
    @Valid
    protected FontSet fontSet;

    @PropertySet
    protected RectangleDimensionsSet dimensionsSet;

    public DataObject() {
        this("dataObject");
    }

    public DataObject(String label) {
        this(new BPMNGeneralSet(label),
             new DataObjectExecutionSet(),
             new BackgroundSet(),
             new FontSet(),
             new RectangleDimensionsSet());
    }

    public DataObject(final @MapsTo("general") BPMNGeneralSet general,
                      final @MapsTo("executionSet") DataObjectExecutionSet executionSet,
                      final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                      final @MapsTo("fontSet") FontSet fontSet,
                      final @MapsTo("dimensionsSet") RectangleDimensionsSet dimensionsSet) {
        this.general = general;
        this.executionSet = executionSet;
        this.backgroundSet = backgroundSet;
        this.fontSet = fontSet;
        this.dimensionsSet = dimensionsSet;
    }

    public DataObjectExecutionSet getExecutionSet() {
        return executionSet;
    }

    public void setExecutionSet(DataObjectExecutionSet executionSet) {
        this.executionSet = executionSet;
    }

    @Override
    public BPMNBaseInfo getGeneral() {
        return general;
    }

    public void setGeneral(BPMNGeneralSet general) {
        this.general = general;
    }

    @Override
    public BackgroundSet getBackgroundSet() {
        return backgroundSet;
    }

    public void setBackgroundSet(BackgroundSet backgroundSet) {
        this.backgroundSet = backgroundSet;
    }

    @Override
    public FontSet getFontSet() {
        return fontSet;
    }

    public void setFontSet(FontSet fontSet) {
        this.fontSet = fontSet;
    }

    public RectangleDimensionsSet getDimensionsSet() {
        return dimensionsSet;
    }

    public void setDimensionsSet(RectangleDimensionsSet dimensionsSet) {
        this.dimensionsSet = dimensionsSet;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(Objects.hashCode(labels),
                                         Objects.hashCode(general),
                                         Objects.hashCode(executionSet),
                                         Objects.hashCode(backgroundSet),
                                         Objects.hashCode(fontSet),
                                         Objects.hashCode(dimensionsSet));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DataObject) {
            DataObject other = (DataObject) o;
            return Objects.equals(labels, other.labels) &&
                    Objects.equals(general, other.general) &&
                    Objects.equals(executionSet, other.executionSet) &&
                    Objects.equals(backgroundSet, other.backgroundSet) &&
                    Objects.equals(fontSet, other.fontSet) &&
                    Objects.equals(dimensionsSet, other.dimensionsSet);
        }
        return false;
    }
}
