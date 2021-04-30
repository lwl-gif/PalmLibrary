package com.example.ul.librarian.main.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesOnlyReadAdapter;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 管理员查看读者详情的活动
 * @Date: Created 16:54 2021/4/21
 * @Modified: by who yyyy-MM-dd
 */
@SuppressLint("NonConstantResourceId")
public class LReaderDetailActivity extends Activity implements HttpUtil.MyCallback, ImageAdapterItemListener {

    private static final String TAG = "LReaderDetailActivity";
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 1100;
    /**请求失败*/
    private static final int REQUEST_FAIL = 11000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 11001;
    /**获取读者详情*/
    private static final int GET_READER_DETAIL = 1101;
    /**获取读者详情成功*/
    private static final int GET_READER_DETAIL_SUCCEED = 11011;
    /**获取读者详情失败*/
    private static final int GET_READER_DETAIL_FAIL = 11010;
    /**删除读者*/
    private static final int CLOSE_ACCOUNT = 1102;
    /**审核通过*/
    private static final int CHECK_OK = 1103;
    /**审核不通过*/
    private static final int CHECK_NO = 1104;
    /**获取权限类别*/
    private static final int PERMISSION_LEVEL = 1105;
    /**权限分类*/
    private JSONArray permissionLevel;
    private JSONArray creditLevel;
    /**服务器返回的读者详情*/
    private JSONObject jsonObject = null;
    /**能否编辑权限信息*/
    private boolean writingPermission = false;
    /**文本*/
    @BindView(R.id.readerDetail_title)
    public TextView rdTitle;
    @BindView(R.id.readerId)
    public TextView rdId;
    @BindView(R.id.title_picture)
    public TextView rdPicture;
    @BindView(R.id.readerName)
    public TextView rdName;
    @BindView(R.id.readerSex)
    public TextView rdSex;
    @BindView(R.id.readerAge)
    public TextView rdAge;
    @BindView(R.id.readerDepartment)
    public TextView rdDepartment;
    @BindView(R.id.readerClassroom)
    public TextView rdClassroom;
    @BindView(R.id.readerUsername)
    public TextView rdUsername;
    @BindView(R.id.readerPassword)
    public TextView rdPassword;
    @BindView(R.id.readerPhone)
    public TextView rdPhone;
    @BindView(R.id.readerEMail)
    public TextView rdEmail;
    @BindView(R.id.readerType)
    public TextView rdType;
    @BindView(R.id.readerPermission)
    public TextView rdPermission;
    /**可编辑文本*/
    @BindView(R.id.readerCredit)
    public EditText rdCredit;
    @BindView(R.id.readerAmount)
    public EditText rdAmount;
    @BindView(R.id.readerTerm)
    public EditText rdTerm;
    /**按钮*/
    @BindView(R.id.button_back)
    public ImageButton buttonBack;
    @BindView(R.id.button_checkNo)
    public Button buttonNo;
    @BindView(R.id.button_checkOk)
    public Button buttonOk;
    @BindView(R.id.button_delete)
    public Button buttonDelete;
    /**展示图片的列表*/
    private RecyclerView recyclerView;
    /**展示图片的适配器*/
    private ImagesOnlyReadAdapter imagesOnlyReadAdapter;
    /**当前读者id*/
    private String readerId = null;

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    static class MyHandler extends Handler {
        private WeakReference<LReaderDetailActivity> lReaderDetailActivity;

        public MyHandler(WeakReference<LReaderDetailActivity> lReaderDetailActivity) {
            this.lReaderDetailActivity = lReaderDetailActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            LReaderDetailActivity myActivity = lReaderDetailActivity.get();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }else if (what == REQUEST_BUT_FAIL_READ_DATA) {
                Toast.makeText(myActivity, "子线程解析数据异常！", Toast.LENGTH_SHORT).show();
            }else if (what == GET_READER_DETAIL_SUCCEED){
                myActivity.fillData();
            }else if(what == PERMISSION_LEVEL) {
                Toast.makeText(myActivity, "权限级别查询成功！", Toast.LENGTH_SHORT).show();
                myActivity.binding();
            }else if(what == CHECK_OK || what == CHECK_NO){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                if("处理成功！".equals(message)){
                    Toast.makeText(myActivity, message, Toast.LENGTH_SHORT).show();
                }else {
                    DialogUtil.showDialog(myActivity,TAG,bundle,false);
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_l_reader_detail);
        ButterKnife.bind(this);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        imagesOnlyReadAdapter = new ImagesOnlyReadAdapter(this,token,this);
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setAdapter(imagesOnlyReadAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        // 返回按钮绑定返回事件
        buttonBack.setOnClickListener(view -> {
            finish();
        });
        buttonNo.setVisibility(View.GONE);
        buttonOk.setVisibility(View.GONE);
        buttonDelete.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 屏幕亮起时开始初始化
        init();
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        readerId = getIntent().getStringExtra("readerId");
        if(readerId == null){
            Toast.makeText(this,"读者Id为空！",Toast.LENGTH_SHORT).show();
            finish();
        }else {
            if(permissionLevel == null || permissionLevel.length() <= 0) {
                // 发送请求获取权限分类
                String url = HttpUtil.BASE_URL + "readerPermission/getPermissionLevel";
                HttpUtil.getRequest(token, url, this, PERMISSION_LEVEL);
            }
            if(jsonObject == null || jsonObject.length() <= 0) {
                // 发送请求
                String url = HttpUtil.BASE_URL + "reader/selectAllById";
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id",readerId);
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.getRequest(token, url, this, GET_READER_DETAIL);
            }else {
                fillData();
            }
        }
    }

    /**
     * @Author: Wallace
     * @Description: 初始化:可编辑文本全部不可编辑
     * @Date: Created 17:15 2021/4/21
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    void init(){
        rdCredit.setFocusable(writingPermission);
        rdCredit.setFocusableInTouchMode(writingPermission);
        rdCredit.setClickable(writingPermission);
        rdCredit.setEnabled(writingPermission);

        rdAmount.setFocusable(writingPermission);
        rdAmount.setFocusableInTouchMode(writingPermission);
        rdAmount.setClickable(writingPermission);
        rdAmount.setEnabled(writingPermission);

        rdTerm.setFocusable(writingPermission);
        rdTerm.setFocusableInTouchMode(writingPermission);
        rdTerm.setClickable(writingPermission);
        rdTerm.setEnabled(writingPermission);
    }
    /**
     * @Author: Wallace
     * @Description: 填充读者详情的数据，根据数据的不同来动态改变组件的属性
     * @Date: Created 17:54  2021/4/4
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    private void fillData() {
        // 定义读者身份验证的几种状态
        String checkedState = "checked";
        String checkingState = "checking";
        // 解析数据
        try {
            this.readerId = jsonObject.getString("id");
            rdId.setText(readerId);
            rdName.setText(jsonObject.getString("name"));
            String sex = "0".equals(jsonObject.getString("sex")) ? "女" : "男";
            rdSex.setText(sex);
            rdAge.setText(jsonObject.getString("age"));
            rdDepartment.setText(jsonObject.getString("department"));
            rdClassroom.setText(jsonObject.getString("classroom"));
            rdUsername.setText(jsonObject.getString("username"));
            rdPassword.setText(jsonObject.getString("password"));
            rdPhone.setText(jsonObject.getString("phone"));
            rdEmail.setText(jsonObject.getString("email"));
            rdAge.setText(jsonObject.getString("age"));
            JSONObject readerPermission = jsonObject.getJSONObject("readerPermission");
            String state = readerPermission.getString("state");
            // 读者身份已审核通过
            if (checkedState.equals(state)) {
                // 补充读者权限信息
                String credit = readerPermission.getString("credit");
                String amount = readerPermission.getString("amount");
                String permissionName = readerPermission.getString("permissionName");
                String t = readerPermission.getString("term");
                String typeName = readerPermission.getString("typeName");
                rdCredit.setText(credit);
                rdAmount.setText(amount);
                rdPermission.setText(permissionName);
                rdType.setText(typeName);
                long l = Long.parseLong(t);
                Date date = new Date(l);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String term = format.format(date);
                rdTerm.setText(term);
                // 隐藏图片信息
                rdPicture.setVisibility(View.GONE);
                // 隐藏图片列表
                recyclerView.setVisibility(View.GONE);
                // 不允许管理员编辑读者的权限信息
                writingPermission = false;
                rdTitle.setText(R.string.checkedReader);
            }
            // 读者身份正在审核中
            else if (checkingState.equals(state)) {
                String typeName = readerPermission.getString("typeName");
                rdType.setText(typeName);
                // 把已提交的证件照展示出来
                String imagePath = readerPermission.getString("image");
                JSONArray pictureNames = readerPermission.getJSONArray("pictures");
                String httpBaseUrl = HttpUtil.BASE_URL + "reader/reader_type_picture/checking/" + imagePath + "/" ;
                ArrayList<String> imageNameUrlList = new ArrayList<>();
                for(int i = 0; i < pictureNames.length(); i++){
                    imageNameUrlList.add(httpBaseUrl+pictureNames.get(i));
                }
                this.imagesOnlyReadAdapter.setImageNameUrlList(imageNameUrlList);
                // 允许管理员编辑读者的权限信息
                writingPermission = true;
                rdTitle.setText(R.string.checkingReader);
            }
            // 读者未进行身份审核
            else{
                // 允许管理员编辑读者的权限信息
                writingPermission = false;
                rdTitle.setText(R.string.uncheckedReader);
            }
            secondInit();
        } catch (JSONException e) {
            Toast.makeText(this,"主程序解析数据时异常！",Toast.LENGTH_LONG).show();
        }
    }
    /**
     * @Author: Wallace
     * @Description: 获取信息之后再次对某些控件的属性进行调整
     * @Date: Created 22:39 2021/4/4
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    private void secondInit() {
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        buttonDelete.setVisibility(View.VISIBLE);
        buttonDelete.setOnClickListener(view -> {
            String url = HttpUtil.BASE_URL + "reader/deleteById";
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id",readerId);
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.deleteRequest(token,url,this,CLOSE_ACCOUNT);
        });
        // 读者正在审核的
        if(writingPermission){
            init();
            // 显示“审核通过”和“审核不通过”两个按钮,绑定事件
            buttonNo.setVisibility(View.VISIBLE);
            buttonNo.setOnClickListener(view -> {
                String url = HttpUtil.BASE_URL + "readerPermission/checkNo";
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("readerId",readerId);
                HttpUtil.putRequest(token,url,hashMap,this,CHECK_NO);
            });
            buttonOk.setVisibility(View.VISIBLE);
            buttonOk.setOnClickListener(view -> {
                String url = HttpUtil.BASE_URL + "readerPermission/checkOk";
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("readerId",readerId);
                hashMap.put("credit",rdCredit.getText().toString().trim());
                hashMap.put("permissionName",rdPermission.getText().toString().trim());
                hashMap.put("amount",rdAmount.getText().toString().trim());
                hashMap.put("term",rdTerm.getText().toString().trim());
                HttpUtil.putRequest(token,url,hashMap,this,CHECK_OK);
            });
        }
    }

    /**
     * @Author: Wallace
     * @Description: 获取到权限分类之后，对信誉分输入框进行监听，
     * 根据信誉分的输入动态改变借阅权限的级别
     * @Date: Created 21:24 2021/4/21
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    private void binding() {
        rdCredit.addTextChangedListener(new TextWatcher() {
            final int max = 100;
            final int min = 0;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            // 在内容改变时，调整内容在合理范围内（0~100）
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //先判断 rdCredit.getText()是否为空才能使用Integer.parseInt，否则会报异常。
                try {
                    if (Integer.parseInt(String.valueOf(rdCredit.getText())) < min) {
                        rdCredit.setText(String.valueOf(min));
                    }
                    if (Integer.parseInt(String.valueOf(rdCredit.getText())) > max) {
                        rdCredit.setText(String.valueOf(max));
                    }
                } catch (NumberFormatException numberFormatException) {
                    rdCredit.setText(String.valueOf(min));
                    Toast.makeText(LReaderDetailActivity.this, "请输入0~100内的数字", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                int result;
                try{
                    result = Integer.parseInt(editable.toString());
                }catch (NumberFormatException numberFormatException){
                    result = 0;
                }
                // 初始为最低权限
                int position = permissionLevel.length() - 1;
                for (int i = 0; i < creditLevel.length(); i++) {
                    try {
                        if (result >= creditLevel.getInt(i)) {
                            position = i;
                            break;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "afterTextChanged: creditLevel.getInt(" + i + ") 异常： " + creditLevel.toString());
                    }
                }
                try {
                    String permissionName = permissionLevel.getString(position);
                    rdPermission.setText(permissionName);
                } catch (JSONException e) {
                    rdPermission.setText("");
                    Log.e(TAG, "afterTextChanged: permissionLevel.getString(" + position + "):" + "数据异常");
                }
            }
        });
        String credit = "100";
        rdCredit.setText(credit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        readerId = null;
        ActivityManager.getInstance().removeActivity(this);
    }
    /**
     * @Author:Wallace
     * @Description: 开启一个Activity，用大图来展示当前的图片
     * @Date: Created in 21:41 2021/4/11
     * @Modified By:
     * @param position item的位置
     * @return: void
     */
    @Override
    public void onClickToShow(int position) {
        Intent intent = new Intent(LReaderDetailActivity.this, ShowPictureActivity.class);
        intent.putExtra("TAG", TAG);
        intent.putExtra("position",position);
        intent.putExtra("imagesPath", imagesOnlyReadAdapter.getImagesPath());
        startActivity(intent);
    }

    @Override
    public void onClickToDelete(int position) {

    }

    @Override
    public void success(Response response, int code) throws IOException {
        //获取服务器响应字符串
        String result = response.body().string().trim();
        switch (code) {
            case GET_READER_DETAIL:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String tip = null;
                    if("查询成功！".equals(message)){
                        //查询成功，获取书籍数据，通知主线程渲染前端
                        this.jsonObject = jsonObject.getJSONObject("object");
                        myHandler.sendEmptyMessage(GET_READER_DETAIL_SUCCEED);
                        break;
                    } else {
                        String c = jsonObject.getString("code");
                        tip = jsonObject.getString("tip");
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("code",c);
                        data.putString("tip",tip);
                        data.putString("message",message);
                        msg.setData(data);
                        msg.what = GET_READER_DETAIL_FAIL;
                        myHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            case CLOSE_ACCOUNT:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("tip",tip);
                    data.putString("message",message);
                    msg.setData(data);
                    msg.what = CLOSE_ACCOUNT;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            case PERMISSION_LEVEL:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("dataObject");
                    this.permissionLevel = jsonObject1.getJSONArray("permissionLevel");
                    this.creditLevel = jsonObject1.getJSONArray("creditLevel");
                    myHandler.sendEmptyMessage(PERMISSION_LEVEL);
                    break;
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            case CHECK_OK:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("tip",tip);
                    data.putString("message",message);
                    msg.setData(data);
                    msg.what = CHECK_OK;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            case CHECK_NO:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    String tip = jsonObject.getString("tip");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("tip",tip);
                    data.putString("message",message);
                    msg.setData(data);
                    msg.what = CHECK_NO;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            default:
                Message msg = new Message();
                Bundle bundle = new Bundle();
                String reason = "未知错误";
                bundle.putString("reason",reason);
                msg.setData(bundle);
                myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        String reason = null;
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
}
