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

package io.i4tech.odata.common.operation.query;

import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.model.*;
import io.i4tech.odata.common.operation.AbstractODataOperationBuilder;
import io.i4tech.odata.common.operation.ODataFilter;
import io.i4tech.odata.common.operation.ODataOperationBuilderException;
import io.i4tech.odata.common.operation.OrderByDirection;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ODataQueryOperationBuilder<E extends ODataEntity> extends AbstractODataOperationBuilder<E> {

    public static final String QUERY_STRING_ALREADY_SET = "Query string already set.";

    private class ODataOrder<E extends ODataEntity> {
        private final ODataFields<E> field;
        private final OrderByDirection direction;

        ODataOrder(ODataFields<E> field, OrderByDirection direction) {
            this.field = field;
            this.direction = direction;
        }
    }

    private ODataFilter<E> filter;
    private List<ODataNavigations<E>> expand = new ArrayList<>();
    private Map<String, List<ODataFields<? extends ODataEntity>>> select = new HashMap<>();
    private List<ODataOrder<E>> orderby = new ArrayList<>();
    private String search;
    private String queryString;
    private Boolean count;
    private Boolean inlineCount;
    private Integer skip;
    private Integer top;

    @Override
    public ODataQueryOperationBuilder<E> client(ODataClient client) {
        return (ODataQueryOperationBuilder<E>) super.client(client);
    }

    private void addSelectFields(String entityPrefix, ODataFields<?>... fields) {
        List<ODataFields<? extends ODataEntity>> selected = this.select.computeIfAbsent(entityPrefix,
                pfx -> new ArrayList<>());
        selected.addAll(Arrays.asList(fields));
    }

    @Override
    public <R extends ODataEntity> ODataQueryOperationBuilder<R> path(Class<R> resourceClass, ODataKeyFields<R> keyField, String keyValue) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        return (ODataQueryOperationBuilder<R>) super.path(resourceClass, keyField, keyValue);
    }

    @Override
    public <R extends ODataEntity> ODataQueryOperationBuilder<R> path(Class<R> resourceClass, ODataKey<R> key) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        return (ODataQueryOperationBuilder<R>) super.path(resourceClass, key);

    }

    @Override
    public <R extends ODataEntity> ODataQueryOperationBuilder<R> path(Class<R> resourceClass) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        return (ODataQueryOperationBuilder<R>) super.path(resourceClass);
    }

    @Override
    public <R extends ODataEntity> ODataQueryOperationBuilder<R> collection(String collectionName, Class<R> resourceClass) {
        return (ODataQueryOperationBuilder<R>) super.collection(collectionName, resourceClass);
    }

    public ODataQueryOperationBuilder<E> count() {
        if (this.count != null) {
            throw new ODataOperationBuilderException("Only one count clause can be added to one request.");
        } else if (this.inlineCount != null) {
            throw new ODataOperationBuilderException("Count and inlinecount are not allowed in the same request.");
        } else if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.count = true;
        return this;
    }

    public ODataQueryOperationBuilder<E> inlineCount() {
        if (this.inlineCount != null) {
            throw new ODataOperationBuilderException("Only one inlinecount clause can be added to one request.");
        } else if (this.count != null) {
            throw new ODataOperationBuilderException("Count and inlinecount are not allowed in the same request.");
        } else if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.inlineCount = true;
        return this;
    }

    public ODataQueryOperationBuilder<E> orderBy(ODataFields<E> field) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.orderby.add(new ODataOrder<>(field, OrderByDirection.ASC));
        return this;
    }

    public ODataQueryOperationBuilder<E> orderBy(ODataFields<E> field, OrderByDirection direction) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.orderby.add(new ODataOrder<>(field, direction));
        return this;
    }

    public ODataQueryOperationBuilder<E> search(String searchString) {
        if (this.search != null) {
            throw new ODataOperationBuilderException("Only one search clause can be added to one request.");
        } else if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.search = searchString;
        return this;
    }

    public ODataQueryOperationBuilder<E> select(ODataFields<E>... fields) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        addSelectFields("", fields);
        return this;
    }

    public ODataQueryOperationBuilder<E> filter(ODataFilter<E> filterExpression) {
        if (this.filter != null) {
            throw new ODataOperationBuilderException("Only one filter clause can be added to one request.");
        } else if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.filter = filterExpression;
        return this;
    }

    public ODataQueryOperationBuilder<E> expand(ODataNavigations<E> navigation) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.expand.add(navigation);
        return this;
    }

    public ODataQueryOperationBuilder<E> expandSelect(ODataNavigations<E> navigation, ODataFields<?>... select) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.expand.add(navigation);
        addSelectFields(navigation.value(), select);
        return this;
    }

    public ODataQueryOperationBuilder<E> skip(int skip) {
        if (this.skip != null) {
            throw new ODataOperationBuilderException("Only one skip clause can be added to one request.");
        } else if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.skip = skip;
        return this;
    }

    public ODataQueryOperationBuilder<E> top(int top) {
        if (this.top != null) {
            throw new ODataOperationBuilderException("Only one top clause can be added to one request.");
        } else if (this.queryString != null) {
            throw new ODataOperationBuilderException(QUERY_STRING_ALREADY_SET);
        }
        this.top = top;
        return this;
    }

    public ODataQueryOperationBuilder<E> queryString(String queryString) {
        if (this.queryString != null) {
            throw new ODataOperationBuilderException("Only one query string can be added to one request.");
        } else if (!(this.filter == null && this.select.isEmpty() && this.search == null && this.expand.isEmpty() &&
                this.orderby.isEmpty() && this.skip == null && this.top == null && this.count == null &&
                this.inlineCount == null)) {
            throw new ODataOperationBuilderException("Query string can only be set if no other clause has been supplied.");
        }
        this.queryString = queryString;
        return this;
    }

    private boolean appendClause(StringBuilder urlBuilder, boolean isFirstClause, String clause) {
        urlBuilder.append(isFirstClause ? "?" : "&").append(clause);
        return false;
    }

    private StringBuilder buildPathUrl() {
        final StringBuilder urlBuilder = new StringBuilder("/");

        if (path.isEmpty() && StringUtils.isBlank(collectionName)) {
            throw new ODataOperationBuilderException("Path expression is mandatory.");
        }
        if (!path.isEmpty() && StringUtils.isNotBlank(collectionName)) {
            throw new ODataOperationBuilderException("Type-safe and textual path expressions cannot be combined.");
        }
        if (!path.isEmpty()) {
            urlBuilder.append(path.stream()
                    .map(ODataPathElement::toString)
                    .collect(Collectors.joining("/")));
        } else {
            urlBuilder.append(collectionName);
        }
        if (count != null) {
            urlBuilder.append("/$count");
        }
        return urlBuilder;
    }

    @Override
    public ODataQueryOperation<E> build() {
        final StringBuilder urlBuilder = buildPathUrl();
        boolean isFirstClause = true;

        if (queryString != null) {
            isFirstClause = appendClause(urlBuilder, isFirstClause, queryString);
        }
        if (search != null) {
            isFirstClause = appendClause(urlBuilder, isFirstClause,
                    String.format("$search='%s'", search));
        }
        if (filter != null) {
            isFirstClause = appendClause(urlBuilder, isFirstClause, String.format("$filter=%s", filter.getFilterExpression()));
        }
        if (!expand.isEmpty()) {
            isFirstClause = appendClause(urlBuilder, isFirstClause,
                    String.format("$expand=%s",
                            expand.stream()
                                    .map(ODataNavigations::value)
                                    .collect(Collectors.joining(",")
                                    )
                    )
            );
        }
        if (!select.isEmpty()) {
            isFirstClause = appendClause(urlBuilder, isFirstClause,
                    String.format("$select=%s",
                            select.keySet().stream()
                                    .map(k -> select.get(k).stream()
                                            .map(f -> StringUtils.isEmpty(k) ? f.value() : k + "/" + f.value())
                                            .collect(Collectors.joining(",")))
                                    .collect(Collectors.joining(",")
                                    )
                    )
            );
        }
        if (!orderby.isEmpty()) {
            isFirstClause = appendClause(urlBuilder, isFirstClause,
                    String.format("$orderby=%s",
                            orderby.stream()
                                    .map(n -> n.field .value()+ " " + n.direction.value())
                                    .collect(Collectors.joining(",")
                                    )
                    )
            );
        }
        if (skip != null) {
            isFirstClause = appendClause(urlBuilder, isFirstClause,
                    String.format("$skip=%d", skip));
        }
        if (top != null) {
            isFirstClause = appendClause(urlBuilder, isFirstClause,
                    String.format("$top=%d", top));
        }
        if (inlineCount != null && inlineCount.booleanValue()) {
            appendClause(urlBuilder, isFirstClause, "$inlinecount=allpages");
        }

        final String entitySetName = StringUtils.isNotBlank(collectionName) ?
                collectionName : entityClass.getAnnotation(ODataEntitySet.class).name();
        return new ODataQueryOperation(entitySetName, entityClass, client, urlBuilder.toString());
    }
}

