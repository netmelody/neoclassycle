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
 * Checks a class graph for unwanted dependencies. The dependencies are
 * described by a dependency definition file (<tt>.ddf</tt>).  
 * 
 * @author  Franz-Josef Elmer
 */
public class DependencyChecker
{
  private final Analyser _analyser;
  private final ResultRenderer _renderer;
  private final DependencyProcessor _processor;
  private final ArrayList _checkingResults = new ArrayList();
  
  /**
   * Creates a new instance. The first three parameters define the graph to be
   * checked. They are identical to the parameters of
   * {@link Analyser#Analyser(String[], StringPattern, StringPattern)}.
   * <p>
   * Note, that the constructor does not create the graph. It only parses
   * <tt>dependencyDefinition</tt> as a preprocessing step. The calculation
   * of the graph is done in {@link #check(PrintWriter)}.
   * 
   * @param classFiles Class files or directories with class files.
   * @param pattern Pattern of classes included in the graph.
   * @param reflectionPattern Pattern of classes referred by plain strings.
   * @param dependencyDefinition Description (as read from a .ddf file) of the
   *        dependencies to be checked.
   * @param renderer Output renderer for unwanted dependencies found.
   */
  public DependencyChecker(String[] classFiles, StringPattern pattern,
                           StringPattern reflectionPattern,
                           String dependencyDefinition, ResultRenderer renderer)
  {
    _analyser = new Analyser(classFiles, pattern, reflectionPattern);
    _renderer = renderer;
    _processor = new DependencyProcessor(dependencyDefinition, renderer);
  }
  
  /**
   * Checks the graph and write unwanted dependencies onto the specified
   * writer.
   * @return <tt>true</tt> if no unwanted dependency has been found.
   */
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
  
  /**
   * Runs the DependencyChecker application. 
   * Exit 0 if no unwanted dependency found otherwise 1 is returned.
   */
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
