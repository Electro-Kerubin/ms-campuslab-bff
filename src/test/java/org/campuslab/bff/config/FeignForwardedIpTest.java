package org.campuslab.bff.config;

import static org.assertj.core.api.Assertions.assertThat;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** El BFF reenvía a los microservicios el token y la IP del cliente original (la auditoría registra "desde dónde"). */
class FeignForwardedIpTest {

    private final RequestInterceptor interceptor = new FeignConfig().authorizationForwardingInterceptor();

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void reenviaElTokenYLaIpQueLlegaDeApiManagement() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc");
        request.addHeader("X-Forwarded-For", "200.1.2.3, 20.206.176.6");

        RequestTemplate template = apply(request);

        assertThat(template.headers().get("Authorization")).containsExactly("Bearer abc");
        assertThat(template.headers().get("X-Forwarded-For")).containsExactly("200.1.2.3, 20.206.176.6");
    }

    @Test
    void sinXForwardedForUsaLaIpDirectaDeQuienLlamoAlBff() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.50");

        RequestTemplate template = apply(request);

        assertThat(template.headers().get("X-Forwarded-For")).containsExactly("192.168.1.50");
        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    void fueraDeUnaPeticionHttpNoHaceNada() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).isEmpty();
    }

    private RequestTemplate apply(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);
        return template;
    }
}
