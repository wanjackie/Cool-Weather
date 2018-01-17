package com.example.coolweather.utils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.R;
import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class ChooseAreaFragment extends Fragment {

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTRY = 2;
    private ProgressDialog progressDialog;
    private TextView title_view;
    private Button back_button;
    private ListView listview;
    private ArrayAdapter adapter;
    private List<String> datalist = new ArrayList<>();
    //省、市、县 列表
    private List<Province> mProvinceList;
    private List<City> mCities;
    private List<Country> mCountries;

    //选中的省、市、级别
    private Province selectProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        title_view = view.findViewById(R.id.title_text);
        back_button = view.findViewById(R.id.back_button);
        listview = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, datalist);
        listview.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectProvince = mProvinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCities.get(position);
                    queryCountries();
                }
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTRY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();

    }

    /**
     * 查询全国所有省，如果本地没有数据，就从服务器上查询数据
     */
    private void queryProvinces() {
        title_view.setText("中国");
        back_button.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            datalist.clear();
            for (Province province : mProvinceList) {
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询全国所有市，如果本地没有数据，就从服务器上查询数据
     */
    private void queryCities() {
        title_view.setText(selectProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        mCities = DataSupport.where("provinceCode = ?", String.valueOf(selectProvince.getId())).find(City.class);
        if (mCities.size() > 0) {
            datalist.clear();
            for (City city : mCities) {
                datalist.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/" + selectProvince.getProvinceCode();
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询全国所有县，如果本地没有数据，就从服务器上查询数据
     */
    private void queryCountries() {
        title_view.setText(selectedCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        mCountries = DataSupport.where("cityID=?", String.valueOf(selectedCity.getId())).find(Country.class);
        if (mCountries.size() > 0) {
            datalist.clear();
            for (Country country : mCountries) {
                datalist.add(country.getCountryName());

            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        } else {
            String address = "http://guolin.tech/api/china/"
                    + selectProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
            queryFromServer(address, "country");
        }
    }

    /**
     * 从服务器获取天气数据
     *
     * @param address 服务器地址
     * @param type    省、市、县
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtils.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDiag();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(responseText, selectProvince.getId());
                } else if ("country".equals(type)) {
                    result = Utility.handleCountriesResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDiag();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("country".equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 关闭对话框
     */
    private void closeProgressDiag() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 显示对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

}
