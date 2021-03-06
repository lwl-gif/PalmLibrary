package com.example.ul.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ul.R;
import com.example.ul.adapter.BookListAdapter;
import com.example.ul.adapter.MySpinnerAdapter;
import com.example.ul.adapter.MySpinnerBelongAdapter;
import com.example.ul.callback.CallbackToBookFragment;
import com.example.ul.callback.CallbackToMainActivity;
import com.example.ul.callback.SearchCallback;
import com.example.ul.model.Book;
import com.example.ul.model.UserInfo;
import com.example.ul.reader.main.RMainActivity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;

/**
 * @Author: Wallace
 * @Description: 查阅图书的页面，管理员和读者可共享同一个页面（共享页面不加“L”和“R”的前缀）。
 * @Date: 2021/3/9 20:29
 * @Modified By:
 */
public class BookFragment extends Fragment implements CallbackToBookFragment, HttpUtil.MyCallback, SearchCallback {
    private static final String TAG = "BookFragment";
    /**未知请求*/
    private static final int UNKNOWN_REQUEST_ERROR = 500;
    /**网络异常，请求失败*/
    private static final int REQUEST_FAIL = 5000;
    /**请求被服务器拦截，请求失败*/
    private static final int REQUEST_INTERCEPTED = 5001;
    /**获取各下拉列表中的内容*/
    private static final int GET_TYPE = 501;
    /**获取书籍信息*/
    private static final int GET_BOOK_LIST = 502;
    /**适配器*/
    private BookListAdapter adapter;
    /**布局中的查询方式下拉列表/排序方式/指定图书馆/指定图书状态/图书类别*/
    private Spinner spinnerSelectBy, spinnerOrderBy, spinnerLibrary, spinnerState, spinnerBelong1, spinnerBelong2;
    /**要填充到各个下拉列表中的内容*/
    private JSONArray jsonArraySelectBy,jsonArrayOrderBy,jsonArrayLibrary,jsonArrayState;
    private List<String> belongs1 = new ArrayList<>();
    private List<List<String>> belongs2 = new ArrayList<>();
    /**当前查询方式/当前排序方式/当前图书馆/图书状态/图书类别*/
    private String selectBy = "null", orderBy = "null", library = "null", state = "null", belong1 = "null", belong2 = "null";
    private String queryString = "null";
    /**回调接口*/
    private CallbackToMainActivity callbackToMainActivity;
    /**token*/
    private String token;

    static class MyHandler extends Handler {
        private final WeakReference<BookFragment> bookFragment;

        public MyHandler(WeakReference<BookFragment> bookFragment){
            this.bookFragment = bookFragment;
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            BookFragment myFragment = bookFragment.get();
            if (what == GET_TYPE) {
                myFragment.fillSpinnerData();
            } else if (what == GET_BOOK_LIST) {
                Bundle data = msg.getData();
                ArrayList<Book> books = data.getParcelableArrayList("bookArrayList");
                myFragment.fillBookData(books);
            } else if (what == REQUEST_INTERCEPTED) {
                Bundle data = msg.getData();
                DialogUtil.showDialog(myFragment.getActivity(), TAG, data, true);
            } else {
                Bundle data = msg.getData();
                Toast.makeText(myFragment.getActivity(),data.getString("reason"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    MyHandler myHandler = new MyHandler(new WeakReference(this));

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (!(context instanceof CallbackToMainActivity)) {
            throw new IllegalStateException(TAG+"所在的Context必须实现CallbackTOMainActivity接口");
        }
        callbackToMainActivity = (CallbackToMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取token
        UserManager userManager = UserManager.getInstance();
        UserInfo userInfo = userManager.getUserInfo(getActivity());
        token = userInfo.getToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        // 获取当前界面视图
        View rootView = inflater.inflate(R.layout.book_manege, container, false);
        MySearchView mySearchView = rootView.findViewById(R.id.mySearchView);
        mySearchView.setSearchCallback(this);
        TextView textView = rootView.findViewById(R.id.textSelect);
        textView.setOnClickListener(view -> {
            query();
        });
        spinnerSelectBy = rootView.findViewById(R.id.selectBy);
        spinnerOrderBy = rootView.findViewById(R.id.orderBy);
        spinnerLibrary = rootView.findViewById(R.id.library);
        spinnerState = rootView.findViewById(R.id.state);
        spinnerBelong1 = rootView.findViewById(R.id.classification1);
        spinnerBelong2 = rootView.findViewById(R.id.classification2);
        // 获取视图中的图书列表
        RecyclerView recyclerViewBookList = rootView.findViewById(R.id.recyclerBookList);
        // 为RecyclerView设置布局管理器
        recyclerViewBookList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        // 获取图片的基本url
        String baseUrl = HttpUtil.BASE_URL + "book/getBookImage/";
        adapter = new BookListAdapter(getActivity(),baseUrl,token,new ArrayList<>(),this);
        recyclerViewBookList.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 定义发送的URL
        String url = HttpUtil.BASE_URL + "book/getType";
        HttpUtil.getRequest(token, url, this, GET_TYPE);
    }
    /**
     * @Author: Wallace
     * @Description: 综合所有条件查询所有书本的部分信息
     * @Date: Created in 13:22 2021/3/8
     * @Modified By:
     * @return: void
     */
    private void query() {
        if("".equals(queryString)){
            queryString = "null";
        }
        if(!"null".equals(selectBy)&& "null".equals(queryString)){
            DialogUtil.showDialog(getActivity(),"当指定了检索方式时，检索内容不能为空。",false);
        }else {
            // 定义发送的URL
            String url = HttpUtil.BASE_URL + "book/selectSome";
            // 使用Map封装请求参数
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("queryString", queryString);
            hashMap.put("selectBy", selectBy);
            hashMap.put("orderBy", orderBy);
            hashMap.put("library", library);
            hashMap.put("state", state);
            hashMap.put("first", belong1);
            hashMap.put("third", belong2);
            url = HttpUtil.newUrl(url,hashMap);
            HttpUtil.getRequest(token, url, this, GET_BOOK_LIST);
        }
    }

    @Override
    public void searchAction(String s) {
        queryString = s;
    }
    /**
     * @Author: Wallace
     * @Description: 为各个Spinner填充信息及绑定选中事件
     * @Date: Created in 8:32 2021/3/31
     * @Modified By:
     * @return: void
     */
    private void fillSpinnerData(){
        MySpinnerAdapter adapterSelectBy = new MySpinnerAdapter(getActivity(),jsonArraySelectBy);
        MySpinnerAdapter adapterOrderBy = new MySpinnerAdapter(getActivity(),jsonArrayOrderBy);
        MySpinnerAdapter adapterLibrary = new MySpinnerAdapter(getActivity(),jsonArrayLibrary);
        MySpinnerAdapter adapterState = new MySpinnerAdapter(getActivity(),jsonArrayState);
        spinnerSelectBy.setAdapter(adapterSelectBy);
        spinnerOrderBy.setAdapter(adapterOrderBy);
        spinnerLibrary.setAdapter(adapterLibrary);
        spinnerState.setAdapter(adapterState);
        MySpinnerBelongAdapter mySpinnerBelongAdapter = new MySpinnerBelongAdapter(getActivity(),belongs1);
        spinnerBelong1.setAdapter(mySpinnerBelongAdapter);
        // 绑定事件
        spinnerSelectBy.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectBy = (String) spinnerSelectBy.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectBy = "null";
            }
        });
        spinnerOrderBy.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                orderBy = (String) spinnerOrderBy.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                orderBy = "null";
            }
        });
        spinnerLibrary.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                library = (String) spinnerLibrary.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                library = "null";
            }
        });
        spinnerState.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                state = (String) spinnerState.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                state = "null";
            }
        });
        // 根据spinnerBelong1选的不同来动态渲染spinnerBelong2
        spinnerBelong1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                belong1 = (String) spinnerBelong1.getItemAtPosition(i);
                // 给spinnerBelong2赋值
                spinnerBelong2.setAdapter(new MySpinnerBelongAdapter(getActivity(),belongs2.get(i)));
                spinnerBelong2.setSelection(spinnerBelong2.getCount()-1,true);
                belong2 = (String)spinnerBelong2.getSelectedItem();
                spinnerBelong2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            belong2 = (String) spinnerBelong2.getItemAtPosition(i);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            belong2 = "null";
                        }
                    });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                belong1 = "null";
            }
        });
        // 设置默认选择值
        spinnerSelectBy.setSelection(spinnerSelectBy.getCount()-1,true);
        spinnerOrderBy.setSelection(spinnerOrderBy.getCount()-1,true);
        spinnerLibrary.setSelection(spinnerLibrary.getCount()-1,true);
        spinnerState.setSelection(spinnerState.getCount()-1,true);
        spinnerBelong1.setSelection(spinnerBelong1.getCount()-1,true);
        query();
    }

    private void fillBookData(ArrayList<Book> books) {
        adapter.setBooks(books);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // 将接口赋值为null
        callbackToMainActivity = null;
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
            if (code == GET_TYPE) {
                // 将整个信息拆分
                JSONArray jsonArraySpinners = jsonObject.getJSONArray("dataArray");
                jsonArraySelectBy = jsonArraySpinners.getJSONArray(0);
                jsonArraySelectBy.add(jsonArraySelectBy.size(), "null");
                jsonArrayOrderBy = jsonArraySpinners.getJSONArray(1);
                jsonArrayOrderBy.add(jsonArrayOrderBy.size(), "null");
                jsonArrayLibrary = jsonArraySpinners.getJSONArray(2);
                jsonArrayLibrary.add(jsonArrayLibrary.size(), "null");
                jsonArrayState = jsonArraySpinners.getJSONArray(3);
                jsonArrayState.add(jsonArrayState.size(), "null");
                JSONArray jsonArrayType = jsonArraySpinners.getJSONArray(4);
                belongs1.clear();
                belongs2.clear();
                for (int i = 0; i < jsonArrayType.size(); i++) {
                    JSONArray belong = jsonArrayType.getJSONArray(i);
                    String belong1 = belong.getString(0);
                    String belong2 = belong.getString(1);
                    String[] arrayStr = belong2.split(",");
                    List<String> list = new ArrayList<String>(Arrays.asList(arrayStr));
                    list.add("null");
                    belongs1.add(belong1);
                    belongs2.add(list);
                }
                List<String> list = new ArrayList<>();
                list.add("null");
                belongs1.add("null");
                belongs2.add(list);
                myHandler.sendEmptyMessage(GET_TYPE);
            } else if (code == GET_BOOK_LIST) {
                String bookListString = jsonObject.getString("object");
                ArrayList<Book> bookArrayList = (ArrayList<Book>) JSON.parseArray(bookListString, Book.class);
                bundle.putParcelableArrayList("bookArrayList",bookArrayList);
                msg.setData(bundle);
                msg.what = GET_BOOK_LIST;
                myHandler.sendMessage(msg);
            } else {
                bundle.putString("reason", "未知错误");
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
    /**
     * @Author: Wallace
     * @Description: 把书本的id返回给所在的主活动，让主活动开启另一个活动查看书本的详情
     * @Date: Created in 9:06 2021/3/22
     * @Modified By:
     * @param position 点击的位置
     * @return: void
     */
    @Override
    public void bookListClickPosition(int position) {
        Book book = adapter.getBooks().get(position);
        int id = book.getId();
        String library = book.getLibrary();
        // 返回id给activity
        callbackToMainActivity.clickToGetBookDetail(id, library, getActivity() == null || !(getActivity() instanceof RMainActivity));
    }
}
