package com.lhcode.litemall.wx.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * spring提供的RestTemplate工具类
 * 调用远程服务
 */
@Configuration
public class RestConfig {

	
	@Bean(name="restTemplate")
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(15000);
        factory.setConnectTimeout(15000);
        return new RestTemplate(factory);
    }

	/**
	 * 处理http和https请求 并且可以加密
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	@Bean(name="ksRestTemplate")
    public RestTemplate ksRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder b = HttpClientBuilder.create();

        //设置允许所有证书的信任策略。
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        SSLContext sslContext = sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        }).build();
        b.setSSLContext(sslContext);

        //也不要检查主机名。
        //如果不想减弱，请使用sslconnectionsocketFactory.getDefaultHostnameVerifier（）。
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        /*
                    这是特别的部分：
        --需要创建一个SSL套接字工厂，使用我们削弱的“信任策略”；
        --并创建一个注册表来注册它。
        */
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        //现在，我们使用注册表创建连接管理器。
        //允许多线程使用
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connMgr.setMaxTotal(200);//最大连接数
        connMgr.setDefaultMaxPerRoute(100);//同路由并发数
        b.setConnectionManager(connMgr);

        //最后，构建httpclient；
        //完成！
        CloseableHttpClient httpClient = b.build();

        HttpComponentsClientHttpRequestFactory httpsFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        httpsFactory.setReadTimeout(50000); //读超时时间
        httpsFactory.setConnectTimeout(50000);//连接超时时间
        httpsFactory.setConnectionRequestTimeout(30 * 1000);  //连接不够用的等待时间

        RestTemplate restTemplate = new RestTemplate(httpsFactory);
        restTemplate.setErrorHandler(
                new ResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse clientHttpResponse) {
                        return false;
                    }

                    @Override
                    public void handleError(ClientHttpResponse clientHttpResponse) {
                        // 默认处理非200的返回，会抛异常
                    }
                });
        
        //处理乱码
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringHttpMessageConverter.setWriteAcceptCharset(true);

        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.ALL);

        for (int i = 0; i < restTemplate.getMessageConverters().size(); i++) {
            if (restTemplate.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                restTemplate.getMessageConverters().remove(i);
                restTemplate.getMessageConverters().add(i, stringHttpMessageConverter);
            }
            if(restTemplate.getMessageConverters().get(i) instanceof MappingJackson2HttpMessageConverter) {
                try {
                    ((MappingJackson2HttpMessageConverter) restTemplate.getMessageConverters().get(i)).setSupportedMediaTypes(mediaTypeList);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return restTemplate;
    }

}