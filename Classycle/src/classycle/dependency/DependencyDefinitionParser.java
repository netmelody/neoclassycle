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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import classycle.util.AndStringPattern;
import classycle.util.NotStringPattern;
import classycle.util.OrStringPattern;
import classycle.util.StringPattern;
import classycle.util.WildCardPattern;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyDefinitionParser
{
  private static final String INDEPENDENT_OF_KEY_WORD = "independentOf";
  private static final String EXCLUDING_KEY_WORD = "excluding";
  private static final String CHECK_KEY_WORD = "check";
  private static final String LAYER_KEY_WORD = "layer";
  private static final String SHOW_KEY_WORD = "show";
  private static final String LAYERING_OF_KEY_WORD = "layeringOf";
  private static final String STRICT_LAYERING_OF_KEY_WORD 
                                            = "strictLayeringOf";
  
  private final ResultRenderer _renderer;
  private final SetDefinitionRepository _setDefinitions 
                    = new SetDefinitionRepository();
  private final LayerDefinitionRepository _layerDefinitions 
                    = new LayerDefinitionRepository();
  private final ArrayList _statements = new ArrayList();
  
  public DependencyDefinitionParser(String dependencyDefinition,
                                    ResultRenderer renderer) 
  {
    _renderer = renderer;
    try
    {
      StringBuffer buffer = new StringBuffer();
      BufferedReader reader = new BufferedReader(new StringReader(
              dependencyDefinition));
      String line;
      int lineNumber = 0;
      int lineNumberOfCurrentLogicalLine = 1;
      while ((line = reader.readLine()) != null)
      {
        lineNumber++;
        line = line.trim();
        if (!line.startsWith("#"))
        {
          buffer.append(line);
          if (line.endsWith("\\"))
          {
            buffer.deleteCharAt(buffer.length() - 1).append(' ');
          } else
          {
            String logicalLine = new String(buffer).trim();
            if (logicalLine.length() > 0)
            {
              StringTokenizer tokenizer = new StringTokenizer(logicalLine);
              String[] tokens = new String[tokenizer.countTokens()];
              for (int i = 0; i < tokens.length; i++)
              {
                tokens[i] = tokenizer.nextToken();
              }
              parseLine(tokens, lineNumberOfCurrentLogicalLine);
            }
            buffer.setLength(0);
            lineNumberOfCurrentLogicalLine = lineNumber + 1;
          }
        }
      }
    } catch (IOException e)
    {
      throw new IllegalArgumentException(e.toString());
    }
  }
  
  public Statement[] getStatements() 
  {
    return (Statement[]) _statements.toArray(new Statement[0]);
  }
  
  private void parseLine(String[] tokens, int lineNumber) {
    String firstToken = tokens[0];
    if (firstToken.startsWith("["))
    {
      parseSetDefinition(tokens, lineNumber);
    } else if (firstToken.equals(SHOW_KEY_WORD))
    {
      parseShowStatement(tokens, lineNumber);
    } else if (firstToken.equals(LAYER_KEY_WORD))
    {
      parseLayerDefinition(tokens, lineNumber);

    } else if (firstToken.equals(CHECK_KEY_WORD))
    {
      parseCheckStatement(tokens, lineNumber);

    } else
    {
      throwException("Expecting either a set name, '" 
                     + SHOW_KEY_WORD + "', '" + LAYER_KEY_WORD + "', or '" 
                     + CHECK_KEY_WORD + "'.", 
                     lineNumber, 0);
    }
  }
  
  private void parseSetDefinition(String[] tokens, int lineNumber)
  {
    String setName = tokens[0];
    if (setName.endsWith("]") == false)
    {
      throwException("Set name has to end with ']'.", lineNumber, 0);
    }
    if (_setDefinitions.contains(setName))
    {
      throwException("Set " + setName + " already defined.", lineNumber, 0);
    }
    checkForEqualCharacter(tokens, lineNumber, 1);
    StringPattern[][] lists = getLists(tokens, lineNumber, 
                                       EXCLUDING_KEY_WORD, 2);
    if (lists[0].length == 0 && lists[1].length == 0) 
    {
      throwException("Missing terms in set definition.", lineNumber, 2);
    }
    AndStringPattern definition = new AndStringPattern();
    definition.appendPattern(createOrSequence(lists[0]));
    definition.appendPattern(new NotStringPattern(createOrSequence(lists[1])));
    _setDefinitions.put(setName, definition);
  }
  
  private void checkForEqualCharacter(String[] tokens, int lineNumber, 
                                      int index)
  {
    if (tokens.length < index + 1 || !tokens[index].equals("="))
    {
      throwException("'=' missing.", lineNumber, index);
    }
  }

  private StringPattern createOrSequence(StringPattern[] patterns) 
  {
    OrStringPattern result = new OrStringPattern();
    for (int i = 0; i < patterns.length; i++)
    {
      result.appendPattern(patterns[i]);
    }
    return result;
  }
  
  private StringPattern createPattern(String term, int lineNumber, 
                                      int tokenIndex)
  {
    StringPattern pattern = _setDefinitions.getPattern(term);
    if (pattern == null) 
    {
      if (term.startsWith("[") && term.endsWith("]")) 
      {
        throwException("Set " + term + " is undefined.", 
                       lineNumber, tokenIndex);
      }
      pattern = new WildCardPattern(term);
    }
    return pattern;
  }

  private void parseLayerDefinition(String[] tokens, int lineNumber)
  {
    if (tokens.length < 2) 
    {
      throwException("Mising layer name.", lineNumber, 1);
    }
    String layerName = tokens[1];
    if (_layerDefinitions.contains(layerName)) 
    {
      throwException("Layer '" + layerName + "' already defined.", 
                     lineNumber, 1);
    }
    checkForEqualCharacter(tokens, lineNumber, 2);
    if (tokens.length < 4) 
    {
      throwException("Missing terms in definition of layer '" 
                     + layerName + "'.", lineNumber, 3);
    }
    ArrayList layer = new ArrayList();
    for (int i = 3; i < tokens.length; i++) 
    {
      layer.add(createPattern(tokens[i], lineNumber, i));
    }
    StringPattern[] sets = new StringPattern[layer.size()];
    _layerDefinitions.put(layerName, (StringPattern[]) layer.toArray(sets));
  }
  
  private void parseShowStatement(String[] tokens, int lineNumber)
  {
    if (tokens.length < 2) {
      throwException("Missing display preference(s).", lineNumber, 1);
    }
    Preference[] preferences = new Preference[tokens.length - 1];
    for (int i = 0; i < preferences.length; i++)
    {
      preferences[i] = _renderer.getPreferenceFactory().get(tokens[i + 1]);
      if (preferences[i] == null) 
      {
        throwException("Unknown display preference: " + tokens[i], 
                       lineNumber, i + 1);
      }
    }
    _statements.add(new ShowStatement(_renderer, preferences));
  }
  
  private void parseCheckStatement(String[] tokens, int lineNumber)
  {
    if (tokens.length < 2) 
    {
      throwException("Missing checking statement.", lineNumber, 1);
    }
    if (tokens[1].equals(STRICT_LAYERING_OF_KEY_WORD) 
        || tokens[1].equals(LAYERING_OF_KEY_WORD))
    {
      createLayeringStatement(tokens, lineNumber);
    } else {
      createDependencyStatement(tokens, lineNumber);
    }
  }
  
  private void createLayeringStatement(String[] tokens, int lineNumber)
  {
    StringPattern[][] layers = new StringPattern[tokens.length - 2][];
    for (int i = 0; i < layers.length; i++) 
    {
      String name = tokens[i + 2];
      layers[i] = _layerDefinitions.getLayer(name);
      if (layers[i] == null)
      {
        throwException("Undefined layer '" + name + "'.", lineNumber, i + 2);
      }
    }
    boolean strict = tokens[1].equals(STRICT_LAYERING_OF_KEY_WORD);
    _statements.add(new LayeringStatement(layers, strict, _setDefinitions, 
                                          _layerDefinitions, _renderer));
  }
  
  private void createDependencyStatement(String[] tokens, int lineNumber)
  {
    StringPattern[][] lists = getLists(tokens, lineNumber, 
                                       INDEPENDENT_OF_KEY_WORD, 1);
    if (lists[0].length == 0) 
    {
      throwException("Missing start sets.", lineNumber, 1);
    }
    if (lists[1].length == 0) 
    {
      throwException("Missing end sets.", lineNumber, tokens.length);
    }
    _statements.add(new DependencyStatement(lists[0], lists[1], _setDefinitions,
                                            _renderer));
  }
  
  private StringPattern[][] getLists(String[] tokens, int lineNumber, 
                                     String keyWord, int startIndex)
  {
    ArrayList startSets = new ArrayList();
    ArrayList endSets = new ArrayList();
    ArrayList currentList = startSets;
    for (int i = startIndex; i < tokens.length; i++) 
    {
      if (tokens[i].equals(keyWord)) 
      {
        if (currentList == endSets) 
        {
          throwException("Invalid appearance of key word '" + keyWord + "'.", 
                         lineNumber, i);
        }
        currentList = endSets;
      } else
      {
        currentList.add(createPattern(tokens[i], lineNumber, i));
      }
    }
    StringPattern[][] result = new StringPattern[2][];
    result[0] = (StringPattern[]) startSets.toArray(new StringPattern[0]);
    result[1] = (StringPattern[]) endSets.toArray(new StringPattern[0]);
    return result;
  }

  private void throwException(String message, int lineNumber, 
                              int tokenIndex) 
  {
    throw new IllegalArgumentException("Error in line " + lineNumber 
                      + " token " + (tokenIndex + 1) + ": " + message);
  }
}
