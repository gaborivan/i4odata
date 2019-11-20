/*
 * MIT License
 *
 * Copyright (c) 2019 i4tech Kft.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.i4tech.odata.common.operation;

import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataFields;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ODataFilter<E extends ODataEntity> {

    @Getter
    private String filterExpression;

    public enum Option {
        EQUALS("%s eq '%s'"),
        GREATER_OR_EQUALS("%s ge '%s'"),
        LESS_OR_EQUALS("%s le '%s'"),
        EQUALS_DATE_TIME("%s eq datetimeoffset'%s'"),
        GREATER_OR_EQUALS_DATE_TIME("%s ge datetimeoffset'%s'"),
        LESS_OR_EQUALS_DATE_TIME("%s le datetimeoffset'%s'"),
        ENDS_WITH("endswith(%s,'%s')"),
        STARTS_WITH("startswith(%s,'%s')");

        private final String value;

        Option(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

    }

    private enum Operation {
        AND("and"),
        OR("or");

        private final String value;

        Operation(String v) {
            value = v;
        }

        public String value() {
            return value;
        }
    }

    private static class ODataFilterExpression<E extends ODataEntity> {
        private final ODataFields<E> field;
        private final Option option;
        private final String value;
        private final Operation operation;
        
        ODataFilterExpression(ODataFields<E> field, Option option, String value) {
            this.field = field;
            this.option = option;
            this.value = value;
            this.operation = null;
        }

        ODataFilterExpression(Operation operation, ODataFields<E> field, Option option, String value) {
            this.field = field;
            this.option = option;
            this.value = value;
            this.operation = operation;
        }

        @Override
        public String toString() {
            StringBuilder expression = new StringBuilder();
            if (operation != null) {
                expression.append(" ").append(operation.value()).append(" ");
            }
            expression.append(String.format(option.value(), field.value(), value));
            return expression.toString();
        }
        
    }
    
    public static class ODataFilterExpressionBuilder<E extends ODataEntity> {

        private List<ODataFilterExpression<E>> expressions = new ArrayList<>();
        
        private ODataFilterExpressionBuilder(ODataFields<E> field, Option option, String value) {
            expressions.add(new ODataFilterExpression<>(field, option, value));
        }

        private ODataFilterExpressionBuilder(ODataFields<E> field, Option option, Date value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(field, option, stringValue));
        }

        private ODataFilterExpressionBuilder(ODataFields<E> field, Option option, Number value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(field, option, stringValue));
        }

        public ODataFilterExpressionBuilder<E> and(ODataFields<E> field, Option option, Date value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(Operation.AND, field, option, stringValue));
            return this;
        }

        public ODataFilterExpressionBuilder<E> or(ODataFields<E> field, Option option, Date value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(Operation.OR, field, option, stringValue));
            return this;
        }

        public ODataFilterExpressionBuilder<E> and(ODataFields<E> field, Option option, LocalDate value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(Operation.AND, field, option, stringValue));
            return this;
        }

        public ODataFilterExpressionBuilder<E> or(ODataFields<E> field, Option option, LocalDate value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(Operation.OR, field, option, stringValue));
            return this;
        }

        public ODataFilterExpressionBuilder<E> and(ODataFields<E> field, Option option, Number value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(Operation.AND, field, option, stringValue));
            return this;
        }

        public ODataFilterExpressionBuilder<E> or(ODataFields<E> field, Option option, Number value) {
            final String stringValue = value.toString();
            expressions.add(new ODataFilterExpression<>(Operation.OR, field, option, stringValue));
            return this;
        }

        public ODataFilterExpressionBuilder<E> and(ODataFields<E> field, Option option, String value) {
            expressions.add(new ODataFilterExpression<>(Operation.AND, field, option, value));
            return this;
        }

        public ODataFilterExpressionBuilder<E> or(ODataFields<E> field, Option option, String value) {
            expressions.add(new ODataFilterExpression<>(Operation.OR, field, option, value));
            return this;
        }

        public ODataFilter<E> build() {
            return new ODataFilter<>(expressions.stream().map(e -> e.toString()).collect(Collectors.joining()));
        }
    }

    private ODataFilter(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public static <E extends ODataEntity> ODataFilterExpressionBuilder<E> builder(ODataFields<E> field, Option option, String value) {
        return new ODataFilterExpressionBuilder<>(field, option, value);
    }

    public static <E extends ODataEntity> ODataFilterExpressionBuilder<E> builder(ODataFields<E> field, Option option, Date value) {
        return new ODataFilterExpressionBuilder<>(field, option, value);
    }

    public static <E extends ODataEntity> ODataFilterExpressionBuilder<E> builder(ODataFields<E> field, Option option, LocalDate value) {
        return new ODataFilterExpressionBuilder<>(field, option, java.sql.Date.valueOf(value));
    }

    public static <E extends ODataEntity> ODataFilterExpressionBuilder<E> builder(ODataFields<E> field, Option option, Number value) {
        return new ODataFilterExpressionBuilder<>(field, option, value);
    }

}
