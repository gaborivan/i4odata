package io.i4tech.odata.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.i4tech.odata.common.model.ODataEntity;
import io.i4tech.odata.common.util.ODataEntityUtils;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.core.ep.entry.ODataEntryImpl;
import org.apache.olingo.odata2.core.ep.feed.FeedMetadataImpl;
import org.apache.olingo.odata2.core.ep.feed.ODataDeltaFeedImpl;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ODataTestUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, Object> entity2PropertyMap(ODataEntity entity) {
        final Map<String, Object> props = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        props.putAll(objectMapper.convertValue(entity, Map.class));

        ODataEntityUtils.getNavigationFields(entity.getClass()).forEach(navField -> {
            final String key = navField.getName();
            navField.setAccessible(true);
            Object navObject;
            try {
                navObject = navField.get(entity);
            } catch (IllegalAccessException e) {
                navObject = null;
            }
            if (navObject instanceof List) {
                ODataFeed navFeed = createFeedFrom((List<ODataEntity>)navObject);
                props.put(key, navFeed);
            }
        });
        return props;
    }

    public static ODataFeed createFeedFrom(List<? extends ODataEntity> entities) {
        return new ODataDeltaFeedImpl(entities.stream()
                .map(ODataTestUtils::entity2PropertyMap)
                .map(m -> new ODataEntryImpl(m, null, null, null))
                .collect(Collectors.toList()), new FeedMetadataImpl());
    }

    public static ODataEntry createEntryFrom(ODataEntity entity) {
        return new ODataEntryImpl(entity2PropertyMap(entity), null, null, null);
    }

    public static <T> Answer<T> countedAnswer(Function<Integer, T> handler) {
        return new Answer<T>() {

            private int count = 0;

            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return handler.apply(count++);
            }
        };
    }

    public static <T> Answer<T> countedAnswer(BiFunction<InvocationOnMock, Integer, T> handler) {
        return new Answer<T>() {

            private int count = 0;

            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                return handler.apply(invocationOnMock, count++);
            }
        };
    }
}
