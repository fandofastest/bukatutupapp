package com.mcc.wpnews.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.mcc.wpnews.R;

import com.mcc.wpnews.data.constant.AppConstant;
import com.mcc.wpnews.utility.ActivityUtils;
import com.mcc.wpnews.utility.Ads;
import com.mcc.wpnews.utility.AppUtils;
import com.mcc.wpnews.utility.FilePicker;
import com.mcc.wpnews.utility.PermissionUtils;
import com.mcc.wpnews.webengine.WebEngine;
import com.mcc.wpnews.webengine.WebListener;

/**
 * Created by SAIF-MCC on 10/3/2017.
 */

public class CustomLinkAndPageActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;
    private String pageTitle, pageUrl;
    private WebView webView;
    private WebEngine webEngine;

    boolean isFromAppLink = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initAppLink();
        initFunctionality();
    }

    private void initVar() {
        mActivity = CustomLinkAndPageActivity.this;
        mContext = mActivity.getApplicationContext();

        Intent intent = getIntent();
        if (intent != null) {
            pageTitle = intent.getStringExtra(AppConstant.BUNDLE_KEY_TITLE);
            pageUrl = intent.getStringExtra(AppConstant.BUNDLE_KEY_URL);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_custom_link);

        initWebEngine();

        initToolbar();
        setToolbarTitle(pageTitle);
        enableBackButton();
    }

    public void initWebEngine() {

        webView = (WebView) findViewById(R.id.web_view);

        webEngine = new WebEngine(webView, mActivity);
        webEngine.initWebView();


        webEngine.initListeners(new WebListener() {
            @Override
            public void onStart() {
                initLoader();
            }

            @Override
            public void onLoaded() {
                hideLoader();
            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onNetworkError() {
                showEmptyView();
            }

            @Override
            public void onPageTitle(String title) {
            }
        });
    }


    private void initFunctionality() {

        showLoader();
        if (!isFromAppLink) {
            loadCustomLinkAndPage();
        } else {
            String postIdText="";
            for(int i=pageUrl.length()-2;i>=0;i--) {
                if(!Character.isDigit(pageUrl.charAt(i))) {
                    break;
                }
                postIdText+=pageUrl.charAt(i);
            }

            try {
                int postId = Integer.parseInt(new StringBuilder(postIdText).reverse().toString());
                ActivityUtils.getInstance().invokePostDetailsForAppLink(mActivity, PostDetailsActivity.class, postId, true, true);
            } catch (Exception e) {
                loadCustomLinkAndPage();
            }

        }


    }

    public void initAppLink() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if(data != null){
            pageUrl = data.toString();
            isFromAppLink = true;
        }

    }

    private void loadCustomLinkAndPage() {
        webEngine.loadPage(pageUrl);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionUtils.isPermissionResultGranted(grantResults)) {
            if (requestCode == PermissionUtils.REQUEST_WRITE_STORAGE_UPLOAD) {
                if (webEngine != null) {
                    webEngine.invokeImagePickerActivity();
                }
            } else if (requestCode == PermissionUtils.REQUEST_WRITE_STORAGE_DOWNLOAD) {
                if (webEngine != null) {
                    webEngine.downloadFile();
                }
            } else if (requestCode == PermissionUtils.REQUEST_CALL) {
                AppUtils.makePhoneCall(mActivity, AppConstant.CALL_NUMBER);
            }
        } else {
            AppUtils.showToast(mActivity, getString(R.string.permission_not_granted));
        }

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (reqCode == WebEngine.KEY_FILE_PICKER) {
                String picturePath = FilePicker.getPickedFilePath(this, data);
                if (webEngine != null) {
                    webEngine.uploadFile(data, picturePath);
                } else {
                    AppUtils.showToast(mContext, getString(R.string.failed));
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (webEngine != null) {
                webEngine.cancelUpload();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

}
