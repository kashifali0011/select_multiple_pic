package com.exampss.selectmultiplepicproject

import android.net.Uri


interface IGenericCallBack {
    fun success(apiName: String, response: Any?)

    fun failure(apiName: String,message: String?)

}

interface ICallBackUri {
    fun imageUri(result: Uri?)
}

interface ICallBackUriMultiple {
    fun imageUri(result: ArrayList<Uri>)
}