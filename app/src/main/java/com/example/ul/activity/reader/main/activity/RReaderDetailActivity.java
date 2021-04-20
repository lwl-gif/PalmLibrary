package com.example.ul.activity.reader.main.activity;


import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.activity.ShowPictureActivity;
import com.example.ul.adapter.ImagesAdapter;
import com.example.ul.callback.ImageAdapterItemListener;
import com.example.ul.model.UserInfo;
import com.example.ul.util.ActivityManager;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 读者查看自己详情的活动
 * @Date: Created 10:08 2021/3/7
 * @Modified: by who yyyy-MM-dd
 */
public class RReaderDetailActivity extends AppCompatActivity implements HttpUtil.MyCallback, ImageAdapterItemListener {

    private static final String TAG = "RReaderDetailActivity";
    //自定义消息代码
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 900;
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
    /**读者自行销户*/
    private static final int CLOSE_ACCOUNT = 903;
    /**读者更新自己的基本信息*/
    private static final int UPDATE_BASIC_INFORMATION = 904;
    /**读者更新自己的基本信息成功*/
    private static final int UPDATE_BASIC_INFORMATION_SUCCEED = 9041;
    /**读者更新自己的基本信息失败*/
    private static final int UPDATE_BASIC_INFORMATION_FAIL = 9040;
    /**读者申请借阅权限*/
    private static final int APPLY_PERMISSION = 905;
    /**服务器返回的读者详情*/
    private JSONObject jsonObject = null;
    /**是否正在编辑基本信息*/
    private boolean writingBasic = false;
    /**是否正在编辑权限信息*/
    private boolean writingPermission = false;
    /**文本*/
    @BindView(R.id.readerDetail_title)
    public TextView rdTitle;
    @BindView(R.id.readerId)
    public TextView rdId;
    @BindView(R.id.title_picture)
    public TextView rdPicture;
    /**可编辑文本*/
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
    /**按钮*/
    @BindView(R.id.button_back)
    public ImageButton buttonBack;
    @BindView(R.id.button_edit_basic_information)
    public Button buttonEditBasicInformation;
    @BindView(R.id.button_apply_reader_permission)
    public Button buttonApplyReaderPermission;
    @BindView(R.id.button_quitAndClean)
    public Button buttonQuitAndClean;
    @BindView(R.id.button_delete)
    public Button buttonDelete;
    /**展示图片的列表*/
    private RecyclerView recyclerView;
    /**展示图片的适配器*/
    private ImagesAdapter imagesAdapter;

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    static class MyHandler extends Handler {
        private WeakReference<RReaderDetailActivity> readerDetailActivity;

        public MyHandler(WeakReference<RReaderDetailActivity> readerDetailActivity) {
            this.readerDetailActivity = readerDetailActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            RReaderDetailActivity myActivity = readerDetailActivity.get();
            if (what == UNKNOWN_REQUEST_ERROR || what == REQUEST_FAIL) {
                Bundle bundle = msg.getData();
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }else if (what == REQUEST_BUT_FAIL_READ_DATA) {
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
                boolean commit = editor.commit();
                if(commit){
                    //销毁所有活动
                    ActivityManager.getInstance().exit();
                }else {
                    //能退出，但不清除数据
                    Toast.makeText(myActivity,"程序退出时数据清理异常！",Toast.LENGTH_LONG).show();
                    //销毁所有活动
                    ActivityManager.getInstance().exit();
                }
            }else if(what == ACCOUNT_OFFLINE_FAIL){
                //能退出，但不清除数据
                Toast.makeText(myActivity,"连接服务器异常！保存数据并退出！",Toast.LENGTH_LONG).show();
                //销毁所有活动
                ActivityManager.getInstance().exit();
            } else {
                Bundle data = msg.getData();
                if(what == GET_READER_DETAIL_FAIL){
                    DialogUtil.showDialog(myActivity,TAG,data,false);
                }else if(what == CLOSE_ACCOUNT){
                    //读者销户后，退出到登录界面
                    DialogUtil.showDialog(myActivity,TAG,data,true);
                }else if(what == UPDATE_BASIC_INFORMATION_FAIL){
                    DialogUtil.showDialog(myActivity,TAG,data,false);
                }else {
                    DialogUtil.showDialog(myActivity,TAG,data,true);
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_reader_detall);
        ButterKnife.bind(this);
        rdTitle.setText("我的详情");
        //获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        imagesAdapter = new ImagesAdapter(this,token,this);
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setAdapter(imagesAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        //返回按钮绑定返回事件
        buttonBack.setOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //屏幕亮起时开始初始化
        init();
        if(jsonObject == null || jsonObject.length()<0) {
            //发送请求
            //获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(this);
            String token = userInfo.getToken();
            String url = HttpUtil.BASE_URL + "reader/selectAllById";
            HttpUtil.getRequest(token, url, this, GET_READER_DETAIL);
        }else {
            fillData();
        }
    }

    /**
     * @Author: Wallace
     * @Description: 初始化。
     *删除读者信息按钮、编辑基本信息按钮、申请借阅权限按钮和退出登录按钮设为GONE
     *其余组件均不可获取焦点和不可编辑
     * @Date: Created 16:22 2021/4/4
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    void init(){
        buttonEditBasicInformation.setVisibility(View.GONE);
        buttonApplyReaderPermission.setVisibility(View.GONE);
        buttonQuitAndClean.setVisibility(View.GONE);
        buttonDelete.setVisibility(View.GONE);

        writingBasic = false;
        rdName.setFocusable(false);
        rdName.setFocusableInTouchMode(false);
        rdName.setClickable(false);
        rdName.setEnabled(false);

        rdSex.setFocusable(false);
        rdSex.setClickable(false);
        rdSex.setEnabled(false);

        rdAge.setFocusable(false);
        rdAge.setFocusableInTouchMode(false);
        rdAge.setClickable(false);
        rdAge.setEnabled(false);

        rdDepartment.setFocusable(false);
        rdDepartment.setFocusableInTouchMode(false);
        rdDepartment.setClickable(false);
        rdDepartment.setEnabled(false);

        rdClassroom.setFocusable(false);
        rdClassroom.setFocusableInTouchMode(false);
        rdClassroom.setClickable(false);
        rdClassroom.setEnabled(false);

        rdUsername.setFocusable(false);
        rdUsername.setFocusableInTouchMode(false);
        rdUsername.setClickable(false);
        rdUsername.setEnabled(false);

        rdPassword.setFocusable(false);
        rdPassword.setFocusableInTouchMode(false);
        rdPassword.setClickable(false);
        rdPassword.setEnabled(false);

        rdPhone.setFocusable(false);
        rdPhone.setFocusableInTouchMode(false);
        rdPhone.setClickable(false);
        rdPhone.setEnabled(false);

        rdEmail.setFocusable(false);
        rdEmail.setFocusableInTouchMode(false);
        rdEmail.setClickable(false);
        rdEmail.setEnabled(false);

        rdCredit.setFocusable(false);
        rdCredit.setFocusableInTouchMode(false);
        rdCredit.setClickable(false);
        rdCredit.setEnabled(false);

        rdAmount.setFocusable(false);
        rdAmount.setFocusableInTouchMode(false);
        rdAmount.setClickable(false);
        rdAmount.setEnabled(false);

        rdPermission.setFocusable(false);
        rdPermission.setFocusableInTouchMode(false);
        rdPermission.setClickable(false);
        rdPermission.setEnabled(false);

        rdTerm.setFocusable(false);
        rdTerm.setFocusableInTouchMode(false);
        rdTerm.setClickable(false);
        rdTerm.setEnabled(false);

        rdType.setFocusable(false);
        rdType.setClickable(false);
        rdType.setEnabled(false);
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
            rdId.setText(jsonObject.getString("id"));
            rdName.setText(jsonObject.getString("name"));
            String sex = "0".equals(jsonObject.getString("sex")) ? "女" : "男";
            //改变列表的值
            for (int i = 0; i < rdSex.getCount(); i++) {
                String s = (String) rdSex.getItemAtPosition(i);
                if (sex.equals(s)) {
                    rdSex.setSelection(i, true);
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
                long l = Long.parseLong(t);
                Date date = new Date(l);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String term = format.format(date);
                rdTerm.setText(term);
                //改变列表的值
                for (int i = 0; i < rdType.getCount(); i++) {
                    String s = (String) rdType.getItemAtPosition(i);
                    if (typeName.equals(s)) {
                        rdType.setSelection(i, true);
                        break;
                    }
                }
                // 隐藏图片信息
                rdPicture.setVisibility(View.GONE);
                // 隐藏图片列表
                recyclerView.setVisibility(View.GONE);
            }
            // 读者身份正在审核中
            else if (checkingState.equals(state)) {
                // 获取证件类型信息
                String typeName = readerPermission.getString("typeName");
                for (int i = 0; i < rdType.getCount(); i++) {
                    String s = (String) rdType.getItemAtPosition(i);
                    if (typeName.equals(s)) {
                        rdType.setSelection(i, true);
                        break;
                    }
                }
                // 把已提交的证件照展示出来
                String imagePath = readerPermission.getString("image");
                JSONArray pictureNames = jsonObject.getJSONArray("pictures");
                String httpBaseUrl = HttpUtil.BASE_URL + "reader/reader_type_picture/checking/" + imagePath + "/" ;
                ArrayList<String> imageNameUrlList = new ArrayList<>();
                for(int i = 0; i < pictureNames.length(); i++){
                    imageNameUrlList.add(httpBaseUrl+pictureNames.get(i));
                }
                this.imagesAdapter.setImageNameUrlList(imageNameUrlList);
                // 可选择证件类型
                rdType.setFocusable(true);
                rdType.setClickable(true);
                rdType.setEnabled(true);
            }
            // 读者未进行身份审核
            else{
                // 可选择证件类型
                rdType.setFocusable(true);
                rdType.setClickable(true);
                rdType.setEnabled(true);
            }
            secondInit(state);
        } catch (JSONException e) {
            Toast.makeText(this,"主程序解析数据时异常！",Toast.LENGTH_LONG).show();
        }
    }
    /**
     * @Author: Wallace
     * @Description: 获取信息之后再次对按钮的属性进行调整
     * @Date: Created 22:39 2021/4/4
     * @Modified: by who yyyy-MM-dd
     * @param needApplyPermission 判断是否需要申请权限的按钮
     * @return: void
     */
    private void secondInit(String needApplyPermission) {
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(this);
        String token = userInfo.getToken();
        buttonDelete.setVisibility(View.VISIBLE);
        buttonDelete.setOnClickListener(view -> {
            String url = HttpUtil.BASE_URL + "reader/deleteById";
            HttpUtil.deleteRequest(token,url,this,CLOSE_ACCOUNT);
        });
        buttonEditBasicInformation.setVisibility(View.VISIBLE);
        buttonEditBasicInformation.setOnClickListener(view -> {
            //如果当前正在编辑
            if (writingBasic) {
                //发送请求
                HashMap<String, String> map = new HashMap<>();
                map.put("name", rdName.getText().toString().trim());
                map.put("sex", rdSex.getSelectedItem().toString().trim());
                map.put("age", rdAge.getText().toString().trim());
                map.put("department", rdDepartment.getText().toString().trim());
                map.put("classroom", rdClassroom.getText().toString().trim());
                map.put("username", rdUsername.getText().toString().trim());
                map.put("password", rdPassword.getText().toString().trim());
                map.put("phone", rdPhone.getText().toString().trim());
                map.put("email", rdEmail.getText().toString().trim());
                String url = HttpUtil.BASE_URL + "reader/updateById";
                HttpUtil.putRequest(token, url, map, this, UPDATE_BASIC_INFORMATION);
                writingBasic = false;
            } else {
                writingBasic = true;
            }
            isAllowEdit();
        });
        buttonQuitAndClean.setVisibility(View.VISIBLE);
        buttonQuitAndClean.setOnClickListener(view -> {
            //使用Map封装请求参数
            HashMap<String, String> map = new HashMap<>();
            map.put("skip", "null");
            //定义发送的请求url
            String url = HttpUtil.BASE_URL + "quit";
            HttpUtil.postRequest(token, url, map, this, ACCOUNT_OFFLINE);
        });
        // 定义读者身份验证的几种状态
        String checkedState = "checked";
        String uncheckState = "unchecked";
        // 已审核
        if(needApplyPermission.equals(checkedState)){
            buttonApplyReaderPermission.setVisibility(View.GONE);
        }else {
            buttonApplyReaderPermission.setVisibility(View.VISIBLE);
            buttonApplyReaderPermission.setOnClickListener(view -> {
                // 申请权限
                if (writingPermission) {
                    writingPermission = false;
                    // 发送请求
                    // 使用Map封装请求参数
                    HashMap<String, String> hashMap = new HashMap<>();
                    // 获取证件类型名称
                    String rdTypeName = (String) this.rdType.getSelectedItem();
                    hashMap.put("type",rdTypeName);
                    // 获取要提交的图片的全路径
                    List<String> list = this.imagesAdapter.getImagesPath();
                    List<String> tempList = list.subList(0,list.size()-1);
                    String url = HttpUtil.BASE_URL + "reader/apply";
                    HttpUtil.postRequest(token,url,hashMap,tempList,this,APPLY_PERMISSION);
                } else {
                    writingPermission = true;
                }
                rdType.setFocusable(writingPermission);
                rdType.setClickable(writingPermission);
                rdType.setEnabled(writingPermission);
            });
            // 未审核
            if (needApplyPermission.equals(uncheckState)) {
                String msg = "检测到您尚未申请借阅权限，选择证件类型，上传证件的正反面照片，经管理员审核通过后可开启借阅权限！";
                DialogUtil.showDialog(this, msg, false);
            }
        }
    }
    /**
     * @Author: Wallace
     * @Description: 根据当前是否正在编辑基本信息，改变控件对应的属性
     * @Date: Created 19:00 2021/4/17
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
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
    public void onClickToShow(int position) {
        /**
         * @Author:Wallace
         * @Description: 先判断是不是最后一个item
         * 1.不是最后一个item,则开启一个Activity，用大图来展示当前的图片
         * 2.是最后一个item，则选择图片添加
         * @Date:Created in 21:41 2021/4/11
         * @Modified By:
         * @param position item的位置
         * @return: void
         */
        // 当前不处于删除状态
        if(!imagesAdapter.getDeleting()){
            if(position == imagesAdapter.getItemCount()-1) {
                //进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(this)
                        //全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .openGallery(PictureMimeType.ofImage())
                        //每行显示个数 int
                        .imageSpanCount(3)
                        .maxSelectNum(30)
                        //多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .selectionMode(PictureConfig.MULTIPLE)
                        //是否可预览图片
                        .previewImage(true)
                        //是否显示拍照按钮 true or false
                        .isCamera(false)
                        //拍照保存图片格式后缀,默认jpeg
                        .imageFormat(PictureMimeType.JPEG)
                        //图片列表点击 缩放效果 默认true
                        .isZoomAnim(true)
                        //int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .withAspectRatio(1, 1)
                        //是否显示uCrop工具栏，默认不显示 true or false
                        .hideBottomControls(false)
                        //裁剪框是否可拖拽 true or false
                        .freeStyleCropEnabled(false)
                        //是否圆形裁剪 true or false
                        .circleDimmedLayer(false)
                        //是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                        .showCropFrame(false)
                        //是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                        .showCropGrid(false)
                        //是否开启点击声音 true or false
                        .openClickSound(true)
                        //同步true或异步false 压缩 默认同步
                        .synOrAsy(true)
                        //裁剪是否可旋转图片 true or false
                        .rotateEnabled(false)
                        //裁剪是否可放大缩小图片 true or false
                        .scaleEnabled(true)
                        //是否可拖动裁剪框(固定)
                        .isDragFrame(false)
                        //结果回调onActivityResult requestCode
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
            else {
                Intent intent = new Intent(RReaderDetailActivity.this, ShowPictureActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("TAG",TAG);
                bundle.putParcelable("Adapter", imagesAdapter);
                bundle.putInt("position",position);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 结果回调
                    imagesAdapter.setSelectList((ArrayList<LocalMedia>) PictureSelector.obtainMultipleResult(data));
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void onClickToDelete(int position) {
        // 当前处于删除状态
        if(imagesAdapter.getDeleting()){
            // 如果当前是第一次删除图片，弹出提示框
            if(imagesAdapter.isFirstDelete()){
                DialogUtil.showDialog(this,this.imagesAdapter,position);
            }else {
                imagesAdapter.removeItem(position);
            }
        }
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
            case APPLY_PERMISSION:

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
        if(code == ACCOUNT_OFFLINE){
            myHandler.sendEmptyMessage(ACCOUNT_OFFLINE_FAIL);
        }else {
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
            bundle.putString("reason",reason);
            message.setData(bundle);
            myHandler.sendMessage(message);
        }
    }
}
