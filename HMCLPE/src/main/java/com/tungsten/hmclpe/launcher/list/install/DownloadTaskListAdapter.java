package com.tungsten.hmclpe.launcher.list.install;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tungsten.hmclpe.R;
import com.tungsten.hmclpe.utils.convert.ConvertUtils;

import java.util.ArrayList;

public class DownloadTaskListAdapter extends RecyclerView.Adapter<DownloadTaskListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DownloadTaskListBean> list;

    public DownloadTaskListAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_task,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadTaskListBean downloadTaskListBean = list.get(position);
        holder.progressBar.setProgress(downloadTaskListBean.progress);
        holder.fileName.setText(downloadTaskListBean.name);
        if ((downloadTaskListBean.path == null || downloadTaskListBean.path.equals("")) && (downloadTaskListBean.url == null || downloadTaskListBean.url.equals("")) && (downloadTaskListBean.sha1 == null || downloadTaskListBean.sha1.equals(""))) {
            holder.progressBar.setIndeterminate(true);
            ViewGroup.LayoutParams layoutParams = holder.progressBar.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.progressBar.setLayoutParams(layoutParams);
        }
        else {
            holder.progressBar.setIndeterminate(false);
            ViewGroup.LayoutParams layoutParams = holder.progressBar.getLayoutParams();
            layoutParams.height = ConvertUtils.dip2px(context,3);
            holder.progressBar.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public DownloadTaskListBean getItem(int i) {
        return list.get(i);
    }

    public void addDownloadTask(DownloadTaskListBean bean) {
        list.add(bean);
        this.notifyItemInserted(list.size() - 1);
    }

    public void onProgress(DownloadTaskListBean bean) {
        for(int i = 0; i < list.size(); ++i) {
            if (list.get(i).url.equals(bean.url)) {
                list.set(i, bean);
                this.notifyItemChanged(i);
            }
        }
    }

    public void onComplete(DownloadTaskListBean bean) {
        for(int i = 0; i < list.size(); ++i) {
            if (list.get(i).url.equals(bean.url)) {
                list.remove(i);
                this.notifyItemRemoved(i);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private ProgressBar progressBar;

        public ViewHolder(View parent) {
            super(parent);
            this.progressBar = parent.findViewById(R.id.download_task_progress);
            this.fileName = parent.findViewById(R.id.download_task_name);
        }
    }
}
