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
package classycle.dependency;

import classycle.graph.AtomicVertex;
import classycle.graph.PathsFinder;
import classycle.graph.VertexCondition;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyStatement implements Statement
{
  private static final String IDOF 
      = DependencyDefinitionParser.INDEPENDENT_OF_KEY_WORD;
  private static final String DIDOF 
      = DependencyDefinitionParser.DIRECTLY_INDEPENDENT_OF_KEY_WORD;
  private static final String CHECK 
      = DependencyDefinitionParser.CHECK_KEY_WORD + ' ';
  private final StringPattern[] _startSets;
  private final StringPattern[] _finalSets;
  private final boolean _directPathsOnly;
  private final VertexCondition[] _startConditions;
  private final VertexCondition[] _finalConditions;
  private final SetDefinitionRepository _repository;
  private final ResultRenderer _renderer;
  
  public DependencyStatement(StringPattern[] startSets,
                             StringPattern[] finalSets,
                             boolean directPathsOnly,
                             SetDefinitionRepository repository,
                             ResultRenderer renderer)
  {
    _startSets = startSets;
    _finalSets = finalSets;
    _directPathsOnly = directPathsOnly;
    _repository = repository;
    _renderer = renderer;
    _startConditions = createVertexConditions(startSets);
    _finalConditions = createVertexConditions(finalSets);
  }
  
  private VertexCondition[] createVertexConditions(StringPattern[] patterns)
  {
    VertexCondition[] fromSets = new VertexCondition[patterns.length];
    for (int i = 0; i < fromSets.length; i++)
    {
      fromSets[i] = new PatternVertexCondition(patterns[i]);
    }
    return fromSets;
  }

  public Result execute(AtomicVertex[] graph)
  {
    ResultContainer result = new ResultContainer();
    for (int i = 0; i < _startConditions.length; i++)
    {
      for (int j = 0; j < _finalConditions.length; j++)
      {
        PathsFinder finder = new PathsFinder(_startConditions[i], 
                                             _finalConditions[j], 
                                             _renderer.onlyShortestPaths(),
                                             _directPathsOnly);
        result.add(new DependencyResult(_startSets[i], _finalSets[j],
                              toString(i, j), finder.findPaths(graph)));
      }
    }
    return result;
  }
  
  private String toString(int i, int j)
  {
    StringBuffer buffer = new StringBuffer(CHECK);
    buffer.append(_repository.toString(_startSets[i])).append(' ')
          .append(getKeyWord()).append(' ')
          .append(_repository.toString(_finalSets[j]));
    return new String(buffer);
  }
  
  public String toString()
  {
    StringBuffer buffer = new StringBuffer(CHECK);
    for (int i = 0; i < _startSets.length; i++)
    {
      buffer.append(_repository.toString(_startSets[i])).append(' ');
    }
    buffer.append(getKeyWord()).append(' ');
    for (int i = 0; i < _finalSets.length; i++)
    {
      buffer.append(_repository.toString(_finalSets[i])).append(' ');
    }

    return new String(buffer.substring(0, buffer.length() - 1));
  }

  private String getKeyWord()
  {
    return _directPathsOnly ? DIDOF : IDOF;
  }

}
