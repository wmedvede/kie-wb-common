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

package org.kie.workbench.common.stunner.bpmn.backend.dataproviders;

import java.util.HashMap;

import org.kie.workbench.common.forms.dynamic.model.config.LiveSearchDataProvider;
import org.kie.workbench.common.forms.dynamic.model.config.SelectorData;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;

public class LiveSearchCalledElementFormProvider implements LiveSearchDataProvider {

    @Override
    public String getProviderName() {
        return getClass().getSimpleName();
    }

    @Override
    public SelectorData search(String pattern,
                               int maxResults,
                               FormRenderingContext renderingContext) {
        HashMap<String, String> values = new HashMap<>();
        values.put("uno",
                   "UNO");
        values.put("dos",
                   "DOS");
        values.put("tres",
                   "TRES");
        SelectorData<String> selectorData = new SelectorData<>();
        selectorData.setValues(values);
        return selectorData;
    }

    @Override
    public SelectorData searchEntry(String key,
                                    FormRenderingContext context) {
        HashMap<String, String> values = new HashMap<>();
        if ("uno".equals(key)) {
            values.put("uno",
                       "UNO");
        } else if ("dos".equals(key)) {
            values.put("dos",
                       "DOS");
        } else if ("tres".equals(key)) {
            values.put("tres",
                       "TRES");
        } else {
            values = null;
        }
        SelectorData<String> selectorData = new SelectorData<>();
        selectorData.setValues(values);
        return selectorData;
    }
}
