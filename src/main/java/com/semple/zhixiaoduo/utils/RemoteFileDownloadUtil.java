package com.semple.zhixiaoduo.utils;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import org.springframework.http.ContentDisposition;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * HTTP、HTTPS 远程文件安全下载工具。
 */
public final class RemoteFileDownloadUtil {

    /**
     * 流式复制缓冲区大小。
     */
    private static final int BUFFER_SIZE = 8 * 1024;

    private RemoteFileDownloadUtil() {
    }

    /**
     * 将远程文件流式写入指定路径，并限制地址范围、重定向次数和实际下载大小。
     *
     * @return 实际写入字节数、响应文件名和最终下载地址
     * @param fileUrl fileUrl 参数。
     * @param targetFile targetFile 参数。
     * @param maxBytes maxBytes 参数。
     * @param connectTimeout connectTimeout 参数。
     * @param readTimeout readTimeout 参数。
     * @param maxRedirects maxRedirects 参数。
     */
    public static RemoteDownloadResult downloadTo(String fileUrl, Path targetFile, long maxBytes,
                                                   int connectTimeout, int readTimeout, int maxRedirects) {
        if (maxBytes <= 0 || maxRedirects < 0) {
            throw new IllegalArgumentException("远程下载限制参数不正确");
        }
        URI currentUri = parseAndValidate(fileUrl);
        for (int redirects = 0; redirects <= maxRedirects; redirects++) {
            HttpURLConnection connection = open(currentUri, connectTimeout, readTimeout);
            try {
                int status = connection.getResponseCode();
                if (isRedirect(status)) {
                    if (redirects == maxRedirects) {
                        throw fileError(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR, "远程文件重定向次数过多");
                    }
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.isBlank()) {
                        throw fileError(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR, "远程文件重定向地址为空");
                    }
                    currentUri = parseAndValidate(currentUri.resolve(location).toString());
                    continue;
                }
                if (status < 200 || status >= 300) {
                    throw fileError(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR,
                            "远程文件下载失败，响应状态：" + status);
                }
                long contentLength = connection.getContentLengthLong();
                if (contentLength > maxBytes) {
                    throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_SIZE_ERROR);
                }
                long size = copy(connection, targetFile, maxBytes);
                return new RemoteDownloadResult(size, responseFilename(connection), currentUri);
            } catch (BaseServiceException exception) {
                throw exception;
            } catch (Exception exception) {
                throw fileError(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR, "远程文件下载失败");
            } finally {
                connection.disconnect();
            }
        }
        throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR);
    }

    /**
     * 仅校验 URL 基础结构，不发起网络请求。
     *
     * @param fileUrl fileUrl 参数。
     */
    public static void validateUrl(String fileUrl) {
        parseUri(fileUrl);
    }

    /**
     * 创建禁用自动重定向的 HTTP 连接。
     *
     * @param uri uri 参数。
     * @param connectTimeout connectTimeout 参数。
     * @param readTimeout readTimeout 参数。
     * @return 处理结果。
     */
    private static HttpURLConnection open(URI uri, int connectTimeout, int readTimeout) {
        try {
            URLConnection connection = uri.toURL().openConnection();
            if (!(connection instanceof HttpURLConnection httpConnection)) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
            }
            httpConnection.setConnectTimeout(connectTimeout);
            httpConnection.setReadTimeout(readTimeout);
            httpConnection.setRequestMethod("GET");
            httpConnection.setInstanceFollowRedirects(false);
            httpConnection.setRequestProperty("User-Agent", "zhixiaoduo-import/1.0");
            return httpConnection;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR);
        }
    }

    /**
     * 流式复制响应内容，并以实际读取字节数执行容量限制。
     *
     * @param connection connection 参数。
     * @param targetFile targetFile 参数。
     * @param maxBytes maxBytes 参数。
     * @return 处理结果。
     */
    private static long copy(HttpURLConnection connection, Path targetFile, long maxBytes) throws Exception {
        Files.createDirectories(targetFile.getParent());
        long total = 0L;
        try (InputStream inputStream = connection.getInputStream();
             OutputStream outputStream = Files.newOutputStream(targetFile,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                total += length;
                if (total > maxBytes) {
                    throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_SIZE_ERROR);
                }
                outputStream.write(buffer, 0, length);
            }
        }
        return total;
    }

    /**
     * 解析 URL 并在每次请求或重定向前拒绝内网及本机地址。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    private static URI parseAndValidate(String fileUrl) {
        URI uri = parseUri(fileUrl);
        try {
            InetAddress[] addresses = InetAddress.getAllByName(uri.getHost());
            if (addresses.length == 0) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
            }
            for (InetAddress address : addresses) {
                if (isPrivateAddress(address)) {
                    throw fileError(ExceptionEnum.IMPORT_FILE_URL_ERROR, "远程文件地址不允许访问内网资源");
                }
            }
            return uri;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw fileError(ExceptionEnum.IMPORT_FILE_URL_ERROR, "远程文件域名无法解析");
        }
    }

    /**
     * 校验 HTTP、HTTPS URL 的基本格式。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    private static URI parseUri(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl).normalize();
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || uri.getHost() == null || uri.getHost().isBlank() || uri.getUserInfo() != null) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
            }
            return uri;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
        }
    }

    /**
     * 判断地址是否属于不允许通过业务 URL 访问的本机、内网或特殊网段。
     *
     * @param address address 参数。
     * @return 处理结果。
     */
    private static boolean isPrivateAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        if (address instanceof Inet6Address) {
            byte first = address.getAddress()[0];
            // Java 对 IPv6 唯一本地地址 fc00::/7 的识别不完整，显式拒绝该网段。
            return (first & 0xFE) == 0xFC;
        }
        return false;
    }

    /**
     * 判断响应状态是否需要手动处理重定向。
     *
     * @param status status 参数。
     * @return 处理结果。
     */
    private static boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER
                || status == 307 || status == 308;
    }

    /**
     * 优先从 Content-Disposition 响应头中读取服务端提供的原始文件名。
     *
     * @param connection connection 参数。
     * @return 处理结果。
     */
    private static String responseFilename(HttpURLConnection connection) {
        String value = connection.getHeaderField("Content-Disposition");
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ContentDisposition.parse(value).getFilename();
        } catch (IllegalArgumentException exception) {
            // 第三方服务可能返回非标准响应头，文件名可继续从最终 URL 中兜底获取。
            return null;
        }
    }

    /**
     * 构造带有统一错误码的下载异常。
     *
     * @param exceptionEnum exceptionEnum 参数。
     * @param message message 参数。
     * @return 处理结果。
     */
    private static BaseServiceException fileError(ExceptionEnum exceptionEnum, String message) {
        return new BaseServiceException(exceptionEnum.getCode(), message);
    }

    /**
     * 远程文件下载结果。
     *
     * @param size 实际下载字节数
     * @param responseFilename Content-Disposition 中的文件名，响应未提供时为空
     * @param finalUri 完成重定向后的最终地址
     */
    public record RemoteDownloadResult(long size, String responseFilename, URI finalUri) {
    }
}
