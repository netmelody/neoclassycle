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
package classycle;

import java.util.ArrayList;

import classycle.graph.AtomicVertex;
import classycle.graph.PathsFinder;
import classycle.renderer.DependencyPathsRenderer;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyChecker
{
  private final Analyser _analyser;
  private final ArrayList _dependencies = new ArrayList();
  private final ArrayList _checkingResults = new ArrayList();
  
  public DependencyChecker(String[] classFiles, StringPattern pattern)
  {
    _analyser = new Analyser(classFiles, pattern);
  }
  
  public void addDefinition(DependencyDefinition dependency)
  {
    _dependencies.add(dependency);
  }
  
  public boolean check()
  {
    boolean result = true;
    AtomicVertex[] classGraph = _analyser.getClassGraph();
    _checkingResults.clear();
    for (int i = 0, n = _dependencies.size(); i < n; i++)
    {
      DependencyDefinition definition 
            = (DependencyDefinition) _dependencies.get(i);
      AtomicVertex[] paths = definition.getPathsFinder().findPaths(classGraph);
      boolean ok = definition.isDependencyExpected() ^ (paths.length == 0);
      result &= ok;
      _checkingResults.add(new DependencyCheckingResult(definition, paths, ok));
    }
    return result;
  }
  
  public int getNumberOfCheckingResults()
  {
    return _checkingResults.size();
  }
  
  public DependencyCheckingResult getCheckingResult(int index)
  {
    return (DependencyCheckingResult) _checkingResults.get(index);
  }
  
  public static void main(String[] args)
  {
    DependencyCheckerCommandLine commandLine 
        = new DependencyCheckerCommandLine(args);
    if (!commandLine.isValid()) {
      System.out.println("Usage: java -cp classycle.jar "
                         + "classycle.DependencyChecker "
                         + commandLine.getUsage());
      System.exit(1);
    }
    
    DependencyChecker dependencyChecker 
        = new DependencyChecker(commandLine.getClassFiles(), 
                                commandLine.getPattern());
    for (int i = 0; i < commandLine.getNumberOfDependencyDefinitions(); i++)
    {
      dependencyChecker.addDefinition(commandLine.getDependencyDefinition(i));
    }
    
    boolean ok = dependencyChecker.check();
    if (ok == false)
    {
      System.err.println(
              "Classycle Dependency Checker found the following violations:");
      for (int i = 0; i < dependencyChecker.getNumberOfCheckingResults(); i++)
      {
        DependencyCheckingResult result 
              = dependencyChecker.getCheckingResult(i);
        if (result.isOk() == false)
        {
          System.err.print("\n" + result);
          PathsFinder pathsFinder = result.getDefinition().getPathsFinder();
          DependencyPathsRenderer renderer 
              = new DependencyPathsRenderer(result.getPaths(), 
                                            pathsFinder.getStartSetCondition(), 
                                            pathsFinder.getFinalSetCondition());
          System.err.println(renderer.renderGraph("  "));
        }
      }
    }
    
    System.exit(ok ? 0 : 1);
  }
  /**
   * @return Returns the checkingResults.
   */
  public ArrayList getCheckingResults()
  {
    return _checkingResults;
  }
}
