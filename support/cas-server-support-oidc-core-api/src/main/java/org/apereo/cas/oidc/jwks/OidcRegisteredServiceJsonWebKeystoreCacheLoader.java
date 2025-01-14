package org.apereo.cas.oidc.jwks;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * This is {@link OidcRegisteredServiceJsonWebKeystoreCacheLoader}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcRegisteredServiceJsonWebKeystoreCacheLoader implements
    CacheLoader<OAuthRegisteredService, Optional<PublicJsonWebKey>> {
    private final ApplicationContext applicationContext;

    @Override
    public Optional<PublicJsonWebKey> load(final @NonNull OAuthRegisteredService service) {
        if (service instanceof OidcRegisteredService) {
            val oidcService = (OidcRegisteredService) service;
            val jwks = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(oidcService, applicationContext);
            if (jwks.isEmpty() || jwks.get().getJsonWebKeys().isEmpty()) {
                return Optional.empty();
            }
            val requestedKid = Optional.ofNullable(oidcService.getJwksKeyId());
            LOGGER.debug("Locating requested key [{}] for service [{}]", requestedKid, oidcService);
            val key = OidcJsonWebKeyStoreUtils.getJsonWebKeyFromJsonWebKeySet(jwks.get(), requestedKid);
            return Optional.ofNullable(key);
        }
        return Optional.empty();
    }
}
