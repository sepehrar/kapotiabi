package com.kapotiabi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtTokenProvider);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void noAuthorizationHeader_doesNotPopulateContext_andCallsChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtTokenProvider, never()).parseToken(any());
  }

  @Test
  void headerWithoutBearerPrefix_doesNotPopulateContext_andCallsChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtTokenProvider, never()).parseToken(any());
  }

  @Test
  void validToken_populatesSecurityContext_withRolePrefixedAuthority() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("42");
    when(claims.get(eq("role"), eq(String.class))).thenReturn("car_owner");
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(claims);

    filter.doFilter(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getPrincipal()).isEqualTo("42");
    assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_CAR_OWNER");
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  void validTokenLowercaseRole_isUppercasedToRoleAuthority() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("7");
    when(claims.get(eq("role"), eq(String.class))).thenReturn("admin");
    when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
    when(jwtTokenProvider.parseToken("some-token")).thenReturn(claims);

    filter.doFilter(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
  }

  @Test
  void parseTokenThrowsJwtException_doesNotPopulateContext_butStillCallsChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer broken-token");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/users/me");
    when(jwtTokenProvider.parseToken("broken-token"))
        .thenThrow(new MalformedJwtException("malformed"));

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  void emptyBearerToken_attemptsParseAndIsRejected() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer ");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/users/me");
    when(jwtTokenProvider.parseToken("")).thenThrow(new MalformedJwtException("empty"));

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  void parseTokenThrowsIllegalArgumentException_isCaught_andChainContinues() throws Exception {
    // JJWT 0.12+ throws IllegalArgumentException for empty/null token strings.
    when(request.getHeader("Authorization")).thenReturn("Bearer ");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/users/me");
    when(jwtTokenProvider.parseToken(""))
        .thenThrow(new IllegalArgumentException("CharSequence cannot be null or empty."));

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  void chainAlwaysCalledExactlyOnce_onSuccess() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("1");
    when(claims.get(eq("role"), eq(String.class))).thenReturn("car_owner");
    when(request.getHeader("Authorization")).thenReturn("Bearer t");
    when(jwtTokenProvider.parseToken("t")).thenReturn(claims);

    filter.doFilter(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }
}
