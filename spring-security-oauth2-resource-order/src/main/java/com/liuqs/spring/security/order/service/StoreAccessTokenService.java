package com.liuqs.spring.security.order.service;

import com.liuqs.spring.security.order.entity.AccessTokenE;
import com.liuqs.spring.security.order.repository.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreAccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    public AccessTokenE.TokenStoreStatus read(String accessToken) {
        AccessTokenE accessTokenE = accessTokenRepository.findByToken(accessToken);
        return accessTokenE.getStatus();
    }


}
