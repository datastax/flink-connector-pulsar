/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.connector.pulsar.source.enumerator.subscriber.impl;

import org.apache.flink.connector.pulsar.common.request.PulsarAdminRequest;
import org.apache.flink.connector.pulsar.source.enumerator.subscriber.PulsarSubscriber;
import org.apache.flink.connector.pulsar.source.enumerator.topic.TopicMetadata;
import org.apache.flink.connector.pulsar.source.enumerator.topic.TopicPartition;
import org.apache.flink.connector.pulsar.source.enumerator.topic.TopicRange;

import org.apache.pulsar.client.admin.PulsarAdminException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.flink.connector.pulsar.common.utils.PulsarExceptionUtils.sneakyThrow;

/** PulsarSubscriber abstract class to simplify Pulsar admin related operations. */
public abstract class BasePulsarSubscriber implements PulsarSubscriber {
    private static final long serialVersionUID = 2053021503331058888L;

    protected TopicMetadata queryTopicMetadata(PulsarAdminRequest adminRequest, String topicName) {
        try {
            return adminRequest.getTopicMetadata(topicName);
        } catch (PulsarAdminException.NotFoundException e) {
            return null;
        } catch (PulsarAdminException e) {
            sneakyThrow(e);
            return null;
        }
    }

    protected List<TopicPartition> toTopicPartitions(
            TopicMetadata metadata, List<TopicRange> ranges) {
        if (!metadata.isPartitioned()) {
            // For non-partitioned topic.
            return singletonList(new TopicPartition(metadata.getName(), -1, ranges));
        } else {
            // For partitioned topic.
            List<TopicPartition> partitions = new ArrayList<>();
            for (int i = 0; i < metadata.getPartitionSize(); i++) {
                partitions.add(new TopicPartition(metadata.getName(), i, ranges));
            }
            return partitions;
        }
    }
}
