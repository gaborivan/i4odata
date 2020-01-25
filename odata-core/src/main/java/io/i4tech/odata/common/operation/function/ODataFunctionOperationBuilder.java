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

package io.i4tech.odata.common.operation.function;

import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataFunction;
import io.i4tech.odata.common.operation.ODataOperationBuilderException;
import io.i4tech.odata.common.util.ODataEntityUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ODataFunctionOperationBuilder<E extends ODataEntity> {

    protected ODataClient client;
    protected ODataFunction<? extends ODataEntity> function;

    private String getHttpMethod() {
        String method = ODataFunction.HTTP_METHOD;
        try {
            Field f = this.function.getClass().getDeclaredField("HTTP_METHOD");
            method = (String)f.get(function);
        } catch (Exception e) {
            // nothing to  do
        }
        return method;
    }

    public ODataFunctionOperationBuilder<E> client(ODataClient client) {
        if (this.client != null) {
            throw new ODataOperationBuilderException("Only one client can be added to one request.");
        }
        this.client = client;
        return this;
    }

    public <R extends ODataEntity> ODataFunctionOperationBuilder<R> function(ODataFunction<R> function) {
        this.function = function;
        return (ODataFunctionOperationBuilder<R>) this;
    }

    public ODataFunctionOperation<E> build() {
        final StringBuilder requestPath = new StringBuilder().append("/").append(function.getClass().getSimpleName());
        final Map<String, String> postParameters = new HashMap<>();
        final String httpMethod = getHttpMethod();

        final List<Field> fields = Arrays.stream(function.getClass().getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toList());

        boolean isFirstParam = true;
        for (int i=0; i < fields.size(); i++) {
            final Field f = fields.get(i);
            try {
                f.setAccessible(true);
                String name = f.getAnnotation(XmlElement.class).name();
                String value = (String) f.get(function);
                if (StringUtils.isNotBlank(value)) {
                    if ("GET".equals(httpMethod)) {
                        requestPath.append(isFirstParam ? "?" : "&");
                        requestPath.append(name).append("='").append(value).append("'");
                    } else if ("POST".equals(httpMethod)) {
                        postParameters.put(name, value);
                    }
                    isFirstParam = false;
                }
            } catch (IllegalAccessException e) {
                throw new ODataOperationBuilderException("Could not build request path.", e);
            }
        }
        return new ODataFunctionOperation<>(
                (Class<E>)ODataEntityUtils.getEntityClass(function.getClass()),
                client, requestPath.toString(), postParameters);
    }
}
