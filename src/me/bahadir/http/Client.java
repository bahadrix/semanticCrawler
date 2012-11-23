/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.bahadir.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


/**
 * Multi-threading ile beraber kullanilabilecek bir http istemcisidir.
 * @author vaıo
 */
public class Client {

    private DefaultHttpClient client;

    public Client() {
        this.client = getThreadSafeClient();
    }
    
    
    /**
     * Multithread ile kullanilirken sorun cikarmayacak bir DefaulHttpClient dondurur.
     * @return 
     */
    private DefaultHttpClient getThreadSafeClient() {
        DefaultHttpClient safeClient = new DefaultHttpClient();
        ClientConnectionManager mgr = safeClient.getConnectionManager();
        HttpParams params = safeClient.getParams();

        safeClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);

        return safeClient;
    }

    /**
     * Verilen adresin icerigini string olarak getirir.
     * @param Address
     * @return 
     */
    public String request(String Address) {
        return request(Address, new ArrayList<NameValuePair>());
    }

    /**
     * Verilen adresini postList ile belirtilen 
     * @param Address
     * @param postList
     * @return 
     */
    public String request(String Address, List<NameValuePair> postList) {

        HttpPost httpPost = new HttpPost(Address);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postList, HTTP.UTF_8));
            HttpResponse response = client.execute(httpPost);

            return EntityUtils.toString(response.getEntity());

        } catch (UnsupportedEncodingException e) {
            System.out.println("Encoding hatası: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Bağlantı hatası filan: " + e.getMessage());
        }
        return "";
    }
}
