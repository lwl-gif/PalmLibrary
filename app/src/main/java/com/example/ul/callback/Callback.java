package com.example.ul.callback;

import org.json.JSONArray;

//搜索时回调的接口
public interface Callback {
    //子碎片请求父碎片的数据
    JSONArray readerManageQuery(String selectBy,String sortBy,String readerType);
}
