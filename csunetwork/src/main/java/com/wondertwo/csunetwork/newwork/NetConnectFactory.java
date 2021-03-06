package com.wondertwo.csunetwork.newwork;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.wondertwo.csunetwork.ui.network.BaseNetworkActivity;
import com.wondertwo.csunetwork.constant.NetworkConstant;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * DigitConnectionFactory网络连接工厂类
 * Created by wondertwo on 2016/4/6.
 */
public class NetConnectFactory {

    public static NetConnectFactory netConnectFactory = null;
    private Context context;
    private DefaultHttpClient defaultHttpClient;

    private final String LOGIN_POST_URL = "http://61.137.86.87:8080/portalNat444/AccessServices/login";
    private final String LOGIN_PEFER = "http://61.137.86.87:8080/portalNat444/index.jsp";
    private final String LOGOUT_POST_URL = "http://61.137.86.87:8080/portalNat444/AccessServices/logout?";
    private final String LOGOUT_PEFER = "http://61.137.86.87:8080/portalNat444/main2.jsp";

    public NetConnectFactory(Context context) {
        this.context = context;
        defaultHttpClient = new DefaultHttpClient();
    }

    public static NetConnectFactory getNCFInstance(Context context) {
        if (netConnectFactory == null) {
            netConnectFactory = new NetConnectFactory(context);
        }
        return netConnectFactory;
    }

    /**
     * 登陆
     */
    public String doLogin(String id, String password) {
        return doLoginWithEncryptedPassword(id, encrytePassword(password));
    }

    /**
     * 加密登陆
     */
    public String doLoginWithEncryptedPassword(String id, String password) {
        HttpResponse response = null;
        HttpEntity entity = null;
        HttpPost httpPost = null;
        String result = "";
        ArrayList<NameValuePair> postData = getAddress();
        if (postData != null) {
            postData.add(new BasicNameValuePair("accountID", id + "@zndx.inter"));
            postData.add(new BasicNameValuePair("password", password));
            httpPost = new HttpPost(LOGIN_POST_URL);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(postData));
                httpPost.setHeader(new BasicHeader("Referer", LOGIN_PEFER));
                response = defaultHttpClient.execute(httpPost);
                entity = response.getEntity();
                result = EntityUtils.toString(entity, "UTF-8");
                Log.d("TAG", result);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 下线
     */
    public String doLogout() {
        String brasAddress = BaseNetworkActivity.getSpUtil().getValue(NetworkConstant.SP_USER_BRAS_ADDRESS, "");
        String userIntranetAddress = BaseNetworkActivity.getSpUtil().getValue(NetworkConstant.SP_USER_INTRANET_ADDRESS, "");
        if (TextUtils.isEmpty(brasAddress) || TextUtils.isEmpty(userIntranetAddress)) {
            return "";
        }
        //添加所需要的post内容
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("brasAddress", brasAddress));
        values.add(new BasicNameValuePair("userIntranetAddress", userIntranetAddress));
        HttpPost httpPost = new HttpPost(LOGOUT_POST_URL);
        HttpResponse responses = null;
        HttpEntity entity = null;
        String result = "";
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(values));
            httpPost.setHeader(new BasicHeader("Referer", LOGOUT_PEFER));
            responses = defaultHttpClient.execute(httpPost);
            entity = responses.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            if (null == entity)
                Log.d("Response content有问题", result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 密码需要经过RSA加密
     */
    public String encrytePassword(String passwordInStr) {
        String mudulusInHexStr = "a8a02b821d52d3d0ca90620c78474b78435423be99da83cc190ab5cb5b9b922a4c8ba6b251e78429757cf11cde119e1eacff46fa3bf3b43ef68ceb29897b7aa6b5b1359fef6f35f32b748dc109fd3d09f3443a2cc3b73e99579f3d0fe6a96ccf6a48bc40056a6cac327d309b93b1d61d6f6e8f4a42fc9540f34f1c4a2e053445";
        String exponentInHexStr = "10001";
        String result = "";
        BigInteger bigInt_mudulus = new BigInteger(mudulusInHexStr, 16);
        BigInteger bigInt_exponent = new BigInteger(exponentInHexStr, 16);
        BigInteger bigInt_password = new BigInteger(new StringBuilder(passwordInStr).reverse().toString().getBytes());
        BigInteger bigInt_result = bigInt_password.modPow(bigInt_exponent, bigInt_mudulus);
        result = bigInt_result.toString(16);
        String zeroStr = "";
        int zeroNum = mudulusInHexStr.length() - result.length();
        for (int i = 0; i < zeroNum; i++)
            zeroStr += "0";
        return zeroStr + result;
    }

    /**
     * 此方法有两个用途：
     * 1. 可以获取userIntranetAddress和brasAddress两个参数
     * 2. 可以使用getAddress()！= null来判断当前已连接数字中南并且没有登录,但注意需要在非ui线程中调用
     */
    public ArrayList<NameValuePair> getAddress() {
        HttpGet httpGet = new HttpGet("http://www.baidu.com");
        ArrayList<NameValuePair> addresses = new ArrayList<NameValuePair>();
        try {
            HttpResponse response = defaultHttpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String html = EntityUtils.toString(entity, "UTF-8");
                Document doc = Jsoup.parse(html);
                System.out.print(doc.val().toString());
                String userIntranetAddress = doc.getElementById("userIntranetAddress").val().toString();
                String brasAddress = doc.getElementById("brasAddress").val().toString();
                System.out.println(userIntranetAddress + "    " + brasAddress);
                addresses.add(new BasicNameValuePair("userIntranetAddress", userIntranetAddress));
                addresses.add(new BasicNameValuePair("brasAddress", brasAddress));
                BaseNetworkActivity.getSpUtil().setValue(NetworkConstant.SP_USER_BRAS_ADDRESS, brasAddress);
                BaseNetworkActivity.getSpUtil().setValue(NetworkConstant.SP_USER_INTRANET_ADDRESS, userIntranetAddress);
            }
        } catch (Exception e) {
            return null;
        }
        return addresses;
    }
}
