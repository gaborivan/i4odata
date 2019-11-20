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

import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.model.ODataKeyFields;
import io.i4tech.odata.common.util.ODataEntityUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractODataOperationBuilder<E extends ODataEntity> implements ODataOperationBuilder<E> {


    protected class ODataPathElement<P extends ODataEntity> {
        private final Class<P> entityClass;
        private final ODataKey<P> key;

        ODataPathElement(Class<P> entityClass, ODataKey<P> key) {
            this.entityClass = entityClass;
            this.key = key;
        }

        @Override
        public String toString() {
            final String entitySetName = ODataEntityUtils.getEntitySetName(entityClass);
            final StringBuilder pathBuilder = new StringBuilder().append(entitySetName);
            if (key != null) {
                pathBuilder.append("(").append(key.toString()).append(")");
            }
            return pathBuilder.toString();
        }
    }

    protected Class<? extends ODataEntity> entityClass;
    protected ODataClient client;
    protected List<ODataPathElement> path = new ArrayList<>();
    protected String collectionName;


    protected <R extends ODataEntity> ODataOperationBuilder<R> path(Class<R> resourceClass, ODataKeyFields<R> keyField, String keyValue) {
        this.entityClass = resourceClass;
        path.add(new ODataPathElement(resourceClass, ODataKey.builder(keyField, keyValue).build()));
        return (ODataOperationBuilder<R>) this;
    }

    protected <R extends ODataEntity> ODataOperationBuilder<R> path(Class<R> resourceClass, ODataKey<R> key) {
        this.entityClass = resourceClass;
        path.add(new ODataPathElement(resourceClass, key));
        return (ODataOperationBuilder<R>) this;
    }

    protected <R extends ODataEntity> ODataOperationBuilder<R> path(Class<R> resourceClass) {
        this.entityClass = resourceClass;
        path.add(new ODataPathElement(resourceClass, null));
        return (ODataOperationBuilder<R>) this;
    }

    protected <R extends ODataEntity> ODataOperationBuilder<R> collection(String collectionName, Class<R> resourceClass) {
        this.entityClass = resourceClass;
        this.collectionName = collectionName;
        return (ODataOperationBuilder<R>) this;
    }

    protected ODataOperationBuilder<E> client(ODataClient client) {
        if (this.client != null) {
            throw new ODataOperationBuilderException("Only one client can be added to one request.");
        }
        this.client = client;
        return this;
    }

    public abstract ODataOperation<E> build();
}
