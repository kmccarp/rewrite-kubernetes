/*
 *  Copyright 2021 the original author or authors.
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  https://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openrewrite.kubernetes.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.yaml.search.YamlSearchResult;
import org.openrewrite.yaml.tree.Yaml;

@Value
@EqualsAndHashCode(callSuper = true)
public class FindMissingAnnotation extends Recipe {

    @Option(displayName = "Annotation name",
            description = "The name of the annotation to search for the existence of.",
            example = "mycompany.io/annotation")
    String annotationName;

    @Option(displayName = "Value",
            description = "An optional glob that will validate values that match.",
            example = "value.*",
            required = false)
    @Nullable
    String value;

    @Override
    public String getDisplayName() {
        return "Find annotation";
    }

    @Override
    public String getDescription() {
        return "Find annotations that optionally match a given value.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        YamlSearchResult missing = new YamlSearchResult(this, "missing:" + annotationName);
        YamlSearchResult invalid = null != value ? new YamlSearchResult(this, "invalid:" + value) : null;

        return new ValidatingMappingEntryVisitor("//metadata/annotations", annotationName, value) {
            @Override
            public Yaml.Mapping visitMissingEntry(Yaml.Mapping mapping, Cursor cursor, ExecutionContext ctx) {
                cursor.putMessageOnFirstEnclosing(Yaml.Mapping.Entry.class, MESSAGE_KEY, missing);
                return mapping;
            }

            @Override
            public Yaml.Mapping.Entry visitInvalidEntry(Yaml.Mapping.Entry entry, Cursor parent, ExecutionContext ctx) {
                if (invalid != null) {
                    parent.putMessageOnFirstEnclosing(Yaml.Mapping.Entry.class, MESSAGE_KEY, invalid);
                }
                return entry;
            }
        };
    }

}
