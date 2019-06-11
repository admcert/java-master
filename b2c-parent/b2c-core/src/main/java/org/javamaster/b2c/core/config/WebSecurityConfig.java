package org.javamaster.b2c.core.config;

import org.javamaster.b2c.core.enums.BizExceptionEnum;
import org.javamaster.b2c.core.exception.BizException;
import org.javamaster.b2c.core.handler.LoginHandler;
import org.javamaster.b2c.core.mapper.ManualSecurityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author yudong
 * @date 2019/6/10
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ManualSecurityMapper manualSecurityMapper;
    @Autowired
    private LoginHandler loginHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/json/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginProcessingUrl("/core/login")
                .successHandler(loginHandler::onAuthenticationSuccess)
                .failureHandler(loginHandler::onAuthenticationFailure)
                .and()
                .logout()
                .clearAuthentication(true)
                .logoutSuccessHandler(loginHandler::onLogoutSuccess)
                .and()
                .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            UserDetails userDetails = manualSecurityMapper.selectUser(username);
            if (userDetails == null) {
                throw new BizException(BizExceptionEnum.INVALID_USER);
            }
            return userDetails;
        }).passwordEncoder(new BCryptPasswordEncoder());
    }

}
