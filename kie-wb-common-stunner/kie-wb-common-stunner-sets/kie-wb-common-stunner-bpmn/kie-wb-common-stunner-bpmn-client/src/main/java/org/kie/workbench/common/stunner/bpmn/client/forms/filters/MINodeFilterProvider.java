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

package org.kie.workbench.common.stunner.bpmn.client.forms.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;

import org.kie.workbench.common.forms.adf.engine.shared.FormElementFilter;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.forms.client.event.FormFieldChanged;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;
import org.kie.workbench.common.stunner.forms.client.formFilters.StunnerFormElementFilterProvider;

public abstract class MINodeFilterProvider<T> implements StunnerFormElementFilterProvider {

    private final SessionManager sessionManager;

    private final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent;

    public MINodeFilterProvider() {
        this(null, null);
    }

    public MINodeFilterProvider(final SessionManager sessionManager,
                                final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent) {
        this.sessionManager = sessionManager;
        this.refreshFormPropertiesEvent = refreshFormPropertiesEvent;
    }

    public abstract boolean isMultipleInstance(T value);

    @Override
    public abstract Class<T> getDefinitionType();

    @Override
    public Collection<FormElementFilter> provideFilters(String elementUUID, Object definition) {
        final List<FormElementFilter> filters = new ArrayList<>();
        final Predicate predicate = o -> isMultipleInstance((T) definition);

        filters.add(new FormElementFilter("executionSet.multipleInstanceCollectionInput", predicate));
        filters.add(new FormElementFilter("executionSet.multipleInstanceDataInput", predicate));
        filters.add(new FormElementFilter("executionSet.multipleInstanceCollectionOutput", predicate));
        filters.add(new FormElementFilter("executionSet.multipleInstanceDataOutput", predicate));
        return filters;
    }

    void onFormFieldChanged(@Observes FormFieldChanged formFieldChanged) {
        final String isMultipleInstance = "executionSet.isMultipleInstance";
        if (!Objects.equals(formFieldChanged.getName(), isMultipleInstance)) {
            return;
        }
        refreshFormPropertiesEvent.fire(new RefreshFormPropertiesEvent(sessionManager.getCurrentSession(), formFieldChanged.getUuid()));
    }
}
