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

package org.kie.workbench.common.stunner.core.marshaller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MarshallingResponse<T> {

    public enum State {
        ERROR,
        SUCCESS
    }

    private List<MarshallingMessage> messages;
    private State state;
    private Optional<T> result;

    private MarshallingResponse(List<MarshallingMessage> messages, State state, Optional<T> result) {
        this.messages = messages;
        this.state = state;
        this.result = result;
    }

    public List<MarshallingMessage> getMessages() {
        return messages;
    }

    public State getState() {
        return state;
    }

    public Optional<T> getResult() {
        return result;
    }

    public static <T> MarshallingResponseBuilder<T> builder() {
        return new MarshallingResponseBuilder<>();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MarshallingResponse{");
        sb.append("messages=").append(messages);
        sb.append(", state=").append(state);
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MarshallingResponse)) {
            return false;
        }
        MarshallingResponse<?> that = (MarshallingResponse<?>) o;
        return Objects.equals(getMessages(), that.getMessages()) &&
                getState() == that.getState() &&
                Objects.equals(getResult(), that.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessages(), getState(), getResult());
    }

    public static class MarshallingResponseBuilder<T> {

        private final List<MarshallingMessage> messages = new ArrayList<>();
        private State state;
        private Optional<T> result = Optional.empty();

        public MarshallingResponseBuilder messages(List<MarshallingMessage> messages) {
            messages.addAll(messages);
            return this;
        }

        public MarshallingResponseBuilder addMessage(MarshallingMessage message) {
            messages.add(message);
            return this;
        }

        public MarshallingResponseBuilder state(State state) {
            this.state = state;
            return this;
        }

        public MarshallingResponseBuilder result(T result) {
            this.result = Optional.ofNullable(result);
            return this;
        }

        public MarshallingResponse build() {
            return new MarshallingResponse(messages, state, result);
        }
    }
}
