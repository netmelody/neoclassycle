/*
 * Copyright (c) 2012 Tom Denley
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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

import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.util.StringPattern;

import static org.netmelody.neoclassycle.dependency.DependencyDefinitionParser.DIRECTLY_INDEPENDENT_OF_KEY_WORD;

/**
 * @author Franz-Josef Elmer
 */
public final class LayeringStatement implements Statement {
    private final StringPattern[][] _layers;
    private final boolean _strictLayering;
    private final SetDefinitionRepository _repository;
    private final LayerDefinitionRepository _layerRepository;
    private final ResultRenderer _renderer;

    public LayeringStatement(final StringPattern[][] layers, final boolean strictLayering, final SetDefinitionRepository repository,
            final LayerDefinitionRepository layerRepository, final ResultRenderer renderer) {
        _layers = layers;
        _repository = repository;
        _layerRepository = layerRepository;
        _strictLayering = strictLayering;
        _renderer = renderer;
    }

    @Override
    public Result execute(final AtomicVertex[] graph) {
        final ResultContainer result = new ResultContainer();
        for (int i = 0; i < _layers.length; i++) {
            checkIntraLayerDependencies(result, _layers[i], graph);
            for (int j = i + 1; j < _layers.length; j++) {
                final DependencyStatement s = new DependencyStatement(_layers[i], _layers[j], DIRECTLY_INDEPENDENT_OF_KEY_WORD, _repository,
                        _renderer);
                result.add(s.execute(graph));
            }
            if (_strictLayering) {
                for (int j = i - 2; j >= 0; j--) {
                    final DependencyStatement s = new DependencyStatement(_layers[i], _layers[j], DIRECTLY_INDEPENDENT_OF_KEY_WORD, _repository,
                            _renderer);
                    result.add(s.execute(graph));
                }
            }
        }
        return result;
    }

    private void checkIntraLayerDependencies(final ResultContainer result, final StringPattern[] patterns, final AtomicVertex[] graph) {
        final StringPattern[] startSets = new StringPattern[1];
        final StringPattern[] endSets = new StringPattern[patterns.length - 1];
        for (int i = 0; i < patterns.length; i++) {
            startSets[0] = patterns[i];
            System.arraycopy(patterns, 0, endSets, 0, i);
            System.arraycopy(patterns, i + 1, endSets, i, patterns.length - i - 1);
            final DependencyStatement s = new DependencyStatement(startSets, endSets, DIRECTLY_INDEPENDENT_OF_KEY_WORD, _repository, _renderer);
            result.add(s.execute(graph));
        }
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer("check ");
        buffer.append(_strictLayering ? "strictLayeringOf" : "layeringOf");
        for (final StringPattern[] _layer : _layers) {
            buffer.append(' ').append(_layerRepository.getName(_layer));
        }
        return new String(buffer);
    }
}
