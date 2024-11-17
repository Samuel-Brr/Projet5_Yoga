package com.openclassrooms.starterjwt.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthEntryPointJwt Tests")
class AuthEntryPointJwtTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private AuthEntryPointJwt authEntryPointJwt;

    private ObjectMapper objectMapper;
    private ByteArrayOutputStream outputStream;

    // Custom ServletOutputStream for testing
    private static class CustomServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream outputStream;

        public CustomServletOutputStream(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // Not needed for testing
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new CustomServletOutputStream(outputStream);
        try {
            when(response.getOutputStream()).thenReturn(servletOutputStream);
        } catch (Exception e) {
            fail("Failed to setup mock response output stream");
        }
    }

    @Nested
    @DisplayName("commence Method Tests")
    class CommenceMethodTests {
        @Test
        @DisplayName("Should set correct response status and content type")
        void shouldSetCorrectResponseStatusAndContentType() throws Exception {
            // Arrange
            when(authException.getMessage()).thenReturn("Unauthorized");
            when(request.getServletPath()).thenReturn("/api/test");

            // Act
            authEntryPointJwt.commence(request, response, authException);

            // Assert
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            // Verify response body is properly formatted
            String responseBody = outputStream.toString();
            assertNotNull(responseBody);
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            assertNotNull(responseMap);
        }
    }
}