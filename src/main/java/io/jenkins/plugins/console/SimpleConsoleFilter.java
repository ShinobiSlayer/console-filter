package io.jenkins.plugins.console;

import hudson.Extension;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Extension
public class SimpleConsoleFilter implements Filter {

    private static final String PIPELINE_SHOW_HIDE_SPAN_REGEX = "<span class=\"pipeline-show-hide\".*>.*(?:</span>)\n?";
    private static final String PIPELINE_NEW_NODE_SPAN_REGEX = "(?s)<span class=\"pipeline-new-node\".*?>.*?</span>";
    private static final List<RegexName> DEFAULT_PATH_REGEX =
            Arrays.asList(new RegexName("Default regex", "/.*/console(Full)?$", true));
    private static final Logger LOGGER = Logger.getLogger(SimpleConsoleFilter.class.getName());

    private SimpleConsoleFilterConfigWrapper configWrapper;
    private HttpServletResponseAdapter httpServletResponseAdapter;

    public SimpleConsoleFilter() {
        this(new SimpleConsoleFilterConfigWrapper(), new HttpServletResponseAdapter());
    }

    public SimpleConsoleFilter(
            SimpleConsoleFilterConfigWrapper configWrapper, HttpServletResponseAdapter httpServletResponseAdapter) {
        this.configWrapper = configWrapper;
        this.httpServletResponseAdapter = httpServletResponseAdapter;
    }

    @Initializer
    public static void init() throws ServletException {
        LOGGER.log(Level.INFO, "Adding SimpleConsoleFilter to PluginServlet Filter");
        PluginServletFilter.addFilter(new SimpleConsoleFilter());
    }

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.log(Level.INFO, "Initializing SimpleConsoleFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        SimpleConsoleFilterConfig config = configWrapper.get();
        if (isEnabled(config)) {
            LOGGER.log(Level.FINEST, "Simple Console Log filtering enabled: true");
            filterConsoleLog(httpServletRequest, httpServletResponse, chain, config);
        } else {
            LOGGER.log(Level.FINEST, "Simple Console Log filtering enabled: false");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {}

    private void filterConsoleLog(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            FilterChain chain,
            SimpleConsoleFilterConfig config)
            throws IOException, ServletException {
        String requestUri = httpServletRequest.getRequestURI();
        LOGGER.log(Level.FINEST, "Request Uri: " + requestUri);
        if (isFilterRequired(requestUri, config)) {
            handleConsoleLog(httpServletRequest, httpServletResponse, chain);
        } else {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    private void handleConsoleLog(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain)
            throws IOException, ServletException {
        ContentCachingResponseWrapper responseWrapper = httpServletResponseAdapter.adapt(httpServletResponse);
        chain.doFilter(httpServletRequest, responseWrapper);
        try {
            String html = createPayLoadFromByteArray(
                    responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
            String filteredHtml = filterHtml(html);
            httpServletResponse.setContentLength(filteredHtml.length());
            httpServletResponse.getWriter().write(filteredHtml);
            httpServletResponse.getWriter().flush();
        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Please contact your developer about this ERROR !" + e.getClass() + " : " + e.getMessage(),
                    e);
            httpServletResponse.setContentLength(responseWrapper.getContentSize());
            httpServletResponse.getOutputStream().write(responseWrapper.getContentAsByteArray());
            httpServletResponse.flushBuffer();
        }
    }

    private String filterHtml(String html) {
        String filteredHtml = html.replaceAll(PIPELINE_SHOW_HIDE_SPAN_REGEX, StringUtils.EMPTY)
                .replaceAll(PIPELINE_NEW_NODE_SPAN_REGEX, StringUtils.EMPTY);
        return filteredHtml;
    }

    private String createPayLoadFromByteArray(byte[] requestBuffer, String charEncoding) {
        try {
            return new String(requestBuffer, charEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isEnabled(SimpleConsoleFilterConfig config) {
        return config.isEnabled();
    }

    private boolean isFilterRequired(String requestUri, SimpleConsoleFilterConfig config) {
        List<RegexName> regexes = findRegexes(config);
        for (RegexName regex : regexes) {
            boolean matched = regex.getCompiledRegex().matcher(requestUri).find();
            LOGGER.log(
                    Level.FINEST,
                    String.format(
                            "Request Uri: %s Matched: %s Name: %s Regex: %s Enabled: %s",
                            requestUri,
                            matched ? "true" : "false",
                            regex.getName(),
                            regex.getRegex(),
                            regex.isRegexEnabled() ? "true" : "false"));
            if (isEnabledAndMatches(regex, matched)) {
                return true;
            }
        }
        LOGGER.log(Level.FINEST, String.format("Request Uri: %s did not match any regex", requestUri));
        return false;
    }

    private List<RegexName> findRegexes(SimpleConsoleFilterConfig config) {
        if (CollectionUtils.isNotEmpty(config.getRegexes())) {
            LOGGER.log(Level.FINEST, "Regexes have been supplied.");
            return config.getRegexes();
        }
        LOGGER.log(Level.FINEST, "Regexes have not been supplied. Falling back to default regex.");
        return DEFAULT_PATH_REGEX;
    }

    private boolean isEnabledAndMatches(RegexName regex, boolean matched) {
        return regex.isRegexEnabled() && matched;
    }
}
