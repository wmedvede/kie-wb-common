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

package org.kie.workbench.common.forms.dynamic.service.shared;

import org.kie.workbench.common.forms.dynamic.model.config.LiveSearchDataProvider;
import org.kie.workbench.common.forms.dynamic.model.config.SelectorData;

public abstract class AbstractLiveSearchDataProviderManager
        extends AbstractDataProviderManager<LiveSearchDataProvider>
        implements LiveSearchDataProviderManager {

    @Override
    public SelectorData searchFromProvider(String pattern,
                                           int maxResults,
                                           FormRenderingContext context,
                                           String provider) {
        LiveSearchDataProvider dataProvider = providers.get(provider);
        return dataProvider != null ?
                dataProvider.search(pattern,
                                    maxResults,
                                    context) : null;
    }

    @Override
    public SelectorData searchEntryFromProvider(String key,
                                                FormRenderingContext context,
                                                String provider) {
        LiveSearchDataProvider dataProvider = providers.get(provider);
        return dataProvider != null ? dataProvider.searchEntry(key,
                                                               context) : null;
    }
}
