package io.github.khram0v.gymcrm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationEntryPointTest {

    @Mock private SecurityResponseWriter responseWriter;
    @InjectMocks private RestAuthenticationEntryPoint entryPoint;

    @Test
    void commence_delegatesToResponseWriter_withUnauthorizedStatus() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("boom"));

        verify(responseWriter).write(request, response, HttpStatus.UNAUTHORIZED,
                "Missing or invalid authentication token");
    }
}
