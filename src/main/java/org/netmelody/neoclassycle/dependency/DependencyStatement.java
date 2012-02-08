/*
 * Copyright (c) 2003-2008, Franz-Josef Elmer, All rights reserved.
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

import java.util.HashSet;
import java.util.Set;

import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.graph.PathsFinder;
import org.netmelody.neoclassycle.graph.Vertex;
import org.netmelody.neoclassycle.graph.VertexCondition;
import org.netmelody.neoclassycle.util.OrStringPattern;
import org.netmelody.neoclassycle.util.StringPattern;

import static org.netmelody.neoclassycle.dependency.DependencyDefinitionParser.DIRECTLY_INDEPENDENT_OF_KEY_WORD;

/**
 * @author Franz-Josef Elmer
 */
public final class DependencyStatement implements Statement {
    private static final class VertexUnionCondition implements VertexCondition {
        private final VertexCondition[] _conditions;

        VertexUnionCondition(final VertexCondition[] conditions) {
            _conditions = conditions;
        }

        @Override
        public boolean isFulfilled(final Vertex vertex) {
            for (final VertexCondition condition : _conditions) {
                if (condition.isFulfilled(vertex)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final String CHECK = DependencyDefinitionParser.CHECK_KEY_WORD + ' ';
    private final StringPattern[] _startSets;
    private final StringPattern[] _finalSets;
    private final StringPattern _finalSet;
    private final String _dependencyType;
    private final VertexCondition[] _startConditions;
    private final VertexCondition[] _finalConditions;
    private final VertexCondition _finalCondition;
    private final SetDefinitionRepository _repository;
    private final ResultRenderer _renderer;

    public DependencyStatement(final StringPattern[] startSets, final StringPattern[] finalSets, final String dependencyType,
            final SetDefinitionRepository repository, final ResultRenderer renderer) {
        _startSets = startSets;
        _finalSets = finalSets;
        _dependencyType = dependencyType;
        _repository = repository;
        _renderer = renderer;
        _startConditions = createVertexConditions(startSets);
        _finalConditions = createVertexConditions(finalSets);
        _finalSet = new OrStringPattern(_finalSets);
        _finalCondition = new VertexUnionCondition(_finalConditions);
    }

    private static VertexCondition[] createVertexConditions(final StringPattern[] patterns) {
        final VertexCondition[] fromSets = new VertexCondition[patterns.length];
        for (int i = 0; i < fromSets.length; i++) {
            fromSets[i] = new PatternVertexCondition(patterns[i]);
        }
        return fromSets;
    }

    @Override
    public Result execute(final AtomicVertex[] graph) {
        final ResultContainer result = new ResultContainer();
        final boolean directPathsOnly = DIRECTLY_INDEPENDENT_OF_KEY_WORD.equals(_dependencyType);
        final boolean dependsOnly = DependencyDefinitionParser.DEPENDENT_ONLY_ON_KEY_WORD.equals(_dependencyType);
        for (int i = 0; i < _startConditions.length; i++) {
            final VertexCondition startCondition = _startConditions[i];
            final StringPattern startSet = _startSets[i];
            if (dependsOnly) {
                final Set<AtomicVertex> invalids = new HashSet<AtomicVertex>();
                for (final AtomicVertex vertex : graph) {
                    if (startCondition.isFulfilled(vertex)) {
                        for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
                            final Vertex headVertex = vertex.getHeadVertex(j);
                            if (_finalCondition.isFulfilled(headVertex) == false && startCondition.isFulfilled(headVertex) == false) {
                                invalids.add(vertex);
                                invalids.add((AtomicVertex) headVertex);
                            }
                        }
                    }
                }
                result.add(new DependencyResult(startSet, _finalSet, toString(startSet, _finalSet), invalids.toArray(new AtomicVertex[0])));
            }
            else {
                for (int j = 0; j < _finalConditions.length; j++) {
                    final PathsFinder finder = new PathsFinder(startCondition, _finalConditions[j], _renderer.onlyShortestPaths(),
                            directPathsOnly);
                    result.add(new DependencyResult(startSet, _finalSets[j], toString(i, j), finder.findPaths(graph)));
                }
            }
        }
        return result;
    }

    private String toString(final int i, final int j) {
        return toString(_startSets[i], _finalSets[j]);
    }

    private String toString(final StringPattern startSet, final StringPattern finalSet) {
        final StringBuffer buffer = new StringBuffer(CHECK);
        buffer.append(_repository.toString(startSet)).append(' ').append(_dependencyType).append(' ')
                .append(_repository.toString(finalSet));
        return new String(buffer);
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer(CHECK);
        for (final StringPattern _startSet : _startSets) {
            buffer.append(_repository.toString(_startSet)).append(' ');
        }
        buffer.append(_dependencyType).append(' ');
        for (final StringPattern _finalSet2 : _finalSets) {
            buffer.append(_repository.toString(_finalSet2)).append(' ');
        }

        return new String(buffer.substring(0, buffer.length() - 1));
    }
}
