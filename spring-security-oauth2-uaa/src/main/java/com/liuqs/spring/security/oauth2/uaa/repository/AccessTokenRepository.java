package com.liuqs.spring.security.oauth2.uaa.repository;

import com.liuqs.spring.security.oauth2.uaa.entity.AccessTokenE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

/**
 * 功能描述:
 *
 * @author liuqianshun
 * @date 2019/09/24 9:22
 */
public interface AccessTokenRepository extends JpaRepository<AccessTokenE, Long> {


    @Transactional
    @Modifying
    @Query(value = "update oauth_access_token t set t.status = ?2 where t.refresh_token = ?1 and t.status = 'active'",nativeQuery = true)
    int updateByFreshToken(String refreshToken, String status);
}
