/*
 * Copyright (c) 2005, ASTcentric group, All rights reserved.
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
/*
 * Created on 24.09.2006
 */
package classycle;

import java.util.Set;

import junit.framework.TestCase;
import classycle.classfile.UTF8Constant;

public class ClassNameExtractorTest extends TestCase
{
  public void testIsValid()
  {
    assertEquals(true, ClassNameExtractor.isValid("java.lang.String"));
    assertEquals(true, ClassNameExtractor.isValid("name"));
    assertEquals(true, ClassNameExtractor.isValid("$name"));
    assertEquals(true, ClassNameExtractor.isValid("_name"));
    assertEquals(true, ClassNameExtractor.isValid("_name12"));
    assertEquals(false, ClassNameExtractor.isValid(""));
    assertEquals(false, ClassNameExtractor.isValid("9"));
    assertEquals(false, ClassNameExtractor.isValid("a."));
    assertEquals(false, ClassNameExtractor.isValid("a..a"));
  }
  
  public void testParseUTF8Constant()
  {
    parseAndCheck("[]", "(Lb;)");
    parseAndCheck("[java.util.List]", "(Ljava/util/List;)V");
    parseAndCheck("[java.lang.Long, java.lang.Class, java.lang.Short]", 
                  "(Ljava/lang/Long;Ljava/lang/Class;)Ljava/lang/Short;");
    parseAndCheck("[java.util.Set, java.util.List]", 
                  "<X::Ljava/util/Set<TT;>;>(Ljava/util/List<+[TX;>;)TX;");
    parseAndCheck("[java.util.List]", "([[Ljava/util/List;)V");
    parseAndCheck("[java.util.List, java.lang.Number]", 
                  "(Ljava/util/List<Ljava/lang/Number;>;)V");
    parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer, "
                    + "java.lang.Number]", 
                  "(Ljava/util/List<Ljava/util/Map"
                    + "<Ljava/lang/Integer;Ljava/lang/Number;>;>;)V");
    parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer, "
                    + "java.lang.Number]", 
                  "(Ljava/util/List<Ljava/util/Map"
                    + "<Ljava/lang/Integer;[Ljava/lang/Number;>;>;)V");
    parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer]", 
                  "(Ljava/util/List<Ljava/util/Map<Ljava/lang/Integer;[I>;>;)V");
    parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer, "
                    + "java.util.Stack]", 
                  "([[Ljava/util/List<[Ljava/util/Map"
                    + "<Ljava/lang/Integer;[Ljava/util/Stack;>;>;)V");
    parseAndCheck("[java.util.List]", "(Ljava/util/List<*>;)V");
    parseAndCheck("[java.util.Map, java.lang.Integer, java.lang.Number]", 
                  "(Ljava/util/Map<Ljava/lang/Integer;+Ljava/lang/Number;>;)V");
    parseAndCheck("[java.util.Map, java.lang.Number]", 
                  "(Ljava/util/Map<*+Ljava/lang/Number;>;)V");
    parseAndCheck("[java.lang.Object, java.util.Collection, java.lang.Short]", 
                  "Ljava/lang/Object;Ljava/util/Collection<Ljava/lang/Short;>;");
    parseAndCheck("[java.util.Set, java.lang.Boolean, java.lang.Object, " 
                    + "java.util.Collection, java.lang.Short]", 
                  "<T::Ljava/util/Set<Ljava/lang/Boolean;>;>" 
                    + "Ljava/lang/Object;Ljava/util/Collection" 
                    + "<Ljava/lang/Short;>;");
  }

  private void parseAndCheck(String expectedList, String constant)
  {
    Set names = new ClassNameExtractor(new UTF8Constant(null, constant)).extract();
    assertEquals(expectedList, names.toString());
  }

}
