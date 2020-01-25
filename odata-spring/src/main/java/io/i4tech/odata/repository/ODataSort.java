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
import io.i4tech.odata.common.model.ODataFields;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class ODataSort<E extends ODataEntity> extends Sort {

    @Getter
    private List<ODataFields<E>> fields;

    @SafeVarargs
    private ODataSort(Direction direction, ODataFields<E>... fields) {
        super(direction, (String[]) Arrays.stream(fields)
                .map(ODataFields::value).toArray(String[]::new));
        this.fields = Arrays.asList(fields);
    }

    public static <E extends ODataEntity> Sort by(ODataFields<E>... fields) {
        return new ODataSort<>(Direction.ASC, fields);
    }

    public static <E extends ODataEntity> Sort by(Direction direction, ODataFields<E>... fields) {
        return new ODataSort<>(direction, fields);
    }

}
