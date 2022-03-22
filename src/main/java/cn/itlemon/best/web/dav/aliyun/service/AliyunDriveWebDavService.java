package cn.itlemon.best.web.dav.aliyun.service;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-23
 */
@Slf4j
@Component
public class AliyunDriveWebDavService {


    /**
     * 创建文件夹
     *
     * @param folderUri 文件夹uri
     */
    public void createFolder(String folderUri) {

    }

    /**
     * 创建资源，阿里云盘暂无创建资源的能力，所以这里不再实现相关功能
     *
     * @param resourceUri 资源uri
     */
    public void createResource(String resourceUri) {
        // do nothing
    }

    /**
     * 下载文件
     *
     * @param resourceUri 资源uri
     * @param request 请求
     * @param size 文件大小
     * @return 响应体
     */
    public Response download(String resourceUri, HttpServletRequest request, long size) {
        return null;
    }

    public void uploadPre(String resourceUri, long contentLength, InputStream content) {

    }
}
