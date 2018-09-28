package JackCompiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Identifier> classTable, subroutineTable;
    private int staticCount, fieldCount, argCount, varCount;
    static int INDEX_NOT_FOUND = -1;

    enum Kind {STATIC, FIELD, ARG, VAR, NONE}

    private class Identifier {
        private String type;
        private String kind;
        private int index;

        private Identifier(String type, String kind, int index) {
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

    void define(String name, String type, String kind) {
        switch (kind) {
            case "static":
                classTable.put(name, new Identifier(
                        type, kind, staticCount++
                ));
                break;
            case "field":
                classTable.put(name, new Identifier(
                        type, kind, fieldCount++
                ));
                break;
            case "argument":
                subroutineTable.put(name, new Identifier(
                        type, kind, argCount++
                ));
                break;
            case "var":
                subroutineTable.put(name, new Identifier(
                        type, kind, varCount++
                ));
        }
    }

    int varCount(String kind) {
        switch (kind) {
            case "static":
                return staticCount;
            case "field":
                return fieldCount;
            case "argument":
                return argCount;
            case "var":
                return varCount;
        }

        return -1;
    }

    String kindOf(String name) {
        Identifier identifier = subroutineTable.get(name);
        if (identifier == null)
            identifier = classTable.get(name);

        if (identifier == null)
            return "none";

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
            return INDEX_NOT_FOUND;

        return identifier.index;
    }
}
