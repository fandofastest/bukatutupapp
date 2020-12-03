package net.bukatutup.mynewsapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.MenuItem;
import android.widget.LinearLayout;


import net.bukatutup.mynewsapp.R;
import net.bukatutup.mynewsapp.api.models.category.Category;
import net.bukatutup.mynewsapp.api.params.HttpParams;
import net.bukatutup.mynewsapp.utility.Ads;
import net.bukatutup.mynewsapp.adapters.CategoryPagerAdapter;
import net.bukatutup.mynewsapp.api.http.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by SAIF-MCC on 9/19/2017.
 */

public class CategoryListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ViewPager mViewPager;
    private CategoryPagerAdapter mCategoryPagerAdapter;
    private TabLayout tabLayout;

    private int mPerPage = 5;
    private List<Category> categoryList;
    private List<Category> subCategoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initFunctionality();
    }

    private void initVar() {
        mActivity = CategoryListActivity.this;
        mContext = mActivity.getApplicationContext();

        categoryList = new ArrayList<>();
        subCategoryList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_tab);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        initLoader();

        initToolbar();
        setToolbarTitle(getString(R.string.category_list));
        enableBackButton();

    }

    private void initFunctionality() {

        mCategoryPagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager(), (ArrayList) categoryList, (ArrayList) subCategoryList);
        mViewPager.setAdapter(mCategoryPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        showLoader();
        loadCategories();

        // show banner ads
        Ads ads = new Ads(getApplicationContext(),false);
        LinearLayout linearLayout= findViewById(R.id.adView);

        ads.showBannerAds(linearLayout,getDisplay());


    }

    public void loadCategories() {
        ApiUtils.getApiInterface().getCategories(mPerPage).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {

                    int totalPages = Integer.parseInt(response.headers().get(HttpParams.HEADER_TOTAL_PAGE));

                    if (totalPages > 1) {
                        mPerPage = mPerPage * totalPages;
                        loadCategories();

                    } else {
                        categoryList.addAll(response.body());
                        for (Category category : categoryList) {
                            if (category.getParent().intValue() == 0) {
                                subCategoryList.add(category);
                            }
                        }

                        mCategoryPagerAdapter.notifyDataSetChanged();

                    }

                    hideLoader();
                } else {
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                t.printStackTrace();
                hideLoader();
                showEmptyView();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
