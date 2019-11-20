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

package io.i4tech.odata.common.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ODataKey<E extends ODataEntity> {

    @Getter
    private final List<KeyValue<E>> keyPredicate;

    public ODataKey(List<KeyValue<E>> keyValues) {
        this.keyPredicate = keyValues;
    }

    @Override
    public String toString() {
        return keyPredicate.size() == 1 ? StringUtils.wrap(keyPredicate.get(0).value, "'") : keyPredicate.stream()
                    .map(p -> p.key.value() + "=" + StringUtils.wrap(p.value, "'"))
                    .collect(Collectors.joining(","));
    }

    public static class KeyValue<E extends ODataEntity> {
        private final ODataKeyFields<E> key;
        private final String value;

        KeyValue(ODataKeyFields<E> key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class ODataEntityKeyBuilder<E extends ODataEntity> {

        private List<KeyValue<E>> keyValues = new ArrayList<>();

        public ODataEntityKeyBuilder(ODataKeyFields<E> key, String value) {
            this.add(key, value);
        }

        public ODataEntityKeyBuilder() {
        }

        public ODataEntityKeyBuilder<E> add(ODataKeyFields<E> key, String value) {
            keyValues.add(new KeyValue<E>(key, value));
            return this;
        }

        public ODataKey<E> build() {
            return new ODataKey<>(keyValues);
        }
    }

    public static <E extends ODataEntity> ODataEntityKeyBuilder<E> builder(ODataKeyFields<E> key, String value) {
        return new ODataEntityKeyBuilder<>(key, value);
    }

    public static <E extends ODataEntity> ODataEntityKeyBuilder<E> builder() {
        return new ODataEntityKeyBuilder<>();
    }
}
