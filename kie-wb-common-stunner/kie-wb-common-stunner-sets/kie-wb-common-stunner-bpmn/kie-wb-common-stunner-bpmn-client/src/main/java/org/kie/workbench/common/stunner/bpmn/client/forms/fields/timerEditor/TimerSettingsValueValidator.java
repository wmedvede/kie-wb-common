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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.timerEditor;

import com.google.gwt.regexp.shared.RegExp;
import org.kie.workbench.common.forms.processing.engine.handling.CustomFieldValidator;
import org.kie.workbench.common.forms.processing.engine.handling.ValidationResult;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.TimerSettingsValue;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;

public class TimerSettingsValueValidator
        implements CustomFieldValidator<TimerSettingsValue> {

    public static final String TimeDurationInvalid = "TimerSettingsValueValidator.TimeDurationInvalid";

    public static final String ISOTimeCycleInvalid = "TimerSettingsValueValidator.ISOTimeCycleInvalid";

    public static final String CronTimeCycleInvalid = "TimerSettingsValueValidator.CronTimeCycleInvalid";

    public static final String TimeDateInvalid = "TimerSettingsValueValidator.TimeDateInvalid";

    public static final String NoValueHasBeenProvided = "TimerSettingsValueValidator.NoValueHasBeenProvided";

    public static final String ISO = "none";

    public static final String CRON = "cron";

    /**
     * ISO8601-Duration
     */
    private static final String ISO_DURATION = "P(?:\\d+(?:\\.\\d+)?D)?(?:T(?:\\d+(?:\\.\\d+)?H)?(?:\\d+(?:\\.\\d+)?M)?(?:\\d+(?:\\.\\d+)?S)?)?";

    /**
     * ISO8601-Repetable Intervals
     */
    private static final String ISO_REPETABLE_INTERVAL = "(R\\d*\\/)" + ISO_DURATION;

    private static final String ISO_DATE_TIME = "^([\\+-]?\\d{4}(?!\\d{2}\\b))(-)(0[1-9]|1[0-2])(-)(0[1-9]|1[0-9]|2[0-9]|3[0-1])T(0[0-9]|1[0-9]|2[0-4])(:)([0-5][0-9])(:)([0-5][0-9])(.[0-9][0-9][0-9])?(((\\+)(0[0-9]|1[0-5]))|((\\-)(0[0-9]|1[0-8]))):([0-5][0-9])$";

    /**
     * Cron interval
     */
    private static final String CRON_INTERVAL = "(\\d+)?(\\d+?d)?(\\s)?(\\d+?h)?(\\s)?(\\d+?m)?(\\s)?(\\d+?s)?(\\s)?(\\d+?ms)?";

    private static final String EXPRESSION = "#{(.+)}";

    private static final RegExp durationExpr = RegExp.compile("^" + ISO_DURATION + "$");

    private static final RegExp repetableIntervalExpr = RegExp.compile("^" + ISO_REPETABLE_INTERVAL + "$");

    private static final RegExp cronIntervalExpr = RegExp.compile("^" + CRON_INTERVAL + "$");

    private static final RegExp expressionExpr = RegExp.compile("^" + EXPRESSION + "$");

    private static final RegExp dateTimeExpr = RegExp.compile("^" + ISO_DATE_TIME + "$");

    private ClientTranslationService translationService;

    public TimerSettingsValueValidator(final ClientTranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public ValidationResult validate(final TimerSettingsValue timerSettings) {
        String value;
        String errorMessage = null;

        if (timerSettings.getTimeDuration() != null) {
            value = timerSettings.getTimeDuration();
            if ((looksLikeExpression(value) && !isValidExpression(value)) ||
                    (!looksLikeExpression(value) && !isValidDuration(value))) {
                errorMessage = translationService.getValue(TimeDurationInvalid);
            }
        } else if (ISO.equals(timerSettings.getTimeCycleLanguage())) {
            value = timerSettings.getTimeCycle();
            if ((looksLikeExpression(value) && !isValidExpression(value)) ||
                    (!looksLikeExpression(value) && !isValidRepetableInterval(value))) {
                errorMessage = translationService.getValue(ISOTimeCycleInvalid);
            }
        } else if (CRON.equals(timerSettings.getTimeCycleLanguage())) {
            value = timerSettings.getTimeCycle();
            if ((looksLikeExpression(value) && !isValidExpression(value)) ||
                    (!looksLikeExpression(value) && !isValidCronExpression(value))) {
                errorMessage = translationService.getValue(CronTimeCycleInvalid);
            }
        } else if (timerSettings.getTimeDate() != null) {
            value = timerSettings.getTimeDate();
            if ((looksLikeExpression(value) && !isValidExpression(value)) ||
                    (!looksLikeExpression(value) && !isValidTimeDate(value))) {
                errorMessage = translationService.getValue(TimeDateInvalid);
            }
        } else {
            errorMessage = translationService.getValue(NoValueHasBeenProvided);
        }

        if (errorMessage != null) {
            return ValidationResult.error(errorMessage);
        }
        return ValidationResult.valid();
    }

    private static boolean looksLikeExpression(final String value) {
        return hasSomething(value) && (value.startsWith("#{") || value.contains("{") || value.contains("}"));
    }

    private static boolean isValidExpression(final String value) {
        return hasSomething(value) && expressionExpr.test(value) && value.length() > 3;
    }

    private static boolean isValidDuration(final String value) {
        return hasSomething(value) && durationExpr.test(value);
    }

    private static boolean isValidRepetableInterval(final String value) {
        return hasSomething(value) && repetableIntervalExpr.test(value);
    }

    private static boolean isValidCronExpression(final String value) {
        return isValidBPMNCronExpression(value) || isValidQuartzExpression(value);
    }

    private static boolean isValidBPMNCronExpression(final String value) {
        return hasSomething(value) && cronIntervalExpr.test(value) && !value.endsWith(" ");
    }

    private static boolean isValidQuartzExpression(final String value) {
        return hasSomething(value) && CronExpression.isValidExpression(value);
    }

    private static boolean isValidTimeDate(final String value) {
        return hasSomething(value) && dateTimeExpr.test(value);
    }

    private static boolean hasSomething(final String value) {
        return value != null && !value.trim().isEmpty();
    }
}