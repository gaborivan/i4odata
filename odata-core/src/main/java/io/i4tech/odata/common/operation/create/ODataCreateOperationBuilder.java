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

package io.i4tech.odata.common.operation.create;

import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.model.ODataKeyFields;
import io.i4tech.odata.common.operation.AbstractODataWriteOperationBuilder;
import io.i4tech.odata.common.operation.ODataOperation;
import io.i4tech.odata.common.client.ODataClient;

import java.util.stream.Collectors;

public class ODataCreateOperationBuilder<E extends ODataEntity> extends AbstractODataWriteOperationBuilder<E> {

    ODataCreateOperationBuilder() {
    }

    public ODataCreateOperationBuilder<E> client(ODataClient client) {
        return (ODataCreateOperationBuilder<E>) super.client(client);
    }

    public <R extends ODataEntity> ODataCreateOperationBuilder<R> path(Class<R> resourceClass, ODataKeyFields<R> keyField, String keyValue) {
        return (ODataCreateOperationBuilder<R>) super.path(resourceClass, keyField, keyValue);
    }

    public <R extends ODataEntity> ODataCreateOperationBuilder<R> path(Class<R> resourceClass, ODataKey<R> key) {
        return (ODataCreateOperationBuilder<R>) super.path(resourceClass, key);
    }

    public <R extends ODataEntity> ODataCreateOperationBuilder<R> path(Class<R> resourceClass) {
        return (ODataCreateOperationBuilder<R>) super.path(resourceClass);
    }

    public ODataCreateOperationBuilder<E> data(E data) {
        return (ODataCreateOperationBuilder<E>) super.data(data);
    }

    @Override
    public ODataOperation<E> build() {
        final String uri = path.stream()
                .map(e -> e.toString())
                .collect(Collectors.joining("/"));
         return new ODataCreateOperation(entityClass, client, uri, data);
    }
}

