package com.liuqs.spring.security.oauth2.uaa.service;

import com.liuqs.spring.security.oauth2.uaa.entity.AccessTokenE;
import com.liuqs.spring.security.oauth2.uaa.repository.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreAccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;


    public void store(String accessToken, String refreshToken) {
        AccessTokenE accessTokenE = new AccessTokenE()
                .setToken(accessToken)
                .setRefreshToken(refreshToken)
                .setStatus(AccessTokenE.TokenStoreStatus.active);

        accessTokenRepository.save(accessTokenE);
    }

    public int refresh(String refreshToken) {
        return accessTokenRepository.updateByFreshToken(refreshToken, AccessTokenE.TokenStoreStatus.refresh.name());
    }

}
