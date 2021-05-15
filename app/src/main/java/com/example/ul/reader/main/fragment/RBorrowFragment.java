package com.example.ul.reader.main.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ul.R;
import com.example.ul.adapter.BorrowListAdapter;
import com.example.ul.callback.CallbackToRBorrowFragment;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.callback.SearchCallback;
import com.example.ul.model.UserInfo;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.MySearchView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 读者借阅管理
 * @Date: 2021/3/9 20:29
 * @Modified By:
 */
public class RBorrowFragment extends Fragment implements CompoundButton.OnCheckedChangeListener,
        CallbackToRBorrowFragment, SearchCallback, HttpUtil.MyCallback, DialogUtil.DialogActionCallback {

    private static final String TAG = "RBorrowFragment";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST_ERROR = 400;
    /**请求失败*/
    private static final int REQUEST_FAIL = 4000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 402;
    /**查询借阅、预约和已过期的记录*/
    private static final int GET_RECORD = 401;
    /**查询成功，通知主线程渲染*/
    private static final int GET_RECORD_SUCCESS_FILL = 40111;
    /**查询成功，但不需要渲染*/
    private static final int GET_RECORD_SUCCESS_NOT_FILL = 40110;
    /**查询失败*/
    private static final int GET_RECORD_FAIL = 4010;
    /**续借*/
    private static final int RENEW_BOOK = 403;
    /**挂失*/
    private static final int LOSS_BOOK = 404;
    /**转借*/
    private static final int LENT_BOOK = 405;
    /**放弃预约*/
    private static final int ABANDON_BOOK = 406;
    /**搜索框内容*/
    String queryString = "null";
    /**checkBox选中标志*/
    private boolean box1,box2,box3;
    /**文本框——当前借阅\当前预约\过期记录*/
    private TextView textView1,textView2,textView3;
    /**列表——当前借阅\当前预约\过期记录*/
    private RecyclerView recyclerViewBorrow,recyclerViewReserve,recyclerExpired;
    /**服务器返回的信息*/
    private JSONArray jsonArray;
    /**每个列表的数据*/
    private JSONArray jsonArrayBorrow;
    private JSONArray jsonArrayReserve;
    private JSONArray jsonArrayExpired;
    /**适配器*/
    private BorrowListAdapter adapterBorrow;
    private BorrowListAdapter adapterReserve;
    private BorrowListAdapter adapterExpired;
    /**根布局*/
    private ConstraintLayout rootView;
    private String token;
    private CallbackToMainActivity callbackToMainActivity;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException(TAG+"所在的Context必须实现CallbackToMainActivity接口");
        }
        callbackToMainActivity = (CallbackToMainActivity) context;
        Log.d(TAG, "onAttach: callbackToMainActivity = " + callbackToMainActivity);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        token = userInfo.getToken();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewTemp = inflater.inflate(R.layout.borrow_manage, container, false);
        rootView = viewTemp.findViewById(R.id.root_borrow_manage);
        MySearchView mySearchView = rootView.findViewById(R.id.mySearchView);
        mySearchView.setSearchCallback(this);
        TextView textView = rootView.findViewById(R.id.textSelect);
        textView.setOnClickListener(view -> {
            query();
        });
        // 复选框——当前借阅\当前预约\过期记录
        CheckBox checkBox1 = rootView.findViewById(R.id.checkBox1);
        checkBox1.setOnCheckedChangeListener(this);
        CheckBox checkBox2 = rootView.findViewById(R.id.checkBox2);
        checkBox2.setOnCheckedChangeListener(this);
        CheckBox checkBox3 = rootView.findViewById(R.id.checkBox3);
        checkBox3.setOnCheckedChangeListener(this);
        textView1 = rootView.findViewById(R.id.nowBorrow);
        recyclerViewBorrow = rootView.findViewById(R.id.recyclerBorrowList);
        textView2 = rootView.findViewById(R.id.nowReserve);
        recyclerViewReserve = rootView.findViewById(R.id.recyclerReserveList);
        textView3 = rootView.findViewById(R.id.expiredRecord);
        recyclerExpired = rootView.findViewById(R.id.recyclerRecordList);
        // 默认的界面设置
        box1 = true;
        textView1.setVisibility(View.VISIBLE);
        checkBox1.setChecked(true);
        box2 = false;
        textView2.setVisibility(View.GONE);
        checkBox2.setChecked(false);
        box3 = false;
        textView3.setVisibility(View.GONE);
        checkBox3.setChecked(false);
        init();
        return rootView;
    }

    void init(){
        // 为RecyclerView设置布局管理器
        recyclerViewBorrow.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        adapterBorrow = new BorrowListAdapter(getActivity(),new JSONArray(),"id","name","readerId","readerName",
                "state","start","end","box1",this);
        recyclerViewBorrow.setAdapter(adapterBorrow);
        // 为RecyclerView设置布局管理器
        recyclerViewReserve.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        adapterReserve = new BorrowListAdapter(getActivity(),new JSONArray(),"id","name","readerId","readerName",
                "state","start","end","box1",this);
        recyclerViewReserve.setAdapter(adapterReserve);
        // 为RecyclerView设置布局管理器
        recyclerExpired.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        adapterExpired = new BorrowListAdapter(getActivity(),new JSONArray(),"id","name","readerId","readerName",
                "state","start","end","box1",this);
        recyclerExpired.setAdapter(adapterExpired);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackToMainActivity = null;
    }

    @Override
    public void searchAction(String s) {
        queryString = s;
    }

    private void query() {
        // 搜索框的内容
        if ((queryString == null) || ("".equals(queryString))) {
            queryString = "null";
        }
        String url = HttpUtil.BASE_URL + "borrow/myBorrow";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("checkBox1", Boolean.toString(box1));
        hashMap.put("checkBox2", Boolean.toString(box2));
        hashMap.put("checkBox3", Boolean.toString(box3));
        hashMap.put("queryString", queryString);
        url = HttpUtil.newUrl(url,hashMap);
        HttpUtil.getRequest(token, url, this, GET_RECORD);
    }

    private void fillData() {
        if((jsonArray == null)||(jsonArray.size() <= 0)){
            Toast.makeText(getActivity(),"未获取到数据！",Toast.LENGTH_LONG).show();
        }else{
            if(box1){
                recyclerViewBorrow.setVisibility(View.VISIBLE);
                if(jsonArrayBorrow.size()>0){
                    adapterBorrow.setJsonArray(jsonArrayBorrow);
                }
            }
            if(box2){
                recyclerViewReserve.setVisibility(View.VISIBLE);
                if(jsonArrayReserve.size()>0){
                    adapterReserve.setJsonArray(jsonArrayReserve);
                }
            }
            if(box3){
                recyclerExpired.setVisibility(View.VISIBLE);
                if(jsonArrayExpired.size()>0){
                    adapterExpired.setJsonArray(jsonArrayExpired);
                }
            }
        }
    }

    @Override
    public void borrowListToWantMore(int i) {

    }

    @Override
    public void borrowListToLent(int i) {
        // 要转借的记录id
        JSONArray temp = adapterBorrow.getJsonArray();
        Integer bookId = temp.getJSONObject(i).getInteger("id");
        String msg = "系统将为您生成此书的转借二维码，对方扫该二维码即可完成转借，该二维码五分钟内有效。";
        HashMap<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("requestCode",LENT_BOOK);
        hashMap.put("bookId",bookId);
        DialogUtil.showDialog(getActivity(),"转借",msg,this,hashMap);
    }

    @Override
    public void borrowListToRenew(int i) {
        // 要续借的记录id
        JSONArray temp = adapterBorrow.getJsonArray();
        Integer bookId = temp.getJSONObject(i).getInteger("id");
        String bookName = temp.getJSONObject(i).getString("name");
        String msg = "确定续借此书？（id:"+bookId+",书名:"+bookName+"）";
        HashMap<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("requestCode",RENEW_BOOK);
        hashMap.put("bookId",bookId);
        DialogUtil.showDialog(getActivity(),"续借",msg,this,hashMap);
    }

    @Override
    public void borrowListToLoss(int i) {
        // 要挂失的记录id
        JSONArray temp = adapterBorrow.getJsonArray();
        Integer bookId = temp.getJSONObject(i).getInteger("id");
        String bookName = temp.getJSONObject(i).getString("name");
        String msg = "确定挂失此书？（id:"+bookId+",书名:"+bookName+"）";
        HashMap<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("requestCode",LOSS_BOOK);
        hashMap.put("bookId",bookId);
        DialogUtil.showDialog(getActivity(),"挂失",msg,this,hashMap);
    }

    @Override
    public void borrowListToAbandon(int i) {
        // 要挂失的记录id
        JSONArray temp = adapterBorrow.getJsonArray();
        Integer bookId = temp.getJSONObject(i).getInteger("id");
        String bookName = temp.getJSONObject(i).getString("name");
        String msg = "确定放弃本次预约？（id:"+bookId+",书名:"+bookName+"）";
        HashMap<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("requestCode",ABANDON_BOOK);
        hashMap.put("bookId",bookId);
        DialogUtil.showDialog(getActivity(),"放弃预约",msg,this,hashMap);
    }

    @Override
    public void positiveAction(HashMap<String, Object> requestParam) {
        Integer requestCode = (Integer) requestParam.get("requestCode");
        if (requestCode == null) {
            Toast.makeText(getActivity(),"请求码为空",Toast.LENGTH_SHORT).show();
        } else {
            if (requestCode == RENEW_BOOK) {
                Integer bookId = (Integer) requestParam.get("bookId");
                HashMap<String, String> hashMap = new HashMap<>(4);
                hashMap.put("bookId", String.valueOf(bookId));
                String url = HttpUtil.BASE_URL + "borrow/renewBook";
                HttpUtil.putRequest(token, url, hashMap, this, RENEW_BOOK);
            } else if (requestCode == LOSS_BOOK) {
                Integer bookId = (Integer) requestParam.get("bookId");
                HashMap<String, String> hashMap = new HashMap<>(4);
                hashMap.put("bookId", String.valueOf(bookId));
                String url = HttpUtil.BASE_URL + "borrow/lossBook";
                HttpUtil.postRequest(token, url, hashMap, this, LOSS_BOOK);
            } else if (requestCode == LENT_BOOK) {
                // 展示二维码的视图
                final View view = LayoutInflater.from(getActivity()).inflate(R.layout.show_qrcode, rootView,false);
                // 添加该视图到布局中
                rootView.addView(view);
                // 动态修改该视图的约束
                ConstraintSet c = new ConstraintSet();
                c.clone(rootView);
                c.connect(view.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
                c.connect(view.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
                c.connect(view.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
                c.connect(view.getId(),ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);
                c.applyTo(rootView);
                TextView titleQrcode = view.findViewById(R.id.title_qrcode);
                titleQrcode.setText("转借二维码");
                final ImageView ivQrcode = view.findViewById(R.id.iv_qrcode);
                TextView tvTipQrcode = view.findViewById(R.id.tv_tip_qrcode);
                tvTipQrcode.setText("提示");
                final TextView tipQrcode = view.findViewById(R.id.tip_qrcode);
                ImageView ivQrcodeDelete = view.findViewById(R.id.iv_qrcode_delete);
                ivQrcodeDelete.setOnClickListener(v -> {
                    ViewGroup parent = (ViewGroup) view.getParent();
                    parent.removeView(view);
                });
                Integer bookId = (Integer) requestParam.get("bookId");
                String url = HttpUtil.BASE_URL + "borrow/lentBook";
                GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                        .addHeader("Authorization", this.token)
                        .addHeader("bookId", String.valueOf(bookId))
                        .build());
                Glide.with(getActivity())
                        .load(glideUrl)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                tipQrcode.setText("二维码加载失败！");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                String s = "对方使用客户端扫一扫功能扫码即可完成转借。"+"\n"+"二维码五分钟内有效。";
                                tipQrcode.setText(s);
                                return false;
                            }
                        })
                        .into(ivQrcode);
            } else if(requestCode == ABANDON_BOOK){
                Integer bookId = (Integer) requestParam.get("bookId");
                HashMap<String, String> hashMap = new HashMap<>(4);
                hashMap.put("bookId", String.valueOf(bookId));
                String url = HttpUtil.BASE_URL + "reservation/abandonBook";
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.deleteRequest(token, url, this, ABANDON_BOOK);
            } else {
                Toast.makeText(getActivity(),"未知动作",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void negativeAction(HashMap<String, Object> requestParam) {
        Integer requestCode = (Integer) requestParam.get("requestCode");
        if(requestCode == null){
            Toast.makeText(getActivity(),"请求码为空",Toast.LENGTH_SHORT).show();
        }else {
            if(requestCode == RENEW_BOOK){
                Toast.makeText(getActivity(),"您取消了续借",Toast.LENGTH_SHORT).show();
            }else if(requestCode == LOSS_BOOK){
                Toast.makeText(getActivity(),"您取消了挂失",Toast.LENGTH_SHORT).show();
            }else if(requestCode == LENT_BOOK){
                Toast.makeText(getActivity(),"您取消了转借",Toast.LENGTH_SHORT).show();
            }else if(requestCode == ABANDON_BOOK){
                Toast.makeText(getActivity(),"您选择了取消",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(),"未知动作",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        switch (checkBox.getId()) {
            // 选中当前借阅
            case R.id.checkBox1:
                if (checked) {
                    box1 = true;
                    textView1.setVisibility(View.VISIBLE);
                    recyclerViewBorrow.setVisibility(View.VISIBLE);
                } else {
                    box1 = false;
                    textView1.setVisibility(View.GONE);
                    recyclerViewBorrow.setVisibility(View.GONE);
                }
                break;
            // 选中当前预约
            case R.id.checkBox2:
                if (checked) {
                    box2 = true;
                    textView2.setVisibility(View.VISIBLE);
                    recyclerViewReserve.setVisibility(View.VISIBLE);
                } else {
                    box2 = false;
                    textView2.setVisibility(View.GONE);
                    recyclerViewReserve.setVisibility(View.GONE);
                }
                break;
            // 选中过期记录
            case R.id.checkBox3:
                if (checked) {
                    box3 = true;
                    textView3.setVisibility(View.VISIBLE);
                    recyclerExpired.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(),"该功能未开发！",Toast.LENGTH_SHORT).show();
                } else {
                    box3 = false;
                    textView3.setVisibility(View.GONE);
                    recyclerExpired.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
        query();
    }
    
    static class MyHandler extends Handler {
        private WeakReference<RBorrowFragment> rBorrowFragment;

        public MyHandler(WeakReference<RBorrowFragment> rBorrowFragment){
            this.rBorrowFragment = rBorrowFragment;
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            RBorrowFragment myFragment = rBorrowFragment.get();
            if(what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myFragment.getActivity(),bundle.getString("reason"),Toast.LENGTH_SHORT).show();
            } else if (what == GET_RECORD_SUCCESS_FILL) {
                myFragment.fillData();
            } else {
                Bundle data = msg.getData();
                String message = data.getString("message");
                String tip = data.getString("tip");
                if (what == GET_RECORD_SUCCESS_NOT_FILL) {
                    Toast.makeText(myFragment.getActivity(),message + tip, Toast.LENGTH_LONG).show();
                }
                else if(what == RENEW_BOOK){
                    if("续借失败！".equals(message)){
                        DialogUtil.showDialog(myFragment.getActivity(),TAG,data,false);
                    }else {
                        Toast.makeText(myFragment.getActivity(),message, Toast.LENGTH_LONG).show();
                    }
                }
                else if(what == LOSS_BOOK){
                    if("挂失失败！".equals(message)){
                        DialogUtil.showDialog(myFragment.getActivity(),TAG,data,false);
                    }else {
                        Toast.makeText(myFragment.getActivity(),message, Toast.LENGTH_LONG).show();
                    }
                }
                else if(what == ABANDON_BOOK){
                    if("操作失败！".equals(message)){
                        DialogUtil.showDialog(myFragment.getActivity(),TAG,data,false);
                    }else {
                        Toast.makeText(myFragment.getActivity(),message, Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    DialogUtil.showDialog(myFragment.getActivity(),TAG,data, what == REQUEST_INTERCEPTED);
                }
            }
        }
    }
    
    MyHandler myHandler = new MyHandler(new WeakReference(this));
    
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
            if (code == GET_RECORD) {
                bundle.putString("code", c);
                bundle.putString("tip", tip);
                bundle.putString("message", message);
                msg.setData(bundle);
                if ("查询成功！".equals(message)) {
                    jsonObject.getJSONArray("dataArray");
                    jsonArray = jsonObject.getJSONArray("dataArray");
                    // 将整个信息拆分
                    jsonArrayBorrow = jsonArray.getJSONArray(0);
                    jsonArrayReserve = jsonArray.getJSONArray(1);
                    jsonArrayExpired = jsonArray.getJSONArray(2);
                    if (jsonArrayBorrow.size() + jsonArrayReserve.size() + jsonArrayExpired.size() == 0) {
                        msg.what = GET_RECORD_SUCCESS_NOT_FILL;
                    }
                    msg.what = GET_RECORD_SUCCESS_FILL;
                } else {
                    msg.what = GET_RECORD_FAIL;
                }
                myHandler.sendMessage(msg);
            } 
            else if(code == RENEW_BOOK || code == LOSS_BOOK || code == ABANDON_BOOK){
                bundle.putString("code", c);
                bundle.putString("tip", tip);
                bundle.putString("message", message);
                msg.setData(bundle);
                msg.what = code;
                myHandler.sendMessage(msg);
            }
            else {
                bundle.putString("reason","未知错误");
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
        bundle.putString("reason",reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}
