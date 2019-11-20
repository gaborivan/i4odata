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

package io.i4tech.odata.common.operation.update;

import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.operation.AbstractODataWriteOperation;
import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.client.ODataResponse;

public class ODataUpdateOperation<E extends ODataEntity> extends AbstractODataWriteOperation<E> {

    public ODataUpdateOperation(Class<E> entityClass, ODataClient client, String requestPath, E data) {
        super(entityClass, client, requestPath, data);
    }

    public ODataResponse<E> execute() {
        return client.update(this.entityClass, this.requestPath, this.data);
    }

    public static ODataUpdateOperationBuilder<?> builder() {
        return new ODataUpdateOperationBuilder<>();
    }
}
