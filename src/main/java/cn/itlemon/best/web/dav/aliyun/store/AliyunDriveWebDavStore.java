package cn.itlemon.best.web.dav.aliyun.store;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.util.concurrent.RateLimiter;

import cn.itlemon.best.web.dav.aliyun.service.AliyunDriveWebDavService;
import cn.itlemon.best.web.dav.aliyun.transaction.ITransactionExt;
import cn.itlemon.best.web.dav.aliyun.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;
import okhttp3.Response;

/**
 * 阿里云网盘的WebDav实现层
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-23
 */
@Slf4j
public class AliyunDriveWebDavStore implements IWebdavStore {

    private static final RateLimiter LOG_RATE_LIMITER = RateLimiter.create(0.5);

    /**
     * 这里将Spring Bean AliyunDriveWebDavService 设置为静态变量，方便全局使用
     */
    private static AliyunDriveWebDavService aliyunDriveWebDavService;

    /**
     * 将初始化好的aliyunDriveWebDavService设置到全局变量中，该方法在Spring容器初始化AliyunDriveWebDavService后调用一次
     *
     * @param aliyunDriveWebDavService aliyunDriveWebDavService对象
     */
    public static void setAliyunDriveWebDavService(AliyunDriveWebDavService aliyunDriveWebDavService) {
        AliyunDriveWebDavStore.aliyunDriveWebDavService = aliyunDriveWebDavService;
    }

    @Override
    public ITransaction begin(Principal principal) {
        log.info("AliyunDriveWebDavStore.begin()");
        // 这里获取HttpServletRequest和HttpServletResponse
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        return new Transaction(principal, request, response);
    }

    @Override
    public void checkAuthentication(ITransaction transaction) {
        log.info("AliyunDriveWebDavStore.checkAuthentication()");
        // do nothing
    }

    @Override
    public void commit(ITransaction transaction) {
        log.info("AliyunDriveWebDavStore.commit()");
        // do nothing
    }

    @Override
    public void rollback(ITransaction transaction) {
        log.info("AliyunDriveWebDavStore.rollback()");
        // do nothing
    }

    @Override
    public void createFolder(ITransaction transaction, String folderUri) {
        log.info("AliyunDriveWebDavStore.createFolder({})", folderUri);
        aliyunDriveWebDavService.createFolder(folderUri);
    }

    @Override
    public void createResource(ITransaction transaction, String resourceUri) {
        log.info("AliyunDriveWebDavStore.createResource({})", resourceUri);
        aliyunDriveWebDavService.createResource(resourceUri);
    }

    @Override
    public InputStream getResourceContent(ITransaction transaction, String resourceUri) {
        log.info("AliyunDriveWebDavStore.getResourceContent({})", resourceUri);
        Transaction tempTransaction;
        if (transaction instanceof ITransactionExt) {
            tempTransaction = (Transaction) transaction;
        } else {
            throw new WebdavException("AliyunDriveWebDavStore not support other Transaction");
        }
        HttpServletRequest request = tempTransaction.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("AliyunDriveWebDavStore.getResourceContent({}) has header: {} = {}", resourceUri, headerName,
                    request.getHeader(headerName));
        }
        HttpServletResponse response = tempTransaction.getResponse();
        long size = getResourceLength(transaction, resourceUri);
        Response resourceResponse = aliyunDriveWebDavService.download(resourceUri, request, size);
        assert resourceResponse.body() != null;
        response.setContentLengthLong(resourceResponse.body().contentLength());
        log.debug("AliyunDriveWebDavStore.getResourceContent({}) response code: {}", resourceUri,
                resourceResponse.code());
        for (String headerName : resourceResponse.headers().names()) {
            log.debug("AliyunDriveWebDavStore.getResourceContent({}) resourceResponse: {} = {}", resourceUri,
                    headerName, resourceResponse.header(headerName));
            response.addHeader(headerName, resourceResponse.header(headerName));
        }
        response.setStatus(resourceResponse.code());
        return resourceResponse.body().byteStream();
    }

    @Override
    public long setResourceContent(ITransaction transaction, String resourceUri, InputStream content,
            String contentType, String characterEncoding) {
        log.info("AliyunDriveWebDavStore.setResourceContent({})", resourceUri);
        Transaction tempTransaction;
        if (transaction instanceof ITransactionExt) {
            tempTransaction = (Transaction) transaction;
        } else {
            throw new WebdavException("AliyunDriveWebDavStore not support other Transaction");
        }
        HttpServletRequest request = tempTransaction.getRequest();
        HttpServletResponse response = tempTransaction.getResponse();

        long contentLength = request.getContentLength();
        if (contentLength < 0) {
            contentLength = Long.parseLong(Optional.ofNullable(request.getHeader("content-length"))
                    .orElse(request.getHeader("X-Expected-Entity-Length")));
        }
        aliyunDriveWebDavService.uploadPre(resourceUri, contentLength, content);

        if (contentLength == 0) {
            String expect = request.getHeader("Expect");

            // 支持大文件上传
            if ("100-continue".equalsIgnoreCase(expect)) {
                try {
                    response.sendError(100, "Continue");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }
        return contentLength;
    }

    @Override
    public String[] getChildrenNames(ITransaction transaction, String folderUri) {
        return new String[0];
    }

    @Override
    public long getResourceLength(ITransaction transaction, String resourceUri) {
        return 0;
    }

    @Override
    public void removeObject(ITransaction transaction, String uri) {

    }

    @Override
    public StoredObject getStoredObject(ITransaction transaction, String uri) {
        return null;
    }
}
