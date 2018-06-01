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
package org.kie.workbench.common.stunner.bpmn.definition.property.task;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.lsListBox.type.LiveSearchListBoxFieldType;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNPropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.util.HashUtil;

@Portable
@Bindable
@PropertySet
@FormDefinition(
        startElement = "calledElement"
)
public class ReusableSubprocessTaskExecutionSet implements BPMNPropertySet {

    @Property
    @SelectorDataProvider(
            type = SelectorDataProvider.ProviderType.REMOTE,
            className = "org.kie.workbench.common.stunner.bpmn.backend.dataproviders.LiveSearchCalledElementFormProvider")
    @FormField(
            type = LiveSearchListBoxFieldType.class
    )
    @Valid
    protected CalledElement calledElement;

    @Property
    @FormField(
            afterElement = "calledElement"
    )
    @Valid
    private Independent independent;

    @Property
    @FormField(
            afterElement = "independent"
    )
    @Valid
    private WaitForCompletion waitForCompletion;

    @Property
    @FormField(
            afterElement = "waitForCompletion"
    )
    @Valid
    private IsAsync isAsync;

    public ReusableSubprocessTaskExecutionSet() {
        this(new CalledElement(),
             new Independent(),
             new WaitForCompletion(),
             new IsAsync());
    }

    public ReusableSubprocessTaskExecutionSet(final @MapsTo("calledElement") CalledElement calledElement,
                                              final @MapsTo("independent") Independent independent,
                                              final @MapsTo("waitForCompletion") WaitForCompletion waitForCompletion,
                                              final @MapsTo("isAsync") IsAsync isAsync) {
        this.calledElement = calledElement;
        this.independent = independent;
        this.waitForCompletion = waitForCompletion;
        this.isAsync = isAsync;
    }

    public CalledElement getCalledElement() {
        return calledElement;
    }

    public Independent getIndependent() {
        return independent;
    }

    public WaitForCompletion getWaitForCompletion() {
        return waitForCompletion;
    }

    public void setCalledElement(final CalledElement calledElement) {
        this.calledElement = calledElement;
    }

    public void setIndependent(final Independent independent) {
        this.independent = independent;
    }

    public void setWaitForCompletion(final WaitForCompletion waitForCompletion) {
        this.waitForCompletion = waitForCompletion;
    }

    public IsAsync getIsAsync() {
        return isAsync;
    }

    public void setIsAsync(IsAsync isAsync) {
        this.isAsync = isAsync;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(calledElement.hashCode(),
                                         independent.hashCode(),
                                         waitForCompletion.hashCode(),
                                         isAsync.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ReusableSubprocessTaskExecutionSet) {
            ReusableSubprocessTaskExecutionSet other = (ReusableSubprocessTaskExecutionSet) o;
            return calledElement.equals(other.calledElement) &&
                    independent.equals(other.independent) &&
                    waitForCompletion.equals(other.waitForCompletion) &&
                    isAsync.equals(other.isAsync);
        }
        return false;
    }
}
