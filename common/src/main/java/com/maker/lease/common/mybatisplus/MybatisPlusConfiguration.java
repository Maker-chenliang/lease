package com.maker.lease.common.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.maker.lease.web.*.mapper")
public class MybatisPlusConfiguration {
}
