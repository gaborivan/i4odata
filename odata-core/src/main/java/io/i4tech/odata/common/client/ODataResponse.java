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

package io.i4tech.odata.common.client;


import io.i4tech.odata.common.model.ODataEntity;
import lombok.Getter;

import java.util.List;

public class ODataResponse<E extends ODataEntity> {

    @Getter
    private final E singleResult;

    @Getter
    private final String resultValue;

    @Getter
    private final List<E> resultList;

    @Getter
    private final long totalCount;

    ODataResponse() {
        this.singleResult = null;
        this.resultList = null;
        this.resultValue = null;
        this.totalCount = 0;
    }

    ODataResponse(E singleResult) {
        this.singleResult = singleResult;
        this.resultList = null;
        this.resultValue = null;
        this.totalCount = 1;
    }

    ODataResponse(List<E> resultList) {
        this.resultList = resultList;
        this.singleResult = (resultList.size() == 1 ? resultList.get(0) : null);
        this.resultValue = null;
        this.totalCount = 0;
    }

    ODataResponse(List<E> resultList, long totalCount) {
        this.resultList = resultList;
        this.singleResult = (resultList.size() == 1 ? resultList.get(0) : null);
        this.resultValue = null;
        this.totalCount = totalCount;
    }

    ODataResponse(String resultValue) {
        this.resultList = null;
        this.singleResult = null;
        this.resultValue = resultValue;
        this.totalCount = 0;
    }

    void merge(ODataResponse<E> other) {
        if (this.resultList != null && other.resultList != null) {
            this.resultList.addAll(other.resultList);
        }
    }
}
