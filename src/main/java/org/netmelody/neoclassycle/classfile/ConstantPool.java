package org.netmelody.neoclassycle.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public final class ConstantPool {
    private static final int MAGIC = 0xcafebabe;
    private static final int CONSTANT_CLASS = 7,
                             CONSTANT_FIELDREF = 9,
                             CONSTANT_METHODREF = 10,
                             CONSTANT_INTERFACE_METHODREF = 11,
                             CONSTANT_STRING = 8,
                             CONSTANT_INTEGER = 3,
                             CONSTANT_FLOAT = 4,
                             CONSTANT_LONG = 5,
                             CONSTANT_DOUBLE = 6,
                             CONSTANT_NAME_AND_TYPE = 12,
                             CONSTANT_UTF8 = 1;
    
    private Constant[] contents;
    
    private ConstantPool(Constant[] contents) {
        this.contents = contents;
    }

    /**
     * Extracts the constant pool from the specified data stream of a class
     * file.
     *
     * @param stream
     *            Input stream of a class file starting at the first byte.
     * @return extracted array of constants.
     * @throws IOException
     *             in case of reading errors or invalid class file.
     */
    public static ConstantPool extractConstantPool(final DataInputStream stream) throws IOException {
        if (stream.readInt() != MAGIC) {
            throw new IOException("Not a class file: Magic number missing.");
        }
        
        stream.readUnsignedShort();
        stream.readUnsignedShort();
        
        final Constant[] pool = new Constant[stream.readUnsignedShort()];
        final ConstantPool cp = new ConstantPool(pool);
        
        for (int i = 1; i < pool.length;) {
            boolean skipIndex = false;
            Constant c = null;
            final int type = stream.readUnsignedByte();
            switch (type) {
            case CONSTANT_CLASS:
                c = new ClassConstant(cp, stream.readUnsignedShort());
                break;
            case CONSTANT_FIELDREF:
                c = new FieldRefConstant(cp, stream.readUnsignedShort(), stream.readUnsignedShort());
                break;
            case CONSTANT_METHODREF:
                c = new MethodRefConstant(cp, stream.readUnsignedShort(), stream.readUnsignedShort());
                break;
            case CONSTANT_INTERFACE_METHODREF:
                c = new InterfaceMethodRefConstant(cp, stream.readUnsignedShort(), stream.readUnsignedShort());
                break;
            case CONSTANT_STRING:
                c = new StringConstant(cp, stream.readUnsignedShort());
                break;
            case CONSTANT_INTEGER:
                c = new IntConstant(cp, stream.readInt());
                break;
            case CONSTANT_FLOAT:
                c = new FloatConstant(cp, stream.readFloat());
                break;
            case CONSTANT_LONG:
                c = new LongConstant(cp, stream.readLong());
                skipIndex = true;
                break;
            case CONSTANT_DOUBLE:
                c = new DoubleConstant(cp, stream.readDouble());
                skipIndex = true;
                break;
            case CONSTANT_NAME_AND_TYPE:
                c = new NameAndTypeConstant(cp, stream.readUnsignedShort(), stream.readUnsignedShort());
                break;
            case CONSTANT_UTF8:
                c = new Utf8Constant(cp, stream.readUTF());
                break;
            }
            pool[i] = c;
            i += skipIndex ? 2 : 1; // double and long constants occupy two entries
        }
        return cp;
    }

    public Constant getConstantAt(int index) {
        return contents[index];
    }

    public Iterable<Constant> getConstants() {
        return Arrays.asList(contents);
    }
    
    @Override
    public String toString() {
        final String lineSeparator = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));
        final StringBuilder result = new StringBuilder();
        int lineNo = 0;
        for (Constant c : contents) {
            result.append(lineNo).append(": ").append(c).append(lineSeparator);
        }
        return result.toString();
    }
}
