package com.agiletec.plugins.jacms.aps.system.services.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agiletec.aps.system.services.keygenerator.KeyGeneratorManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.entando.entando.ent.exception.EntException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;

@ExtendWith(MockitoExtension.class)
class IFContentLocalCacheTest {

    private Cache<String, Object> localCache;

    @Mock
    private Supplier<ContentRecordVO> actionMock;

    @Mock
    private ContentRecordVO contentVOMock;

    @BeforeEach
    void setUp() {
        localCache = Caffeine.newBuilder()
                .maximumSize(2).build();
    }

    @Test
    void allocateCache_enabled() {
        String contentId = "2677";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedStatic.when(() -> IFContentLocalCache.instantiateLocalCache(
                    anyLong(),
                    anyLong(),
                    any(Boolean.class)
            )).thenCallRealMethod();

            Cache<String, Object> cache = IFContentLocalCache.instantiateLocalCache(100, 100, true);
            assertNotNull(cache);
            cache.put(contentId, contentVOMock);
            assertTrue(cache.asMap().containsKey(contentId));
        }
    }

    @Test
    void allocateCache_disabled() {
        String contentId = "2677";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(false);
            mockedStatic.when(() -> IFContentLocalCache.instantiateLocalCache(
                    anyLong(),
                    anyLong(),
                    any(Boolean.class)
            )).thenCallRealMethod();

            Cache<String, Object> cache = IFContentLocalCache.instantiateLocalCache(100, 100, true);
            assertNotNull(cache);
            cache.put(contentId, contentVOMock);
            // NOTE: we are not explicitly flushing the cache, we are only cleaning up expired entries
            // since the cache size SHOULD be zero, the cache is emptied as a SIDE EFFECT!!!
            cache.cleanUp();
            assertFalse(cache.asMap().containsKey(contentId));
        }
    }

    @Test
    void loadAndCacheContentVO_disabled() {
        String contentId = "2677";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(false);
            mockedStatic.when(() -> IFContentLocalCache.loadAndCacheContentVO(
                    anyString(),
                    any(Cache.class),
                    any(Supplier.class)
            )).thenCallRealMethod();

            when(actionMock.get()).thenReturn(contentVOMock);
            // put the object in the cache; it won't be accessed though
            localCache.put(contentId, contentVOMock);
            ContentRecordVO result = IFContentLocalCache.loadAndCacheContentVO(contentId, localCache, actionMock);

            assertEquals(contentVOMock, result);
            // verify that the supplier has been called to collect the data even if it's been cached
            verify(actionMock, times(1)).get();
        }
    }

    @Test
    void loadAndCacheContentVO_enabled_cacheMiss() {
        String contentId = "2677";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedStatic.when(() -> IFContentLocalCache.loadAndCacheContentVO(
                    anyString(),
                    any(Cache.class),
                    any(Supplier.class)
            )).thenCallRealMethod();

            when(actionMock.get()).thenReturn(contentVOMock);
            ContentRecordVO result = IFContentLocalCache.loadAndCacheContentVO(contentId, localCache, actionMock);

            assertEquals(contentVOMock, result);
            // verify that the supplier has been called to collect the data
            verify(actionMock, times(1)).get();
            assertTrue(localCache.asMap().containsKey(contentId));
        }
    }

    @Test
    void loadAndCacheContentVO_enabled_cacheHit() {
        String contentId = "2677";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedStatic.when(() -> IFContentLocalCache.loadAndCacheContentVO(
                    anyString(),
                    any(Cache.class),
                    any(Supplier.class)
            )).thenCallRealMethod();
            // put in cache
            localCache.put(contentId, contentVOMock);

            lenient().when(actionMock.get()).thenReturn(contentVOMock);
            ContentRecordVO result = IFContentLocalCache.loadAndCacheContentVO(contentId, localCache, actionMock);

            assertEquals(contentVOMock, result);
            // verify that the supplier was never called to collect the data
            verify(actionMock, never()).get();
        }
    }

    @Test
    void evict_enabled() {
        String contentIdA = "2677";
        String contentIdB = "2381";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    anyString(),
                    any(Cache.class)
            )).thenCallRealMethod();
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(List.class),
                    any(Cache.class)
            )).thenCallRealMethod();
            // put in cache
            localCache.put(contentIdA, contentVOMock);

            IFContentLocalCache.evict(contentIdA, localCache);
            // verify that the supplier was never called to collect the data
            assertFalse(localCache.asMap().containsKey(contentIdA));

            // group evict
            localCache.put(contentIdA, contentVOMock);
            localCache.put(contentIdB, contentVOMock);

            List<String> candidates = new ArrayList<>();
            candidates.add(contentIdA);
            candidates.add(contentIdB);
            IFContentLocalCache.evict(candidates, localCache);

            assertFalse(localCache.asMap().containsKey(contentIdA));
            assertFalse(localCache.asMap().containsKey(contentIdB));
        }
    }

    @Test
    void evict_disabled() {
        String contentIdA = "2677";
        String contentIdB = "2381";

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(false);
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    anyString(),
                    any(Cache.class)
            )).thenCallRealMethod();
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(List.class),
                    any(Cache.class)
            )).thenCallRealMethod();
            // put in cache
            localCache.put(contentIdA, contentVOMock);
            localCache.put(contentIdB, contentVOMock);

            // do nothing, the cache is left unchanged
            IFContentLocalCache.evict(contentIdA, localCache);

            // verify that the supplier was never called to collect the data
            // NOTE: the real cache would have size 0, therefore, it would be empty
            assertTrue(localCache.asMap().containsKey(contentIdA));

            List<String> candidates = new ArrayList<>();
            candidates.add(contentIdA);
            candidates.add(contentIdB);
            IFContentLocalCache.evict(candidates, localCache);

            assertTrue(localCache.asMap().containsKey(contentIdA));
            assertTrue(localCache.asMap().containsKey(contentIdB));
        }
    }

    @Mock
    private KeyGeneratorManager keyGeneratorManagerMock;

    @Mock
    private ContentDAO contentDAOMock;

    @Mock
    private BeanFactory beanFactoryMock;

    @Mock
    private Cache<String, Object> localCacheMock;

    @InjectMocks
    private ContentManager contentManager;

    @Test
    void content_evict_enabled_existing_content() {
        Content cnt = new Content();

        cnt.setId("CNT2381");
        contentManager.setLocalCache(localCacheMock);

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(Content.class),
                    any(Cache.class)
            )).thenCallRealMethod();
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(String.class),
                    any(Cache.class)
            )).thenCallRealMethod();

            when(contentManager.loadContentVO(anyString())).thenReturn(contentVOMock);

            contentManager.addContent(cnt);

            // let's verify that the eviction has been called. it must ALWAYS be invoked even when
            // the local cache is disabled by the feature flag
            mockedStatic.verify(
                    () -> IFContentLocalCache.evict(any(Content.class), eq(localCacheMock)),
                    times(1)
            );
            verify(localCacheMock).invalidate(anyString());

        } catch (EntException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void content_evict_enabled_new_content() {
        Content cnt = new Content();

        contentManager.setLocalCache(localCacheMock);

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(Content.class),
                    any(Cache.class)
            )).thenCallRealMethod();
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(String.class),
                    any(Cache.class)
            )).thenCallRealMethod();

            when(keyGeneratorManagerMock.getUniqueKeyCurrentValue()).thenReturn(2677);
            when(contentManager.loadContentVO(anyString())).thenReturn(contentVOMock);
            when(beanFactoryMock.getBean(anyString())).thenReturn(keyGeneratorManagerMock);


            contentManager.addContent(cnt);

            // let's verify that the eviction has been called. it must ALWAYS be invoked even when
            // the local cache is disabled by the feature flag
            mockedStatic.verify(
                    () -> IFContentLocalCache.evict(any(Content.class), eq(localCacheMock)),
                    times(1)
            );
            // the content is new, nothing to evict
            verify(localCacheMock, never()).invalidate(anyString());

        } catch (EntException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void content_evict_disabled_new_content() {
        Content cnt = new Content();

        contentManager.setLocalCache(localCacheMock);

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(false);
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(Content.class),
                    any(Cache.class)
            )).thenCallRealMethod();
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(String.class),
                    any(Cache.class)
            )).thenCallRealMethod();

            when(keyGeneratorManagerMock.getUniqueKeyCurrentValue()).thenReturn(2677);
            when(contentManager.loadContentVO(anyString())).thenReturn(contentVOMock);
            when(beanFactoryMock.getBean(anyString())).thenReturn(keyGeneratorManagerMock);

            contentManager.addContent(cnt);

            // let's verify that the eviction has been called. it must ALWAYS be invoked even when
            // the local cache is disabled by the feature flag
            mockedStatic.verify(
                    () -> IFContentLocalCache.evict(any(Content.class), eq(localCacheMock)),
                    times(1)
            );
            // the content is new, and the cache disabled, the cache implementation does not get called
            verify(localCacheMock, never()).invalidate(anyString());

        } catch (EntException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void content_evict_disabled_existing_content() {
        Content cnt = new Content();

        cnt.setId("CNT2381");
        contentManager.setLocalCache(localCacheMock);

        try (MockedStatic<IFContentLocalCache> mockedStatic = mockStatic(IFContentLocalCache.class)) {
            mockedStatic.when(IFContentLocalCache::checkEnabled).thenReturn(false);
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(Content.class),
                    any(Cache.class)
            )).thenCallRealMethod();
            mockedStatic.when(() -> IFContentLocalCache.evict(
                    any(String.class),
                    any(Cache.class)
            )).thenCallRealMethod();

            when(contentManager.loadContentVO(anyString())).thenReturn(contentVOMock);

            contentManager.addContent(cnt);

            // let's verify that the eviction has been called. it must ALWAYS be invoked even when
            // the local cache is disabled by the feature flag
            mockedStatic.verify(
                    () -> IFContentLocalCache.evict(any(Content.class), eq(localCacheMock)),
                    times(1)
            );
            // the caching is disabled, no eviction in the cache implementation gets called
            verify(localCacheMock, never()).invalidate(anyString());

        } catch (EntException e) {
            throw new RuntimeException(e);
        }
    }
}
