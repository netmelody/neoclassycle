package org.netmelody.neoclassycle;

import java.util.Set;

import org.junit.Test;
import org.netmelody.neoclassycle.classfile.UTF8Constant;

import static org.junit.Assert.assertEquals;

public final class ClassNameExtractorTest {
    @Test
    public void testIsValid() {
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

    @Test
    public void testParseUTF8Constant() {
        parseAndCheck("[]", "(Lb;)");
        parseAndCheck("[java.util.List]", "(Ljava/util/List;)V");
        parseAndCheck("[java.lang.Long, java.lang.Class, java.lang.Short]", "(Ljava/lang/Long;Ljava/lang/Class;)Ljava/lang/Short;");
        parseAndCheck("[java.util.Set, java.util.List]", "<X::Ljava/util/Set<TT;>;>(Ljava/util/List<+[TX;>;)TX;");
        parseAndCheck("[java.util.List]", "([[Ljava/util/List;)V");
        parseAndCheck("[java.util.List, java.lang.Number]", "(Ljava/util/List<Ljava/lang/Number;>;)V");
        parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer, " + "java.lang.Number]", "(Ljava/util/List<Ljava/util/Map"
                + "<Ljava/lang/Integer;Ljava/lang/Number;>;>;)V");
        parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer, " + "java.lang.Number]", "(Ljava/util/List<Ljava/util/Map"
                + "<Ljava/lang/Integer;[Ljava/lang/Number;>;>;)V");
        parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer]", "(Ljava/util/List<Ljava/util/Map<Ljava/lang/Integer;[I>;>;)V");
        parseAndCheck("[java.util.List, java.util.Map, java.lang.Integer, " + "java.util.Stack]", "([[Ljava/util/List<[Ljava/util/Map"
                + "<Ljava/lang/Integer;[Ljava/util/Stack;>;>;)V");
        parseAndCheck("[java.util.List]", "(Ljava/util/List<*>;)V");
        parseAndCheck("[java.util.Map, java.lang.Integer, java.lang.Number]", "(Ljava/util/Map<Ljava/lang/Integer;+Ljava/lang/Number;>;)V");
        parseAndCheck("[java.util.Map, java.lang.Number]", "(Ljava/util/Map<*+Ljava/lang/Number;>;)V");
        parseAndCheck("[java.lang.Object, java.util.Collection, java.lang.Short]",
                "Ljava/lang/Object;Ljava/util/Collection<Ljava/lang/Short;>;");
        parseAndCheck("[java.util.Set, java.lang.Boolean, java.lang.Object, " + "java.util.Collection, java.lang.Short]",
                "<T::Ljava/util/Set<Ljava/lang/Boolean;>;>" + "Ljava/lang/Object;Ljava/util/Collection" + "<Ljava/lang/Short;>;");
    }

    private static void parseAndCheck(final String expectedList, final String constant) {
        final Set<String> names = new ClassNameExtractor(new UTF8Constant(null, constant)).extract();
        assertEquals(expectedList, names.toString());
    }

}
