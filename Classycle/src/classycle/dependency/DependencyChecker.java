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

import java.io.PrintWriter;
import java.util.ArrayList;

import classycle.Analyser;
import classycle.graph.AtomicVertex;
import classycle.util.StringPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyChecker
{
  private final Analyser _analyser;
  private final ResultRenderer _renderer;
  private final DependencyProcessor _processor;
  private final ArrayList _checkingResults = new ArrayList();
  
  public DependencyChecker(String[] classFiles, StringPattern pattern,
                           StringPattern reflectionPattern,
                           String dependencyDefinition, ResultRenderer renderer)
  {
    _analyser = new Analyser(classFiles, pattern, reflectionPattern);
    _renderer = renderer;
    _processor = new DependencyProcessor(dependencyDefinition, renderer);
  }
  
  public boolean check(PrintWriter writer)
  {
    boolean ok = true;
    AtomicVertex[] graph = _analyser.getClassGraph();
    while (_processor.hasMoreStatements())
    {
      Result result = _processor.executeNextStatement(graph);
//      System.out.println(result);
      if (result.isOk() == false)
      {
        ok = false;
      }
      writer.print(_renderer.render(result));
    }
    return ok;
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
                                commandLine.getPattern(), 
                                commandLine.getReflectionPattern(),
                                commandLine.getDependencyDefinition(), 
                                commandLine.getRenderer());
    PrintWriter printWriter = new PrintWriter(System.out);
    boolean ok = dependencyChecker.check(printWriter);
    printWriter.flush();
    System.exit(ok ? 0 : 1);
  }
}
