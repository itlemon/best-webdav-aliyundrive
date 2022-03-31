package cn.itlemon.best.web.dav.aliyun.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.itlemon.best.web.dav.aliyun.constant.AliyunDriveConstant;
import cn.itlemon.best.web.dav.aliyun.constant.FileType;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
import cn.itlemon.best.web.dav.aliyun.service.AliyunDriveWebDavService;
import cn.itlemon.best.web.dav.aliyun.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavServlet;
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

    /**
     * 该构造方法是 {@link WebdavServlet#constructStore(String clazzName, File root)} 在反射构建AliyunDriveWebDavStore对象的时候用的构造方法
     *
     * @param file root File
     */
    public AliyunDriveWebDavStore(File file) {
        log.info("WebdavServlet.constructStore() is init AliyunDriveWebDavStore.");
    }

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
        Transaction tempTransaction = castTransaction(transaction);
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
        Transaction tempTransaction = castTransaction(transaction);
        HttpServletRequest request = tempTransaction.getRequest();
        HttpServletResponse response = tempTransaction.getResponse();

        long contentLength = request.getContentLength();
        if (contentLength < 0) {
            contentLength = Long.parseLong(
                    Optional.ofNullable(request.getHeader(AliyunDriveConstant.CONTENT_LENGTH_HEADER_NAME))
                            .orElse(request.getHeader(AliyunDriveConstant.X_EXPECTED_ENTITY_LENGTH_HEADER_NAME)));
        }
        aliyunDriveWebDavService.uploadPre(resourceUri, contentLength, content);

        if (contentLength == 0) {
            String expect = request.getHeader(AliyunDriveConstant.EXPECT_HEADER_NAME);

            // 这里支持客户端大文件传输的询问请求，当收到100-continue报文后，这边会返回100状态码告诉客户端，服务端支持大文件传输
            if (AliyunDriveConstant.EXPECT_HEADER_VALUE.equalsIgnoreCase(expect)) {
                try {
                    response.sendError(HttpStatus.CONTINUE.value(), HttpStatus.CONTINUE.getReasonPhrase());
                } catch (IOException e) {
                    log.error("AliyunDriveWebDavStore.setResourceContent fail, send Continue to client fail.", e);
                }
                return 0;
            }
        }
        return contentLength;
    }

    @Override
    public String[] getChildrenNames(ITransaction transaction, String folderUri) {
        log.info("AliyunDriveWebDavStore.getChildrenNames({})", folderUri);
        AliyunDriveFile aliyunDriveFile = aliyunDriveWebDavService.getAliyunDriveFile(folderUri);
        if (aliyunDriveFile.getType().equals(FileType.FILE.getType())) {
            // 说明是文件，没有Children
            return new String[0];
        }
        Set<AliyunDriveFile> aliyunDriveFileSet =
                aliyunDriveWebDavService.getChildrenFiles(aliyunDriveFile.getFileId());
        return aliyunDriveFileSet.stream().map(AliyunDriveFile::getName).toArray(String[]::new);
    }

    @Override
    public long getResourceLength(ITransaction transaction, String resourceUri) {
        log.info("AliyunDriveWebDavStore.getResourceLength({})", resourceUri);
        AliyunDriveFile aliyunDriveFile = aliyunDriveWebDavService.getAliyunDriveFile(resourceUri);
        if (aliyunDriveFile == null || aliyunDriveFile.getSize() == null) {
            return 384;
        }
        return aliyunDriveFile.getSize();
    }

    @Override
    public void removeObject(ITransaction transaction, String uri) {
        log.info("AliyunDriveWebDavStore.removeObject({})", uri);
        aliyunDriveWebDavService.remove(uri);
    }

    @Override
    public StoredObject getStoredObject(ITransaction transaction, String uri) {
        log.info("AliyunDriveWebDavStore.getStoredObject({}), aliyunDriveWebDavService: {}", uri,
                aliyunDriveWebDavService);
        AliyunDriveFile aliyunDriveFile = aliyunDriveWebDavService.getAliyunDriveFile(uri);
        if (aliyunDriveFile != null) {
            StoredObject so = new StoredObject();
            so.setFolder(FileType.FOLDER.getType().equalsIgnoreCase(aliyunDriveFile.getType()));
            so.setResourceLength(getResourceLength(transaction, uri));
            so.setCreationDate(aliyunDriveFile.getCreateAt());
            so.setLastModified(aliyunDriveFile.getUpdatedAt());
            return so;
        }
        return null;
    }

    /**
     * 将ITransaction强转成Transaction
     *
     * @param transaction transaction
     * @return Transaction对象
     */
    private Transaction castTransaction(ITransaction transaction) {
        if (transaction instanceof Transaction) {
            return (Transaction) transaction;
        } else {
            throw new WebdavException("AliyunDriveWebDavStore not support other Transaction");
        }
    }

}
