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

package org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.lsListBox.definition;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.fields.shared.AbstractFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.lsListBox.type.LiveSearchListBoxFieldType;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FieldType;

@Portable
@Bindable
public class LiveSearchListBoxFieldDefinition extends AbstractFieldDefinition {

    public static final LiveSearchListBoxFieldType FIELD_TYPE = new LiveSearchListBoxFieldType();

    private String provider;

    public LiveSearchListBoxFieldDefinition() {
        super(String.class.getName());
    }

    @Override
    public FieldType getFieldType() {
        return FIELD_TYPE;
    }

    public String getDataProvider() {
        return provider;
    }

    public void setDataProvider(String provider) {
        this.provider = provider;
    }

    @Override
    protected void doCopyFrom(FieldDefinition other) {
        if (other instanceof LiveSearchListBoxFieldDefinition) {
            setDataProvider(((LiveSearchListBoxFieldDefinition) other).getDataProvider());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LiveSearchListBoxFieldDefinition that = (LiveSearchListBoxFieldDefinition) o;

        return provider != null ? provider.equals(that.provider) : that.provider == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = ~~result;
        return result;
    }
}
