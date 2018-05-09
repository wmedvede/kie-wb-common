/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.client.resources;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Window;
import org.kie.workbench.common.stunner.core.client.shape.ImageDataUriGlyph;
import org.kie.workbench.common.stunner.core.definition.shape.Glyph;

public class BPMNSVGGlyphFactory {

    private static final Map<String, Glyph> ieGlyphs = new HashMap<>();
    private static final Map<String, Glyph> allBrowserGlyphs = new HashMap<>();
    private static boolean isIE11 = false;

    public enum Glyphs {
        NONE_TASK_GLYPH,
        USER_TASK_GLYPH,
        SCRIPT_TASK_GLYPH,
        BUSINESS_RULE_TASK_GLYPH,
        PARALLEL_MULTIPLE_GATEWAY_GLYPH,
        EXCLUSIVE_GATEWAY_GLYPH,
        INCLUSIVE_GATEWAY_GLYPH,
        START_NONE_EVENT_GLYPH,
        START_SIGNAL_EVENT_GLYPH,
        START_TIMER_EVENT_GLYPH,
        START_MESSAGE_EVENT_GLYPH,
        START_ERROR_EVENT_GLYPH,
        END_NONE_EVENT_GLYPH,
        END_SIGNAL_EVENT_GLYPH,
        END_MESSAGE_EVENT_GLYPH,
        END_TERMINATE_EVENT_GLYPH,
        END_ERROR_EVENT_GLYPH,
        INTERMEDIATE_MESSAGE_EVENT_GLYPH,
        INTERMEDIATE_SIGNAL_EVENT_GLYPH,
        INTERMEDIATE_TIMER_EVENT_GLYPH,
        INTERMEDIATE_ERROR_EVENT_GLYPH,
        INTERMEDIATE_SIGNAL_EVENT_THROWING_GLYPH,
        INTERMEDIATE_MESSAGE_EVENT_THROWING_GLYPH,
        LANE_GLYPH,
        REUSABLE_SUBPROCESS_GLYPH,
        EMBEDDED_SUBPROCESS_GLYPH,
        ADHOC_SUBPROCESS_GLYPH,
        EVENT_SUBPROCESS_GLYPH,
        MULTIPLE_INSTANCE_SUBPROCESS_GLYPH
        }

    private static final ImageDataUriGlyph NONE_TASK_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.task().getSafeUri());

    private static final ImageDataUriGlyph USER_TASK_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.taskUser().getSafeUri());

    private static final ImageDataUriGlyph SCRIPT_TASK_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.taskScript().getSafeUri());

    private static final ImageDataUriGlyph BUSINESS_RULE_TASK_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.taskBusinessRule().getSafeUri());

    private static final ImageDataUriGlyph PARALLEL_MULTIPLE_GATEWAY_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.gatewayParallelMultiple().getSafeUri());

    private static final ImageDataUriGlyph EXCLUSIVE_GATEWAY_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.gatewayExclusive().getSafeUri());

    private static final ImageDataUriGlyph INCLUSIVE_GATEWAY_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.gatewayInclusive().getSafeUri());

    private static final ImageDataUriGlyph START_NONE_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventStartNone().getSafeUri());

    private static final ImageDataUriGlyph START_SIGNAL_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventStartSignal().getSafeUri());

    private static final ImageDataUriGlyph START_TIMER_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventStartTimer().getSafeUri());

    private static final ImageDataUriGlyph START_MESSAGE_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventStartMessage().getSafeUri());

    private static final ImageDataUriGlyph START_ERROR_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventStartError().getSafeUri());

    private static final ImageDataUriGlyph END_NONE_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventEndNone().getSafeUri());

    private static final ImageDataUriGlyph END_SIGNAL_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventEndSignal().getSafeUri());

    private static final ImageDataUriGlyph END_MESSAGE_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventEndMessage().getSafeUri());

    private static final ImageDataUriGlyph END_TERMINATE_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventEndTerminate().getSafeUri());

    private static final ImageDataUriGlyph END_ERROR_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventEndError().getSafeUri());

    private static final ImageDataUriGlyph INTERMEDIATE_MESSAGE_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventIntermediateMessage().getSafeUri());

    private static final ImageDataUriGlyph INTERMEDIATE_SIGNAL_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventIntermediateSignal().getSafeUri());

    private static final ImageDataUriGlyph INTERMEDIATE_TIMER_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventIntermediateTimer().getSafeUri());

    private static final ImageDataUriGlyph INTERMEDIATE_ERROR_EVENT_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventIntermediateError().getSafeUri());

    private static final ImageDataUriGlyph INTERMEDIATE_SIGNAL_EVENT_THROWING_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventIntermediateSignalThrowing().getSafeUri());

    private static final ImageDataUriGlyph INTERMEDIATE_MESSAGE_EVENT_THROWING_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.eventIntermediateMessageThrowing().getSafeUri());

    private static final ImageDataUriGlyph LANE_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.lane().getSafeUri());

    private static final ImageDataUriGlyph REUSABLE_SUBPROCESS_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.subProcessReusable().getSafeUri());

    private static final ImageDataUriGlyph EMBEDDED_SUBPROCESS_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.subProcessEmbedded().getSafeUri());

    private static final ImageDataUriGlyph ADHOC_SUBPROCESS_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.subProcessAdHoc().getSafeUri());

    private static final ImageDataUriGlyph EVENT_SUBPROCESS_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.subProcessEvent().getSafeUri());

    private static final ImageDataUriGlyph MULTIPLE_INSTANCE_SUBPROCESS_GLYPH = ImageDataUriGlyph.create(BPMNImageResources.INSTANCE.subProcessMultipleInstance().getSafeUri());

    static {
        isIE11 = isIE11(Window.Navigator.getUserAgent());
        ieGlyphs.put(Glyphs.NONE_TASK_GLYPH.name(), NONE_TASK_GLYPH);
        ieGlyphs.put(Glyphs.USER_TASK_GLYPH.name(), USER_TASK_GLYPH);
        ieGlyphs.put(Glyphs.SCRIPT_TASK_GLYPH.name(), SCRIPT_TASK_GLYPH);
        ieGlyphs.put(Glyphs.BUSINESS_RULE_TASK_GLYPH.name(), BUSINESS_RULE_TASK_GLYPH);
        ieGlyphs.put(Glyphs.PARALLEL_MULTIPLE_GATEWAY_GLYPH.name(), PARALLEL_MULTIPLE_GATEWAY_GLYPH);
        ieGlyphs.put(Glyphs.EXCLUSIVE_GATEWAY_GLYPH.name(), EXCLUSIVE_GATEWAY_GLYPH);
        ieGlyphs.put(Glyphs.INCLUSIVE_GATEWAY_GLYPH.name(), INCLUSIVE_GATEWAY_GLYPH);
        ieGlyphs.put(Glyphs.START_NONE_EVENT_GLYPH.name(), START_NONE_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.START_SIGNAL_EVENT_GLYPH.name(), START_SIGNAL_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.START_TIMER_EVENT_GLYPH.name(), START_TIMER_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.START_MESSAGE_EVENT_GLYPH.name(), START_MESSAGE_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.START_ERROR_EVENT_GLYPH.name(), START_ERROR_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.END_NONE_EVENT_GLYPH.name(), END_NONE_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.END_SIGNAL_EVENT_GLYPH.name(), END_SIGNAL_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.END_MESSAGE_EVENT_GLYPH.name(), END_MESSAGE_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.END_TERMINATE_EVENT_GLYPH.name(), END_TERMINATE_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.END_ERROR_EVENT_GLYPH.name(), END_ERROR_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.INTERMEDIATE_MESSAGE_EVENT_GLYPH.name(), INTERMEDIATE_MESSAGE_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.INTERMEDIATE_SIGNAL_EVENT_GLYPH.name(), INTERMEDIATE_SIGNAL_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.INTERMEDIATE_TIMER_EVENT_GLYPH.name(), INTERMEDIATE_TIMER_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.INTERMEDIATE_ERROR_EVENT_GLYPH.name(), INTERMEDIATE_ERROR_EVENT_GLYPH);
        ieGlyphs.put(Glyphs.INTERMEDIATE_SIGNAL_EVENT_THROWING_GLYPH.name(), INTERMEDIATE_SIGNAL_EVENT_THROWING_GLYPH);
        ieGlyphs.put(Glyphs.INTERMEDIATE_MESSAGE_EVENT_THROWING_GLYPH.name(), INTERMEDIATE_MESSAGE_EVENT_THROWING_GLYPH);
        ieGlyphs.put(Glyphs.LANE_GLYPH.name(), LANE_GLYPH);
        ieGlyphs.put(Glyphs.REUSABLE_SUBPROCESS_GLYPH.name(), REUSABLE_SUBPROCESS_GLYPH);
        ieGlyphs.put(Glyphs.EMBEDDED_SUBPROCESS_GLYPH.name(), EMBEDDED_SUBPROCESS_GLYPH);
        ieGlyphs.put(Glyphs.ADHOC_SUBPROCESS_GLYPH.name(), ADHOC_SUBPROCESS_GLYPH);
        ieGlyphs.put(Glyphs.EVENT_SUBPROCESS_GLYPH.name(), EVENT_SUBPROCESS_GLYPH);
        ieGlyphs.put(Glyphs.MULTIPLE_INSTANCE_SUBPROCESS_GLYPH.name(), MULTIPLE_INSTANCE_SUBPROCESS_GLYPH);
    }

    public static Glyph getGlyph(final Glyphs glyph) {
        return getGlyph(glyph.name());
    }

    private static Glyph getGlyph(final String glyphId) {
        return isIE11 ? ieGlyphs.get(glyphId) : allBrowserGlyphs.get(glyphId);
    }

    private static boolean isIE11(final String userAgent) {
        final String lowerCaseUserAgent = userAgent != null ? userAgent.toLowerCase() : "";
        return lowerCaseUserAgent.contains("trident/7.0") &&
                (lowerCaseUserAgent.contains("like") && lowerCaseUserAgent.contains("gecko") || lowerCaseUserAgent.contains("msie"));

    }
}