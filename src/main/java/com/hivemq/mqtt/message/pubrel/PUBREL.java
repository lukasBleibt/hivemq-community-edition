/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.mqtt.message.pubrel;

import com.hivemq.annotations.NotNull;
import com.hivemq.annotations.Nullable;
import com.hivemq.mqtt.message.MessageType;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.mqtt.message.mqtt5.MqttMessageWithUserProperties;
import com.hivemq.mqtt.message.reason.Mqtt5PubRelReasonCode;

/**
 * The MQTT PUBREL message
 *
 * @author Dominik Obermaier
 * @author Waldemar Ruck
 * @since 1.4
 */
public class PUBREL extends MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode<Mqtt5PubRelReasonCode> implements Mqtt3PUBREL, Mqtt5PUBREL {

    //MQTT 3
    public PUBREL(final int packetIdentifier) {
        super(packetIdentifier, Mqtt5PubRelReasonCode.SUCCESS, null, Mqtt5UserProperties.NO_USER_PROPERTIES);
    }

    //MQTT 5
    public PUBREL(final int packetIdentifier,
                  @NotNull final Mqtt5PubRelReasonCode reasonCode,
                  @Nullable final String reasonString,
                  @NotNull final Mqtt5UserProperties userProperties) {
        super(packetIdentifier, reasonCode, reasonString, userProperties);
    }

    @NotNull
    @Override
    public MessageType getType() {
        return MessageType.PUBREL;
    }
}
