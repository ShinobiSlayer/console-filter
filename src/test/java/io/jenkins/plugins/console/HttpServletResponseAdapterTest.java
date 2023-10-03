package io.jenkins.plugins.console;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingResponseWrapper;

@ExtendWith(MockitoExtension.class)
class HttpServletResponseAdapterTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    private HttpServletResponseAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new HttpServletResponseAdapter();
    }

    @Test
    void adapt() {
        ContentCachingResponseWrapper wrapper = adapter.adapt(httpServletResponse);
        assertNotNull(wrapper);
    }
}
