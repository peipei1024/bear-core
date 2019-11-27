package com.bear.core.http;

import com.bear.core.retry.RetryExhaustedException;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

/**
 * @author shomop
 * @date 2019/11/6 10:38
 * 参考：原文链接：https://blog.csdn.net/w372426096/article/details/82713315
 * https://www.jianshu.com/p/ba8dd2ce380a
 * 添加@EnableRetry注解启用失败重试功能
 */
@Component
public class AClient {

    private RequestConfig defaultRequestConfig = null;
    private Logger log = LoggerFactory.getLogger(AClient.class);

    @Autowired
    public void init(){
        defaultRequestConfig = RequestConfig.custom()
                //一、连接目标服务器超时时间：ConnectionTimeout-->指的是连接一个url的连接等待时间
                .setConnectTimeout(1000)
                //三、从连接池获取连接的超时时间:ConnectionRequestTimeout
                .setConnectionRequestTimeout(1000)
                //二、读取目标服务器数据超时时间：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                .setSocketTimeout(1000 * 5)
                .build();

    }

    private HttpRequestRetryHandler createRetryHandler() {
        return new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i >= 3) {// 如果已经重试了2次，就放弃
                    return false;
                }
                if (e instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (e instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (e instanceof InterruptedIOException) {// 超时
                    return true;
                }
                if (e instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (e instanceof SSLException) {// SSL握手异常
                    return false;
                }
                if (e instanceof SocketException) {
                    return true;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
                HttpRequest request = clientContext.getRequest();

                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
    }

    @Retryable(value = RetryExhaustedException.class, maxAttempts = 3, backoff = @Backoff(delay = 500L))
    public String doGet(String url, Proxy proxy){
        return _doGet(url, proxy, null);
    }

    @Retryable(value = RetryExhaustedException.class, maxAttempts = 3, backoff = @Backoff(delay = 500L))
    public String doGet(String url){
        return _doGet(url, null, null);
    }

    @Retryable(value = RetryExhaustedException.class, maxAttempts = 3, backoff = @Backoff(delay = 500L))
    public String doGet(String url, Proxy proxy, String respCharset){
        return _doGet(url, proxy, respCharset);
    }

    private String _doGet(String url, Proxy proxy, String respCharset) {
        String result = null;
        try {
            if (respCharset == null) {
                respCharset = "utf-8";
            }
            RequestConfig requestConfig = null;
            if(proxy != null){
                InetSocketAddress address = (InetSocketAddress) proxy.address();
                requestConfig = RequestConfig.copy(defaultRequestConfig)
                        .setProxy(new HttpHost(address.getHostName(), address.getPort()))
                        .build();
            }else {
                requestConfig = defaultRequestConfig;
            }

            HttpGet httpget = new HttpGet(url);
            httpget.setConfig(requestConfig);
            CloseableHttpResponse response = this.getHttpClient(null).execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, respCharset);
                log.info("[Http-client] success {}, Response code: {}, Response content length: {}", url, response.getStatusLine(), result.length());
//            log.info("Response content: {}", stringResult);
            }
            response.close();
        }catch (Exception e){
            log.info("[Http-client] fail {}, {}", url, e.getMessage());
            throw new RetryExhaustedException(url + " retry fail", e);
        }
        return result;
    }

    @Recover
    public String recover(RetryExhaustedException e){
        log.error("[Http-client] error {}", e.getMessage());
        return null;
    }

    private CloseableHttpClient getHttpClient(HttpRequestRetryHandler retryHandler){
        if(retryHandler != null){
            return HttpClients.custom().setRetryHandler(retryHandler).build();
        }
        return HttpClients.custom().build();
    }

}