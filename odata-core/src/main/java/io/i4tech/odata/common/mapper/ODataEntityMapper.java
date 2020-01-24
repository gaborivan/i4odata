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

package io.i4tech.odata.common.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.util.ODataEntityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ODataEntityMapper {

    private final ObjectMapper objectMapper;

    private final Map<Class<?>, Map<String, Field>> entityClassFields = new HashMap<>();

    public ODataEntityMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        configureSerialization();
        configureDeserialization();
    }

    private synchronized Map<String, Field> getEntityFields(Class<?> entityClass) {
        return entityClassFields.computeIfAbsent(entityClass,
                clz -> Arrays.stream(clz.getDeclaredFields())
                    .collect(Collectors.toMap(f -> f.getName().toLowerCase(), Function.identity()))
        );
    }

    protected void configureSerialization() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    protected void configureDeserialization() {
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected void mapNavigationField(Map<String, Object> properties, ODataEntity entity, Field navField) {
        final String propName = navField.getAnnotation(XmlElement.class).name();
        final Class<?> navClz = ODataEntityUtils.getGenericType(navField);
        final ODataFeed feed = (ODataFeed)properties.get(propName);
        if (feed != null) {
            feed.getEntries().forEach(nav -> {
                final Object navigationEntity = mapPropertiesToEntity(nav.getProperties(), (Class) navClz);
                try {
                    navField.setAccessible(true);
                    if (Collection.class.isAssignableFrom(navField.getType())) {
                        List list = (List) navField.get(entity);
                        if (list == null) {
                            list = new ArrayList<>();
                            navField.set(entity, list);
                        }
                        list.add(navigationEntity);
                    } else {
                        navField.set(entity, navigationEntity);
                    }
                } catch (Exception e) {
                    log.error("Could not map field {}.", navField.getName(), e);
                }
            });
        }
    }

    protected String mapFieldName(Class<?> entityClass, String fieldName) {
        final Field field = getEntityFields(entityClass).get(fieldName.toLowerCase());
        String mappedName = fieldName;
        if (field != null && field.getAnnotation(XmlElement.class) != null) {
            mappedName = field.getAnnotation(XmlElement.class).name();
        }
        return mappedName;
    }

    protected Object mapFieldValue(Class<?> entityClass, String fieldName, Object fieldValue) {
        final Field field = getEntityFields(entityClass).get(fieldName.toLowerCase());
        Object mappedValue = fieldValue;
        if (field != null && field.getAnnotation(XmlElement.class) != null && field.getType().isAssignableFrom(UUID.class)) {
            mappedValue = UUID.fromString((String) fieldValue);
        }
        return mappedValue;
    }

    public <E extends ODataEntity> E mapPropertiesToEntity(Map<String, Object> properties, Class<E> entityClass) {
        final E entity = objectMapper.convertValue(properties, entityClass);
        ODataEntityUtils.getNavigationFields(entityClass)
                .forEach(f -> mapNavigationField(properties, entity, f));
        return entity;
    }


    public <E extends ODataEntity> Map<String, Object> mapEntityToProperties(E data, Class<E> entityClass) {
        final Map<String, Object> dataMap = new HashMap<>();
        objectMapper.convertValue(data, Map.class).forEach((k, v) ->
            dataMap.put(
                    mapFieldName(entityClass, (String)k),
                    mapFieldValue(entityClass, (String)k, v)
            )
        );
        return dataMap;
    }
}
