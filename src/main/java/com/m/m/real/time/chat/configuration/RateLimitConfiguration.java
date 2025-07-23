package com.m.m.real.time.chat.configuration;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Principal;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
public class RateLimitConfiguration {


    private final long rateLimitCapacity;

    private final long rateLimitRefillTokens;
    private final long rateLimitRefillInterval;

    private final ConcurrentMap<String, Bucket> bucketList = new ConcurrentHashMap<>();


    public RateLimitConfiguration(
            @Value("${bucket4j.rate-limit-capacity}") long rateLimitCapacity,
            @Value("${bucket4j.rate-limit-refill-tokens}") long rateLimitRefillTokens,
            @Value("${bucket4j.rate-limit-refill-interval}") long rateLimitRefillInterval
           ) {
        this.rateLimitCapacity = rateLimitCapacity;
        this.rateLimitRefillTokens = rateLimitRefillTokens;
        this.rateLimitRefillInterval = rateLimitRefillInterval;

    }

    public boolean isRateLimitExceeded(Principal principal) {
        String sender = principal.getName();
        Bucket bucket = bucketList.computeIfAbsent(sender, user -> createNewBucket());

        return !bucket.tryConsume(1);
    }

    private Bucket createNewBucket() {
        return Bucket.builder().addLimit(limit -> limit
                        .capacity(rateLimitCapacity)
                        .refillGreedy(rateLimitRefillTokens, Duration.ofSeconds(rateLimitRefillInterval)))
                .build();
    }

}
