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
import io.i4tech.odata.common.model.ODataKey;
import io.i4tech.odata.common.model.ODataKeyFields;
import io.i4tech.odata.common.operation.create.ODataCreateOperation;
import io.i4tech.odata.common.operation.delete.ODataDeleteOperation;
import io.i4tech.odata.common.operation.query.ODataQueryOperation;
import io.i4tech.odata.common.operation.query.ODataQueryOperationBuilder;
import io.i4tech.odata.common.operation.update.ODataUpdateOperation;
import io.i4tech.odata.common.client.ODataClient;
import io.i4tech.odata.common.client.ODataException;
import io.i4tech.odata.common.client.ODataResponse;
import io.i4tech.odata.common.util.ODataEntityUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("unchecked")
@NoRepositoryBean
public class ODataEntityRepository<E extends ODataEntity> implements PagingAndSortingRepository<E, String> {

    @Autowired
    protected ODataClient client;

    private Class<E> getEntityClass() {
        return (Class<E>) ODataEntityUtils.getEntityClass(getClass());
    }

    private void applySort(ODataQueryOperationBuilder<E> queryBuilder, Sort sort) {
        if (sort instanceof ODataSort) {
            final ODataSort odataSort = (ODataSort) sort;
            odataSort.getFields().forEach(s -> queryBuilder.orderBy((ODataFields<E>) s));
        }
    }

    protected ODataQueryOperationBuilder<E> getPagedQueryBuilder(Pageable pageable) {
        final ODataQueryOperationBuilder<E> operationBuilder = (ODataQueryOperationBuilder<E>) ODataQueryOperation.builder()
                .client(client)
                .skip((int) pageable.getOffset())
                .top(pageable.getPageSize())
                .inlineCount();

        applySort(operationBuilder, pageable.getSort());

        return operationBuilder;
    }

    protected ODataQueryOperationBuilder<? extends ODataEntity> getQueryBuilder() {
        return ODataQueryOperation.builder()
                .client(client);
    }

    protected Page<E> renderPagedResponse(ODataResponse<E> response, Pageable pageable) {
        return new PageImpl<>(response.getResultList(), pageable, response.getTotalCount());
    }

    @Override
    public Iterable<E> findAll(Sort sort) {
        final ODataQueryOperationBuilder<E> operationBuilder = ODataQueryOperation.builder()
                .client(client)
                .path(getEntityClass());

        applySort(operationBuilder, sort);

        return operationBuilder.build().execute().getResultList();
    }

    @Override
    public Page<E> findAll(Pageable pageable) {
        return renderPagedResponse(getPagedQueryBuilder(pageable)
                .path(getEntityClass())
                .build()
                .execute(), pageable);
    }

    @Override
    public <S extends E> S save(S entity) {
        // Determine if create or update
        if (ODataEntityUtils.allKeyFieldsSet(entity)) {
            final ODataKey<E> key = ODataEntityUtils.getODataKey(entity);
            return (S) ODataUpdateOperation.builder()
                    .client(client)
                    .path(getEntityClass(), key)
                    .data(entity)
                    .build()
                    .execute().getSingleResult();
        } else {
            return (S) ODataCreateOperation.builder()
                    .client(client)
                    .path(getEntityClass())
                    .data(entity)
                    .build()
                    .execute().getSingleResult();
        }
    }

    @Override
    public <S extends E> Iterable<S> saveAll(Iterable<S> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<E> findById(String id) {
        try {
            return getQueryBuilder()
                    .path(getEntityClass(), ODataEntityUtils.getKeyField(getEntityClass()), id)
                    .build()
                    .execute()
                    .getResultList().stream().findFirst();
        } catch (ODataException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String id) {
        final ODataKeyFields<E> key = ODataEntityUtils.getKeyField(getEntityClass());
        try {
            return getQueryBuilder()
                    .path(getEntityClass(), key, id)
                    .select(key)
                    .build()
                    .execute()
                    .getResultList().size() == 1;
        } catch (ODataException e) {
            return false;
        }
    }

    @Override
    public Iterable<E> findAll() {
        return getQueryBuilder()
                .path(getEntityClass())
                .build()
                .execute()
                .getResultList();
    }

    @Override
    public Iterable<E> findAllById(Iterable<String> keys) {
        return StreamSupport.stream(keys.spliterator(), false)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return Long.parseLong(getQueryBuilder()
                .path(getEntityClass())
                .count()
                .build()
                .execute()
                .getResultValue());
    }

    @Override
    public void deleteById(String id) {
        ODataDeleteOperation.builder()
                .client(client)
                .path(getEntityClass(), ODataEntityUtils.getKeyField(getEntityClass()), id)
                .build()
                .execute();
    }

    @Override
    public void delete(E entity) {
        ODataDeleteOperation.builder()
                .client(client)
                .path(getEntityClass(), ODataEntityUtils.getODataKey(entity))
                .build()
                .execute();
    }

    @Override
    public void deleteAll(Iterable<? extends E> entities) {
        StreamSupport.stream(entities.spliterator(), false)
                .forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException("Delete All operation is not supported.");
    }

    public Page<E> search(String term, Pageable pageable) {
        return renderPagedResponse(
                getPagedQueryBuilder(pageable)
                        .path(getEntityClass())
                        .search(term)
                        .build()
                        .execute(),
                pageable);
    }
}
