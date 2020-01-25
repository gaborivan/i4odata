package io.i4tech.odata.common.model;

public enum EdmSimpleType {
    STRING("Edm.String"),
    DATETIMEOFFSET("Edm.DateTimeOffset"),
    DATETIME("Edm.DateTime"),
    GUID("Edm.Guid"),
    INT32("Edm.Int32"),
    INT16("Edm.Int16"),
    BOOLEAN("Edm.Boolean"),
    TIME("Edm.Time"),
    DECIMAL("Edm.Decimal"),
    BINARY("Edm.Binary");

    private final String value;

    EdmSimpleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EdmSimpleType fromValue(String v) {
        for (EdmSimpleType t: EdmSimpleType.values()) {
            if (t.value.equals(v)) {
                return t;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
