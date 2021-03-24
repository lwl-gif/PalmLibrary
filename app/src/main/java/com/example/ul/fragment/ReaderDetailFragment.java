package com.example.ul.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ul.R;
import com.example.ul.model.UserInfo;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

/**
 * @Author:Wallace
 * @Description:显示某个读者详情的碎片（读者和管理员共用一个碎片）。
 * @Date:2021/3/7 10:14
 * @Modified By:
 */
public class ReaderDetailFragment extends Fragment implements HttpUtil.MyCallback{

    //自定义消息代码
    private final int GET_CODE_CODE = 0401; //查询读者详情


    private static final String TAG = "ReaderDetailFragment";
    //读者的id
    private String id;
    //视图+控件
    private View rootView;
    TextView rdId;
    TextView rdName;
    TextView rdSex;
    TextView rdAge;
    TextView rdDepartment;
    TextView rdClassroom;
    TextView rdUsername;
    TextView rdPassword;
    TextView rdPhone;
    TextView rdEmail;
    TextView rdCredit;
    TextView rdAmount;
    TextView rdPermission;
    TextView rdType;
    TextView rdTerm;
    //服务器返回的数据
    private JSONObject jsonObject;

    public ReaderDetailFragment(String id){
        this.id = id;
        Log.i(TAG,"id="+id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle bundle) {
        //获取当前界面视图
        rootView = inflater.inflate(R.layout.reader_detall,container,false);
        //获取界面中的相关组件
        rdId = rootView.findViewById(R.id.rdId);
        rdName = rootView.findViewById(R.id.rdName);
        rdSex = rootView.findViewById(R.id.rdSex);
        rdAge = rootView.findViewById(R.id.rdAge);
        rdDepartment = rootView.findViewById(R.id.rdDepartment);
        rdClassroom = rootView.findViewById(R.id.rdClassroom);
        rdUsername = rootView.findViewById(R.id.rdUsername);
        rdPassword = rootView.findViewById(R.id.rdPassword);
        rdPhone = rootView.findViewById(R.id.rdPhone);
        rdEmail = rootView.findViewById(R.id.rdEmail);
        rdCredit = rootView.findViewById(R.id.rdCredit);
        rdAmount = rootView.findViewById(R.id.rdAmount);
        rdPermission = rootView.findViewById(R.id.rdPermission);
        rdType = rootView.findViewById(R.id.rdType);
        rdTerm = rootView.findViewById(R.id.rdTerm);
//        ImageButton imageFront = rootView.findViewById(R.id.imageFront);
//        ImageButton imageBack = rootView.findViewById(R.id.imageBack);
        return rootView;
    }

    //启动碎片时，访问服务器查询数据
    @Override
    public void onStart() {
        super.onStart();
        //获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        String token = userInfo.getToken();
        //定义发送请求的URL
        String url = HttpUtil.BASE_URL + "reader/selectAllById/" + id;
        String result = null;
//        try{
//            //发送请求
//            result = HttpUtil.getRequest(token,url);
//            jsonObject = new JSONObject(result);
//        }catch (JSONException e){
//            e.printStackTrace();
//        }catch (Exception e){
//            DialogUtil.showDialog(getActivity(),"服务器异常！",false);
//            e.printStackTrace();
//        }
        //填充数据
        try {
            rdId.setText(jsonObject.getString("id"));
            rdName.setText(jsonObject.getString("name"));
            //获取性别代号
            String sex = jsonObject.getString("sex");
            if(sex.equals("1")){
                rdSex.setText("男");
            }else {
                rdSex.setText("女");
            }
            rdAge.setText(jsonObject.getString("age"));
            rdDepartment.setText(jsonObject.getString("department"));
            rdClassroom.setText(jsonObject.getString("classroom"));
            rdUsername.setText(jsonObject.getString("username"));
            rdPassword.setText(jsonObject.getString("password"));
            rdPhone.setText(jsonObject.getString("phone"));
            rdEmail.setText(jsonObject.getString("email"));
            JSONObject jsonObject0 = jsonObject.getJSONObject("readerPermission");
            rdCredit.setText(jsonObject0.getString("credit"));
            rdAmount.setText(jsonObject0.getString("amount"));
            rdPermission.setText(jsonObject0.getString("permission"));
            rdType.setText(jsonObject0.getString("type"));
            rdTerm.setText(jsonObject0.getString("term"));
//            imageFront.setText(jsonObject.getString("term"));
//            imageBack.setText(jsonObject.getString("term"));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void success(Response response, int code) throws IOException {

    }

    @Override
    public void failed(IOException e, int code) {

    }
}
