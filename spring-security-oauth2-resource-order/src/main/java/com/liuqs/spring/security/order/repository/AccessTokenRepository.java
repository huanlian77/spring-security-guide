package com.liuqs.spring.security.order.repository;

import com.liuqs.spring.security.order.entity.AccessTokenE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 功能描述:
 *
 * @author liuqianshun
 * @date 2019/09/24 9:22
 */
public interface AccessTokenRepository extends JpaRepository<AccessTokenE, Long> {

    AccessTokenE findByToken(String accessToken);

}
