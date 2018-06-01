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

package org.kie.workbench.common.forms.dynamic.backend.server.shared.impl;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.forms.dynamic.model.config.SelectorData;
import org.kie.workbench.common.forms.dynamic.service.shared.BackendLiveSearchDataProviderService;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;

@Service
@ApplicationScoped
public class BackendLiveSearchDataProviderServiceImpl implements BackendLiveSearchDataProviderService {

    private BackendLiveSearchDataProviderManager providerManager;

    @Inject
    public BackendLiveSearchDataProviderServiceImpl(BackendLiveSearchDataProviderManager providerManager) {
        this.providerManager = providerManager;
    }

    @Override
    public Map<String, String> availableProviders() {
        return providerManager.availableProviders();
    }

    @Override
    public SelectorData searchFromProvider(String pattern,
                                           int maxResults,
                                           FormRenderingContext context,
                                           String provider) {
        return providerManager.searchFromProvider(pattern,
                                                  maxResults,
                                                  context,
                                                  provider);
    }

    @Override
    public SelectorData searchEntryFromProvider(String key,
                                                FormRenderingContext context,
                                                String provider) {
        return providerManager.searchEntryFromProvider(key,
                                                       context,
                                                       provider);
    }
}
