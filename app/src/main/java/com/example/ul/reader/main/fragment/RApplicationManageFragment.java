package com.example.ul.reader.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ul.R;
import com.example.ul.adapter.ApplicationListAdapter;
import com.example.ul.callback.CallbackToRApplicationManageFragment;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.model.UserInfo;
import com.example.ul.util.DialogUtil;
import com.example.ul.util.HttpUtil;
import com.example.ul.util.UserManager;
import com.example.ul.view.MySearchView;

import org.json.JSONArray;

/**
 * @Author:Wallace
 * @Description:读者挂失和缴费处理的页面
 * @Date:2021/3/9 20:30
 * @Modified By:
 */
public class RApplicationManageFragment extends Fragment implements CallbackToRApplicationManageFragment {
        private static final String TAG = "ApplicationFragment";
        //服务器返回的信息
        private JSONArray jsonArray;
        //搜索框布局
        private MySearchView mySearchView;
        //搜索框
        private SearchView et_search;
        //“点击搜索”文本
        private TextView textViewSelect;
        //列表——申请记录
        private RecyclerView recyclerApplication;
        //适配器
        private ApplicationListAdapter adapter;
        //
        private CallbackToMainActivity listItemClickedCallbackActivity;

//        public void onAttach(Context context) {
//            /**
//             * @Author:Wallace
//             * @Description:当该Fragment被添加到Context时回调该方法,该方法只被调用一次
//             * @Date:Created in 20:15 2021/3/10
//             * @Modified By:
//             * @param context
//             * @return: void
//             */
//            super.onAttach(context);
//            Log.i(TAG, "缴费处理界面加载了！");
//            //如果Context没有实现ListClickedCallback接口，则抛出异常
//            if (!(context instanceof ListItemClickedCallbackMain)) {
//                throw new IllegalStateException("ApplicationFragment所在的Context必须实现ListClickedCallback接口");
//            }
//            //把该Context当初listClickedCallback对象
//            listItemClickedCallbackMain = (ListItemClickedCallbackMain) context;
//        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            /**
             * @Author:Wallace
             * @Description:创建fragment时被回调，该方法只会被调用一次
             * @Date:Created in 20:16 2021/3/10
             * @Modified By:
             * @param savedInstanceState
             * @return: void
             */
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            /**
             * @Author:Wallace
             * @Description:每次创建、绘制该fragment的View组件时调用的方法，Fragment将会显示该方法返回的View组件
             * @Date:Created in 20:17 2021/3/10
             * @Modified By:
             * @param inflater
             * @param container
             * @param savedInstanceState
             * @return: android.view.View
             */
            View rootView = inflater.inflate(R.layout.borrow_manage, container, false);
            mySearchView = rootView.findViewById(R.id.mySearchView);
            et_search = (SearchView) mySearchView.findViewById(R.id.et_search);
            textViewSelect = rootView.findViewById(R.id.textSelect);
            recyclerApplication = rootView.findViewById(R.id.recyclerApplicationList);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            /**
             * @Author:Wallace
             * @Description:当Fragment所在的Activity被启动完成后回调该方法
             * @Date:Created in 20:18 2021/3/10
             * @Modified By:
             * @param savedInstanceState
             * @return: void
             */
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onStart() {
            /**
             * @Author:Wallace
             * @Description:启动fragment时被回调
             * @Date:Created in 20:19 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
//            //执行查询
//            query();
//            //执行渲染
//            fill();
            super.onStart();
        }

        @Override
        public void onStop() {
            /**
             * @Author:Wallace
             * @Description:停止fragment时被回调
             * @Date:Created in 20:19 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
            super.onStop();
        }

        @Override
        public void onDestroyView() {
            /**
             * @Author:Wallace
             * @Description:销毁该fragment所包含的View组件时回调
             * @Date:Created in 20:19 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
            super.onDestroyView();
        }

        @Override
        public void onDestroy() {
            /**
             * @Author:Wallace
             * @Description:销毁fragment时被回调，该方法只会执行一次
             * @Date:Created in 20:20 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
            super.onDestroy();
        }


        public void onDetach() {
            /**
             * @Author:Wallace
             * @Description:当该Fragment从它所属的AContext中被删除、替换完成时回调该方法
             * @Date:Created in 20:20 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
            super.onDetach();
            //将接口赋值为null
            listItemClickedCallbackActivity = null;
        }


        private void query() {
            /**
             * @Author:Wallace
             * @Description:根据搜索框内容进行查询
             * @Date:Created in 20:43 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
            //搜索框的内容
            String queryString;
            if((et_search.getQuery().toString()==null)||(et_search.getQuery().toString().equals(""))){
                queryString = "null";
            }else {
                queryString = et_search.getQuery().toString();
            }
            //获取token
            UserManager userManager = UserManager.getInstance();
            UserInfo userInfo = userManager.getUserInfo(getActivity());
            String token = userInfo.getToken();
            //根据条件构造发送请求的URL
            String url = HttpUtil.BASE_URL + "application/QueryById";
//            try{
//                jsonArray = new JSONArray(HttpUtil.getRequest(token,url));
//            } catch (Exception e) {
//                DialogUtil.showDialog(getActivity(),"服务器响应异常，请稍后再试！",false);
//                e.printStackTrace();
//            }
        }

        private void fill() {
            /**
             * @Author:Wallace
             * @Description:将查询结果jsonArray渲染到界面
             * @Date:Created in 20:43 2021/3/10
             * @Modified By:
             * @param
             * @return: void
             */
            if((jsonArray==null)||!(jsonArray.length()>0)){
                DialogUtil.showDialog(getActivity(),"为获取到数据！",false);
            }else{
                    recyclerApplication.setHasFixedSize(true);
                    //为RecyclerView设置布局管理器
                recyclerApplication.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                    //将服务器响应包装成Adapter
                    adapter = new ApplicationListAdapter(getActivity(),jsonArray,"id","name","readerId","readerName",
                            "applicationDescription","applicationMoney",this);
                recyclerApplication.setAdapter(adapter);
            }
        }

        @Override
        public void applicationListToPay(int i) {

        }
        @Override
        public void applicationToWantMore(int i) {

        }
    }

