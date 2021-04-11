package com.example.ul.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.ul.R;
import com.example.ul.adapter.PictureListAdapter;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.PictureSelectViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @Author:Wallace
 * @Description:显示某个读者详情的碎片（读者和管理员共用一个活动）。
 * @Date:2021/3/7 10:14
 * @Modified By:
 */
public class ReaderDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback,PictureListAdapter.ItemClickListener{

    /**自定义消息代码*/
    /**未知请求*/
    private static final int UNKNOWN_REQUEST = 900;
    /**请求失败*/
    private static final int REQUEST_FAIL = 9000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 9001;
    /**获取读者详情*/
    private static final int GET_READER_DETAIL = 901;
    /**获取读者详情成功*/
    private static final int GET_READER_DETAIL_SUCCEED = 9011;
    /**获取读者详情失败*/
    private static final int GET_READER_DETAIL_FAIL = 9010;
    /**退出登录请求*/
    private static final int ACCOUNT_OFFLINE = 902;
    /**退出成功*/
    private static final int ACCOUNT_OFFLINE_SUCCEED = 9021;
    /**退出失败*/
    private static final int ACCOUNT_OFFLINE_FAIL = 9020;
    /**管理员注销账户或者读者自行销户*/
    private static final int CLOSE_ACCOUNT = 903;
    /**读者更新自己的基本信息*/
    private static final int UPDATE_BASIC_INFORMATION = 904;
    /**读者更新自己的基本信息成功*/
    private static final int UPDATE_BASIC_INFORMATION_SUCCEED = 9041;
    /**读者更新自己的基本信息失败*/
    private static final int UPDATE_BASIC_INFORMATION_FAIL = 9040;
    /**读者申请借阅权限*/
    private static final int APPLY_PERMISSION = 905;
    
    private static final String TAG = "ReaderDetailActivity";

    /**当前页面读者的id*/
    private String id;
    /**可编辑文本*/
    @BindView(R.id.readerDetail_title)
    public TextView rdTitle;
    @BindView(R.id.readerId)
    public TextView rdId;
    @BindView(R.id.readerName)
    public EditText rdName;
    @BindView(R.id.readerSex)
    public Spinner rdSex;
    @BindView(R.id.readerAge)
    public EditText rdAge;
    @BindView(R.id.readerDepartment)
    public EditText rdDepartment;
    @BindView(R.id.readerClassroom)
    public EditText rdClassroom;
    @BindView(R.id.readerUsername)
    public EditText rdUsername;
    @BindView(R.id.readerPassword)
    public EditText rdPassword;
    @BindView(R.id.readerPhone)
    public EditText rdPhone;
    @BindView(R.id.readerEMail)
    public EditText rdEmail;
    @BindView(R.id.readerCredit)
    public EditText rdCredit;
    @BindView(R.id.readerAmount)
    public EditText rdAmount;
    @BindView(R.id.readerPermission)
    public EditText rdPermission;
    @BindView(R.id.readerTerm)
    public EditText rdTerm;
    @BindView(R.id.readerType)
    public Spinner rdType;
    //图片选择组件
    private PictureSelectViewGroup pictureSelectViewGroup;
    //该界面中选择图片最多数量
    private final int PICTURES_MAX = 3;

    PictureListAdapter pictureListAdapter;
    GridLayoutManager layoutManager=new GridLayoutManager(this,3);
    /**按钮*/
    @BindView(R.id.button_back)
    public ImageButton buttonBack;
    @BindView(R.id.button_delete_reader)
    public Button buttonDeleteReader;
    @BindView(R.id.button_edit_basic_information)
    public Button buttonEditBasicInformation;
    @BindView(R.id.button_apply_reader_permission)
    public Button buttonApplyReaderPermission;
    @BindView(R.id.button_quitAndClean)
    public Button buttonQuitAndClean;

    /**服务器返回的读者详情*/
    private JSONObject jsonObject = null;
    /**是否正在编辑基本信息*/
    private boolean writingBasic = false;
    /**是否正在编辑权限信息*/
    private boolean writingPermission = false;

    static class MyHandler extends Handler {
        private WeakReference<ReaderDetailActivity> readerDetailActivity;

        public MyHandler(WeakReference<ReaderDetailActivity> readerDetailActivity) {
            this.readerDetailActivity = readerDetailActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            ReaderDetailActivity myActivity = readerDetailActivity.get();
            if (what == UNKNOWN_REQUEST) {
                Toast.makeText(myActivity, "未知请求，无法处理！", Toast.LENGTH_SHORT).show();
            } else if (what == REQUEST_FAIL) {
                Toast.makeText(myActivity, "连接服务器异常！", Toast.LENGTH_SHORT).show();
            } else if (what == REQUEST_BUT_FAIL_READ_DATA) {
                Toast.makeText(myActivity, "子线程解析数据异常！", Toast.LENGTH_SHORT).show();
            } else if (what == GET_READER_DETAIL_SUCCEED){
                myActivity.fillData();
                Toast.makeText(myActivity, "查询成功！", Toast.LENGTH_SHORT).show();
            } else if(what == UPDATE_BASIC_INFORMATION_SUCCEED){
                Toast.makeText(myActivity,"更新成功！",Toast.LENGTH_LONG).show();
            } else if(what == ACCOUNT_OFFLINE_SUCCEED){
                UserInfo userInfo = UserManager.getInstance().getUserInfo(myActivity);
                //退出，且清除数据
                userInfo.setRole(null);
                userInfo.setPassword(null);
                userInfo.setUserName(null);
                userInfo.setToken(null);
                SharedPreferences sp = myActivity.getSharedPreferences("userInfo", Context.MODE_PRIVATE);//Context.MODE_PRIVATE表示SharedPreferences的数据只有自己应用程序能访问。
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("username", null);
                editor.putString("password", null);
                editor.putString("role",null);
                editor.putString("token",null);
                editor.commit();
                //销毁所有活动
                ActivityManager.getInstance().exit();
            }else if(what == ACCOUNT_OFFLINE_FAIL){
                //能退出，但不清除数据
                Toast.makeText(myActivity,"连接服务器异常！保存数据并退出！",Toast.LENGTH_LONG);
                //销毁所有活动
                ActivityManager.getInstance().exit();
            } else {
                Bundle data = msg.getData();
                if(what == GET_READER_DETAIL_FAIL){
                    DialogUtil.showDialog(myActivity,TAG,data,false);
                }else if(what == CLOSE_ACCOUNT){
                    //读者销户后，退出到登录界面
                    if("myself".equals(myActivity.id)){
                        DialogUtil.showDialog(myActivity,TAG,data,true);
                    }
                    else {
                        DialogUtil.showDialog(myActivity,TAG,data,false);
                    }
                }else if(what == UPDATE_BASIC_INFORMATION_FAIL){
                    DialogUtil.showDialog(myActivity,TAG,data,false);
                }
                else {
                    DialogUtil.showDialog(myActivity,TAG,data,true);
                }
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_reader_detall);
        ButterKnife.bind(this);
        pictureSelectViewGroup = findViewById(R.id.pictureSelectViewGroup);
        pictureSelectViewGroup.setMaxNumber(PICTURES_MAX);
        pictureSelectViewGroup.setTitle("证件照");
        //返回按钮绑定返回事件
        buttonBack.setOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onStart() {
        //屏幕亮起时开始初始化
        init();
        if(jsonObject == null || jsonObject.length()<0){
            //发送请求
            //获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            String token = userInfo.getToken();
            String url = HttpUtil.BASE_URL + "reader/selectAllById";
            id = this.getIntent().getStringExtra("id");
            //如果是读者查看自己的详情
            if("myself".equals(id)){
                rdTitle.setText("我的详情");
                HttpUtil.getRequest(token,url,this,GET_READER_DETAIL);
            }
            //如果是管理员查看读者详情
            else{
                rdTitle.setText("读者详情");
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", id);
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.getRequest(token,url,this,GET_READER_DETAIL);
            }
        }else {
            fillData();
        }
        super.onStart();
    }

    /**
     * @Author:Wallace
     * @Description:初始化。
     * 删除读者信息按钮、编辑基本信息按钮、申请借阅权限按钮和退出登录按钮设为GONE
     * 其余组件均不可获取焦点和不可编辑
     * @Date:Created in 16:22 2021/4/4
     * @Modified By:
     */
    void init(){
        buttonDeleteReader.setVisibility(View.GONE);
        buttonEditBasicInformation.setVisibility(View.GONE);
        buttonApplyReaderPermission.setVisibility(View.GONE);
        buttonQuitAndClean.setVisibility(View.GONE);

        writingBasic = false;
        rdName.setFocusable(writingBasic);
        rdName.setFocusableInTouchMode(writingBasic);
        rdName.setClickable(writingBasic);
        rdName.setEnabled(writingBasic);

        rdSex.setFocusable(writingBasic);
        rdSex.setClickable(writingBasic);
        rdSex.setEnabled(writingBasic);

        rdAge.setFocusable(writingBasic);
        rdAge.setFocusableInTouchMode(writingBasic);
        rdAge.setClickable(writingBasic);
        rdAge.setEnabled(writingBasic);

        rdDepartment.setFocusable(writingBasic);
        rdDepartment.setFocusableInTouchMode(writingBasic);
        rdDepartment.setClickable(writingBasic);
        rdDepartment.setEnabled(writingBasic);

        rdClassroom.setFocusable(writingBasic);
        rdClassroom.setFocusableInTouchMode(writingBasic);
        rdClassroom.setClickable(writingBasic);
        rdClassroom.setEnabled(writingBasic);

        rdUsername.setFocusable(writingBasic);
        rdUsername.setFocusableInTouchMode(writingBasic);
        rdUsername.setClickable(writingBasic);
        rdUsername.setEnabled(writingBasic);

        rdPassword.setFocusable(writingBasic);
        rdPassword.setFocusableInTouchMode(writingBasic);
        rdPassword.setClickable(writingBasic);
        rdPassword.setEnabled(writingBasic);

        rdPhone.setFocusable(writingBasic);
        rdPhone.setFocusableInTouchMode(writingBasic);
        rdPhone.setClickable(writingBasic);
        rdPhone.setEnabled(writingBasic);

        rdEmail.setFocusable(writingBasic);
        rdEmail.setFocusableInTouchMode(writingBasic);
        rdEmail.setClickable(writingBasic);
        rdEmail.setEnabled(writingBasic);

        rdCredit.setFocusable(writingBasic);
        rdCredit.setFocusableInTouchMode(writingBasic);
        rdCredit.setClickable(writingBasic);
        rdCredit.setEnabled(writingBasic);

        rdAmount.setFocusable(writingBasic);
        rdAmount.setFocusableInTouchMode(writingBasic);
        rdAmount.setClickable(writingBasic);
        rdAmount.setEnabled(writingBasic);

        rdPermission.setFocusable(writingBasic);
        rdPermission.setFocusableInTouchMode(writingBasic);
        rdPermission.setClickable(writingBasic);
        rdPermission.setEnabled(writingBasic);

        rdTerm.setFocusable(writingBasic);
        rdTerm.setFocusableInTouchMode(writingBasic);
        rdTerm.setClickable(writingBasic);
        rdTerm.setEnabled(writingBasic);

        rdType.setFocusable(writingBasic);
        rdType.setClickable(writingBasic);
        rdType.setEnabled(writingBasic);
    }

    /**
     * @Author:Wallace
     * @Description:填充读者详情的数据，根据数据的不同来动态改变组件的属性
     * @Date:Created in 17:54 2021/4/4
     * @Modified By:
     * @param
     * @return: void
     */
    private void fillData() {
        //解析数据
        try{
            rdId.setText(jsonObject.getString("id"));
            rdName.setText(jsonObject.getString("name"));
            String sex = "0".equals(jsonObject.getString("sex"))? "女":"男";
            //改变列表的值
            for(int i = 0;i < rdSex.getCount(); i++){
                String s = (String) rdSex.getItemAtPosition(i);
                if(sex.equals(s)){
                    rdSex.setSelection(i,true);
                    break;
                }
            }
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
            if("unchecked".equals(state)){

            }else {
                String credit = readerPermission.getString("credit");
                String amount = readerPermission.getString("amount");
                String permissionName = readerPermission.getString("permissionName");
                String t = readerPermission.getString("term");
                String typeName = readerPermission.getString("typeName");
                String imagePath = readerPermission.getString("image");
                JSONArray pictures = readerPermission.getJSONArray("pictures");
                rdCredit.setText(credit);
                rdAmount.setText(amount);
                rdPermission.setText(permissionName);
                Long l = Long.parseLong(t);
                Date date = new Date(l);
                SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
                String term = format.format(date);
                rdTerm.setText(term);
                //改变列表的值
                for(int i = 0;i < rdType.getCount(); i++){
                    String s = (String) rdType.getItemAtPosition(i);
                    if(typeName.equals(s)){
                        rdType.setSelection(i,true);
                        break;
                    }
                }
                ArrayList<String> data = new ArrayList<>();
                for(int i = 0; i < pictures.length(); i++){
                    data.add(pictures.getString(i));
                }
                UserManager userManager = UserManager.getInstance();
                UserInfo userInfo = userManager.getUserInfo(this);
                String token = userInfo.getToken();
                StringBuffer buffer = new StringBuffer(HttpUtil.BASE_URL);
                buffer.append("reader/reader_type_picture/").append(state).append("/").append(imagePath).append("/");
                String baseUrl = buffer.toString();
                if("checked".equals(state)){
                    pictureSelectViewGroup.setState("已审核通过");
                    pictureListAdapter = new PictureListAdapter(this, this, baseUrl, token, data, true, false);
                }else{
                    pictureSelectViewGroup.setState("正在审核");
                    pictureListAdapter = new PictureListAdapter(this, this, baseUrl, token, data, true,false);
                }
                pictureSelectViewGroup.setPictureListAdapter(layoutManager,pictureListAdapter);
            }
            secondInit(state);
        } catch (JSONException e) {
            Toast.makeText(this,"主程序解析数据时异常！",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @Author:Wallace
     * @Description:获取信息之后再次对控件属性进行调整
     * @Date:Created in 22:39 2021/4/4
     * @Modified By:
     * @param needApplyPermission 判断是否需要申请权限的按钮
     * @return: void
     */
    private void secondInit(String needApplyPermission) {
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        //是读者，有注销账户按钮,编辑基本信息的按钮，退出登录按钮
        if("myself".equals(this.id)){
            buttonEditBasicInformation.setVisibility(View.VISIBLE);
            buttonEditBasicInformation.setOnClickListener(view -> {
                //如果当前正在编辑
                if(writingBasic){
                    //发送请求
                    HashMap<String,String> map = new HashMap<>();
                    map.put("name",rdName.getText().toString().trim());
                    map.put("sex",rdSex.getSelectedItem().toString().trim());
                    map.put("age",rdAge.getText().toString().trim());
                    map.put("department",rdDepartment.getText().toString().trim());
                    map.put("classroom",rdClassroom.getText().toString().trim());
                    map.put("username",rdUsername.getText().toString().trim());
                    map.put("password",rdPassword.getText().toString().trim());
                    map.put("phone",rdPhone.getText().toString().trim());
                    map.put("email",rdEmail.getText().toString().trim());
                    String url = HttpUtil.BASE_URL + "reader/updateById";
                    HttpUtil.putRequest(token,url,map,this,UPDATE_BASIC_INFORMATION);
                    writingBasic = false;
                }else {
                    writingBasic = true;
                }
                isAllowEdit();
            });
            buttonQuitAndClean.setVisibility(View.VISIBLE);
            buttonQuitAndClean.setOnClickListener(view -> {
                //使用Map封装请求参数
                HashMap<String,String> map = new HashMap<>();
                map.put("skip","null");
                //定义发送的请求url
                String url = HttpUtil.BASE_URL + "quit";
                HttpUtil.postRequest(token, url, map,this,ACCOUNT_OFFLINE);
            });
            buttonDeleteReader.setText(R.string.closeAccount);
            buttonDeleteReader.setOnClickListener(view -> {
                String url = HttpUtil.BASE_URL + "reader/deleteById";
                HttpUtil.deleteRequest(token, url,this,CLOSE_ACCOUNT);
            });
            if("unchecked".equals(needApplyPermission)){
                String msg = "检测到您尚未申请借阅权限，选择证件类型，上传证件的正反面照片，经管理员审核通过后可开启借阅权限！";
                DialogUtil.showDialog(this,msg,false);
                buttonApplyReaderPermission.setVisibility(View.VISIBLE);
                buttonApplyReaderPermission.setOnClickListener(view -> {
                    //申请权限
                    if(writingPermission == true){
                        writingPermission  = false;
                        //发送请求
                        //使用Map封装请求参数
                        HashMap<String,String> map = new HashMap<>();
                        String url = HttpUtil.BASE_URL + "reader/apply";
//                        HttpUtil.postRequest(token, url, map,this,APPLY_PERMISSION);
                    }else {
                        writingPermission  = true;
                    }
                    rdType.setFocusable(writingPermission);
                    rdType.setClickable(writingPermission);
                    rdType.setEnabled(writingPermission);
                });
            }
        }
        //是管理员，有删除读者按钮
        else {
            buttonDeleteReader.setText(R.string.delete_reader);
            buttonDeleteReader.setOnClickListener(view -> {
                String url = HttpUtil.BASE_URL + "reader/deleteById";
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id",this.id);
                url = HttpUtil.newUrl(url,hashMap);
                HttpUtil.deleteRequest(token,url,this,CLOSE_ACCOUNT);
            });
        }
        buttonDeleteReader.setVisibility(View.VISIBLE);
    }

    private void isAllowEdit() {
        if(!writingBasic){
            buttonEditBasicInformation.setText(R.string.edit_basic_information);
        }else {
            buttonEditBasicInformation.setText(R.string.save);
        }
        //isEdit若为false，则所有EditText和Spinner不可获取焦点,不可点击，不可编辑
        rdName.setFocusable(writingBasic);
        rdName.setFocusableInTouchMode(writingBasic);
        rdName.setClickable(writingBasic);
        rdName.setEnabled(writingBasic);

        rdSex.setFocusable(writingBasic);
        rdSex.setClickable(writingBasic);
        rdSex.setEnabled(writingBasic);

        rdAge.setFocusable(writingBasic);
        rdAge.setFocusableInTouchMode(writingBasic);
        rdAge.setClickable(writingBasic);
        rdAge.setEnabled(writingBasic);

        rdDepartment.setFocusable(writingBasic);
        rdDepartment.setFocusableInTouchMode(writingBasic);
        rdDepartment.setClickable(writingBasic);
        rdDepartment.setEnabled(writingBasic);

        rdClassroom.setFocusable(writingBasic);
        rdClassroom.setFocusableInTouchMode(writingBasic);
        rdClassroom.setClickable(writingBasic);
        rdClassroom.setEnabled(writingBasic);

        rdUsername.setFocusable(writingBasic);
        rdUsername.setFocusableInTouchMode(writingBasic);
        rdUsername.setClickable(writingBasic);
        rdUsername.setEnabled(writingBasic);

        rdPhone.setFocusable(writingBasic);
        rdPhone.setFocusableInTouchMode(writingBasic);
        rdPhone.setClickable(writingBasic);
        rdPhone.setEnabled(writingBasic);

        rdEmail.setFocusable(writingBasic);
        rdEmail.setFocusableInTouchMode(writingBasic);
        rdEmail.setClickable(writingBasic);
        rdEmail.setEnabled(writingBasic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
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
                    e.printStackTrace();
                }
                break;
            case ACCOUNT_OFFLINE:
                myHandler.sendEmptyMessage(ACCOUNT_OFFLINE_SUCCEED);
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
            case UPDATE_BASIC_INFORMATION:
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("message");
                    String c = jsonObject.getString("code");
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("code",c);
                    data.putString("message",message);
                    if(("更新成功！".equals(message))&&("200".equals(c))){
                        msg.what = UPDATE_BASIC_INFORMATION_SUCCEED;
                    }else {
                        String tip = jsonObject.getString("tip");
                        data.putString("tip",tip);
                        msg.setData(data);
                        msg.what = UPDATE_BASIC_INFORMATION_FAIL;
                    }
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    myHandler.sendEmptyMessage(REQUEST_BUT_FAIL_READ_DATA);
                }
                break;
            default:
                myHandler.sendEmptyMessage(UNKNOWN_REQUEST);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        if(code == ACCOUNT_OFFLINE){
            myHandler.sendEmptyMessage(ACCOUNT_OFFLINE_FAIL);
        }else {
            myHandler.sendEmptyMessage(REQUEST_FAIL);
        }
        e.printStackTrace();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(ReaderDetailActivity.this, ShowPictureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemLongClick(PictureListAdapter pictureListAdapter, int position) {
        DialogUtil.showDialog(this,pictureListAdapter,position);
    }

//    @Override
//    public void showByBigPicture(PictureListAdapter pictureListAdapter) {
//
//    }
}
