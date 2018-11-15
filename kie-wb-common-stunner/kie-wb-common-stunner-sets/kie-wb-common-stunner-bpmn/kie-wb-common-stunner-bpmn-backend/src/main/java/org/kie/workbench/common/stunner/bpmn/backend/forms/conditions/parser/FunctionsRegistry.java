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

package org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;

public class FunctionsRegistry {

    private static FunctionsRegistry instance = new FunctionsRegistry();

    private Map<String, FunctionDef> registry = new TreeMap<>();

    private FunctionsRegistry() {
        initRegistry();
    }

    public static FunctionsRegistry getInstance() {
        if (instance == null) {
            instance = new FunctionsRegistry();
        }
        return instance;
    }

    public FunctionDef getFunction(String functionName) {
        return registry.get(functionName);
    }

    public Collection<FunctionDef> getFunctions() {
        return Collections.unmodifiableCollection(registry.values());
    }

    private void initRegistry() {

        //Operators for all types:

        FunctionDef isNull = FunctionDef.FunctionDefBuilder.newFunction(Condition.IS_NULL)
                .withParam("param1", Object.class.getName())
                .build();
        registry.put(isNull.getName(),
                     isNull);

        //Global operators:

        FunctionDef equalsTo = FunctionDef.FunctionDefBuilder.newFunction(Condition.EQUALS_TO)
                .withParam("param1", Object.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(equalsTo.getName(), equalsTo);

        //Operators for String type:

        FunctionDef isEmpty = FunctionDef.FunctionDefBuilder.newFunction(Condition.IS_EMPTY)
                .withParam("param1", String.class.getName())
                .build();
        registry.put(isEmpty.getName(), isEmpty);

        FunctionDef contains = FunctionDef.FunctionDefBuilder.newFunction(Condition.CONTAINS)
                .withParam("param1", String.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(contains.getName(), contains);

        FunctionDef startsWith = FunctionDef.FunctionDefBuilder.newFunction(Condition.STARTS_WITH)
                .withParam("param1", String.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(startsWith.getName(), startsWith);

        FunctionDef endsWith = FunctionDef.FunctionDefBuilder.newFunction(Condition.ENDS_WITH)
                .withParam("param1", String.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(endsWith.getName(), endsWith);

        // Operators for Numeric types:

        FunctionDef greaterThan = FunctionDef.FunctionDefBuilder.newFunction(Condition.GREATER_THAN)
                .withParam("param1", Number.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(greaterThan.getName(), greaterThan);

        FunctionDef greaterOrEqualThan = FunctionDef.FunctionDefBuilder.newFunction(Condition.GREATER_OR_EQUAL_THAN)
                .withParam("param1", Number.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(greaterOrEqualThan.getName(), greaterOrEqualThan);

        FunctionDef lessThan = FunctionDef.FunctionDefBuilder.newFunction(Condition.LESS_THAN)
                .withParam("param1", Number.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(lessThan.getName(), lessThan);

        FunctionDef lessOrEqualThan = FunctionDef.FunctionDefBuilder.newFunction(Condition.LESS_OR_EQUAL_THAN)
                .withParam("param1", Number.class.getName())
                .withParam("param2", String.class.getName())
                .build();
        registry.put(lessOrEqualThan.getName(), lessOrEqualThan);

        FunctionDef between = FunctionDef.FunctionDefBuilder.newFunction(Condition.BETWEEN)
                .withParam("param1", Number.class.getName())
                .withParam("param2", String.class.getName())
                .withParam("param3", String.class.getName())
                .build();
        registry.put(between.getName(), between);

        // Operators for Boolean type:

        FunctionDef isTrue = FunctionDef.FunctionDefBuilder.newFunction(Condition.IS_TRUE)
                .withParam("param1", Boolean.class.getName())
                .build();
        registry.put(isTrue.getName(), isTrue);

        FunctionDef isFalse = FunctionDef.FunctionDefBuilder.newFunction(Condition.IS_FALSE)
                .withParam("param1", Boolean.class.getName())
                .build();
        registry.put(isFalse.getName(), isFalse);
    }
}
