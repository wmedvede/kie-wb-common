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

import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

public class ConditionGenerator {

    public static final String PARAMETER_NULL_EMPTY = "Parameter can not be null nor empty";
    public static final String MISSING_CONDITION_ERROR = "A condition must be provided";

    public String generateScript(Condition condition) throws GenerateConditionException {
        final StringBuilder script = new StringBuilder();

        if (condition == null) {
            throw new GenerateConditionException(MISSING_CONDITION_ERROR);
        }

        if (!isValidFunction(condition.getFunction())) {
            throw new GenerateConditionException("Invalid function: " + condition.getFunction());
        }

        String function = condition.getFunction().trim();
        script.append("return ");
        script.append(ConditionParser.KIE_FUNCTIONS);
        script.append(function);
        script.append("(");
        boolean first = true;
        for (String param : condition.getParams()) {
            if (first) {
                //first parameter is always a process variable name.
                script.append(param);
                first = false;
            } else {
                //the other parameters are always string parameters.
                script.append(", ");
                script.append("\"");
                script.append(escapeJava(param));
                script.append("\"");
            }
            if (param == null || param.isEmpty()) {
                throw new GenerateConditionException(PARAMETER_NULL_EMPTY);
            }
        }
        script.append(");");
        return script.toString();
    }

    private boolean isValidFunction(String function) {
        return !FunctionsRegistry.getInstance().getFunctions(function).isEmpty();
    }
}
