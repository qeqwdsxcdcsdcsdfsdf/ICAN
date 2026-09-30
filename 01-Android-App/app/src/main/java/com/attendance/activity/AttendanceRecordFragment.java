package com.attendance.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.attendance.R;
import com.attendance.api.ApiService;
import com.attendance.api.AttendanceApi;
import com.attendance.bean.ApiResponse;
import com.attendance.bean.AttendanceRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceRecordFragment extends Fragment {

    private ListView listView;
    private TextView tvEmpty;
    private List<AttendanceRecord> recordList = new ArrayList<>();
    private Handler handler;
    private Runnable refreshRunnable;
    private RecordAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        listView = view.findViewById(R.id.list_records);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new RecordAdapter();
        listView.setAdapter(adapter);

        loadRecords();
        startRefreshTimer();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRefreshTimer();
    }

    private void loadRecords() {
        SharedPreferences sp = getContext().getSharedPreferences("user_info", getContext().MODE_PRIVATE);
        String staffNo = sp.getString("staffNo", "");

        AttendanceApi api = ApiService.getAttendanceApi(getContext());
        Call<ApiResponse<List<AttendanceRecord>>> call = api.getAttendanceRecords(staffNo);

        call.enqueue(new Callback<ApiResponse<List<AttendanceRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AttendanceRecord>>> call, Response<ApiResponse<List<AttendanceRecord>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<AttendanceRecord>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        recordList.clear();
                        recordList.addAll(apiResponse.getData());
                        adapter.notifyDataSetChanged();

                        if (recordList.isEmpty()) {
                            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                            if (listView != null) listView.setVisibility(View.GONE);
                        } else {
                            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                            if (listView != null) listView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AttendanceRecord>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private class RecordAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recordList.size();
        }

        @Override
        public Object getItem(int position) {
            return recordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.record_item, parent, false);
                holder = new ViewHolder();
                holder.tvIcon = convertView.findViewById(R.id.tv_icon);
                holder.tvTime = convertView.findViewById(R.id.tv_time);
                holder.tvType = convertView.findViewById(R.id.tv_type);
                holder.tvResult = convertView.findViewById(R.id.tv_result);
                holder.tvLocation = convertView.findViewById(R.id.tv_location);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AttendanceRecord record = recordList.get(position);

            String signTimeStr = "";
            if (record.getSignTime() != null) {
                signTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getSignTime());
            }
            holder.tvTime.setText(signTimeStr);

            boolean isMorning = record.getSignType() != null && record.getSignType() == 1;
            holder.tvType.setText(isMorning ? "上班" : "下班");
            holder.tvIcon.setText(isMorning ? "🌅" : "🌙");

            if (record.getSignResult() != null) {
                boolean isSuccess = record.getSignResult() == 0;
                holder.tvResult.setText(isSuccess ? "成功" : "失败");
                holder.tvResult.setTextColor(isSuccess ? 0xFF4CAF50 : 0xFFF44336);
            }

            if (record.getSignLat() != null && record.getSignLng() != null) {
                holder.tvLocation.setText(String.format("定位:(%.4f,%.4f)", record.getSignLat(), record.getSignLng()));
                holder.tvLocation.setVisibility(View.VISIBLE);
            } else {
                holder.tvLocation.setVisibility(View.GONE);
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvIcon;
            TextView tvTime;
            TextView tvType;
            TextView tvResult;
            TextView tvLocation;
        }
    }

    private void startRefreshTimer() {
        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = () -> {
            loadRecords();
            handler.postDelayed(refreshRunnable, 10000);
        };
        handler.postDelayed(refreshRunnable, 10000);
    }

    private void stopRefreshTimer() {
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
}
