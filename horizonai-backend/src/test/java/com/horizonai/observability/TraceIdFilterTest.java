package com.horizonai.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void clearTraceId() {
        MDC.remove("traceId");
    }

    @Test
    void acceptsSafeIncomingTraceIdAndCleansMdcAfterRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(TraceIdFilter.TRACE_HEADER)).thenReturn("trace-12345678");
        when(response.getStatus()).thenReturn(200);
        doAnswer(invocation -> {
            assertEquals("trace-12345678", MDC.get("traceId"));
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader(TraceIdFilter.TRACE_HEADER, "trace-12345678");
        assertNull(MDC.get("traceId"));
    }

    @Test
    void replacesMissingOrUnsafeTraceIdWithGeneratedValue() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(TraceIdFilter.TRACE_HEADER)).thenReturn("unsafe trace id");
        when(response.getStatus()).thenReturn(200);

        filter.doFilterInternal(request, response, chain);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(headerCaptor.capture(), valueCaptor.capture());
        assertEquals(TraceIdFilter.TRACE_HEADER, headerCaptor.getValue());
        assertTrue(valueCaptor.getValue().matches("[a-f0-9]{32}"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void cleansMdcWhenFilterChainFails() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(TraceIdFilter.TRACE_HEADER)).thenReturn("trace-12345678");
        when(response.getStatus()).thenReturn(500);
        doThrow(new ServletException("downstream failure")).when(chain).doFilter(request, response);

        assertThrows(ServletException.class, () -> filter.doFilterInternal(request, response, chain));
        assertNull(MDC.get("traceId"));
    }
}
