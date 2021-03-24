package com.example.ul.activity.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import com.example.ul.R;
import com.example.ul.activity.ui.lmain.LMainFragmentPagerAdapter;
import com.example.ul.callback.Callback;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import java.util.Iterator;
import java.util.Map;

public class LMainFragment extends Fragment implements Callback {

    private HomeViewModel homeViewModel;
    private static final String LAG = "LMainFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View rootView = inflater.inflate(R.layout.fragment_l_main, container, false);
        FragmentManager manager = getChildFragmentManager();
        LMainFragmentPagerAdapter lMainFragmentPagerAdapter = new LMainFragmentPagerAdapter(this, manager);
        ViewPager viewPager = rootView.findViewById(R.id.l_main_fragment_view_pager);
        viewPager.setAdapter(lMainFragmentPagerAdapter);
        TabLayout tabs = rootView.findViewById(R.id.tabs);
        Drawable d = null;
        tabs.setupWithViewPager(viewPager);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.getTabAt(i).setCustomView(lMainFragmentPagerAdapter.getTabView(i));
        }
        return rootView;
    }
    @Override
    public JSONArray readerManageQuery(String selectBy, String sortBy, String readerType) {
//        //带上自己搜索框的内容
//        String queryString;
//        if((et_search.getQuery().toString()==null)||(et_search.getQuery().toString().equals(""))){
//            queryString = "null";
//        }else {
//            queryString = et_search.getQuery().toString();
//        }
//        //检测参数情况，不允许selectBy!="null"但queryString=='null'的情况出现
//        if(!selectBy.equals("null")&&queryString.equals("null")){
//            DialogUtil.showDialog(getActivity(),"当查询方式不为空时，查询框内容不能为空！",false);
//            return null;
//        }
//        //获取token
//        UserManager userManager = UserManager.getInstance();
//        UserInfo userInfo = userManager.getUserInfo(getActivity());
//        String token = userInfo.getToken();
//        //定义发送请求的URL
//        String url = HttpUtil.BASE_URL + "reader/librarian/integratedQuery";
//        //使用Map包装成一个请求
//        Map<String,String> map = new HashMap<>();
//        map.put("token",token);
//        map.put("queryString",queryString);
//        map.put("selectBy",selectBy);
//        map.put("orderBy",sortBy);
//        map.put("readerType",readerType);
//        map.put("url",url);
//        //发送请求
//        return sendHttpGetRequest(map);
        return null;
    }

    //解析出请求url与token，发送请求
    private JSONArray sendHttpGetRequest(Map map){
        String token = (String) map.get("token");
        map.remove("token");
        String url = (String) map.get("url");
        map.remove("url");
        //根据综合条件构造url
        String params = "";
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = (String) map.get(key);
            params = params + key + "=" + value + "&";
        }
        if(!params.equals("")){ //不为空，有参数
            //截去最后一个字符"&"，在开头增加一个字符"?"
            params = params.substring(0,params.length()-1);
            params = "?" + params;
        }
        url = url + params;
        JSONArray jsonArray;
//        try{
//            jsonArray = new JSONArray(HttpUtil.getRequest(token,url));
//            return jsonArray;
//        } catch (Exception e) {
//            DialogUtil.showDialog(getActivity(),"服务器响应异常，请稍后再试！",false);
//            e.printStackTrace();
//        }
        return null;
    }
}