package com.attendance.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.attendance.R;

public class ProfileFragment extends Fragment {

    private TextView tvStaffNo;
    private TextView tvStaffName;
    private TextView tvDepartment;
    private TextView tvPosition;
    private TextView tvPhone;
    private TextView tvEmail;
    private TextView tvCreateTime;
    private Button btnLogout;
    private LinearLayout llSettings;
    private LinearLayout llChangePassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvStaffNo = view.findViewById(R.id.tv_staff_no);
        tvStaffName = view.findViewById(R.id.tv_staff_name);
        tvDepartment = view.findViewById(R.id.tv_department);
        tvPosition = view.findViewById(R.id.tv_position);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvEmail = view.findViewById(R.id.tv_email);
        tvCreateTime = view.findViewById(R.id.tv_create_time);
        btnLogout = view.findViewById(R.id.btn_logout);
        llSettings = view.findViewById(R.id.ll_settings);
        llChangePassword = view.findViewById(R.id.ll_change_password);

        loadUserInfo();

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        if (llSettings != null) {
            llSettings.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getContext(), SettingActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (llChangePassword != null) {
            llChangePassword.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(getContext(), ChangePasswordActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void loadUserInfo() {
        SharedPreferences sp = getContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "--");
        String staffName = sp.getString("staffName", "--");
        String department = sp.getString("department", "--");
        String position = sp.getString("position", "--");
        String phone = sp.getString("phone", "--");
        String email = sp.getString("email", "--");

        if (tvStaffNo != null) tvStaffNo.setText(staffNo);
        if (tvStaffName != null) tvStaffName.setText(staffName);
        if (tvDepartment != null) tvDepartment.setText(department.isEmpty() ? "--" : department);
        if (tvPosition != null) tvPosition.setText(position.isEmpty() ? "--" : position);
        if (tvPhone != null) tvPhone.setText(phone.isEmpty() ? "--" : phone);
        if (tvEmail != null) tvEmail.setText(email.isEmpty() ? "--" : email);
        if (tvCreateTime != null) tvCreateTime.setText("--");
    }

    private void logout() {
        SharedPreferences sp = getContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
