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

public class VariableMetadata {

    private String name;

    private String type;

    private List<AccessorMetadata> accessors = new ArrayList<>();

    public VariableMetadata(String name,
                            String type) {
        this.name = name;
        this.type = type;
    }

    public VariableMetadata(String name,
                            String type,
                            List<AccessorMetadata> accessors) {
        this.name = name;
        this.type = type;
        this.accessors = accessors;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<AccessorMetadata> getAccessors() {
        return accessors;
    }
}
