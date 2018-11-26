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

package org.kie.workbench.common.stunner.bpmn.forms.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.workbench.common.stunner.core.util.HashUtil;

@Portable
public class TypeMetadataQueryResult {

    private List<TypeMetadata> typeMetadatas = new ArrayList<>();

    private List<String> missingTypes = new ArrayList<>();

    public TypeMetadataQueryResult(final @MapsTo("typeMetadatas") List<TypeMetadata> typeMetadatas,
                                   final @MapsTo("missingTypes") List<String> missingTypes) {
        this.typeMetadatas = typeMetadatas;
        this.missingTypes = missingTypes;
    }

    public List<String> getMissingTypes() {
        return missingTypes;
    }

    public void setMissingTypes(List<String> missingTypes) {
        this.missingTypes = missingTypes;
    }

    public List<TypeMetadata> getTypeMetadatas() {
        return typeMetadatas;
    }

    public void setTypeMetadatas(List<TypeMetadata> typeMetadatas) {
        this.typeMetadatas = typeMetadatas;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(Objects.hashCode(missingTypes),
                                         Objects.hashCode(typeMetadatas));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TypeMetadataQueryResult) {
            TypeMetadataQueryResult other = (TypeMetadataQueryResult) o;
            return Objects.equals(missingTypes, other.missingTypes) &&
                    Objects.equals(typeMetadatas, other.typeMetadatas);
        }
        return false;
    }
}