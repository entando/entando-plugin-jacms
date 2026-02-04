package com.agiletec.plugins.jacms.aps.system.services.content;

import com.agiletec.aps.system.ApsSystemUtils.ApsDeepDebug;
import com.agiletec.aps.system.common.entity.model.attribute.AbstractListAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.CompositeAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.ListAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.MonoListAttribute;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SymbolicLink;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.LinkAttribute;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.entando.entando.aps.system.services.IFeatureFlag;

public interface IFContentLocalCache extends IFeatureFlag {
    boolean LOCAL_CMS_CACHE_ENABLED = checkEnabled(); // useful for test

    String CMS_LOCAL_CACHE = "cms-local-cache";

    default boolean isEnabled() {
        return LOCAL_CMS_CACHE_ENABLED;
    }

    static boolean checkEnabled() {
        return IFeatureFlag.readEnablementStatus("LOCAL_CMS_CACHE");
    }

    /**
     *
     * @param id the content id used as a key in the cache
     * @param localCache the current Cache object
     * @param action the action to perform in case of cache miss
     * @return the contentRecordVO found in cache or loaded from the database
     */
    static ContentRecordVO loadAndCacheContentVO(final String id,
            final Cache<String, Object> localCache,
            final Supplier<ContentRecordVO> action) {

        if (action == null)
            return null;
        if (checkEnabled()
                && localCache != null) {
            if (ApsDeepDebug.isTagEnabled(CMS_LOCAL_CACHE)) {
                if (localCache.asMap().containsKey(id)) {
                    ApsDeepDebug.print(CMS_LOCAL_CACHE,"cache HIT " + id);
                } else {
                    ApsDeepDebug.print(CMS_LOCAL_CACHE,"cache miss " + id);
                }
            }
            return (ContentRecordVO) localCache.get(id, key -> action.get());
        } else {
            return action.get();
        }
    }

    /**
     * Evict a single key from the cache given the content object
     * @param content the content object associated with the key to remove
     * @param localCache the cache instance
     */
    static void evict(final Content content, final Cache<String, Object> localCache) {
        if (content != null) {
            evict(content.getId(), localCache);
        }
    }

    /**
     * Evict a single key from the cache
     * @param key the key associated with the object to remove
     * @param localCache the cache instance
     */
    static void evict(final String key, final Cache<String, Object> localCache) {
        if (checkEnabled()
                && localCache != null
                && StringUtils.isNotBlank(key)) {
            ApsDeepDebug.print(CMS_LOCAL_CACHE, "Evicting key from cache: " + key);
            localCache.invalidate(key);
        }
    }

    /**
     * Evict multiple keys from the cache
     * @param keys the list of keys to remove
     * @param localCache the cache instance
     */
    static void evict(final List<String> keys, final Cache<String, Object> localCache) {
        if (checkEnabled()
                && localCache != null
                && keys != null) {
            ApsDeepDebug.print(CMS_LOCAL_CACHE, "Evicting keys from cache: " + keys);
            localCache.invalidateAll(keys);
        }
    }

    /**
     * Flush the cache references for a given content. This gets called typically from content actions
     * @param content the content in session
     * @param cm the content manager instance
     */
    static void flushReferences(final Content content,IContentManager cm) {
        try {
            if (checkEnabled()
                    && cm != null) {
                List<String> refs = getContentReferences(content);
                cm.evict(refs);
            }
        } catch (Exception e) {
            ApsDeepDebug.print(CMS_LOCAL_CACHE, "Error cleaning cache when flushing references from action");
        }
    }

    /**
     * Get the contents referenced by analyzing the attributes list
     * @param content the content in session
     * @return the list of the references
     */
    static List<String> getContentReferences(final Content content) {
        final List<String> references = new ArrayList<>();

        if (content != null) {
            content.getAttributeList()
                    .forEach(a -> {
                        if (a instanceof LinkAttribute) {
                            processLinkAttribute((LinkAttribute) a, references);
                        }
                        if (a instanceof CompositeAttribute) {
                            processCompositeAttribute((CompositeAttribute) a, references);
                        }
                        if (a instanceof ListAttribute) {
                            processListAttribute((ListAttribute) a, references);
                        }
                        if (a instanceof MonoListAttribute) {
                            processListAttribute((MonoListAttribute) a, references);
                        }
                    });
        }
        return references;
    }

    static void processListAttribute(final AbstractListAttribute attr, final List<String> references) {
        if (attr != null) {
            attr.getAttributes().forEach(ca -> {
                if (ca instanceof LinkAttribute) {
                    processLinkAttribute((LinkAttribute) ca, references);
                }
                if (ca instanceof CompositeAttribute) {
                    processCompositeAttribute((CompositeAttribute) ca, references);
                }
            });
        }
    }

    static void processCompositeAttribute(final CompositeAttribute attr, final List<String> references) {
        if (attr != null) {
            attr.getAttributes().forEach(ca -> {
                if (ca instanceof LinkAttribute) {
                    processLinkAttribute((LinkAttribute) ca, references);
                }
            });
        }
    }

    static void processLinkAttribute(final LinkAttribute attr, final List<String> reference) {
        if (attr != null && attr.getValue() instanceof SymbolicLink) {
            final SymbolicLink l = (SymbolicLink) attr.getValue();

            if (l.getDestType() == SymbolicLink.CONTENT_TYPE) {
                reference.add(l.getContentDest());
            }
        }
    }

    static Cache<String, Object> instantiateLocalCache(long maxSize, long expireMinutes, boolean stats) {
        if (checkEnabled()) {
            Caffeine<Object, Object> builder =
                    com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                            .maximumSize(maxSize)
                            .expireAfterAccess(expireMinutes, TimeUnit.MINUTES);
            if (stats) {
                builder.recordStats();
            }
            return builder.build();
        }
        // effectively a no-op!
        return Caffeine.newBuilder()
                .maximumSize(0).build();
    }
}
