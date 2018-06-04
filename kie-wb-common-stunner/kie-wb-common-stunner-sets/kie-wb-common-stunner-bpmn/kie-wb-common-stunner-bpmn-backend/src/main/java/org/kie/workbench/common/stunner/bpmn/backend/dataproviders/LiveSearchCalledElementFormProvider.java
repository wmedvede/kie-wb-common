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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.kie.workbench.common.forms.dynamic.model.config.LiveSearchDataProvider;
import org.kie.workbench.common.forms.dynamic.model.config.SelectorData;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueIndexTerm;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueResourceIndexTerm;
import org.kie.workbench.common.services.refactoring.model.query.RefactoringPageRow;
import org.kie.workbench.common.services.refactoring.service.RefactoringQueryService;
import org.kie.workbench.common.services.refactoring.service.ResourceType;
import org.kie.workbench.common.stunner.bpmn.backend.query.FindBpmnProcessIdsQuery;
import org.uberfire.backend.vfs.Path;

public class LiveSearchCalledElementFormProvider implements LiveSearchDataProvider {

    protected RefactoringQueryService queryService;

    public LiveSearchCalledElementFormProvider() {
        //weld proxying
    }

    @Inject
    public LiveSearchCalledElementFormProvider(RefactoringQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public String getProviderName() {
        return getClass().getSimpleName();
    }

    @Override
    public SelectorData search(String pattern,
                               int maxResults,
                               FormRenderingContext renderingContext) {
        //String queryPattern = pattern != null && !pattern.trim().isEmpty() ? (pattern.trim() + "*") : "";
        //String queryPattern = pattern != null ? "*" + pattern.trim() : "*";

        //TODO, WM, Continue here, for some reason if I create a project1 and process Process1, Process2
        //the search seems to not be working well project1.Process2
        //Hay algun problema aca, la busqueda tendria que ir, pero *project1.Process2* no me retorna el puto valor
        //es raro xq en la Library va bien...
        
        String queryPattern = pattern;
        final Set<ValueIndexTerm> queryTerms = new HashSet<ValueIndexTerm>() {{
            add(new ValueResourceIndexTerm(queryPattern,
                                           ResourceType.BPMN2,
                                           ValueIndexTerm.TermSearchType.WILDCARD));
        }};
        List<RefactoringPageRow> results = queryService.query(
                FindBpmnProcessIdsQuery.NAME,
                queryTerms);
        return buildResult(results);
    }

    @Override
    public SelectorData searchEntry(String key,
                                    FormRenderingContext context) {
        if (key != null) {
            final Set<ValueIndexTerm> queryTerms = new HashSet<ValueIndexTerm>() {{
                add(new ValueResourceIndexTerm(key,
                                               ResourceType.BPMN2,
                                               ValueIndexTerm.TermSearchType.NORMAL));
            }};
            List<RefactoringPageRow> results = queryService.query(
                    FindBpmnProcessIdsQuery.NAME,
                    queryTerms);
            return buildResult(results);
        } else {
            return new SelectorData();
        }
    }

    private SelectorData buildResult(List<RefactoringPageRow> results) {
        HashMap<String, String> values = new HashMap<>();
        for (RefactoringPageRow row : results) {
            Map<String, Path> mapRow = (Map<String, Path>) row.getValue();
            for (String rKey : mapRow.keySet()) {
                values.put(rKey,
                           rKey);
            }
        }
        SelectorData<String> selectorData = new SelectorData<>();
        selectorData.setValues(values);
        return selectorData;
    }
}
