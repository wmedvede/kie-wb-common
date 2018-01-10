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

package org.kie.workbench.common.stunner.bpmn.definition.property.task;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

@Portable
@Bindable
public class ScriptTypeListValue {

    private List<ScriptTypeValue> values = new ArrayList<>();

    public ScriptTypeListValue() {
    }

    public ScriptTypeListValue(@MapsTo("values") final List<ScriptTypeValue> values) {
        this.values = values;
    }

    public List<ScriptTypeValue> getValues() {
        return values;
    }

    public void setValues(List<ScriptTypeValue> values) {
        this.values = values;
    }

    public void addValue(ScriptTypeValue value) {
        this.values.add(value);
    }

    public boolean isEmpty() {
        return values == null || values.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScriptTypeListValue that = (ScriptTypeListValue) o;

        return values != null ? values.equals(that.values) : that.values == null;
    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }
}
