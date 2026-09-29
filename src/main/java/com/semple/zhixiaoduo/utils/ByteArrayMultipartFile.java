package com.semple.zhixiaoduo.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;

/**
 * 基于字节数组的MultipartFile实现，用于将后端生成的文件复用现有上传服务。
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final byte[] bytes;
    private final String originalFilename;
    private final String contentType;

    public ByteArrayMultipartFile(byte[] bytes, String originalFilename, String contentType) {
        this.bytes = bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(dest)) {
            outputStream.write(bytes);
        }
    }
}
