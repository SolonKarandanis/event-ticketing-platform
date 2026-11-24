package com.etp.ticketservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

// The fail-closed cases need a different app.metrics.token than the integration test's,
// so they're checked here against the filter alone rather than with another full context.
class MetricsTokenFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Authentication filter(String configuredToken, String authorizationHeader)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        new MetricsTokenFilter(configuredToken)
                .doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void theConfiguredTokenAuthenticatesAsTheScraper() throws Exception {
        Authentication authentication = filter("s3cret", "Bearer s3cret");

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(MetricsTokenFilter.SCRAPER_AUTHORITY);
    }

    @Test
    void aWrongTokenOrNoHeaderAuthenticatesNothing() throws Exception {
        assertThat(filter("s3cret", "Bearer s3cre")).isNull();
        assertThat(filter("s3cret", "s3cret")).isNull();
        assertThat(filter("s3cret", null)).isNull();
    }

    @Test
    void anEmptyConfiguredTokenAcceptsNothing() throws Exception {
        assertThat(filter("", "Bearer ")).isNull();
        assertThat(filter("", "Bearer anything")).isNull();
        assertThat(filter(null, "Bearer ")).isNull();
    }
}
