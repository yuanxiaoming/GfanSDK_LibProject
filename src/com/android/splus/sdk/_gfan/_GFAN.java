package com.android.splus.sdk._gfan;

import com.android.splus.sdk.apiinterface.APIConstants;
import com.android.splus.sdk.apiinterface.DateUtil;
import com.android.splus.sdk.apiinterface.IPayManager;
import com.android.splus.sdk.apiinterface.InitBean;
import com.android.splus.sdk.apiinterface.InitBean.InitBeanSuccess;
import com.android.splus.sdk.apiinterface.InitCallBack;
import com.android.splus.sdk.apiinterface.LoginCallBack;
import com.android.splus.sdk.apiinterface.LoginParser;
import com.android.splus.sdk.apiinterface.LogoutCallBack;
import com.android.splus.sdk.apiinterface.MD5Util;
import com.android.splus.sdk.apiinterface.NetHttpUtil;
import com.android.splus.sdk.apiinterface.NetHttpUtil.DataCallback;
import com.android.splus.sdk.apiinterface.RechargeCallBack;
import com.android.splus.sdk.apiinterface.RequestModel;
import com.android.splus.sdk.apiinterface.UserAccount;
import com.mappn.sdk.pay.GfanChargeCallback;
import com.mappn.sdk.pay.GfanPay;
import com.mappn.sdk.pay.GfanPayCallback;
import com.mappn.sdk.pay.model.Order;
import com.mappn.sdk.uc.GfanUCenter;
import com.mappn.sdk.uc.User;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Properties;

public class _GFAN implements IPayManager {
    private static final String TAG = "_Gfan";
    private InitBean mInitBean;
    private static _GFAN mGFAN = null;
    private Properties mProperties;
    private InitCallBack mInitCallBack;
    private Activity mActivity=null;
    private String mGfan_cpid;
    private ProgressDialog mProgressDialog;
    private String mAppKey;
    private LoginCallBack mLoginCallback;
    private RechargeCallBack mRechargeCallBack;
 // 下面参数仅在测试时用
    private UserAccount mUserModel;
    private float mMoney ;
    private String mPayway="Gfan" ;
    private int mUid;
    private String mPassport;
    private String mSessionid;

    private _GFAN(){

    }

    /**
     * @Title: getInstance(获取实例)
     * @author xiaoming.yuan
     * @data 2014-2-26 下午2:30:02
     * @return _Gfan 返回类型
     */
    public static _GFAN getInstance() {

        if (mGFAN == null) {
            synchronized (_GFAN.class) {
                if (mGFAN == null) {
                    mGFAN = new _GFAN();
                }
            }
        }
        return mGFAN;
    }

    @Override
    public void setInitBean(InitBean bean) {
        this.mInitBean = bean;
        this.mProperties = mInitBean.getProperties();
    }

    @Override
    public void init(Activity activity, Integer gameid, String appkey, InitCallBack initCallBack, boolean useUpdate, Integer orientation) {
        this.mInitCallBack = initCallBack;
        this.mActivity = activity;
        mInitBean.initSplus(activity, initCallBack, new InitBeanSuccess() {

            @Override
            public void initBeaned(boolean initBeanSuccess) {
                if (mProperties != null) {
                    mGfan_cpid = mProperties.getProperty("gfan_cpid") == null ? "0" : mProperties.getProperty("gfan_cpid");
                    mAppKey = mProperties.getProperty("gfan_appkey") == null ? "" : mProperties.getProperty("gfan_appkey");
                }

                GfanPay.getInstance(mActivity).init();
                mInitCallBack.initSuccess("初始化成功", null);
            }
        });
    }

    @Override
    public void login(Activity activity, LoginCallBack loginCallBack) {
        this.mActivity = activity;
        this.mLoginCallback = loginCallBack;
        GfanUCenter.login(activity, mLoginGfanUCCallback);


    }

    com.mappn.sdk.uc.GfanUCCallback mLoginGfanUCCallback = new com.mappn.sdk.uc.GfanUCCallback(){


        /**
         * @Fields serialVersionUID
         * Description:（用一句话描述这个变量表示什么）
         */

        private static final long serialVersionUID = 1L;

        @Override
        public void onSuccess(User user, int i) {

            HashMap<String, Object> params = new HashMap<String, Object>();
            Integer gameid = mInitBean.getGameid();
            String partner = mInitBean.getPartner();
            String referer = mInitBean.getReferer();
            long unixTime = DateUtil.getUnixTime();
            String deviceno=mInitBean.getDeviceNo();
            String signStr =deviceno+gameid+partner+referer+unixTime+mInitBean.getAppKey();
            String sign=MD5Util.getMd5toLowerCase(signStr);

            params.put("deviceno", deviceno);
            params.put("gameid", gameid);
            params.put("partner",partner);
            params.put("referer", referer);
            params.put("time", unixTime);
            params.put("sign", sign);
            params.put("partner_sessionid", "");
            params.put("partner_uid", user.getUid());
            params.put("partner_token", user.getToken().toString().trim());
            params.put("partner_nickname", "");
            params.put("partner_username", user.getUserName());
            params.put("partner_appid", mAppKey);
            String hashMapTOgetParams = NetHttpUtil.hashMapTOgetParams(params, APIConstants.LOGIN_URL);
            System.out.println(hashMapTOgetParams);

            showProgressDialog(mActivity);
            NetHttpUtil.getDataFromServerPOST(mActivity,new RequestModel(APIConstants.LOGIN_URL, params, new LoginParser()),mLoginDataCallBack);
        }

        @Override
        public void onError(int i) {
            mLoginCallback.loginFaile("登录失败");
        }

    };

    private DataCallback<JSONObject> mLoginDataCallBack = new DataCallback<JSONObject>() {

        @Override
        public void callbackSuccess(JSONObject paramObject) {
            closeProgressDialog();
            Log.d("Gfan", "mLoginDataCallBack---------"+paramObject.toString());
            try {
                if (paramObject != null && paramObject.optInt("code") == 1) {
                    JSONObject data = paramObject.optJSONObject("data");
                    mUid = data.optInt("uid");
                    mPassport = data.optString("passport");
                    mSessionid = data.optString("sessionid");
                    mUserModel=new UserAccount() {

                        @Override
                        public Integer getUserUid() {
                            return mUid;

                        }

                        @Override
                        public String getUserName() {
                            return mPassport;

                        }

                        @Override
                        public String getSession() {
                            return mSessionid;

                        }
                    };
                    mLoginCallback.loginSuccess(mUserModel);

                } else {
                    mLoginCallback.loginFaile(paramObject.optString("msg"));
                }
            } catch (Exception e) {
                mLoginCallback.loginFaile(e.getLocalizedMessage());
            }
        }

        @Override
        public void callbackError(String error) {
            closeProgressDialog();
            mLoginCallback.loginFaile(error);
        }

    };

    @Override
    public void recharge(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String outOrderid, String pext, RechargeCallBack rechargeCallBack) {
        rechargeByQuota(activity, serverId, serverName, roleId, roleName, outOrderid, pext, 0f, rechargeCallBack);
    }

    @Override
    public void rechargeByQuota(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String outOrderid, String pext, Float money, RechargeCallBack rechargeCallBack) {
        this.mActivity = activity;
        this.mRechargeCallBack = rechargeCallBack;
        this.mMoney=money;

        HashMap<String, Object> params = new HashMap<String, Object>();
        Integer gameid = mInitBean.getGameid();
        String partner = mInitBean.getPartner();
        String referer = mInitBean.getReferer();
        long unixTime = DateUtil.getUnixTime();
        String deviceno=mInitBean.getDeviceNo();
        String signStr =gameid+serverName+deviceno+referer+partner+mUid+mMoney+mPayway+unixTime+mInitBean.getAppKey();
        String sign=MD5Util.getMd5toLowerCase(signStr);

        params.put("deviceno", deviceno);
        params.put("gameid", gameid);
        params.put("partner",partner);
        params.put("referer", referer);
        params.put("time", unixTime);
        params.put("sign", sign);
        params.put("uid",mUid);
        params.put("passport",mPassport);
        params.put("serverId",serverId);
        params.put("serverName",serverName);
        params.put("roleId",roleId);
        params.put("roleName",roleName);
        params.put("money",mMoney);
        params.put("pext",pext);
        params.put("payway",mPayway);
        params.put("outOrderid",outOrderid);
        String hashMapTOgetParams = NetHttpUtil.hashMapTOgetParams(params, APIConstants.PAY_URL);
        System.out.println(hashMapTOgetParams);
        NetHttpUtil.getDataFromServerPOST(activity, new RequestModel(APIConstants.PAY_URL, params,new LoginParser()),mRechargeDataCallBack);
    }

    private DataCallback<JSONObject> mRechargeDataCallBack = new DataCallback<JSONObject>() {
        @Override
        public void callbackSuccess(JSONObject paramObject) {
            Log.d(TAG, "mRechargeDataCallBack---------"+paramObject.toString());
            try {
                if (paramObject != null && (paramObject.optInt("code") == 1||paramObject.optInt("code") == 24)) {
                    JSONObject data = paramObject.optJSONObject("data");
                    String orderid=data.optString("orderid");
                    if(mMoney == 0){
                        GfanPay.getInstance(mActivity).charge(new GfanChargeCallback() {

                                    @Override
                                    public void onSuccess(User user) {
                                        mRechargeCallBack.rechargeSuccess(mUserModel);
                                    }

                                    @Override
                                    public void onError(User user) {
                                        mRechargeCallBack.rechargeFaile("购买失败");
                                    }
                                });


                    }else{
                        Order order = new Order("元宝", "游戏道具", (int) mMoney, orderid);
                        GfanPay.getInstance(mActivity).pay(order,
                                new GfanPayCallback() {

                                    @Override
                                    public void onSuccess(User user, Order order) {
                                        mRechargeCallBack.rechargeSuccess(mUserModel);
                                    }

                                    @Override
                                    public void onError(User user) {
                                        if (user != null) {
                                            mRechargeCallBack.rechargeFaile("购买失败");
                                        } else {
                                            Toast.makeText(mActivity, "用户未登录",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });



                    }





                } else {
                    Log.d(TAG, paramObject.optString("msg"));
                    mRechargeCallBack.rechargeFaile(paramObject.optString("msg"));
                }

            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
                mRechargeCallBack.rechargeFaile(e.getLocalizedMessage());
            }
        }

        @Override
        public void callbackError(String error) {
            Log.d(TAG, error);
            mRechargeCallBack.rechargeFaile(error);

        }

    };

    @Override
    public void exitSDK() {
    }

    @Override
    public void logout(Activity activity, LogoutCallBack logoutCallBack) {
    }

    @Override
    public void setDBUG(boolean logDbug) {
    }

    @Override
    public void enterUserCenter(Activity activity, LogoutCallBack logoutCallBack) {
    }

    @Override
    public void sendGameStatics(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String level) {
    }

    @Override
    public void enterBBS(Activity activity) {
    }

    @Override
    public void creatFloatButton(Activity activity, boolean showlasttime, int align, float position) {
    }

    @Override
    public void onResume(Activity activity) {
    }

    @Override
    public void onPause(Activity activity) {
    }

    @Override
    public void onStop(Activity activity) {
    }

    @Override
    public void onDestroy(Activity activity) {
    }

    /**
     * @return void 返回类型
     * @Title: showProgressDialog(设置进度条)
     * @author xiaoming.yuan
     * @data 2013-7-12 下午10:09:36
     */
    protected void showProgressDialog(Activity activity) {
        if (! activity.isFinishing()) {
            try {
                this.mProgressDialog = new ProgressDialog(activity);// 实例化
                // 设置ProgressDialog 的进度条style
                this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格，风格为圆形，旋转的
                this.mProgressDialog.setTitle("登陆");
                this.mProgressDialog.setMessage("加载中...");// 设置ProgressDialog 提示信息
                // 设置ProgressDialog 的进度条是否不明确
                this.mProgressDialog.setIndeterminate(false);
                // 设置ProgressDialog 的进度条是否不明确
                this.mProgressDialog.setCancelable(false);
                this.mProgressDialog.setCanceledOnTouchOutside(false);
                this.mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * @return void 返回类型
     * @Title: closeProgressDialog(关闭进度条)
     * @author xiaoming.yuan
     * @data 2013-7-12 下午10:09:30
     */
    protected void closeProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing())
            this.mProgressDialog.dismiss();
    }

}
