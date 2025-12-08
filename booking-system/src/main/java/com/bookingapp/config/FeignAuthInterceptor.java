package com.bookingapp.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {
    private static final String AUTH_HEADER = "Authorization";
    private static final String X_AUTH_ROLES = "X-Auth-Roles";
    private static final String X_AUTH_USERNAME = "X-Auth-Username";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        HttpServletRequest request = attrs.getRequest();

        String auth = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(auth)) {
            template.header(AUTH_HEADER, auth);
        }

        String roles = request.getHeader(X_AUTH_ROLES);
        if (StringUtils.hasText(roles)) {
            template.header(X_AUTH_ROLES, roles);
        }

        String uname = request.getHeader(X_AUTH_USERNAME);
        if (StringUtils.hasText(uname)) {
            template.header(X_AUTH_USERNAME, uname);
        }
    }
}
