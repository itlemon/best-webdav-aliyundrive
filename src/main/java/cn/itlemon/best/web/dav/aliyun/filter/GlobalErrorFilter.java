package cn.itlemon.best.web.dav.aliyun.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;
import net.sf.webdav.WebdavStatus;

/**
 * 此代码参考zxbu
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-30
 */
@Slf4j
public class GlobalErrorFilter extends OncePerRequestFilter {

    private static String ERROR_PAGE;

    static {
        try {
            ClassPathResource classPathResource = new ClassPathResource("error.xml");
            InputStream inputStream = classPathResource.getInputStream();
            byte[] buffer = new byte[(int) classPathResource.contentLength()];
            IOUtils.readFully(inputStream, buffer);
            ERROR_PAGE = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ERROR_PAGE = "";
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            FilterChain filterChain) throws IOException {
        ErrorWrapperResponse wrapperResponse = new ErrorWrapperResponse(httpServletResponse);

        try {
            filterChain.doFilter(httpServletRequest, wrapperResponse);
            if (wrapperResponse.hasErrorToSend()) {
                int status = wrapperResponse.getStatus();
                if (status == 401) {
                    log.warn("http status: {}", status);
                }
                httpServletResponse.setStatus(status);
                String message = wrapperResponse.getMessage();
                if (message == null) {
                    message = WebdavStatus.getStatusText(status);
                }
                String errorXml = ERROR_PAGE.replace("{{code}}", status + "").replace("{{message}}", message);
                httpServletResponse.getWriter().write(errorXml);
            }
            httpServletResponse.flushBuffer();
        } catch (Throwable t) {
            httpServletResponse.setStatus(500);
            httpServletResponse.getWriter().write(t.getMessage());
            httpServletResponse.flushBuffer();
        }
    }

    private static class ErrorWrapperResponse extends HttpServletResponseWrapper {
        private int status;
        private String message;
        private boolean hasErrorToSend = false;

        ErrorWrapperResponse(HttpServletResponse response) {
            super(response);
        }

        public void sendError(int status) throws IOException {
            this.sendError(status, (String) null);
        }

        public void sendError(int status, String message) throws IOException {
            this.status = status;
            this.message = message;
            this.hasErrorToSend = true;
        }

        public int getStatus() {
            return this.hasErrorToSend ? this.status : super.getStatus();
        }

        public void flushBuffer() throws IOException {
            super.flushBuffer();
        }


        String getMessage() {
            return this.message;
        }

        boolean hasErrorToSend() {
            return this.hasErrorToSend;
        }

        public PrintWriter getWriter() throws IOException {
            return super.getWriter();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            return super.getOutputStream();
        }
    }
}
