/*
 * Copyright 2020 Netflix, Inc.
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

package com.netflix.titus.master.mesos.kubeapiserver;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.netflix.titus.common.util.CollectionsExt;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class NodeDataGenerator {

    public static final String NODE_NAME = "node1";

    @SafeVarargs
    public static V1Node newNode(String nodeName, Function<V1Node, V1Node>... transformers) {
        V1Node node = new V1Node()
                .metadata(new V1ObjectMeta().name(nodeName))
                .status(new V1NodeStatus());
        transform(node, transformers);
        return node;
    }

    @SafeVarargs
    public static V1Node newNode(Function<V1Node, V1Node>... transformers) {
        return newNode(NODE_NAME, transformers);
    }

    @SafeVarargs
    public static void transform(V1Node node, Function<V1Node, V1Node>... transformers) {
        for (Function<V1Node, V1Node> transformer : transformers) {
            transformer.apply(node);
        }
    }

    public static V1Node andIpAddress(String ipAddress, V1Node node) {
        return andIpAddress(ipAddress).apply(node);
    }

    public static Function<V1Node, V1Node> andIpAddress(String ipAddress) {
        return node -> {
            node.status(new V1NodeStatus()
                    .addresses(Collections.singletonList(
                            new V1NodeAddress().address(ipAddress).type(KubeUtil.TYPE_INTERNAL_IP)
                    ))
            );
            return node;
        };
    }

    public static V1Node andNodeAnnotations(V1Node node, String... keyValuePairs) {
        return andNodeAnnotations(keyValuePairs).apply(node);
    }

    public static Function<V1Node, V1Node> andNodeAnnotations(String... keyValuePairs) {
        return node -> {
            Map<String, String> annotations = CollectionsExt.copyAndAdd(
                    CollectionsExt.nonNull(node.getMetadata().getAnnotations()),
                    CollectionsExt.asMap(keyValuePairs)
            );
            node.getMetadata().annotations(annotations);
            return node;
        };
    }
}
