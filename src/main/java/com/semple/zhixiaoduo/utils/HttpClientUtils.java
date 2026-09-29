package com.semple.zhixiaoduo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @filename HttpClientUtils
 * @description HTTP请求工具类
 * @autor aofaming
 * @date 2023/5/5 14:19
 */
@Slf4j
public class HttpClientUtils {

    private static int statusCode = 200;
    private static int statuscode = 201;

    /**
     * GET请求
     *
     * @param path 请求地址
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/5 14:29
     */
    public static String doGet(String path) throws IOException {
        log.info("请求url:{}", path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(600000);
        int responseCode = conn.getResponseCode();
        if (responseCode == statusCode) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } else {
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            String content = strBuf.toString();
            reader.close();
            Map<String, String> map = new HashMap<>();
            map.put("code", responseCode+"");
            map.put("message", content);
            map.put("msg", content);
            log.info("http失败，url为{},返回状态码为{}，返回信息为{}", path, responseCode, content);
            return JsonUtils.toJson(map);
        }
    }

    /**
     * POST请求
     *
     * @param path 请求地址
     * @param data 请求参数
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/5 14:30
     */
    public static String doPost(String path, String data) throws IOException {
        log.info("参数：{},请求url:{}", data, path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(600000);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            os.write(postData, 0, postData.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == statusCode) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                log.info("请求url:{},结果:{}", path, sb.toString());
                return sb.toString();
            }
        } else {
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            String content = strBuf.toString();
            reader.close();
            Map<String, String> map = new HashMap<>();
            map.put("code", responseCode+"");
            map.put("message", content);
            map.put("msg", content);
            log.info("http失败，url为{},返回状态码为{}，返回信息为{}", path, responseCode, content);
            return JsonUtils.toJson(map);
        }
    }

    /**
     * GET请求
     *
     * @param path    请求地址
     * @param headers 请求头
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/5 14:30
     */
    public static String doGet(String path, Map<String, String> headers) throws IOException {
        log.info("header:{} ,请求url:{}", JsonUtils.toJson(headers), path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(600000);
        addHeaders(conn, headers);
        int responseCode = conn.getResponseCode();
        if (responseCode == statusCode) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                log.info("请求url:{},结果:{}", path, sb.toString());
                return sb.toString();
            }
        }else {
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            String content = strBuf.toString();
            reader.close();
            Map<String, String> map = new HashMap<>();
            map.put("code", responseCode+"");
            map.put("message", content);
            map.put("msg", content);
            log.info("http失败，url为{},返回状态码为{}，返回信息为{}", path, responseCode, content);
            return JsonUtils.toJson(map);
        }
    }

    /**
     * GET请求
     *
     * @param path    请求地址
     * @param headers 请求头
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/5 14:30
     * @param data data 参数。
     */
    public static String doGet(String path, String data, Map<String, String> headers) throws IOException, IllegalAccessException {
        log.info("header:{} ,请求url:{}", JsonUtils.toJson(headers), path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(600000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        addHeaders(conn, headers);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            os.write(postData, 0, postData.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == statusCode) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                log.info("请求url:{},结果:{}", path, sb.toString());
                return sb.toString();
            }
        } else {
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            String content = strBuf.toString();
            reader.close();
            Map<String, String> map = new HashMap<>();
            map.put("code", responseCode+"");
            map.put("message", content);
            map.put("msg", content);
            log.info("http失败，url为{},返回状态码为{}，返回信息为{}", path, responseCode, content);
            return JsonUtils.toJson(map);
        }
    }

    /**
     * POST请求
     *
     * @param path    请求地址
     * @param data    请求数据
     * @param headers 请求头
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/5 14:31
     */
    public static String doPost(String path, String data, Map<String, String> headers) throws IOException {
        log.info("参数：{}，header:{} ,请求url:{}", data, JsonUtils.toJson(headers), path);
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(600000);
        conn.setDoOutput(true);
        addHeaders(conn, headers);


        try (OutputStream os = conn.getOutputStream()) {
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            os.write(postData, 0, postData.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == statusCode) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                log.info("请求url:{},结果:{}", path, sb.toString());
                return sb.toString();
            }
        } else {
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            String content = strBuf.toString();
            reader.close();

            Map<String, String> map = new HashMap<>();
            map.put("code", responseCode+"");
            map.put("message", content);
            map.put("msg", content);
            log.info("http失败，url为{},返回状态码为{}，返回信息为{}", path, responseCode, content);
            return JsonUtils.toJson(map);
        }
    }

    /***
     * @description
     * @author qinjing
     * @date 2024/4/25 16:27
     * @param path    请求地址
     * @param data    请求数据
     * @param headers 请求头
     * @return java.lang.String
     * @return
     */
    public static String doPostOcr(String path, String data, Map<String, String> headers) throws IOException {
        log.info("参数：{}，header:{} ,请求url:{}", data, JsonUtils.toJson(headers), path);
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        addHeaders(conn, headers);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            os.write(postData, 0, postData.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == statusCode || responseCode == statuscode) {
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            String sb = headerFields.get("X-Subject-Token").get(0);

            log.info("请求url:{},结果:{}", path, sb.toString());
            return sb.toString();
        } else {
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            String content = strBuf.toString();
            reader.close();
            Map<String, String> map = new HashMap<>();
            map.put("code", responseCode+"");
            map.put("message", content);
            map.put("msg", content);
            log.info("http失败，url为{},返回状态码为{}，返回信息为{}", path, responseCode, content);
            return JsonUtils.toJson(map);
        }
    }

    /***
     *post表单请求
     * @author aofaming
     * @date 2024/2/28 9:53
     * @param path
     * @param data
     * @param
     * @return String
     * @param headers headers 参数。
     */
    public static String doPostForm(String path, Map<String, Object> data, Map<String, String> headers) throws IOException {
        log.info("参数：{} ,请求url:{}", data, path);
        String twoHeaders = "--";
        String BOUNDARY = "011000010111000001101001";
        String end = "\r\n";
        BufferedReader in = null;
        URL url = new URL(path);
        HttpURLConnection conn = null;
        DataOutputStream outputStream = null;
        String rs = "";
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
            addHeaders(conn, headers);
            outputStream = new DataOutputStream(conn.getOutputStream());
            Set<Map.Entry<String, Object>> entries = data.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                // 每次都清空buffer，避免写入上次的数据
                outputStream.writeBytes(twoHeaders+BOUNDARY+end);
                Object value = entry.getValue();
                if (!(value instanceof File)) {
                    outputStream.writeBytes("Content-Disposition: form-data; name=\""+entry.getKey()+"\""+end);
                    outputStream.writeBytes(end);
                    outputStream.writeBytes(value.toString()+end);
                } else {
                    File file = (File) entry.getValue();
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\";filename=\""+ file.getName()+"\"" +end+end);
                    FileInputStream ins = new FileInputStream(file.getPath());
                    int bytes = -1;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = ins.read(bufferOut)) != -1) {
                        outputStream.write(bufferOut, 0, bytes);
                    }
                    // 文件流后面添加换行，否则文件后面的一个参数会丢失
                    outputStream.writeBytes(end);
                    ins.close();
                }
            }
            if (entries != null && data.size() > 0) {
                outputStream.writeBytes(twoHeaders+BOUNDARY+twoHeaders+end);
            }
            outputStream.flush();
            try {
                int responseCode = conn.getResponseCode();
                if (responseCode == statusCode) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        rs += line;
                    }
                }else{
                    Map<String, String> map = new HashMap<>();
                    map.put("code", responseCode+"");
                    map.put("message", "");
                    map.put("msg", "");
                    return JsonUtils.toJson(map);
                }
                log.info("请求url:{},结果:{}", path, rs);
            } catch (Exception e) {
                log.error("请求表单数据异常 ，url为{},参数为{},异常信息为{}", path, data, ExceptionUtils.getThrowables(e));
                rs = null;
            }
            return rs;
        } finally {
            try {
                outputStream.close();
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
            outputStream = null;
            if (conn != null)
                conn.disconnect();
            conn = null;
        }
    }


    /**
     * 向指定的HttpURLConnection对象添加请求头。
     *
     * @param conn    HttpURLConnection对象
     * @param headers 包含请求头信息的Map对象。如果为null，则不添加请求头。
     * @return void
     * @author aofaming
     * @date 2023/5/5 14:32
     */
    private static void addHeaders(HttpURLConnection conn, Map<String, String> headers) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
    }

}
