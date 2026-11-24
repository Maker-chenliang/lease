package com.maker.lease.common.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private String username;
    private String userIdentity;
}