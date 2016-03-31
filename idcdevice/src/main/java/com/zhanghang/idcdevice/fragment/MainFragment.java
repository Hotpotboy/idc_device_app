package com.zhanghang.idcdevice.fragment;

import android.support.v4.view.ViewPager;

import com.zhanghang.idcdevice.R;
import com.zhanghang.self.base.BaseFragment;
import com.zhanghang.self.fragment.ViewPagerFragement;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-03-29.
 */
public class MainFragment extends ViewPagerFragement {
    @Override
    protected ArrayList<BaseFragment> specifyFragmentList() {
        return null;
    }

    @Override
    protected ViewPager specifyViewPager() {
        return (ViewPager) mRootView.findViewById(R.id.fragment_main_viewPager);
    }

    @Override
    protected int specifyRootLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
