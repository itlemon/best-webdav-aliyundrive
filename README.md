# best-webdav-aliyundrive
Alibaba cloud disk WebDAV protocol implementation.

### 说明：
项目`webdav-servlet`已经在各大仓库找不到jar了，它对应的项目是：https://github.com/ceefour/webdav-servlet
可以将其clone下来，并安装到本地的maven仓库中，也可以下载本项目提供的jar，直接安装到本地仓库中，安装到本地仓库后对应的坐标信息如下所示：
```xml
<!-- webdav-servlet 底层实现 -->
<dependency>
    <groupId>net.sf.webdav-servlet</groupId>
    <artifactId>webdav-servlet</artifactId>
    <version>2.0.1</version>
</dependency>
```
各位开发者可以点击[这里](https://www.aliyundrive.com/s/Rd419RWgUYo)进行下载三个jar包，如下所示：

- webdav-servlet-2.0.1.jar
- webdav-servlet-2.0.1-sources.jar
- webdav-servlet-2.0.1-javadoc.jar

下载完成后，进入到下载目录，并执行下面三条命令即可将其安装到本地仓库：
```shell
mvn install:install-file -Dfile=webdav-servlet-2.0.1.jar -DgroupId=net.sf.webdav-servlet -DartifactId=webdav-servlet -Dversion=2.0.1 -Dpackaging=jar
mvn install:install-file -Dfile=webdav-servlet-2.0.1-sources.jar -DgroupId=net.sf.webdav-servlet -DartifactId=webdav-servlet -Dversion=2.0.1 -Dpackaging=jar -Dclassifier=sources
mvn install:install-file -Dfile=webdav-servlet-2.0.1-javadoc.jar -DgroupId=net.sf.webdav-servlet -DartifactId=webdav-servlet -Dversion=2.0.1 -Dpackaging=jar -Dclassifier=javadoc
```
