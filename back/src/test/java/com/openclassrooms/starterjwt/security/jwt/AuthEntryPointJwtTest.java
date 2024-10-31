package com.openclassrooms.starterjwt.security.jwt;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        @DisplayName("Should log error message")
        void shouldLogErrorMessage() throws Exception {
            // Arrange
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = loggerContext.getLogger(AuthEntryPointJwt.class);

            ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
            listAppender.start();
            logger.addAppender(listAppender);

            String errorMessage = "Test unauthorized error";
            when(authException.getMessage()).thenReturn(errorMessage);
            when(request.getServletPath()).thenReturn("/api/test");

            try {
                // Act
                authEntryPointJwt.commence(request, response, authException);

                // Assert
                boolean found = listAppender.list.stream()
                        .anyMatch(event ->
                                event.getLevel() == Level.ERROR &&
                                        event.getFormattedMessage().contains("Unauthorized error: " + errorMessage));

                assertTrue(found, "Expected error log message not found");
            } finally {
                // Cleanup
                logger.detachAppender(listAppender);
            }
        }

        @Test
        @DisplayName("Should handle IOException during response writing")
        void shouldHandleIOException() throws Exception {
            // Arrange
            when(authException.getMessage()).thenReturn("Unauthorized");
            when(request.getServletPath()).thenReturn("/api/test");
            IOException mockIOException = new IOException("Test IO Exception");
            when(response.getOutputStream()).thenThrow(mockIOException);

            // Act & Assert
            IOException thrown = assertThrows(IOException.class, () -> authEntryPointJwt.commence(request, response, authException));

            assertEquals("Test IO Exception", thrown.getMessage());
        }

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

        @Test
        @DisplayName("Should write correct error response body")
        void shouldWriteCorrectErrorResponseBody() throws Exception {
            // Arrange
            String errorMessage = "Test unauthorized error";
            String servletPath = "/api/test";

            when(authException.getMessage()).thenReturn(errorMessage);
            when(request.getServletPath()).thenReturn(servletPath);

            // Act
            authEntryPointJwt.commence(request, response, authException);

            // Assert
            String responseBody = outputStream.toString();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, responseMap.get("status"));
            assertEquals("Unauthorized", responseMap.get("error"));
            assertEquals(errorMessage, responseMap.get("message"));
            assertEquals(servletPath, responseMap.get("path"));
        }

        @Test
        @DisplayName("Should handle null error message")
        void shouldHandleNullErrorMessage() throws Exception {
            // Arrange
            when(authException.getMessage()).thenReturn(null);
            when(request.getServletPath()).thenReturn("/api/test");

            // Act
            authEntryPointJwt.commence(request, response, authException);

            // Assert
            String responseBody = outputStream.toString();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            assertNull(responseMap.get("message"));
        }

        @Test
        @DisplayName("Should handle null servlet path")
        void shouldHandleNullServletPath() throws Exception {
            // Arrange
            when(authException.getMessage()).thenReturn("Unauthorized");
            when(request.getServletPath()).thenReturn(null);

            // Act
            authEntryPointJwt.commence(request, response, authException);

            // Assert
            String responseBody = outputStream.toString();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            assertNull(responseMap.get("path"));
        }
    }
}