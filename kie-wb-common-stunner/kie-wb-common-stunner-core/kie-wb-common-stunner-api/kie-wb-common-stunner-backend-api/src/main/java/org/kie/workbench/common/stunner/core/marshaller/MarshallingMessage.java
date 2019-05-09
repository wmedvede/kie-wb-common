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

import java.util.List;
import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.workbench.common.stunner.core.validation.DomainViolation;

@Portable
public class MarshallingMessage implements DomainViolation {

    private String elementUUID;
    private int code;
    private Type type;
    private String message;
    private String messageKey;
    private List<?> messageArguments;

    public String getElementUUID() {
        return elementUUID;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public List<?> getMessageArguments() {
        return messageArguments;
    }

    @Override
    public Type getViolationType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public MarshallingMessage(@MapsTo("elementUUID") String elementUUID,
                              @MapsTo("code") int code,
                              @MapsTo("type") Type type,
                              @MapsTo("message") String message,
                              @MapsTo("messageKey") String messageKey,
                              @MapsTo("messageArguments") List<?> messageArguments) {
        this.elementUUID = elementUUID;
        this.code = code;
        this.type = type;
        this.message = message;
        this.messageKey = messageKey;
        this.messageArguments = messageArguments;
    }

    public static MarshallingMessageBuilder builder(){
        return new MarshallingMessageBuilder();
    }

    public static class MarshallingMessageBuilder {

        private String elementUUID;
        private int code;
        private Type type = Type.ERROR;
        private String message;
        private String messageKey;
        private List<?> messageArguments;

        public MarshallingMessageBuilder elementUUID(String elementUUID) {
            this.elementUUID = elementUUID;
            return this;
        }

        public MarshallingMessageBuilder code(int code) {
            this.code = code;
            return this;
        }

        public MarshallingMessageBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public MarshallingMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public MarshallingMessageBuilder messageKey(String messageKey) {
            this.messageKey = messageKey;
            return this;
        }

        public MarshallingMessageBuilder messageArguments(List<?> messageArguments) {
            this.messageArguments = messageArguments;
            return this;
        }

        public MarshallingMessage build() {
            return new MarshallingMessage(elementUUID, code, type, message, messageKey, messageArguments);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MarshallingMessage)) {
            return false;
        }
        MarshallingMessage that = (MarshallingMessage) o;
        return getCode() == that.getCode() &&
                Objects.equals(getElementUUID(), that.getElementUUID()) &&
                type == that.type &&
                Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElementUUID(), getCode(), type, getMessage());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MarshallingMessage{");
        sb.append("elementUUID='").append(elementUUID).append('\'');
        sb.append(", code=").append(code);
        sb.append(", type=").append(type);
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
