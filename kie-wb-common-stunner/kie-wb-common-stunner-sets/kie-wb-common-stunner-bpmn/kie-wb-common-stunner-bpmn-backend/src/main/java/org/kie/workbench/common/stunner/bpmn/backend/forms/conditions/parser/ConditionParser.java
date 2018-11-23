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

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParamDef;

public class ConditionParser {

    public static final String KIE_FUNCTIONS = "KieFunctions.";

    private int parseIndex = 0;

    private String expression;

    private String functionName;

    public static final String FUNCTION_NAME_NOT_RECOGNIZED_ERROR = "The function name \"{0}\" is not recognized by system.";

    public static final String FUNCTION_CALL_NOT_FOUND_ERROR = "Function call was not found, a token like \"" + KIE_FUNCTIONS + "functionName(variable, params)\" is expected.";

    public static final String VALID_FUNCTION_CALL_NOT_FOUND_ERROR = "The \"" + KIE_FUNCTIONS + "\" keyword must be followed by one of the following function names: \"{0}\"";

    public static final String CONDITION_OUT_OF_BOUNDS_ERROR = "Out of bounds error, condition has missing parameters or is not properly configured.";

    public static final String FUNCTION_CALL_NOT_CLOSED_PROPERLY_ERROR = "Function call \"{0}\" is not closed properly, character \")\" is expected.";

    public static final String SENTENCE_NOT_CLOSED_PROPERLY_ERROR = "Condition not closed properly, character \";\" is expected.";

    public static final String FIELD_NAME_EXPECTED_ERROR = "A valid field name is expected.";

    public static final String PARAMETER_DELIMITER_EXPECTED_ERROR = "Parameter delimiter \",\" is expected.";

    public static final String STRING_PARAMETER_EXPECTED_ERROR = "String parameter value like \"some value\" is expected.";

    public static final String SENTENCE_EXPECTED_AT_POSITION_ERROR = "Sentence \"{0}\" is expected at position {1}.";

    public static final String BLANK_AFTER_RETURN_EXPECTED_ERROR = "Sentence \"{0}\" must be followed by a blank space or a line break.";

    public static final String METHOD_NOT_PROPERLY_OPENED_ERROR = "Method \"{0}\" invocation is not properly opened, character \"(\" is expected.";

    public static final String METHOD_NOT_PROPERLY_CLOSED_ERROR = "Method \"{0}\" invocation is not properly closed, character \")\" is expected.";

    private static String functionNames = buildFunctionNames();

    private static final String RETURN_SENTENCE = "return";

    public ConditionParser(String expression) {
        this.expression = expression;
        this.parseIndex = expression != null ? 0 : -1;
    }

    public Condition parse() throws ParseException {
        parseReturnSentence();
        functionName = parseFunctionName();
        functionName = functionName.substring(KIE_FUNCTIONS.length(), functionName.length());
        List<FunctionDef> functionDefs = FunctionsRegistry.getInstance().getFunctions(functionName);

        if (functionDefs.isEmpty()) {
            throw new ParseException(errorMessage(FUNCTION_NAME_NOT_RECOGNIZED_ERROR, functionName), parseIndex);
        }

        ParseException lastTryException = null;
        for (FunctionDef functionDef : functionDefs) {
            try {
                reset();
                return parse(functionDef);
            } catch (ParseException e) {
                lastTryException = e;
            }
        }
        throw lastTryException;
    }

    private Condition parse(FunctionDef functionDef) throws ParseException {
        parseReturnSentence();

        functionName = parseFunctionName();
        functionName = functionName.substring(KIE_FUNCTIONS.length(), functionName.length());

        Condition condition = new Condition(functionName);
        String param;
        String[] variableParam;
        boolean first = true;

        for (ParamDef paramDef : functionDef.getParams()) {
            if (first) {
                variableParam = parseVariableParam();
                param = variableParam[0] + (variableParam[1] != null ? ("." + variableParam[1]) : "");
                first = false;
            } else {
                parseParamDelimiter();
                param = parseStringParameter();
            }
            condition.addParam(param);
        }

        //all parameters were consumed
        parseFunctionClose();
        parseSentenceClose();

        return condition;
    }

    private void reset() {
        parseIndex = 0;
        functionName = null;
    }

    private String parseReturnSentence() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || !expression.startsWith(RETURN_SENTENCE, index)) {
            throw new ParseException(errorMessage(SENTENCE_EXPECTED_AT_POSITION_ERROR, RETURN_SENTENCE, parseIndex), parseIndex);
        }
        setParseIndex(index + RETURN_SENTENCE.length());

        //next character after return must be a \n or a " "
        if (!isBlank(expression.charAt(parseIndex))) {
            throw new ParseException(errorMessage(BLANK_AFTER_RETURN_EXPECTED_ERROR, RETURN_SENTENCE), parseIndex);
        }

        return RETURN_SENTENCE;
    }

    private String parseFunctionName() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || !expression.startsWith(KIE_FUNCTIONS, index)) {
            throw new ParseException(errorMessage(FUNCTION_CALL_NOT_FOUND_ERROR), parseIndex);
        }

        for (FunctionDef functionDef : FunctionsRegistry.getInstance().getFunctions()) {
            if (expression.startsWith(KIE_FUNCTIONS + functionDef.getName() + "(", index)) {
                functionName = KIE_FUNCTIONS + functionDef.getName();
                break;
            }
        }

        if (functionName == null) {
            throw new ParseException(errorMessage(VALID_FUNCTION_CALL_NOT_FOUND_ERROR, functionNames()), parseIndex);
        }

        setParseIndex(index + functionName.length() + 1);
        return functionName;
    }

    private String parseFunctionClose() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ')') {
            throw new ParseException(errorMessage(FUNCTION_CALL_NOT_CLOSED_PROPERLY_ERROR, functionName), parseIndex);
        }
        setParseIndex(index + 1);
        return ")";
    }

    private String parseSentenceClose() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ';') {
            throw new ParseException(errorMessage(SENTENCE_NOT_CLOSED_PROPERLY_ERROR), parseIndex);
        }

        parseIndex = index + 1;
        while (parseIndex < expression.length()) {
            if (!isBlank(expression.charAt(parseIndex))) {
                throw new ParseException(errorMessage(SENTENCE_NOT_CLOSED_PROPERLY_ERROR),
                                         parseIndex);
            }
            parseIndex++;
        }
        return ";";
    }

    private String[] parseVariableParam() throws ParseException {
        String[] result = new String[2];
        String variableName = parseVariableName();
        String methodName = null;
        if (expression.charAt(parseIndex) == '.') {
            setParseIndex(parseIndex + 1);
            methodName = parseMethodName();
        }
        result[0] = variableName;
        result[1] = methodName;
        return result;
    }

    private String parseVariableName() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) {
            throw new ParseException(errorMessage(FIELD_NAME_EXPECTED_ERROR), parseIndex);
        }
        String result = ParsingUtils.parseJavaName(expression, index, new char[]{' ', '.', ',', ')'});
        setParseIndex(index + result.length());
        return result;
    }

    private String parseMethodName() throws ParseException {
        String result = ParsingUtils.parseJavaName(expression, parseIndex, new char[]{'('});
        setParseIndex(parseIndex + result.length());
        if (expression.charAt(parseIndex) != '(') {
            throw new ParseException(errorMessage(METHOD_NOT_PROPERLY_OPENED_ERROR, result), parseIndex);
        }
        setParseIndex(parseIndex + 1);
        if (expression.charAt(parseIndex) != ')') {
            throw new ParseException(errorMessage(METHOD_NOT_PROPERLY_CLOSED_ERROR, result), parseIndex);
        }
        setParseIndex(parseIndex + 1);
        return result;
    }

    private String parseParamDelimiter() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ',') {
            throw new ParseException(errorMessage(PARAMETER_DELIMITER_EXPECTED_ERROR), parseIndex);
        }
        setParseIndex(index + 1);
        return ",";
    }

    private String parseStringParameter() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != '"') {
            throw new ParseException(STRING_PARAMETER_EXPECTED_ERROR, parseIndex);
        }

        int shift = 1;
        Character scapeChar = Character.valueOf('\\');
        Character last = null;
        boolean strReaded = false;
        StringBuilder param = new StringBuilder();
        for (int i = index + 1; i < expression.length(); i++) {
            if (expression.charAt(i) == '\\') {
                if (scapeChar.equals(last)) {
                    shift += 2;
                    param.append('\\');
                    last = null;
                } else {
                    last = expression.charAt(i);
                }
            } else if (expression.charAt(i) == '"') {
                if (scapeChar.equals(last)) {
                    shift += 2;
                    param.append('"');
                    last = null;
                } else {
                    shift++;
                    strReaded = true;
                    break;
                }
            } else if (expression.charAt(i) == 'n') {
                if (scapeChar.equals(last)) {
                    shift += 2;
                    param.append('\n');
                } else {
                    shift += 1;
                    param.append(expression.charAt(i));
                }
                last = null;
            } else {
                if (last != null) {
                    shift++;
                    param.append(last);
                }
                last = null;
                shift++;
                param.append(expression.charAt(i));
            }
        }

        if (!strReaded) {
            throw new ParseException(STRING_PARAMETER_EXPECTED_ERROR, parseIndex);
        }
        setParseIndex(index + shift);
        return param.toString();
    }

    private int nextNonBlank() {
        if (parseIndex < 0) {
            return -1;
        }
        for (int i = parseIndex; i < expression.length(); i++) {
            if (!isBlank(expression.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isBlank(Character character) {
        return character != null && (character.equals('\n') || character.equals(' '));
    }

    private String errorMessage(String message, Object... params) {
        return MessageFormat.format(message, params);
    }

    private void setParseIndex(int parseIndex) throws ParseException {
        if (parseIndex > expression.length()) {
            throw new ParseException(errorMessage(CONDITION_OUT_OF_BOUNDS_ERROR, functionName), parseIndex);
        }
        this.parseIndex = parseIndex;
    }

    private String functionNames() {
        return functionNames;
    }

    private static String buildFunctionNames() {
        String functionNames = FunctionsRegistry.getInstance().getFunctions().stream()
                .map(FunctionDef::getName)
                .collect(Collectors.joining(", "));
        return "{" + functionNames + "}";
    }
}