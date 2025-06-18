package com.example.myrobotapp.Class;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myrobotapp.Fragments.Connection.ActiveConsFragment;
import com.example.myrobotapp.Fragments.Connection.CredentialsFragment;
import com.example.myrobotapp.Fragments.Features.Settings.CustomDevices.PDOFragment;
import com.example.myrobotapp.Fragments.Features.Settings.CustomDevices.RxPDOFragment;
import com.example.myrobotapp.Fragments.Features.Settings.CustomDevices.SyncFragment;
import com.example.myrobotapp.Fragments.Features.Settings.CustomDevices.TxPDOFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new RxPDOFragment();
            case 1:
                return new TxPDOFragment();
            case 2:
                return new PDOFragment();
            case 3:
                return new SyncFragment();
            default:
                return new RxPDOFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
