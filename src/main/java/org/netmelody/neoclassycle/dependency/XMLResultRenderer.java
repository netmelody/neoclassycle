/*
 * Copyright (c) 2011, Franz-Josef Elmer, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.netmelody.neoclassycle.dependency;

import java.util.List;
import java.util.Stack;

import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.graph.Attributes;
import org.netmelody.neoclassycle.graph.NameAttributes;
import org.netmelody.neoclassycle.graph.StrongComponent;
import org.netmelody.neoclassycle.renderer.AbstractStrongComponentRenderer;
import org.netmelody.neoclassycle.util.StringPattern;
import org.netmelody.neoclassycle.util.Text;

/**
 * Renderer which renders dependency checking results as XML. It ignores
 * preferences.
 *
 * @author Franz-Josef Elmer
 */
public class XMLResultRenderer extends ResultRenderer {
    private static final String ELEMENT_DEPENDENCY_RESULT = "dependency-checking-results";
    private static final String ATTRIBUTE_STATEMENT = "statement";

    private static final class XMLBuilder {
        private static final int INDENTATION_INCREMENT = 2;
        private final StringBuilder _builder = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n");

        private final Stack<String> _stack = new Stack<String>();
        private boolean _unfinishedStartTag;
        private boolean _textAdded;

        void begin(final String element) {
            if (_unfinishedStartTag) {
                _builder.append(">\n");
            }
            indent();
            _builder.append("<").append(element);
            _stack.push(element);
            _unfinishedStartTag = true;
        }

        void attribute(final String name, final String value) {
            _builder.append(' ').append(name).append("=\'").append(Text.excapeForXML(value)).append("\'");
        }

        void text(final String text) {
            _builder.append(">").append(Text.excapeForXML(text));
            _unfinishedStartTag = false;
            _textAdded = true;
        }

        void end() {
            final String element = _stack.pop();
            if (_unfinishedStartTag) {
                _builder.append("/>\n");
                _unfinishedStartTag = false;
            }
            else {
                if (_textAdded == false) {
                    indent();
                }
                _textAdded = false;
                _builder.append("</").append(element).append(">\n");
            }
        }

        private void indent() {
            for (int i = 0; i < _stack.size() * INDENTATION_INCREMENT; i++) {
                _builder.append(' ');
            }
        }

        @Override
        public String toString() {
            return _builder.toString();
        }
    }

    @Override
    public PreferenceFactory getPreferenceFactory() {
        return new PreferenceFactory() {
            @Override
            public Preference get(final String key) {
                return new Preference() {
                    @Override
                    public String getKey() {
                        return key;
                    }
                };
            }
        };
    }

    @Override
    public void considerPreference(final Preference preference) { }

    @Override
    public Result getDescriptionOfCurrentPreferences() {
        return new TextResult("");
    }

    @Override
    public String render(final Result result) {
        final XMLBuilder builder = new XMLBuilder();
        builder.begin(ELEMENT_DEPENDENCY_RESULT);
        addTo(builder, result);
        builder.end();
        return builder.toString();
    }

    private void addTo(final XMLBuilder builder, final Result result) {
        if (result instanceof CyclesResult) {
            addTo(builder, (CyclesResult) result);
        }
        else if (result instanceof DependencyResult) {
            addTo(builder, (DependencyResult) result);
        }
        else if (result instanceof ResultContainer) {
            addTo(builder, (ResultContainer) result);
        }
        else if (result instanceof TextResult) {
            addTo(builder, (TextResult) result);
        }
    }

    private static void addTo(final XMLBuilder builder, final CyclesResult result) {
        builder.begin("cycles");
        builder.attribute(ATTRIBUTE_STATEMENT, result.getStatement());
        builder.attribute("vertex-type", result.isPackageCycle() ? "package" : "class");
        final List<StrongComponent> cycles = result.getCycles();
        for (final StrongComponent component : cycles) {
            builder.begin("cycle");
            builder.attribute("name", AbstractStrongComponentRenderer.createName(component));
            for (int i = 0, n = component.getNumberOfVertices(); i < n; i++) {
                builder.begin("class");
                final Attributes attributes = component.getVertex(i).getAttributes();
                if (attributes instanceof NameAttributes) {
                    builder.text(((NameAttributes) attributes).getName());
                }
                builder.end();
            }
            builder.end();
        }
        builder.end();
    }

    private void addTo(final XMLBuilder builder, final DependencyResult result) {
        builder.begin("unexpected-dependencies");
        builder.attribute(ATTRIBUTE_STATEMENT, result.getStatement());
        final AtomicVertex[] paths = result.getPaths();
        final StringPattern startSet = result.getStartSet();
        final StringPattern finalSet = result.getFinalSet();
        final DependencyPathsRenderer renderer = new DependencyPathsRenderer(paths, startSet, finalSet);
        renderer.renderGraph(createPathRenderer(builder));
        builder.end();
    }

    private DependencyPathRenderer createPathRenderer(final XMLBuilder builder) {
        return new DependencyPathRenderer() {
            private boolean _openTag;

            @Override
            public void increaseIndentation() {
                _openTag = false;
            }

            @Override
            public void decreaseIndentation() {
                if (_openTag) {
                    builder.end();
                }
                _openTag = false;
                builder.end();
            }

            @Override
            public void add(final String nodeName) {
                if (_openTag) {
                    builder.end();
                }
                builder.begin("node");
                builder.attribute("name", nodeName);
                _openTag = true;
            }
        };
    }

    private void addTo(final XMLBuilder builder, final ResultContainer result) {
        final int numberOfResults = result.getNumberOfResults();
        for (int i = 0; i < numberOfResults; i++) {
            addTo(builder, result.getResult(i));
        }
    }

    private static void addTo(final XMLBuilder builder, final TextResult result) {
        if (result.isOk() == false || result.toString().trim().length() > 0) {
            builder.begin(result.isOk() ? "info" : "checking-error");
            builder.text(result.toString());
            builder.end();
        }
    }

}
