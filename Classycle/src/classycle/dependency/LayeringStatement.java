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

import classycle.graph.AtomicVertex;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class LayeringStatement implements Statement
{
  private final StringPattern[][] _layers;
  private final boolean _strictLayering;
  private final ResultRenderer _renderer;
  
  public LayeringStatement(StringPattern[][] layers, boolean strictLayering,
                           ResultRenderer renderer)
  {
    _layers = layers;
    _strictLayering = strictLayering;
    _renderer = renderer;
  }

  public Result execute(AtomicVertex[] graph)
  {
    ResultContainer result = new ResultContainer();
    for (int i = 0; i < _layers.length; i++)
    {
      checkIntraLayerDependencies(result, _layers[i], graph);
      for (int j = i + 1; j < _layers.length; j++) 
      {
        DependencyStatement s 
            = new DependencyStatement(_layers[i], _layers[j], _renderer);
        result.add(s.execute(graph));
      }
      if (_strictLayering) 
      {
        for (int j = i - 2; j >= 0; j--) 
        {
          DependencyStatement s 
              = new DependencyStatement(_layers[i], _layers[j], _renderer);
          result.add(s.execute(graph));
        }
      }
    }
    return result;
  }
  
  private void checkIntraLayerDependencies(ResultContainer result, 
                                           StringPattern[] patterns,
                                           AtomicVertex[] graph)
  {
    StringPattern[] startSet = new StringPattern[1];
    StringPattern[] endSets = new StringPattern[patterns.length - 1];
    for (int i = 0; i < patterns.length; i++)
    {
      startSet[0] = patterns[i];
      System.arraycopy(patterns, 0, endSets, 0, i);
      System.arraycopy(patterns, i + 1, endSets, i, patterns.length - i - 1);
      DependencyStatement s 
          = new DependencyStatement(startSet, endSets, _renderer);
      result.add(s.execute(graph));
    }
  }
  
  private void checkInterLayerDependencies(ResultContainer result,
                                           StringPattern[] startLayer,
                                           StringPattern[] endLayer,
                                           AtomicVertex[] graph)
  {
  }
}
