package com.liuqs.spring.security.order.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Table(name = "oauth_access_token")
@Data
@Entity
@Accessors(chain = true)
public class AccessTokenE {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Enumerated(EnumType.STRING)
    private TokenStoreStatus status;

    public static enum TokenStoreStatus {
        active,
        refresh;
    }
}
