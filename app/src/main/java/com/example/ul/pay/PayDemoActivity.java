package com.example.ul.pay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.example.ul.R;
import com.example.ul.adapter.PayWayAdapter;
import com.example.ul.model.Application;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 *  重要说明：
 *  
 *  本 Demo 只是为了方便直接向商户展示支付宝的整个支付流程，所以将加签过程直接放在客户端完成
 *  （包括 OrderInfoUtil2_0_HK 和 OrderInfoUtil2_0）。
 *
 *  在真实 App 中，私钥（如 RSA_PRIVATE 等）数据严禁放在客户端，同时加签过程务必要放在服务端完成，
 *  否则可能造成商户私密数据泄露或被盗用，造成不必要的资金损失，面临各种安全风险。
 * @author luoweili
 */
@SuppressLint("NonConstantResourceId")
public class PayDemoActivity extends Activity implements HttpUtil.MyCallback, PayWayAdapter.AfterSelectedChange, DialogUtil.DialogActionCallback{

	private static final String TAG = "PayDemoActivity";
	/**未知错误*/
	private static final int UNKNOWN_REQUEST_ERROR = 1900;
	/**请求失败*/
	private static final int REQUEST_FAIL = 19000;
	/**请求被服务器拦截，请求失败*/
	private static final int REQUEST_INTERCEPTED = 19001;
	/**请求服务器发起付款订单*/
	private static final int REQUEST_TO_PAY = 1904;
	/**服务器生成支付订单，完成验签*/
	private static final int REQUEST_TO_PAY_OK = 19041;
	/**服务器验签失败*/
	private static final int REQUEST_TO_PAY_ERROR = 19040;
	/**支付结果同步通知*/
	private static final int PAY_RESULT_NOTIFY = 1905;
	private static final int SDK_PAY_FLAG = 1;
	private static final int SDK_AUTH_FLAG = 2;

	@BindView(R.id.iv_back)
	public ImageView ivBack;
	@BindView(R.id.pay_id)
	public TextView payId;
	@BindView(R.id.pay_money)
	public TextView payMoney;
	@BindView(R.id.pay_desc)
	public TextView payDesc;
	@BindView(R.id.recyclerView_payWay)
	public RecyclerView payWayRecyclerView;
	@BindView(R.id.btn_pay)
	public Button btnPay;

	String orderInfo = null;
	PayWayAdapter payWayAdapter;
	String token;
	int position = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
		super.onCreate(savedInstanceState);
		ActivityManager.getInstance().addActivity(this);
		setContentView(R.layout.pay_main);
		ButterKnife.bind(this);
		// 获取订单id
		int id = getIntent().getIntExtra("id",0);
		if(id == 0){
			Toast.makeText(this, "数据错误！", Toast.LENGTH_SHORT).show();
			finish();
		}else {
			// 获取token
			UserManager userManager = UserManager.getInstance();
			UserInfo userInfo = userManager.getUserInfo(this);
			token = userInfo.getToken();
			init(id);
		}
	}

	void init(int id) {
		ivBack.setOnClickListener(v -> PayDemoActivity.this.finish());
		payWayAdapter = new PayWayAdapter(this);
		payWayRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
		btnPay.setOnClickListener(v -> {
			// 支付宝支付
			if(position == 0){
				if(orderInfo == null){
					Toast.makeText(PayDemoActivity.this,"无订单信息，无法发起支付！",Toast.LENGTH_LONG).show();
				}else {
					payV2(orderInfo);
				}
			}
			// 花呗支付
			else if(position == 1){
				Toast.makeText(PayDemoActivity.this,"花呗支付功能尚未开发，请使用支付宝支付",Toast.LENGTH_LONG).show();
			}
			// 花呗分期
			else if(position == 2){
				Toast.makeText(PayDemoActivity.this,"花呗分期支付功能尚未开发，请使用支付宝支付",Toast.LENGTH_LONG).show();
			}
			// 未选择
			else {
				Toast.makeText(PayDemoActivity.this,"请先选择一种支付方式",Toast.LENGTH_LONG).show();
			}
		});
		// 发出缴费请求
		HashMap<String, String> hashMap = new HashMap<>(4);
		hashMap.put("id", String.valueOf(id));
		String url = HttpUtil.BASE_URL + "application/toPay";
		HttpUtil.putRequest(token, url, hashMap, this, REQUEST_TO_PAY);
	}

	@Override
	public void changeButtonText(CompoundButton lastCheckedRB) {
		Resources resources = this.getResources();
		position = (int) lastCheckedRB.getTag();
		String s;
		switch (position){
			case 0:
				s = resources.getString(R.string.payWay_zhifubao) + "支付" + resources.getString(R.string.tvSymbol) + payMoney.getText().toString().trim();
				break;
			case 1:
				s = resources.getString(R.string.payWay_huabei) + "支付" + resources.getString(R.string.tvSymbol) + payMoney.getText().toString().trim();
				break;
			case 2:
				s = resources.getString(R.string.payWay_huabeifenqi) + "支付" + resources.getString(R.string.tvSymbol) + payMoney.getText().toString().trim();
				break;
			default:
				s = "请先选择支付方式";
		}
		btnPay.setText(s);
	}

	void fillDate(Application application){
		String payIdString = application.getPayId() == null ? "" : application.getPayId();
		String payMoneyString = application.getMoney() == null ? "0.00" : application.getMoney().toString();
		String payDescString = application.getDescription() == null ? "" : application.getDescription();
		payId.setText(payIdString);
		payMoney.setText(payMoneyString);
		payDesc.setText(payDescString);
		payWayRecyclerView.setAdapter(payWayAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		token = null;
		ActivityManager.getInstance().removeActivity(this);
	}

	/**
	 * 支付宝支付业务示例
	 */
	public void payV2(final String orderInfo) {
		/*
		 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
		 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
		 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险； 
		 * 
		 * orderInfo 的获取必须来自服务端；
		 */
		final Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				PayTask alipay = new PayTask(PayDemoActivity.this);
				Map<String, String> result = alipay.payV2(orderInfo, true);
				Log.i("msp", result.toString());
				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				myHandler.sendMessage(msg);
			}
		};
		// 必须异步调用
		HttpUtil.threadPool.execute(payRunnable);
//		Thread payThread = new Thread(payRunnable);
//		payThread.start();
	}

	/**
	 * 支付宝账户授权业务示例
	 */
	public void authV2(final String authInfo) {
		Runnable authRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造AuthTask 对象
				AuthTask authTask = new AuthTask(PayDemoActivity.this);
				// 调用授权接口，获取授权结果
				Map<String, String> result = authTask.authV2(authInfo, true);
				Message msg = new Message();
				msg.what = SDK_AUTH_FLAG;
				msg.obj = result;
				myHandler.sendMessage(msg);
			}
		};
		HttpUtil.threadPool.execute(authRunnable);
	}

	@Override
	public void success(Response response, int code) throws IOException {
		// 获取服务器响应字符串
		String result = response.body().string().trim();
		JSONObject jsonObject = JSON.parseObject(result);
		Message msg = new Message();
		Bundle bundle = new Bundle();
		String message = jsonObject.getString("message");
		String c = jsonObject.getString("code");
		String tip = jsonObject.getString("tip");
		// 返回值为true,说明请求被拦截
		if (HttpUtil.requestIsIntercepted(jsonObject)) {
			bundle.putString("code", c);
			bundle.putString("tip", tip);
			bundle.putString("message", message);
			msg.setData(bundle);
			msg.what = REQUEST_INTERCEPTED;
			myHandler.sendMessage(msg);
		} else {
			if (code == REQUEST_TO_PAY) {
				if("申请成功！".equals(message) && "缴费订单已生成！".equals(tip)){
					// 获取订单信息
					JSONObject jsonOrderStr = jsonObject.getJSONObject("dataObject");
					String orderStr = jsonOrderStr.getString("orderStr");
					String applicationString = jsonObject.getString("object");
					Application application = JSON.parseObject(applicationString,Application.class);
					bundle.putString("orderStr",orderStr);
					bundle.putParcelable("application",application);
					msg.setData(bundle);
					msg.what = REQUEST_TO_PAY_OK;
				}else {
					// 显示错误信息
					bundle.putString("message",message);
					bundle.putString("code",c);
					bundle.putString("tip",tip);
					msg.setData(bundle);
					msg.what = REQUEST_TO_PAY_ERROR;
				}
				myHandler.sendMessage(msg);
			}else {
				String reason = "未知错误";
				bundle.putString("reason",reason);
				msg.setData(bundle);
				myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
			}
		}
	}

	@Override
	public void failed(IOException e, int code) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		String reason;
		if (e instanceof SocketTimeoutException) {
			reason = "连接超时";
			message.what = REQUEST_FAIL;
		} else if (e instanceof ConnectException) {
			reason = "连接服务器失败";
			message.what = REQUEST_FAIL;
		} else if (e instanceof UnknownHostException) {
			reason = "网络异常";
			message.what = REQUEST_FAIL;
		} else {
			reason = "未知错误";
			message.what = UNKNOWN_REQUEST_ERROR;
		}
		bundle.putString("reason", reason);
		message.setData(bundle);
		myHandler.sendMessage(message);
	}

	MyHandler myHandler = new MyHandler(new WeakReference<>(this));

	@Override
	public void positiveAction(HashMap<String, Object> requestParam) {
		Integer requestCode = (Integer) requestParam.get("requestCode");
		if (requestCode == null) {
			Toast.makeText(this,"请求码为空",Toast.LENGTH_SHORT).show();
		} else {
			if(requestCode == PAY_RESULT_NOTIFY){
				finish();
			}else {
				Toast.makeText(this,"未知操作",Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void negativeAction(HashMap<String, Object> requestParam) {

	}

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		private WeakReference<PayDemoActivity> payDemoActivityWeakReference;

		public MyHandler(WeakReference<PayDemoActivity> payDemoActivityWeakReference) {
			this.payDemoActivityWeakReference = payDemoActivityWeakReference;
		}

		@Override
		// 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知
		public void handleMessage(Message msg) {
			PayDemoActivity myActivity = payDemoActivityWeakReference.get();
			int what = msg.what;
			// 跳转支付结果
			if (what == SDK_PAY_FLAG) {
				@SuppressWarnings("unchecked")
				PayResult payResult = new PayResult((Map<String, String>) msg.obj);
				// 同步返回需要验证的信息
				String resultInfo = payResult.getResult();
				String resultStatus = payResult.getResultStatus();
				HashMap<String, Object> hashMap = new HashMap<>(8);
				hashMap.put("requestCode",PAY_RESULT_NOTIFY);
				hashMap.put("tAG",TAG);
				hashMap.put("code",resultStatus);
				// 判断resultStatus 为9000则代表支付成功
				if (TextUtils.equals(resultStatus, "9000")) {
					hashMap.put("message",getString(R.string.pay_success));
					hashMap.put("tip","");
				} else {
					hashMap.put("message",getString(R.string.pay_failed));
					hashMap.put("tip","若有疑问，请联系管理员！");
				}
				DialogUtil.showDialog(PayDemoActivity.this,true,false,"支付结果通知",PayDemoActivity.this,hashMap);
			}
			// 授权支付结果
			else if (what == SDK_AUTH_FLAG) {
				@SuppressWarnings("unchecked")
				AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
				String resultStatus = authResult.getResultStatus();
				// 判断resultStatus 为“9000”且result_code
				// 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
				if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
					// 获取alipay_open_id，调支付时作为参数extern_token 的value
					// 传入，则支付账户为该授权账户
					showAlert(myActivity, getString(R.string.auth_success) + authResult);
				} else {
					// 其他状态值则为授权失败
					showAlert(myActivity, getString(R.string.auth_failed) + authResult);
				}
			}
			else if(what == REQUEST_INTERCEPTED) {
				Bundle data = msg.getData();
				DialogUtil.showDialog(myActivity, TAG, data, true);
			}
			else if (what == REQUEST_TO_PAY_OK) {
				Bundle bundle = msg.getData();
				// 取出订单信息
				orderInfo = bundle.getString("orderStr");
				Application application = bundle.getParcelable("application");
				fillDate(application);
			}
			else if (what == REQUEST_TO_PAY_ERROR) {
				Bundle bundle = msg.getData();
				DialogUtil.showDialog(myActivity,TAG,bundle,false);
			}
			// 默认
			else  {
				Bundle bundle = msg.getData();
				Toast.makeText(myActivity,bundle.getString("reason"),Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 获取支付宝 SDK 版本号。
	 */
	public void showSdkVersion() {
		PayTask payTask = new PayTask(this);
		String version = payTask.getVersion();
		showAlert(this, getString(R.string.alipay_sdk_version_is) + version);
	}
	/**
	 * 将 H5 网页版支付转换成支付宝 App 支付的示例
	 */
	public void h5Pay(View v) {
		WebView.setWebContentsDebuggingEnabled(true);
		Intent intent = new Intent(this, H5PayDemoActivity.class);
		Bundle extras = new Bundle();
		/*
		 * URL 是要测试的网站，在 Demo App 中会使用 H5PayDemoActivity 内的 WebView 打开。
		 *
		 * 可以填写任一支持支付宝支付的网站（如淘宝或一号店），在网站中下订单并唤起支付宝；
		 * 或者直接填写由支付宝文档提供的“网站 Demo”生成的订单地址
		 * （如 https://mclient.alipay.com/h5Continue.htm?h5_route_token=303ff0894cd4dccf591b089761dexxxx）
		 * 进行测试。
		 * 
		 * H5PayDemoActivity 中的 MyWebViewClient.shouldOverrideUrlLoading() 实现了拦截 URL 唤起支付宝，
		 * 可以参考它实现自定义的 URL 拦截逻辑。
		 *
		 * 注意：WebView 的 shouldOverrideUrlLoading(url) 无法拦截直接调用 open(url) 打开的第一个 url，
		 * 所以直接设置 url = "https://mclient.alipay.com/cashier/mobilepay.htm......" 是无法完成网页转 Native 的。
		 * 如果需要拦截直接打开的支付宝网页支付 URL，可改为使用 shouldInterceptRequest(view, request) 。
		 */
		String url = "https://m.taobao.com";
		extras.putString("url", url);
		intent.putExtras(extras);
		startActivity(intent);
	}

	private static void showAlert(Context ctx, String info) {
		showAlert(ctx, info, null);
	}

	private static void showAlert(Context ctx, String info, DialogInterface.OnDismissListener onDismiss) {
		new AlertDialog.Builder(ctx)
				.setMessage(info)
				.setPositiveButton(R.string.confirm, null)
				.setOnDismissListener(onDismiss)
				.show();
	}
}
