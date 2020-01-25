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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class CollectionCollector<T, S extends Collection<T>, F extends Collection<T>> implements Collector<Collection<T>, S, F> {

    private Supplier<S> supplier;

    private Function<S, F> finisher;

    public CollectionCollector(Supplier<S> supplier, Function<S, F> finisher) {
        this.supplier = supplier;
        this.finisher = finisher;
    }

    public static <T, S extends Collection<T>, F extends Collection<T>> CollectionCollector<T, S, F> combine(Supplier<S> supplier, Function<S, F> finisher) {
        return new CollectionCollector<>(supplier, finisher);
    }

    @Override
    public Supplier<S> supplier() {
        return supplier;
    }

    @Override
    public BiConsumer<S, Collection<T>> accumulator() {
        return S::addAll;
    }

    @Override
    public BinaryOperator<S> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<S, F> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

}
