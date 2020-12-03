package net.bukatutup.mynewsapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;

import com.mcc.wpnews.R;

import net.bukatutup.mynewsapp.api.models.category.Category;
import net.bukatutup.mynewsapp.api.params.HttpParams;
import net.bukatutup.mynewsapp.utility.ActivityUtils;
import net.bukatutup.mynewsapp.utility.ItemTouchHelperCallback;
import net.bukatutup.mynewsapp.adapters.SelectableCategoryAdapter;
import net.bukatutup.mynewsapp.api.http.ApiUtils;
import net.bukatutup.mynewsapp.data.constant.AppConstant;
import net.bukatutup.mynewsapp.data.sqlite.SelectableCatDbController;
import net.bukatutup.mynewsapp.listeners.ListItemClickListener;
import net.bukatutup.mynewsapp.listeners.OnCategoryListChangedListener;
import net.bukatutup.mynewsapp.listeners.OnStartDragListener;
import net.bukatutup.mynewsapp.models.SelectableCategoryModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by SAIF-MCC on 11/20/2017.
 */

public class HomeCategoriesActivity extends BaseActivity implements OnCategoryListChangedListener,
        OnStartDragListener {

    private Activity mActivity;
    private Context mContext;
    private SelectableCategoryAdapter categoryAdapter;
    private RecyclerView categoriesRecyclerView;
    private int mPerPage = 5;
    private SelectableCatDbController selectableCatDbController;
    private List<Category> categoryList;
    private List<SelectableCategoryModel> selectableCategoryList;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initFunctionality();
        initListener();
    }

    private void initVar() {
        mActivity = HomeCategoriesActivity.this;
        mContext = mActivity.getApplicationContext();
        categoryList = new ArrayList<>();
        selectableCategoryList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_menu_or_home_cat_list);

        categoriesRecyclerView = (RecyclerView) findViewById(R.id.rv_menus);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        initLoader();

        initToolbar();
        setToolbarTitle(getString(R.string.home_cat_list));
        enableBackButton();

    }

    private void initFunctionality() {
        showLoader();
        loadCategories();

        categoryAdapter = new SelectableCategoryAdapter(mContext, (ArrayList) categoryList, this, this);
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(categoryAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(categoriesRecyclerView);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        getAllDatabaseData();


    }

    public void initListener() {
        categoryAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Category clickedCategory = categoryList.get(position);

                clickedCategory.setCategorySelected(!clickedCategory.isCategorySelected());
                if (clickedCategory.isCategorySelected()) {
                    selectableCatDbController.insertData(clickedCategory.getID().intValue(), clickedCategory.getName(), position);
                } else {
                    selectableCatDbController.deleteCat(clickedCategory.getID().intValue());
                }
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateUI() {
        // sort categoryList according to category-order
        Comparator<Category> comparator = new Comparator<Category>() {
            @Override
            public int compare(Category left, Category right) {
                return left.getCategoryOrder() - right.getCategoryOrder();
            }
        };

        Collections.sort(categoryList, comparator);
        categoryAdapter.notifyDataSetChanged();
    }

    public void getAllDatabaseData() {
        if (selectableCatDbController == null) {
            selectableCatDbController = new SelectableCatDbController(mContext);
        }
        selectableCategoryList.clear();
        selectableCategoryList.addAll(selectableCatDbController.getAllData());
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
                        List<Category> categories = response.body();
                        if (categories != null && !categories.isEmpty()) {
                            //for (Category category : categories) {
                            for (Iterator<Category> it = categories.iterator(); it.hasNext(); ) {
                                Category category = it.next();

                                if (category.getCount().intValue() > 0) {
                                    // if present category is selected to display at home
                                    //for (SelectableCategoryModel selectableCategoryModel : selectableCategoryList) {

                                    for (Iterator<SelectableCategoryModel> it1 = selectableCategoryList.iterator(); it1.hasNext(); ) {
                                        SelectableCategoryModel selectableCategoryModel = it1.next();

                                        if (selectableCategoryModel.getCategoryId() == category.getID().intValue()) {
                                            category.setCategorySelected(true);
                                            category.setCategoryOrder(selectableCategoryModel.getCategoryOrder());
                                            break;
                                        }
                                    }
                                    if (!category.isCategorySelected()) {
                                        category.setCategoryOrder(AppConstant.MAX_VALUE);
                                    }
                                    categoryList.add(category);
                                }
                            }
                        }

                        updateUI();
                    }

                    hideLoader();
                } else {
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                t.printStackTrace();
                showEmptyView();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtils.getInstance().invokeActivity(HomeCategoriesActivity.this, MainActivity.class, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ActivityUtils.getInstance().invokeActivity(HomeCategoriesActivity.this, MainActivity.class, true);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onListChanged(List<Category> categories) {
        //after drag and drop operation, the new list of Categories is passed in here
        getAllDatabaseData();
        for (Category category : categories) {
            // if present category is selected for home screen display
            for (SelectableCategoryModel selectableCategoryModel : selectableCategoryList) {
                if (selectableCategoryModel.getCategoryId() == category.getID().intValue()) {
                    selectableCatDbController.updateCategoryOrder(selectableCategoryModel.getId(), categories.indexOf(category));
                    break;
                }
            }
        }
    }
}
