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

package io.i4tech.odata.common.util;

import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.model.ODataFields;
import io.i4tech.odata.common.operation.ODataFilter;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ODataOperationUtils {

    public static  <T extends ODataEntity> ODataFilter<T> multiStringFilter( ODataFields<T> field, ODataFilter.Option option, List<String> values) {
        ODataFilter.ODataFilterExpressionBuilder<T> filterBuilder = ODataFilter
                .builder(field, option, values.get(0));
        for (int i = 1; i < values.size(); i++) {
            filterBuilder.or(field, option, values.get(i));
        }
        return filterBuilder.build();
    }

    public static  <T, N> Set<String> getNavigationFields(T entity, Function<T, List<N>> navigation, Function<N, String> getter) {
        return (!CollectionUtils.isEmpty(navigation.apply(entity)) ?
                navigation.apply(entity)
                        .stream()
                        .map(n -> getter.apply(n))
                        .collect(Collectors.toSet())
                : Collections.emptySet()
        );
    }
}
