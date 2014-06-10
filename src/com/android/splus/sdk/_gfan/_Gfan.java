package com.android.splus.sdk._gfan;



import com.android.splus.sdk.apiinterface.IPayManager;
import com.android.splus.sdk.apiinterface.InitBean;
import com.android.splus.sdk.apiinterface.InitBean.InitBeanSuccess;
import com.android.splus.sdk.apiinterface.InitCallBack;
import com.android.splus.sdk.apiinterface.LoginCallBack;
import com.android.splus.sdk.apiinterface.LogoutCallBack;
import com.android.splus.sdk.apiinterface.RechargeCallBack;
import com.android.splus.sdk.apiinterface.UserAccount;
import com.mappn.sdk.pay.GfanChargeCallback;
import com.mappn.sdk.pay.GfanPay;
import com.mappn.sdk.pay.model.Order;
import com.mappn.sdk.statitistics.GfanPayAgent;
import com.mappn.sdk.uc.GfanUCCallback;
import com.mappn.sdk.uc.GfanUCenter;
import com.mappn.sdk.uc.User;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.Properties;

public class _Gfan implements IPayManager {

    private static final String TAG = "_Gfan";

    private static _Gfan _mGfan;

    // 平台参数
    private Properties mProperties;

    private String mAppId;

    private String mAppkey;

    private InitBean mInitBean;

    private InitCallBack mInitCallBack;

    private Activity mActivity;

    private LoginCallBack mLoginCallBack;

    private RechargeCallBack mRechargeCallBack;

    private LogoutCallBack mLogoutCallBack;

    // 下面参数仅在测试时用
    private UserAccount mUserModel;

    private int mUid = 0;

    private String mPassport = "";

    private String mSessionid = "";


    /**
     * @Title: _Gfan
     * @Description:( 将构造函数私有化)
     */
    private _Gfan() {

    }

    /**
     * @Title: getInstance(获取实例)
     * @author xiaoming.yuan
     * @data 2014-2-26 下午2:30:02
     * @return _Gfan 返回类型
     */
    public static _Gfan getInstance() {

        if (_mGfan == null) {
            synchronized (_Gfan.class) {
                if (_mGfan == null) {
                    _mGfan = new _Gfan();
                }
            }
        }
        return _mGfan;
    }

    @Override
    public void setInitBean(InitBean bean) {
        this.mInitBean = bean;
        this.mProperties = mInitBean.getProperties();
        if (mProperties != null) {
            mAppId = mProperties.getProperty("xiaomi_appid") == null ? "0" : mProperties.getProperty("xiaomi_appid");
            mAppkey = mProperties.getProperty("xiaomi_appkey") == null ? "0" : mProperties.getProperty("xiaomi_appkey");
        }
    }

    @Override
    public void init(Activity activity, Integer gameid, String appkey, InitCallBack initCallBack, boolean useUpdate, Integer orientation) {

        this.mInitCallBack = initCallBack;
        this.mActivity = activity;
        GfanPay.getInstance(activity).init();
        mInitBean.initSplus(activity, initCallBack, new InitBeanSuccess() {
            @Override
            public void initBeaned(boolean initBeanSuccess) {
                mInitCallBack.initSuccess("初始化完成", null);

            }
        });
    }

    @Override
    public void login(Activity activity, LoginCallBack loginCallBack) {
        this.mActivity = activity;
        this.mLoginCallBack = loginCallBack;

        GfanUCenter.login(activity, mLoginGfanUCCallback);

    }
    com.mappn.sdk.uc.GfanUCCallback mLoginGfanUCCallback=new com.mappn.sdk.uc.GfanUCCallback(){

        @Override
        public void onError(int loginType) {
            mLoginCallBack.loginFaile("登录失败");

        }

        @Override
        public void onSuccess(User user, int loginType) {
            Log.d(TAG,"登录成功 user：" + user.getUserName());
            mLoginCallBack.loginSuccess(null);
        }



    };




    @Override
    public void recharge(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String outOrderid, String pext, RechargeCallBack rechargeCallBack) {
        rechargeByQuota(activity, serverId, serverName, roleId, roleName, outOrderid, pext, 0f, rechargeCallBack);
    }

    @Override
    public void rechargeByQuota(Activity activity, Integer serverId, final String serverName, final Integer roleId, final String roleName, final String outOrderid, String pext, Float money, RechargeCallBack rechargeCallBack) {
        this.mActivity = activity;
        this.mRechargeCallBack = rechargeCallBack;
        if (money == 0) {
            GfanPay.getInstance(activity).charge(mGfanChargeCallback);
        }else{
            Order order = new Order(outOrderid, outOrderid, money.intValue(), outOrderid);
            GfanPay.getInstance(activity).pay(order, mGfanPayCallback);

        }

    }
    com.mappn.sdk.pay.GfanPayCallback mGfanPayCallback=new com.mappn.sdk.pay.GfanPayCallback(){

        @Override
        public void onError(User user) {
            if (user != null) {
               mRechargeCallBack.rechargeFaile("充值失败");
            } else {
                Toast.makeText(mActivity, "用户未登录",
                        Toast.LENGTH_SHORT).show();
                mRechargeCallBack.rechargeFaile("充值失败");
            }

        }

        @Override
        public void onSuccess(User user, Order order) {
            mRechargeCallBack.rechargeSuccess(null);

        }



    };

    com.mappn.sdk.pay.GfanChargeCallback mGfanChargeCallback=new GfanChargeCallback() {

        @Override
        public void onSuccess(User user) {
            mRechargeCallBack.rechargeSuccess(null);
        }

        @Override
        public void onError(User user) {
            if (user != null) {
                mRechargeCallBack.rechargeFaile("充值失败");
             } else {
                 Toast.makeText(mActivity, "用户未登录",
                         Toast.LENGTH_SHORT).show();
                 mRechargeCallBack.rechargeFaile("充值失败");
             }

        }
    };

    @Override
    public void exitSDK() {

    }

    @Override
    public void logout(Activity activity, LogoutCallBack logoutCallBack) {
        this.mLogoutCallBack = logoutCallBack;
        this.mActivity = activity;
        GfanUCenter.logout(activity);
        mLogoutCallBack.logoutCallBack();

    }



    @Override
    public void setDBUG(boolean logDbug) {
    }

    @Override
    public void enterUserCenter(Activity activity, LogoutCallBack logoutCallBack) {
        this.mActivity = activity;
        this.mLogoutCallBack = logoutCallBack;
        GfanUCenter.modfiy(activity, new GfanUCCallback() {

            /**
             * @Fields serialVersionUID
             * Description:（用一句话描述这个变量表示什么）
             */

            private static final long serialVersionUID = 1L;

            @Override
            public void onSuccess(User user, int returnType) {
                if (GfanUCenter.RETURN_TYPE_MODFIY == returnType) {
                    //TODO  完善账号成功
                }
            }

            @Override
            public void onError(int returnType) {
                if (GfanUCenter.RETURN_TYPE_MODFIY == returnType) {
                    //TODO  完善账号失败
                }
            }
        });


    }

    @Override
    public void sendGameStatics(Activity activity, Integer serverId, String serverName, Integer roleId, String roleName, String level) {
        GfanPay.submitNewRole(activity);
    }

    @Override
    public void enterBBS(Activity activity) {
    }

    @Override
    public void creatFloatButton(Activity activity, boolean showlasttime, int align, float position) {

    }

    @Override
    public void onResume(Activity activity) {
        GfanPayAgent.onResume (activity);
    }

    @Override
    public void onPause(Activity activity) {
        GfanPayAgent.onPause(activity);
    }

    @Override
    public void onStop(Activity activity) {
    }

    @Override
    public void onDestroy(Activity activity) {
        GfanPay.getInstance(activity).onDestroy();
    }
}
