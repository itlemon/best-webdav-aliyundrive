package cn.itlemon.best.web.dav.aliyun.config;

import java.security.Principal;
import java.util.Collections;

import org.apache.catalina.CredentialHandler;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
import org.apache.catalina.realm.RealmBase;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-31
 */
@Component
@ConditionalOnProperty(prefix = "aliyundrive.base.auth", name = "enabled", matchIfMissing = true)
public class AliyunDriveWebDavAuthTomcatConfig implements
        WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>, Ordered {

    private final AliyunDriveConfig aliyunDriveConfig;

    public AliyunDriveWebDavAuthTomcatConfig(AliyunDriveConfig aliyunDriveConfig) {
        this.aliyunDriveConfig = aliyunDriveConfig;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = (TomcatServletWebServerFactory) factory;

        tomcatServletWebServerFactory.addContextCustomizers(context -> {

            RealmBase realm = new RealmBase() {
                @Override
                protected String getPassword(String username) {
                    if (aliyunDriveConfig.getAuth().getUsername().equals(username)) {
                        return aliyunDriveConfig.getAuth().getPassword();
                    }
                    return null;
                }

                @Override
                protected Principal getPrincipal(String username) {
                    return new GenericPrincipal(username, aliyunDriveConfig.getAuth().getPassword(),
                            Collections.singletonList("*"));
                }
            };

            CredentialHandler credentialHandler = new MessageDigestCredentialHandler();
            realm.setCredentialHandler(credentialHandler);
            context.setRealm(realm);

            AuthenticatorBase digestAuthenticator = new BasicAuthenticator();
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setAuthConstraint(true);
            securityConstraint.addAuthRole("**");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
            context.getPipeline().addValve(digestAuthenticator);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
