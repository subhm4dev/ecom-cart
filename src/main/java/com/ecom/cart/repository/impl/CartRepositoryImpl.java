package com.ecom.cart.repository.impl;

import com.ecom.cart.model.Cart;
import com.ecom.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis implementation of CartRepository
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepositoryImpl implements CartRepository {
    
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long DEFAULT_TTL_SECONDS = 7 * 24 * 60 * 60; // 7 days
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private String buildKey(UUID tenantId, UUID userId) {
        return CART_KEY_PREFIX + tenantId + ":" + userId;
    }
    
    @Override
    public Optional<Cart> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        String key = buildKey(tenantId, userId);
        Cart cart = (Cart) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(cart);
    }
    
    @Override
    public void save(Cart cart, long ttlSeconds) {
        String key = buildKey(cart.getTenantId(), cart.getUserId());
        redisTemplate.opsForValue().set(key, cart, Duration.ofSeconds(ttlSeconds));
        log.debug("Saved cart to Redis: {}", key);
    }
    
    @Override
    public void save(Cart cart) {
        save(cart, DEFAULT_TTL_SECONDS);
    }
    
    @Override
    public void delete(UUID tenantId, UUID userId) {
        String key = buildKey(tenantId, userId);
        redisTemplate.delete(key);
        log.debug("Deleted cart from Redis: {}", key);
    }
    
    @Override
    public boolean exists(UUID tenantId, UUID userId) {
        String key = buildKey(tenantId, userId);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}

