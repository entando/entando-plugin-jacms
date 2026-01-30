package org.entando.entando.plugins.jacms.aps.system.config;

import com.agiletec.plugins.jacms.aps.system.services.content.IFContentLocalCache;
import com.github.benmanes.caffeine.cache.Cache;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LocalCacheConfig implements IFContentLocalCache {

    private final Environment env;
    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    public LocalCacheConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "localEditContentCache")
    public Cache<String, Object> localCache() {

        long maxSize = env.getProperty("CMS_LOCAL_CACHE_MAX_SIZE", Long.class, 50L);
        long expireMinutes = env.getProperty("CMS_LOCAL_CACHE_EXPIRE_MINUTES", Long.class, 5L);
        boolean stats = env.getProperty("CMS_LOCAL_CACHE_STATS", Boolean.class, false);

        logger.info("Configuring Local Cache: maxSize={}, expireMinutes={}, stats={}",
                maxSize, expireMinutes, stats);

        return IFContentLocalCache.instantiateLocalCache(maxSize, expireMinutes, stats);
    }

}
