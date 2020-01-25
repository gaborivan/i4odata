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

import io.i4tech.odata.common.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ODataEntityUtils {

    @SneakyThrows
    public static Class<?> getFieldJavaType(ODataFields<?> field) {
        final Class<?> entityClz = (Class<?>) ((ParameterizedType) field.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        return Arrays.stream(entityClz.getDeclaredFields())
          .filter(f -> f.getAnnotation(XmlElement.class) != null)
          .filter(f -> f.getAnnotation(XmlElement.class).name().equals(field.value()))
          .map(Field::getType).findAny().orElse(null);
    }

  @SneakyThrows
  public static EdmSimpleType getFieldEdmType(ODataFields<?> field) {
    final Class<?> entityClz = (Class<?>) ((ParameterizedType) field.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    return Arrays.stream(entityClz.getDeclaredFields())
        .filter(f -> f.getAnnotation(XmlElement.class) != null)
        .filter(f -> f.getAnnotation(ODataEdmType.class) != null)
        .filter(f -> f.getAnnotation(XmlElement.class).name().equals(field.value()))
        .map(f -> f.getAnnotation(ODataEdmType.class).value())
            .findAny()
            .map(EdmSimpleType::fromValue)
            .orElse(null);
  }

    public static Class<?> getEntityClass(Class<?> clz) {
        return (Class<?>) ((ParameterizedType) clz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static Class<?> getGenericType(Field f) {
        if (Collection.class.isAssignableFrom(f.getType())) {
            return (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
        } else {
            return f.getType();
        }
    }

    public static <E extends ODataEntity> ODataKeyFields<E> getKeyField(Class<E> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> ODataKeyFields.class.isAssignableFrom(f.getType()))
                .map(f -> {
                    try {
                        return ((ODataKeyFields<E>)f.get(entityClass));
                    } catch (Exception e) {
                        // do nothing, null will be returned
                    }
                    return null;
                })
                .findFirst().orElse(null);
    }

    public static <E extends ODataEntity> boolean allKeyFieldsSet(E data) {
        return Arrays.stream(data.getClass().getDeclaredFields())
                .filter(f -> ODataKeyFields.class.isAssignableFrom(f.getType()))
                .map(f -> {
                    try {
                        String keyName = ((ODataKeyFields<?>)f.get(data)).value();
                        keyName = keyName.substring(0,1).toLowerCase().concat(keyName.substring(1));
                        final Field keyField = data.getClass().getDeclaredField(keyName);
                        keyField.setAccessible(true);
                        return keyField.get(data) != null;
                    } catch (Exception e) {
                        // skip evaluation for this key field
                    }
                    return false;
                }).noneMatch(b -> !b);
    }

    public static <E extends ODataEntity> ODataKey<E> getODataKey(E data) {
        ODataKey.ODataEntityKeyBuilder keyBuilder = ODataKey.builder();
        Arrays.stream(data.getClass().getDeclaredFields())
                .filter(f -> ODataKeyFields.class.isAssignableFrom(f.getType()))
                .forEach(f -> {
                    try {
                        final ODataKeyFields<E> keyDef = (ODataKeyFields<E>) f.get(data);
                        String keyName = keyDef.value();
                        keyName = keyName.substring(0,1).toLowerCase().concat(keyName.substring(1));
                        final Field keyField = data.getClass().getDeclaredField(keyName);
                        keyField.setAccessible(true);
                        final String keyValue = (String) keyField.get(data);
                        keyBuilder.add(keyDef, keyValue);
                    } catch (Exception e) {
                        // skip this key
                    }
                });
        return keyBuilder.build();
    }

    public static Stream<Field> getNavigationFields(Class<? extends ODataEntity> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> ODataEntity.class.isAssignableFrom(ODataEntityUtils.getGenericType(f)));
    }

    public static <E extends ODataEntity> String getEntitySetName(Class<E> entityClass) {
        return entityClass.getAnnotation(ODataEntitySet.class).name();
    }
}
