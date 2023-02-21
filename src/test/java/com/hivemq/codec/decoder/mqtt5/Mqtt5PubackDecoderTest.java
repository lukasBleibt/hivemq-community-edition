/*
 * Copyright 2019-present HiveMQ GmbH
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
package com.hivemq.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import com.hivemq.bootstrap.ClientConnectionContext;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import com.hivemq.mqtt.message.puback.PUBACK;
import com.hivemq.mqtt.message.reason.Mqtt5PubAckReasonCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import util.DummyClientConnection;
import util.TestMqttDecoder;

import static org.junit.Assert.*;

/**
 * @author Waldemar Ruck
 * @since 4.0
 */
public class Mqtt5PubackDecoderTest extends AbstractMqtt5DecoderTest {

    private final @NotNull Mqtt5PubAckReasonCode reasonCode = Mqtt5PubAckReasonCode.SUCCESS;

    @Test
    public void test_fixed_header() {
        final byte[] encoded0001 = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0001,
                //   remaining length
                8,
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                4,
                //     reason string
                0x1F, 0, 1, 'x'
        };

        decodeNullExpected(encoded0001);

        channel = new EmbeddedChannel(TestMqttDecoder.create());
        clientConnection = new DummyClientConnection(channel, null);
        clientConnection.setProtocolVersion(protocolVersion);
        channel.attr(ClientConnectionContext.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);

        final byte[] encoded0010 = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0010,
                //   remaining length
                8,
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                4,
                //     reason string
                0x1F, 0, 1, 'x'
        };

        decodeNullExpected(encoded0010);

        channel = new EmbeddedChannel(TestMqttDecoder.create());
        clientConnection = new DummyClientConnection(channel, null);
        clientConnection.setProtocolVersion(protocolVersion);
        channel.attr(ClientConnectionContext.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);

        final byte[] encoded0100 = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0100,
                //   remaining length
                8,
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                4,
                //     reason string
                0x1F, 0, 1, 'x'
        };

        decodeNullExpected(encoded0100);

        channel = new EmbeddedChannel(TestMqttDecoder.create());
        clientConnection = new DummyClientConnection(channel, null);
        clientConnection.setProtocolVersion(protocolVersion);
        channel.attr(ClientConnectionContext.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);

        final byte[] encoded1000 = {
                // fixed header
                //   type, flags
                (byte) 0b0100_1000,
                //   remaining length
                8,
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                4,
                //     reason string
                0x1F, 0, 1, 'x'
        };

        decodeNullExpected(encoded1000);
    }

    @Test
    public void decode_big_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0100_0000,
                //   remaining length (150)
                (byte) (128 + 22), 1,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                (byte) reasonCode.getCode(),
                //   properties (145)
                (byte) (128 + 17), 1,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 5, 't', 'e', 's', 't', '0', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '3', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '4', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '5', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '6', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '7', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '8', 0, 5, 'v', 'a', 'l', 'u', 'e',
        };

        final PUBACK pubAck = decode(encoded);
        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());

        assertEquals(reasonCode, pubAck.getReasonCode());

        assertEquals("success", pubAck.getReasonString());

        final ImmutableList<MqttUserProperty> userProperties = pubAck.getUserProperties().asList();
        assertEquals(9, userProperties.size());
        for (int i = 0; i < 9; i++) {
            assertEquals("test" + i, userProperties.get(i).getName());
            assertEquals("value", userProperties.get(i).getValue());
        }

    }

    @Test
    public void decode_1_byte_packet_id() {

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(1);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final PUBACK pubAck = channel.readInbound();

        assertNull(pubAck);
    }

    @Test
    public void decode_failed_reason_code() {

        final byte[] encoded = {

                //fixed header
                //  type, flags
                (byte) 0b0100_0000,
                //  remaining length
                3,
                //   packet identifier
                0, 5,
                //  reason code
                0x50

        };

        decodeNullExpected(encoded);
    }

    @Test
    public void decode_minimal_packet_with_reason_code() {

        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0100_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                (byte) reasonCode.getCode(),
                //   properties
                0
        };

        final PUBACK pubAck = decode(encoded);

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(reasonCode, pubAck.getReasonCode());
    }

    @Test
    public void decode_invalid_packed_identifier() {

        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0100_0000,
                //   remaining length
                4,
                // variable header
                //   packet identifier
                0, 0,
                //   reason code (success)
                (byte) reasonCode.getCode(),
                //   properties
                0
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void decode_invalid_property() {

        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0100_0000,
                //   remaining length
                8,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                (byte) reasonCode.getCode(),
                //   properties
                4,
                0x15, 0, 1, 'x'
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void decode_minimal_packet() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);

        channel.writeInbound(byteBuf);
        final PUBACK pubAck = channel.readInbound();

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(Mqtt5PubAckReasonCode.SUCCESS, pubAck.getReasonCode());
        assertNull(pubAck.getReasonString());
        assertEquals(0, pubAck.getUserProperties().asList().size());
    }

    @Test
    public void decode_packet_without_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(3);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code
        byteBuf.writeByte(0x10);

        channel.writeInbound(byteBuf);
        final PUBACK pubAck = channel.readInbound();

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, pubAck.getReasonCode());
        assertNull(pubAck.getReasonString());
        assertEquals(0, pubAck.getUserProperties().asList().size());
    }

    @Test
    public void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final PUBACK pubAck = channel.readInbound();

        assertNull(pubAck);
    }

    @Test
    public void decode_not_enough_bytes_for_fixed_header() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(128);

        channel.writeInbound(byteBuf);
        final PUBACK pubAck = channel.readInbound();

        assertNull(pubAck);
    }

    @NotNull
    private PUBACK decode(final byte @NotNull [] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final PUBACK pubAck = channel.readInbound();
        assertNotNull(pubAck);

        return pubAck;
    }

    @Test
    public void test_decode_user_properties_length_gt_packet_length() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                14,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };

        final PUBACK pubAck = decode(encoded);
        assertNotNull(pubAck);

        assertEquals(reasonCode, pubAck.getReasonCode());
        assertEquals("success", pubAck.getReasonString());
        assertEquals(0, pubAck.getUserProperties().asList().size());
    }

    @Test
    public void test_decode_user_properties_incorrect_key_length_gt_must_be() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                28,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                24,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void test_decode_user_properties_incorrect_key_length_lt_must_be() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                27,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                23,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void test_decode_property_length_too_short() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                12,
                //   packet identifier
                0, 5,
                // variable header
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                8,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void test_decode_invalid_remaining_length() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                -1,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'
        };

        decodeChannelOpen(encoded);
    }

    @Test
    public void test_decode_invalid_remaining_length_and_property_length() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                -1,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                -3,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'
        };

        decodeChannelOpen(encoded);
    }

    @Test
    public void test_decode_property_length_gt_packet_length() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                14,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                9,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void test_decode_property_length_eq_packet_length() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                10,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'
        };

        decodeNullExpected(encoded);
    }

    @Test
    public void test_decode_incorrect_property_length() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b0100_0000,
                //   remaining length
                13,
                //   packet identifier
                0, 5,
                //   reason code (continue)
                (byte) reasonCode.getCode(),
                //   properties
                15,
                //     reason string
                0x1F, 0, 8, 's', 'u', 'c', 'c', 'e', 's', 's', 's'
        };

        decodeNullExpected(encoded);
    }

    private void decodeChannelOpen(final byte @NotNull [] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final PUBACK puback = channel.readInbound();
        assertNull(puback);

        assertTrue(channel.isOpen());

        channel = new EmbeddedChannel(TestMqttDecoder.create());
        clientConnection = new DummyClientConnection(channel, null);
        clientConnection.setProtocolVersion(protocolVersion);
        channel.attr(ClientConnectionContext.CHANNEL_ATTRIBUTE_NAME).set(clientConnection);
    }
}
