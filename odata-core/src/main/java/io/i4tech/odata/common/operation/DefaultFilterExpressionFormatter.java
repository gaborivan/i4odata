package io.i4tech.odata.common.operation;

import io.i4tech.odata.common.model.EdmSimpleType;
import io.i4tech.odata.common.util.EncoderUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DefaultFilterExpressionFormatter implements ODataFilterExpressionFormatter {

    public String format(EdmSimpleType edmType, Object value) {
        String stringValue;
        if (edmType == EdmSimpleType.DATETIME && value instanceof Date) {
            stringValue = formatDateTime((Date) value);
        } else if (edmType == EdmSimpleType.DATETIMEOFFSET && value instanceof Date) {
            stringValue = formatDateTimeOffset((Date) value);
        } else if (edmType == EdmSimpleType.GUID) {
            stringValue = formatGuid(value);
        } else {
            stringValue = StringUtils.wrap(EncoderUtils.escape(value.toString()), "'");
        }
        return stringValue;
    }

    private String formatGuid(Object value) {
        return "guid" + StringUtils.wrap(value.toString(), "'");
    }

    private String formatDateTimeOffset(Date value) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String stringValue = df.format(value);
        return "datetimeoffset" + StringUtils.wrap(stringValue, "'");
    }

    private String formatDateTime(Date value) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String stringValue = df.format(value);
        return "datetime" + StringUtils.wrap(stringValue, "'");
    }

}
