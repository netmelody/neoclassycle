/*
 * Copyright (c) 2003-2004, Franz-Josef Elmer, All rights reserved.
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

import classycle.PatternVertexCondition;
import classycle.graph.AtomicVertex;
import classycle.graph.PathsFinder;
import classycle.graph.VertexCondition;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyStatement implements Statement
{
  private final StringPattern[] _startSets;
  private final StringPattern[] _finalSets;
  private final VertexCondition[] _startConditions;
  private final VertexCondition[] _finalConditions;
  private final ResultRenderer _renderer;
  
  public DependencyStatement(StringPattern[] startSets,
                             StringPattern[] finalSets, 
                             ResultRenderer renderer)
  {
    _startSets = startSets;
    _finalSets = finalSets;
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
                                             _renderer.onlyShortestPaths());
        result.add(new DependencyResult(_startSets[i], _finalSets[j], 
                                        finder.findPaths(graph)));
      }
    }
    return result;
  }

}
