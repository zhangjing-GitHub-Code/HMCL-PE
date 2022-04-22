package com.tungsten.hmclpe.launcher.list.local.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;

import com.tungsten.filepicker.Constants;
import com.tungsten.filepicker.FileBrowser;
import com.tungsten.hmclpe.R;
import com.tungsten.hmclpe.launcher.MainActivity;
import com.tungsten.hmclpe.launcher.dialogs.RenameVersionDialog;
import com.tungsten.hmclpe.launcher.launch.boat.BoatMinecraftActivity;
import com.tungsten.hmclpe.launcher.launch.boat.VirGLService;
import com.tungsten.hmclpe.launcher.launch.pojav.PojavMinecraftActivity;
import com.tungsten.hmclpe.launcher.manifest.AppManifest;
import com.tungsten.hmclpe.launcher.setting.game.PrivateGameSetting;
import com.tungsten.hmclpe.utils.file.FileUtils;
import com.tungsten.hmclpe.utils.gson.GsonUtils;
import com.tungsten.hmclpe.utils.file.DrawableUtils;

import java.io.File;
import java.util.ArrayList;

public class GameListAdapter extends BaseAdapter {

    private Context context;
    private MainActivity activity;
    private ArrayList<GameListBean> list;

    private class ViewHolder{
        LinearLayout item;
        RadioButton radioButton;
        ImageView icon;
        TextView name;
        TextView version;
        ImageButton startGame;
        ImageButton moreVert;
    }

    public GameListAdapter(Context context,MainActivity activity,ArrayList<GameListBean> list){
        this.context = context;
        this.activity = activity;
        this.list = list;
    }

    private void testGame(String name) {
        Intent intent;
        PrivateGameSetting privateGameSetting;
        String settingPath = activity.launcherSetting.gameFileDirectory + "/versions/" + name + "/hmclpe.cfg";
        String finalPath;
        if (new File(settingPath).exists() && GsonUtils.getPrivateGameSettingFromFile(settingPath) != null && (GsonUtils.getPrivateGameSettingFromFile(settingPath).forceEnable || GsonUtils.getPrivateGameSettingFromFile(settingPath).enable)) {
            finalPath = settingPath;
            privateGameSetting = GsonUtils.getPrivateGameSettingFromFile(settingPath);
        }
        else {
            finalPath = AppManifest.SETTING_DIR + "/private_game_setting.json";
            privateGameSetting = activity.privateGameSetting;
        }
        if (privateGameSetting.boatLauncherSetting.enable){
            intent = new Intent(context, BoatMinecraftActivity.class);
            if (privateGameSetting.boatLauncherSetting.renderer.equals("VirGL")) {
                Intent virGLService = new Intent(context, VirGLService.class);
                context.startService(virGLService);
            }
        }
        else {
            intent = new Intent(context, PojavMinecraftActivity.class);
        }
        Bundle bundle = new Bundle();
        bundle.putString("setting_path",finalPath);
        bundle.putBoolean("test",true);
        bundle.putString("version",activity.launcherSetting.gameFileDirectory + "/versions/" + name);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_local_version,null);
            viewHolder.item = convertView.findViewById(R.id.local_version_item);
            viewHolder.radioButton = convertView.findViewById(R.id.select_version);
            viewHolder.icon = convertView.findViewById(R.id.version_icon);
            viewHolder.name = convertView.findViewById(R.id.version_name);
            viewHolder.version = convertView.findViewById(R.id.version_id);
            viewHolder.startGame = convertView.findViewById(R.id.test_game);
            viewHolder.moreVert = convertView.findViewById(R.id.more_vert);
            activity.exteriorConfig.apply(viewHolder.radioButton);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.item.setOnClickListener(v -> {
            activity.uiManager.gameManagerUI.versionName = list.get(position).name;
            activity.uiManager.switchMainUI(activity.uiManager.gameManagerUI);
            activity.uiManager.gameManagerUI.gameManagerUIManager.switchGameManagerUIs(activity.uiManager.gameManagerUI.gameManagerUIManager.versionSettingUI);
        });
        if ((activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name).equals(activity.publicGameSetting.currentVersion)){
            list.get(position).isSelected = true;
        }
        if (list.get(position).isSelected){
            viewHolder.radioButton.setChecked(true);
        }
        else {
            viewHolder.radioButton.setChecked(false);
        }
        viewHolder.radioButton.setOnClickListener(v -> {
            if (!list.get(position).isSelected){
                for (int i = 0;i < list.size();i++){
                    if (i == position){
                        list.get(i).isSelected = true;
                    }
                    else {
                        list.get(i).isSelected = false;
                    }
                }
                activity.publicGameSetting.currentVersion = activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name;
                if (activity.privateGameSetting.gameDirSetting.type == 1){
                    activity.uiManager.settingUI.settingUIManager.universalGameSettingUI.gameDirText.setText(activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name);
                }
                GsonUtils.savePublicGameSetting(activity.publicGameSetting, AppManifest.SETTING_DIR + "/public_game_setting.json");
                notifyDataSetChanged();
            }
        });
        if (!list.get(position).iconPath.equals("") || new File(list.get(position).iconPath).exists()){
            viewHolder.icon.setBackground(DrawableUtils.getDrawableFromFile(list.get(position).iconPath));
        }
        viewHolder.name.setText(list.get(position).name);
        viewHolder.version.setText(list.get(position).version);
        viewHolder.startGame.setOnClickListener(v -> testGame(list.get(position).name));
        viewHolder.moreVert.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(context, R.style.MenuStyle);
            PopupMenu menu = new PopupMenu(wrapper, viewHolder.item,Gravity.RIGHT);
            menu.inflate(R.menu.local_version_menu);
            menu.setForceShowIcon(true);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case R.id.local_version_menu_test_game:
                        testGame(list.get(position).name);
                        return true;
                    case R.id.local_version_menu_generate_launch_script:
                        return true;
                    case R.id.local_version_menu_game_manage:
                        activity.uiManager.gameManagerUI.versionName = list.get(position).name;
                        activity.uiManager.switchMainUI(activity.uiManager.gameManagerUI);
                        activity.uiManager.gameManagerUI.gameManagerUIManager.switchGameManagerUIs(activity.uiManager.gameManagerUI.gameManagerUIManager.versionSettingUI);
                        return true;
                    case R.id.local_version_menu_rename:
                        RenameVersionDialog dialog = new RenameVersionDialog(context, list.get(position).name, new RenameVersionDialog.OnVersionRenameListener() {
                            @Override
                            public void onRename(String name) {
                                new Thread(() -> {
                                    FileUtils.rename(activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name + "/" + list.get(position).name + ".jar",name + ".jar");
                                    FileUtils.rename(activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name + "/" + list.get(position).name + ".json",name + ".json");
                                    FileUtils.rename(activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name,name);
                                    if (list.get(position).isSelected) {
                                        activity.publicGameSetting.currentVersion = activity.launcherSetting.gameFileDirectory + "/versions/" + name;
                                        GsonUtils.savePublicGameSetting(activity.publicGameSetting,AppManifest.SETTING_DIR + "/public_game_setting.json");
                                    }
                                    activity.uiManager.versionListUI.refreshVersionList();
                                }).start();
                            }
                        });
                        dialog.show();
                        return true;
                    case R.id.local_version_menu_copy:

                        return true;
                    case R.id.local_version_menu_delete:
                        PrivateGameSetting s;
                        String sp = activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name + "/hmclpe.cfg";
                        AlertDialog.Builder deleteAlertBuilder = new AlertDialog.Builder(context);
                        deleteAlertBuilder.setTitle(context.getString(R.string.dialog_delete_version_title));
                        deleteAlertBuilder.setPositiveButton(context.getString(R.string.dialog_delete_version_positive), (dialogInterface, i) -> {
                            new Thread(() -> {
                                FileUtils.deleteDirectory(activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name);
                                activity.uiManager.versionListUI.refreshVersionList();
                            }).start();
                        });
                        deleteAlertBuilder.setNegativeButton(context.getString(R.string.dialog_delete_version_negative), (dialogInterface, i) -> {});
                        if (new File(sp).exists() && GsonUtils.getPrivateGameSettingFromFile(sp) != null && (GsonUtils.getPrivateGameSettingFromFile(sp).forceEnable || GsonUtils.getPrivateGameSettingFromFile(sp).enable)) {
                            s = GsonUtils.getPrivateGameSettingFromFile(sp);
                        }
                        else {
                            s = activity.privateGameSetting;
                        }
                        if (s.gameDirSetting.type == 1){
                            deleteAlertBuilder.setMessage(context.getString(R.string.dialog_delete_version_isolate_pri) + " " + list.get(position).name + " " + context.getString(R.string.dialog_delete_version_isolate_sec));
                        }
                        else {
                            deleteAlertBuilder.setMessage(context.getString(R.string.dialog_delete_version_pri) + " " + list.get(position).name + " " + context.getString(R.string.dialog_delete_version_sec));
                        }
                        deleteAlertBuilder.create().show();
                        return true;
                    case R.id.local_version_menu_export_pack:
                        return true;
                    case R.id.local_version_menu_game_folder:
                        PrivateGameSetting privateGameSetting;
                        String settingPath = activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name + "/hmclpe.cfg";
                        if (new File(settingPath).exists() && GsonUtils.getPrivateGameSettingFromFile(settingPath) != null && (GsonUtils.getPrivateGameSettingFromFile(settingPath).forceEnable || GsonUtils.getPrivateGameSettingFromFile(settingPath).enable)) {
                            privateGameSetting = GsonUtils.getPrivateGameSettingFromFile(settingPath);
                        }
                        else {
                            privateGameSetting = activity.privateGameSetting;
                        }
                        String gameDir;
                        if (privateGameSetting.gameDirSetting.type == 0){
                            gameDir = activity.launcherSetting.gameFileDirectory;
                        }
                        else if (privateGameSetting.gameDirSetting.type == 1){
                            gameDir = activity.launcherSetting.gameFileDirectory + "/versions/" + list.get(position).name;
                        }
                        else {
                            gameDir = privateGameSetting.gameDirSetting.path;
                        }
                        Intent intent = new Intent(context, FileBrowser.class);
                        intent.putExtra(Constants.INITIAL_DIRECTORY, gameDir);
                        context.startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            });
            menu.show();
        });
        return convertView;
    }
}
