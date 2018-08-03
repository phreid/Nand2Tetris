package JackCompiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Identifier> classTable, subroutineTable;
    private int staticCount, fieldCount, argCount, varCount = 0;

    enum Kind {STATIC, FIELD, ARG, VAR, NONE};

    private class Identifier {
        private String type;
        private Kind kind;
        private int index;

        private Identifier(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    SymbolTable() {
        classTable = new HashMap<>();
        subroutineTable = new HashMap<>();
    }

    void startSubroutine() {
        subroutineTable.clear();
        argCount = varCount = 0;
    }

    void define(String name, String type, Kind kind) {
        switch (kind) {
            case STATIC:
                classTable.put(name, new Identifier(
                        type, kind, staticCount++
                ));
                break;
            case FIELD:
                classTable.put(name, new Identifier(
                        type, kind, fieldCount++
                ));
                break;
            case ARG:
                subroutineTable.put(name, new Identifier(
                        type, kind, argCount++
                ));
                break;
            case VAR:
                subroutineTable.put(name, new Identifier(
                        type, kind, varCount++
                ));
        }
    }

    int varCount(Kind kind) {
        switch (kind) {
            case STATIC:
                return staticCount;
            case FIELD:
                return fieldCount;
            case ARG:
                return argCount;
            case VAR:
                return varCount;
        }

        return -1;
    }

    Kind kindOf(String name) {
        Identifier identifier = subroutineTable.get(name);
        if (identifier == null)
            identifier = classTable.get(name);

        if (identifier == null)
            return Kind.NONE;

        return identifier.kind;
    }

    String typeOf(String name) {
        Identifier identifier = subroutineTable.get(name);
        if (identifier == null)
            identifier = classTable.get(name);

        if (identifier == null)
            return null;

        return identifier.type;
    }

    int indexOf(String name) {
        Identifier identifier = subroutineTable.get(name);
        if (identifier == null)
            identifier = classTable.get(name);

        if (identifier == null)
            return -1;

        return identifier.index;
    }
}
