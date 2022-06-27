package com.redhat.acs.exporter.client;

import java.io.IOException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AcsApiClient implements AutoCloseable {

    private final static Logger log = LogManager.getLogger(AcsApiClient.class);

    private String baseUrl;
    private String credentials;
    private CloseableHttpClient client;

    /**
     * 
     * @param baseUrl
     * @param credentials
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public AcsApiClient(String baseUrl, String credentials) {
        this.baseUrl = baseUrl;
        this.credentials = credentials;
        try {
            this.client = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param clusterId
     * @param ns
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String getNetworkGraph(String clusterId, String ns) throws ClientProtocolException, IOException {
        String query = String.format(
                "query=Namespace:%s&since=2000-01-01T07:47:06.995Z&includePorts=true",
                ns);

        StringBuilder uri = new StringBuilder();
        uri
            .append(baseUrl)
            .append(String.format("/v1/networkgraph/cluster/%s", clusterId))
            .append("?")
            .append(query);
        
        log.debug("GET: {}", uri);

        HttpGet request = new HttpGet(uri.toString());
        request.setHeader("Authorization", credentials);

        CloseableHttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() != 200)
            throw new RuntimeException("GET NetworkGraph response status:: " + response.getStatusLine().getStatusCode());

        return EntityUtils.toString(response.getEntity());
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
