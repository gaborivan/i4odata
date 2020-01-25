package io.i4tech.odata.common.operation;

import io.i4tech.odata.common.model.EdmSimpleType;

public interface ODataFilterExpressionFormatter {
    String format(EdmSimpleType edmType, Object value);
}
