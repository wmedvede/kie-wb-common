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

import java.text.ParseException;
import java.util.List;

import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.binaryFunctions;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.buildBinaryConditionScripts;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.buildTernaryConditionScripts;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.buildUnaryConditionScripts;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.failingBinaryFunctionExpressions;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.failingTernaryFunctionExpressions;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.failingUnaryFunctionExpressions;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.stringParams;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.ternaryFunctions;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.unaryFunctions;
import static org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionTestCommons.variableParams;

public class ConditionParserTest {

    @Test
    public void testUnaryFunctionsParsingSuccessful() throws Exception {
        for (String function : unaryFunctions) {
            testUnaryFunctionParsingSuccessful(function);
        }
    }

    @Test
    public void testBinaryFunctionsParsingSuccessful() throws Exception {
        for (String function : binaryFunctions) {
            testBinaryFunctionParsingSuccessful(function);
        }
    }

    @Test
    public void testTernaryFunctionsParsingSuccessful() throws Exception {
        for (String function : ternaryFunctions) {
            testTernaryFunctionParsingSuccessful(function);
        }
    }

    @Test
    public void testUnaryFunctionsParsingUnsuccessful() throws Exception {
        for (String function : unaryFunctions) {
            testFunctionParsingUnsuccessful(function, failingUnaryFunctionExpressions);
        }
    }

    @Test
    public void testBinaryFunctionsParsingUnsuccessful() throws Exception {
        for (String function : binaryFunctions) {
            testFunctionParsingUnsuccessful(function, failingBinaryFunctionExpressions);
        }
    }

    @Test
    public void testTernaryFunctionsParsingUnsuccessful() throws Exception {
        for (String function : ternaryFunctions) {
            testFunctionParsingUnsuccessful(function, failingTernaryFunctionExpressions);
        }
    }

    private void testUnaryFunctionParsingSuccessful(String function) throws Exception {
        List<String> conditions = buildUnaryConditionScripts(function);
        for (int i = 0; i < conditions.size(); i++) {
            ConditionParser parser = new ConditionParser(conditions.get(i));
            Condition condition = parser.parse();
            assertEquals(function, condition.getFunction());
            assertEquals(1, condition.getParams().size());
            assertEquals(variableParams.get(i), condition.getParams().get(0));
        }
    }

    private void testBinaryFunctionParsingSuccessful(String function) throws Exception {
        List<String> conditions = buildBinaryConditionScripts(function);
        for (int i = 0; i < conditions.size(); i++) {
            ConditionParser parser = new ConditionParser(conditions.get(i));
            Condition condition = parser.parse();
            assertEquals(function, condition.getFunction());
            assertEquals(2, condition.getParams().size());
            assertEquals(variableParams.get(i), condition.getParams().get(0));
            assertEquals(stringParams.get(i), condition.getParams().get(1));
        }
    }

    private void testTernaryFunctionParsingSuccessful(String function) throws Exception {
        List<String> conditions = buildTernaryConditionScripts(function);
        for (int i = 0; i < conditions.size(); i++) {
            ConditionParser parser = new ConditionParser(conditions.get(i));
            Condition condition = parser.parse();
            assertEquals(function, condition.getFunction());
            assertEquals(3, condition.getParams().size());
            assertEquals(variableParams.get(i), condition.getParams().get(0));
            assertEquals(stringParams.get(i), condition.getParams().get(1));
            assertEquals(stringParams.get(i), condition.getParams().get(2));
        }
    }

    private void testFunctionParsingUnsuccessful(String function, List<String> expressionsSet) {
        for (int i = 0; i < expressionsSet.size(); i++) {
            String expression = String.format(expressionsSet.get(i), "KieFunctions." + function);
            ConditionParser parser = new ConditionParser(expression);
            try {
                parser.parse();
                fail("A parsing error was expected for expression at position " + i + ": " + expression);
            } catch (ParseException e) {
            }
        }
    }
}
