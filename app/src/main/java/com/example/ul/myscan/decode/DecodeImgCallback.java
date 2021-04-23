package com.example.ul.myscan.decode;

import com.google.zxing.Result;

/**
 * @Author: Wallace
 * @Description: 解析图片的回调
 * @Date: Created 0:28 2021/4/23
 * @Modified: by who yyyy-MM-dd
 */
public interface DecodeImgCallback {

    void onImageDecodeSuccess(Result result);

    void onImageDecodeFailed();
}
