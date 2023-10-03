package io.jenkins.plugins.console;

import javax.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class HttpServletResponseAdapter {

    public ContentCachingResponseWrapper adapt(HttpServletResponse httpServletResponse) {
        return new ContentCachingResponseWrapper(httpServletResponse);
    }
}
