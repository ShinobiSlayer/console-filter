package io.jenkins.plugins.console;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingResponseWrapper;

@ExtendWith(MockitoExtension.class)
class SimpleConsoleFilterTest {

    private static final String HTML =
            "<span class=\"pipeline-new-node\" nodeid=\"8\" startid=\"8\" enclosingid=\"7\" label=\"Example\">[Pipeline] { (Example)<span class=\"pipeline-show-hide\"> (<a href=\"#\" onclick=\"showHidePipelineSection(this); return false\">hide</a>)</span>\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"9\" enclosingid=\"8\">[Pipeline] echo<span class=\"pipeline-show-hide\"> (<a href=\"#\" onclick=\"showHidePipelineSection(this); return false\">hide</a>)</span>\n"
                    + "</span><span class=\"pipeline-node-9\">echo hi\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"10\" enclosingid=\"8\">[Pipeline] sleep<span class=\"pipeline-show-hide\"> (<a href=\"#\" onclick=\"showHidePipelineSection(this); return false\">hide</a>)</span>\n"
                    + "</span><span class=\"pipeline-node-10\">Sleeping for 30 sec\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"11\" enclosingid=\"8\">[Pipeline] echo<span class=\"pipeline-show-hide\"> (<a href=\"#\" onclick=\"showHidePipelineSection(this); return false\">hide</a>)</span>\n"
                    + "</span><span class=\"pipeline-node-11\">echo hi\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"12\" startid=\"8\">[Pipeline] }\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"13\" startid=\"7\">[Pipeline] // stage\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"14\" startid=\"6\">[Pipeline] }\n"
                    + "</span><span class=\"pipeline-new-node\" nodeid=\"15\" startid=\"5\">[Pipeline] // timeout"
                    + "</span>";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String FILTERED_HTML = "<span class=\"pipeline-node-9\">echo hi\n"
            + "</span><span class=\"pipeline-node-10\">Sleeping for 30 sec\n"
            + "</span><span class=\"pipeline-node-11\">echo hi\n"
            + "</span>";
    public static final int CONTENT_SIZE = 1000;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private FilterChain chain;

    @Mock
    private SimpleConsoleFilterConfigWrapper wrapper;

    @Mock
    private SimpleConsoleFilterConfig config;

    @Mock
    private HttpServletResponseAdapter httpServletResponseAdapter;

    @Mock
    private ContentCachingResponseWrapper contentCachingResponseWrapper;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private ServletOutputStream outputStream;

    private SimpleConsoleFilter filter;

    @BeforeEach
    void setUp() {
        when(wrapper.get()).thenReturn(config);
        filter = new SimpleConsoleFilter(wrapper, httpServletResponseAdapter);
    }

    @Test
    void shouldNotFilterDueToBeingDisabled() throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(false);

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(chain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletRequest, never()).getRequestURI();
    }

    @Test
    void shouldFilterUsingDefaultFilterAsUriPathMatchesDefaultRegex() throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(true);
        when(httpServletRequest.getRequestURI()).thenReturn("/job/test/job/test/17/consoleFull");
        when(httpServletResponseAdapter.adapt(httpServletResponse)).thenReturn(contentCachingResponseWrapper);
        when(contentCachingResponseWrapper.getContentAsByteArray()).thenReturn(HTML.getBytes());
        when(contentCachingResponseWrapper.getCharacterEncoding()).thenReturn(ISO_8859_1);
        when(httpServletResponse.getWriter()).thenReturn(printWriter);

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(httpServletResponse).setContentLength(149);
        verify(printWriter).write(FILTERED_HTML);
        verify(printWriter).flush();
    }

    @Test
    void shouldNotFilterUsingDefaultFilterAsUriPathDoesNotMatchDefaultRegex() throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(true);
        when(httpServletRequest.getRequestURI()).thenReturn("/job/invalid/job/invalid/17/invalid");

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(chain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, never()).setContentLength(149);
        verify(printWriter, never()).write(HTML);
        verify(printWriter, never()).flush();
    }

    @Test
    void shouldFilterUsingDefaultFilterAsUriPathMatchesCustomRegex() throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(true);
        when(config.getRegexes())
                .thenReturn(Arrays.asList(new RegexName("test", "^/job/test/.*/console(Full)?$", true)));
        when(httpServletRequest.getRequestURI()).thenReturn("/job/test/job/test/17/consoleFull");
        when(httpServletResponseAdapter.adapt(httpServletResponse)).thenReturn(contentCachingResponseWrapper);
        when(contentCachingResponseWrapper.getContentAsByteArray()).thenReturn(HTML.getBytes());
        when(contentCachingResponseWrapper.getCharacterEncoding()).thenReturn(ISO_8859_1);
        when(httpServletResponse.getWriter()).thenReturn(printWriter);

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(httpServletResponse).setContentLength(149);
        verify(printWriter).write(FILTERED_HTML);
        verify(printWriter).flush();
    }

    @Test
    void shouldNotFilterUsingDefaultFilterAsUriDoesNotMatchCustomRegex() throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(true);
        when(config.getRegexes())
                .thenReturn(Arrays.asList(new RegexName("test", "^/job/test/.*/console(Full)?$", true)));
        when(httpServletRequest.getRequestURI()).thenReturn("/job/invalid/job/invalid/17/invalid");

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(chain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, never()).setContentLength(149);
        verify(printWriter, never()).write(HTML);
        verify(printWriter, never()).flush();
    }

    @Test
    void shouldNotFilterUsingDefaultFilterAsUriPathMatchesCustomRegexButIsDisabled()
            throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(true);
        when(config.getRegexes())
                .thenReturn(Arrays.asList(new RegexName("test", "^/job/test/.*/console(Full)?$", false)));
        when(httpServletRequest.getRequestURI()).thenReturn("/job/test/job/test/17/consoleFull");

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(chain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, never()).setContentLength(149);
        verify(printWriter, never()).write(HTML);
        verify(printWriter, never()).flush();
    }

    @Test
    void throwsExceptionDueToErrorWithWriter() throws ServletException, IOException {
        when(config.isEnabled()).thenReturn(true);
        when(config.getRegexes())
                .thenReturn(Arrays.asList(new RegexName("test", "^/job/test/.*/console(Full)?$", true)));
        when(httpServletRequest.getRequestURI()).thenReturn("/job/test/job/test/17/consoleFull");
        when(httpServletResponseAdapter.adapt(httpServletResponse)).thenReturn(contentCachingResponseWrapper);
        when(contentCachingResponseWrapper.getContentAsByteArray()).thenReturn(HTML.getBytes());
        when(contentCachingResponseWrapper.getCharacterEncoding()).thenReturn(null);
        when(httpServletResponse.getOutputStream()).thenReturn(outputStream);
        when(contentCachingResponseWrapper.getContentSize()).thenReturn(CONTENT_SIZE);

        filter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(httpServletResponse).setContentLength(CONTENT_SIZE);
        verify(outputStream).write(HTML.getBytes());
        verify(httpServletResponse).flushBuffer();
    }
}
