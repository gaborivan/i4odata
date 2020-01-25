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

package io.i4tech.odata.common.operation.delete;

import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.model.ODataKeyFields;
import io.i4tech.odata.common.operation.AbstractODataWriteOperationBuilder;

import java.util.stream.Collectors;

public class ODataDeleteOperationBuilder<E extends ODataEntity> extends AbstractODataWriteOperationBuilder<E> {

    ODataDeleteOperationBuilder() {
    }

    @Override
    public ODataDeleteOperationBuilder<E> client(ODataClient client) {
        return (ODataDeleteOperationBuilder<E>) super.client(client);
    }

    @Override
    public <R extends ODataEntity> ODataDeleteOperationBuilder<R> path(Class<R> resourceClass, ODataKeyFields<R> keyField, String keyValue) {
        return (ODataDeleteOperationBuilder<R>) super.path(resourceClass, keyField, keyValue);
    }

    @Override
    public <R extends ODataEntity> ODataDeleteOperationBuilder<R> path(Class<R> resourceClass, ODataKey<R> key) {
        return (ODataDeleteOperationBuilder<R>) super.path(resourceClass, key);
    }

    @Override
    public ODataDeleteOperation<E> build() {
        final String uri = "/" + path.stream()
                .map(ODataPathElement::toString)
                .collect(Collectors.joining("/"));
         return new ODataDeleteOperation(entityClass, client, uri);
    }
}

