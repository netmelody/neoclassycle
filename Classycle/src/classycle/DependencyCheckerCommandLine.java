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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import classycle.graph.VertexCondition;
import classycle.util.StringPattern;
import classycle.util.WildCardPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyCheckerCommandLine extends CommandLine
{
  private static final String DEPENDENCIES = "-dependencies=";
  
  private String _dependencies;
  private DependencyDefinition[] _dependencyDefinitions;
  
  public DependencyCheckerCommandLine(String[] args)
  {
    super(args);
    createDependencyDefinitions();
  }

  protected void handleOption(String argument)
  {
    if (argument.startsWith(DEPENDENCIES)) 
    {
      handleDependenciesOption(argument.substring(DEPENDENCIES.length()));
    } else
    {
      super.handleOption(argument);
    }
  }

  /** Returns the usage of correct command line arguments and options. */
  public String getUsage() 
  {
    return "[" + DEPENDENCIES + "<description>|" 
               + DEPENDENCIES + "@<description file>] " + super.getUsage();
  }
  
  public int getNumberOfDependencyDefinitions()
  {
    return _dependencyDefinitions.length;
  }
  
  public DependencyDefinition getDependencyDefinition(int index)
  {
    return _dependencyDefinitions[index];
  }
  
  private void handleDependenciesOption(String option)
  {
    if (option.startsWith("@"))
    {
      try
      {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader 
            = new BufferedReader(new FileReader(option.substring(1)));
        String line;
        boolean previousLineEndsWithBackSlash = true;
        while ((line = reader.readLine()) != null)
        {
          if (!line.startsWith("#"))
          {
            line = line.trim();
            if (previousLineEndsWithBackSlash == false)
            {
              buffer.append(':');
            }
            buffer.append(line);
            previousLineEndsWithBackSlash = line.endsWith("\\");
            if (previousLineEndsWithBackSlash)
            {
              buffer.deleteCharAt(buffer.length() - 1);
            }
          }
        }
        option = new String(buffer);
      } catch (IOException e)
      {
        System.err.println("Error in reading dependencies description file: " 
                           + e.toString());
        option = "";
      }
    }
    _dependencies = option;
    if (_dependencies.length() == 0) 
    {
      _valid = false;
    }
  }
  
  private void createDependencyDefinitions()
  {
    StringTokenizer tokenizer = new StringTokenizer(_dependencies, ":;");
    _dependencyDefinitions = new DependencyDefinition[tokenizer.countTokens()];
    for (int i = 0; i < _dependencyDefinitions.length; i++)
    {
      String description = tokenizer.nextToken().trim();
      _dependencyDefinitions[i] = createDependencyDefinition(description);
    }
  }
  
  private DependencyDefinition createDependencyDefinition(String description)
  {
    DependencyDefinition result = null;
    int index = description.indexOf('>');
    if (index < 2)
    {
      System.err.println("Error: Missung '->', '=>', '+>', or '#>': " 
                         + description);
      _valid = false;
    } else 
    {
      char c = description.charAt(index - 1);
      boolean shortestPathsOnly = c == '-' || c == '+';
      boolean dependencyExpected = c == '-' || c == '=';
      VertexCondition startCondition 
          = createCondition(description.substring(0, index - 1));
      VertexCondition endCondition 
          = createCondition(description.substring(index + 1));
      result = new DependencyDefinition(startCondition, endCondition, 
                                        shortestPathsOnly, dependencyExpected);
    }
    return result;
  }

  private VertexCondition createCondition(String description)
  {
    StringPattern p = WildCardPattern.createFromsPatterns(description, " ,");
    return new PatternVertexCondition(p);
  }
}
