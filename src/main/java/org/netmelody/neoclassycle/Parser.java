/*
 * Copyright (c) 2012 Tom Denley
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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
package org.netmelody.neoclassycle;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.netmelody.neoclassycle.classfile.ClassConstant;
import org.netmelody.neoclassycle.classfile.Constant;
import org.netmelody.neoclassycle.classfile.StringConstant;
import org.netmelody.neoclassycle.classfile.UTF8Constant;
import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.util.StringPattern;
import org.netmelody.neoclassycle.util.TrueStringPattern;

/**
 * Utility methods for parsing class files and creating directed graphs. The
 * nodes of the graph are classes. The initial vertex of an edge is the class
 * which uses the class specified by the terminal vertex.
 *
 * @author Franz-Josef Elmer
 */
public final class Parser {
    private static final int ACC_INTERFACE = 0x200, ACC_ABSTRACT = 0x400;
    private static final String[] ZIP_FILE_TYPES = new String[] { ".zip", ".jar", ".war", ".ear" };

    /** Private constructor to prohibit instanciation. */
    private Parser() {
    }

    /**
     * Reads and parses class files and creates a direct graph. Short-cut of
     * <tt>readClassFiles(classFiles, new {@link TrueStringPattern}(),
     * null, false);</tt>
     */
    public static AtomicVertex[] readClassFiles(final String[] classFiles) throws IOException {
        return readClassFiles(classFiles, new TrueStringPattern(), null, false);
    }

    /**
     * Reads the specified class files and creates a directed graph where each
     * vertex represents a class. The head vertex of an arc is a class which is
     * used by the tail vertex of the arc. The elements of <tt>classFiles</tt>
     * are file names (relative to the working directory) which are interpreted
     * depending on its file type as
     * <ul>
     * <li>name of a class file (file type <tt>.class</tt>)
     * <li>name of a file of type <code>.zip</code>, <code>.jar</code>,
     * <code>.war</code>, or <code>.ear</code> containing class file
     * <li>name of a folder containing class files or zip/jar/war/ear files
     * </ul>
     * Folders and zip/jar/war/ear files are searched recursively for class
     * files. If a folder is specified only the top-level zip/jar/war/ear files
     * of that folder are analysed.
     *
     * @param classFiles
     *            Array of file names.
     * @param pattern
     *            Pattern fully qualified class names have to match in order to
     *            be added to the graph. Otherwise they count as 'external'.
     * @param reflectionPattern
     *            Pattern ordinary string constants of a class file have to
     *            fullfill in order to be handled as class references. In
     *            addition they have to be syntactically valid fully qualified
     *            class names. If <tt>null</tt> ordinary string constants will
     *            not be checked.
     * @param mergeInnerClasses
     *            If <code>true</code> merge inner classes with its outer class
     * @return directed graph.
     */
    public static AtomicVertex[] readClassFiles(final String[] classFiles, final StringPattern pattern, final StringPattern reflectionPattern,
            final boolean mergeInnerClasses) throws IOException {
        final ArrayList<UnresolvedNode> unresolvedNodes = new ArrayList<UnresolvedNode>();
        for (final String classFile : classFiles) {
            final File file = new File(classFile);
            if (file.isDirectory()) {
                analyseClassFile(file, classFile, unresolvedNodes, reflectionPattern);
                final File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File candidateFile) {
                        return isZipFile(candidateFile);
                    }
                });
                for (final File file2 : files) {
                    final String source = createSourceName(classFile, file2.getName());
                    analyseClassFiles(new ZipFile(file2.getAbsoluteFile()), source, unresolvedNodes, reflectionPattern);
                }
            }
            else if (file.getName().endsWith(".class")) {
                analyseClassFile(file, null, unresolvedNodes, reflectionPattern);
            }
            else if (isZipFile(file)) {
                analyseClassFiles(new ZipFile(file.getAbsoluteFile()), classFile, unresolvedNodes, reflectionPattern);
            }
            else {
                throw new IOException(classFile + " is an invalid file.");
            }
        }
        final List<UnresolvedNode> filteredNodes = new ArrayList<UnresolvedNode>();
        for (int i = 0, n = unresolvedNodes.size(); i < n; i++) {
            final UnresolvedNode node = unresolvedNodes.get(i);
            if (node.isMatchedBy(pattern)) {
                filteredNodes.add(node);
            }
        }
        UnresolvedNode[] nodes = new UnresolvedNode[filteredNodes.size()];
        nodes = filteredNodes.toArray(nodes);
        return GraphBuilder.createGraph(nodes, mergeInnerClasses);
    }

    private static String createSourceName(final String classFile, final String name) {
        return classFile + (classFile.endsWith(File.separator) ? name : File.separatorChar + name);
    }

    private static boolean isZipFile(final File file) {
        boolean result = false;
        final String name = file.getName();
        for (final String element : ZIP_FILE_TYPES) {
            if (name.endsWith(element)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static void analyseClassFile(final File file, final String source, final ArrayList<UnresolvedNode> unresolvedNodes, final StringPattern reflectionPattern)
            throws IOException {
        if (file.isDirectory()) {
            final String[] files = file.list();
            for (final String file2 : files) {
                final File child = new File(file, file2);
                if (child.isDirectory() || file2.endsWith(".class")) {
                    analyseClassFile(child, source, unresolvedNodes, reflectionPattern);
                }
            }
        }
        else {
            unresolvedNodes.add(extractNode(file, source, reflectionPattern));
        }
    }

    private static UnresolvedNode extractNode(final File file, final String source, final StringPattern reflectionPattern) throws IOException {
        InputStream stream = null;
        UnresolvedNode result = null;
        try {
            stream = new FileInputStream(file);
            result = Parser.createNode(stream, source, (int) file.length(), reflectionPattern);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (final IOException e) {
                }
            }
        }
        return result;
    }

    private static void analyseClassFiles(final ZipFile zipFile, final String source, final ArrayList<UnresolvedNode> unresolvedNodes, final StringPattern reflectionPattern)
            throws IOException {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                final InputStream stream = zipFile.getInputStream(entry);
                final int size = (int) entry.getSize();
                unresolvedNodes.add(Parser.createNode(stream, source, size, reflectionPattern));
            }
        }
    }

    /**
     * Creates a new node with unresolved references.
     *
     * @param stream
     *            A just opended byte stream of a class file. If this method
     *            finishes succefully the internal pointer of the stream will
     *            point onto the superclass index.
     * @param source
     *            Optional source of the class file. Can be <code>null</code>.
     * @param size
     *            Number of bytes of the class file.
     * @param reflectionPattern
     *            Pattern used to check whether a {@link StringConstant} refer
     *            to a class. Can be <tt>null</tt>.
     * @return a node with unresolved link of all classes used by the analysed
     *         class.
     */
    private static UnresolvedNode createNode(final InputStream stream, final String source, final int size, final StringPattern reflectionPattern)
            throws IOException {
        // Reads constant pool, accessFlags, and class name
        final DataInputStream dataStream = new DataInputStream(stream);
        final Constant[] pool = Constant.extractConstantPool(dataStream);
        final int accessFlags = dataStream.readUnsignedShort();
        final String name = ((ClassConstant) pool[dataStream.readUnsignedShort()]).getName();
        ClassAttributes attributes = null;
        if ((accessFlags & ACC_INTERFACE) != 0) {
            attributes = ClassAttributes.createInterface(name, source, size);
        }
        else {
            if ((accessFlags & ACC_ABSTRACT) != 0) {
                attributes = ClassAttributes.createAbstractClass(name, source, size);
            }
            else {
                attributes = ClassAttributes.createClass(name, source, size);
            }
        }

        // Creates a new node with unresolved references
        final UnresolvedNode node = new UnresolvedNode();
        node.setAttributes(attributes);
        for (final Constant constant : pool) {
            if (constant instanceof ClassConstant) {
                final ClassConstant cc = (ClassConstant) constant;
                if (!cc.getName().startsWith(("[")) && !cc.getName().equals(name)) {
                    node.addLinkTo(cc.getName());
                }
            }
            else if (constant instanceof UTF8Constant) {
                parseUTF8Constant((UTF8Constant) constant, node, name);
            }
            else if (reflectionPattern != null && constant instanceof StringConstant) {
                final String str = ((StringConstant) constant).getString();
                if (ClassNameExtractor.isValid(str) && reflectionPattern.matches(str)) {
                    node.addLinkTo(str);
                }
            }
        }
        return node;
    }

    /**
     * Parses an UFT8Constant and picks class names if it has the correct syntax
     * of a field or method descirptor.
     */
    static void parseUTF8Constant(final UTF8Constant constant, final UnresolvedNode node, final String className) {
        final Set<String> classNames = new ClassNameExtractor(constant).extract();
        for (final String string : classNames) {
            final String element = string;
            if (className.equals(element) == false) {
                node.addLinkTo(element);
            }
        }
    }

}