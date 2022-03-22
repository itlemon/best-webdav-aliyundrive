package cn.itlemon.best.web.dav.aliyun.transaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.ITransaction;

/**
 * 扩展接口ITransaction
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-23
 */
public interface ITransactionExt extends ITransaction {

    /**
     * 获取请求
     *
     * @return 请求
     */
    HttpServletRequest getRequest();

    /**
     * 获取响应
     *
     * @return 响应
     */
    HttpServletResponse getResponse();

}
