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

package io.i4tech.odata.repository;

import io.i4tech.odata.common.model.ODataEntity;
import org.springframework.data.domain.PageRequest;

public class ODataPageRequest extends PageRequest {
    public ODataPageRequest(int page, int size) {
        super(page, size);
    }

    public <E extends ODataEntity> ODataPageRequest(int page, int size, ODataSort<E> sort) {
        super(page, size, sort);
    }

    public static PageRequest of(int page, int size) {
        return new ODataPageRequest(page, size);
    }

    public static <E extends ODataEntity> PageRequest of(int page, int size, ODataSort<E> sort) {
        return new ODataPageRequest(page, size, sort);
    }

}
