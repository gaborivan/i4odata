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
import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.client.ODataResponse;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Locale;

public abstract class AbstractODataOperation<E extends ODataEntity> implements ODataOperation<E> {

    protected ODataClient client;

    protected Class<E> entityClass;

    @Getter
    protected String requestPath;

    protected AbstractODataOperation() {
    }

    protected AbstractODataOperation(Class<E> entityClass, ODataClient client, String requestPath) {
        this.entityClass = entityClass;
        this.client = client;
        this.requestPath = requestPath;
    }


    public abstract ODataResponse<E> execute();

    public ODataResponse<E> execute(Locale locale) {
        Locale clientLocale = client.getRequestLocale();
        client.setRequestLocale(locale);
        final ODataResponse<E> response = execute();
        client.setRequestLocale(clientLocale);
        return response;
    }

    public ODataResponse<E> execute(String path) {
        throw new NotImplementedException("Raw execute method not implemented for this operation.");
    }

}
