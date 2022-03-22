package cn.itlemon.best.web.dav.aliyun.transaction;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-23
 */
@AllArgsConstructor
public class Transaction implements ITransactionExt {

    private final Principal principal;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }
}
