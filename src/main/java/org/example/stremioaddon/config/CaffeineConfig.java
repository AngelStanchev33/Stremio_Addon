package org.example.stremioaddon.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "subsUnacsSearch",      // Level 1: SubsUnacs search cache
            "subsUnacsFiles",       // Level 2: SubsUnacs file cache
            "yavkaSearch",          // Level 1: Yavka search cache
            "yavkaFormData",        // Yavka form data cache
            "yavkaArchives"         // Level 2: Yavka archive cache
        );
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(300)
                        .expireAfterWrite(24, TimeUnit.HOURS)
        );

        return cacheManager;
    }
}
