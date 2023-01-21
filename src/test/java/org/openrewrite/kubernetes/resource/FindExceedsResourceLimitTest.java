/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.kubernetes.resource;

import org.junit.jupiter.api.Test;
import org.openrewrite.kubernetes.KubernetesParserTest;

import static org.openrewrite.yaml.Assertions.yaml;

class FindExceedsResourceLimitTest extends KubernetesParserTest {

    @Test
    void findLimitsThatExceedAGivenMaximum() {
        rewriteRun(
          spec -> spec.recipe(new FindExceedsResourceValue(
            "limits",
            "memory",
            "64m",
            null
          )),
          yaml(
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      limits:
                          cpu: "500Mi"
                          memory: "256m"
              """,
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      limits:
                          cpu: "500Mi"
                          memory: ~~(exceeds maximum of 64M)~~>"256m"
              """
          )
        );
    }

    @Test
    void convertLimitsInDifferentUnits() {
        rewriteRun(
          spec -> spec.recipe(new FindExceedsResourceValue(
            "limits",
            "memory",
            "1Gi",
            null
          )),
          yaml(
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      limits:
                          cpu: "500Mi"
                          memory: "2000M"
              """,
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      limits:
                          cpu: "500Mi"
                          memory: ~~(exceeds maximum of 1Gi)~~>"2000M"
              """
          )
        );
    }

    @Test
    void findRequestsThatExceedAGivenMaximum() {
        rewriteRun(
          spec -> spec.recipe(new FindExceedsResourceValue(
            "requests",
            "cpu",
            "100m",
            null
          )),
          yaml(
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      requests:
                          cpu: "500Mi"
                          memory: "256m"
              """,
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      requests:
                          cpu: ~~(exceeds maximum of 100M)~~>"500Mi"
                          memory: "256m"
              """
          )
        );
    }

    @Test
    void convertRequestsInDifferentUnits() {
        rewriteRun(
          spec -> spec.recipe(new FindExceedsResourceValue(
            "requests",
            "memory",
            "1Gi",
            null
          )),
          yaml(
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      requests:
                          cpu: "500Mi"
                          memory: "2000M"
              """,
            """
              apiVersion: v1
              kind: Pod
              metadata:
                labels:
                  app: application
              spec:
                containers:
                - image: nginx:latest
                  resources:
                      requests:
                          cpu: "500Mi"
                          memory: ~~(exceeds maximum of 1Gi)~~>"2000M"
              """
          )
        );
    }
}
