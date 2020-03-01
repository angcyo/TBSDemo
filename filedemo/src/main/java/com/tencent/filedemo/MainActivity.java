package com.tencent.filedemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tencent.tbs.reader.ITbsReader;
import com.tencent.tbs.reader.ITbsReaderCallback;
import com.tencent.tbs.reader.TbsFileInterfaceImpl;
import com.tencent.tbs.reader.TbsReaderView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    int mSettingId = R.id.action_settings_dialog;
    public static final int EXTERNAL_SDCARD_CODE = 1101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_SDCARD_CODE);
            }
        }

        TbsFileInterfaceImpl.initEngine(MainActivity.this);
        TbsFileInterfaceImpl.setProviderSetting("android.support.v4.content.FileProvider");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
                FileReaderDialog readerDialog = new FileReaderDialog(MainActivity.this, sdcard, new FileReaderDialog.FileReaderDialogCallback() {
                    @Override
                    public void onFileSelect(String fileName, FrameLayout rootlayout) {
                        if (mSettingId == R.id.action_settings_view) {
                            loadReaderView(fileName, rootlayout);
                            return;
                        }

                        String[] items = fileName.split("\\.");
                        if (items.length > 1) {
                            String extName = items[items.length - 1].toLowerCase();
                            if (!TbsFileInterfaceImpl.canOpenFile(extName)) {
                                Toast.makeText(MainActivity.this, "ext " + extName + " not supported", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        Bundle param = new Bundle();
                        param.putString("filePath", fileName);
                        ITbsReaderCallback callback = new ITbsReaderCallback() {
                            @Override
                            public void onCallBackAction(Integer actionType, Object args, Object result) {
                                Log.i("FileReaderdemo", "actionType=" + actionType);

                                if (ITbsReader.OPEN_FILEREADER_PLUGIN_SUCCESS == actionType) {
                                    Log.i("FileReaderdemo", "plugin success");
                                } else if (ITbsReader.OPEN_FILEREADER_PLUGIN_FAILED == actionType) {
                                    Log.i("FileReaderdemo", "plugin failed");
                                }

                            }
                        };

                        int ret = -1;
                        if (false)//true)
                        {
                            //VIEW TO SHOW
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                            lp.topMargin = 90;
                            FrameLayout contentLayout = new FrameLayout(MainActivity.this);
                            rootlayout.addView(contentLayout, lp);
                            //param.putInt("windowType",2); //默认不设置，是全屏dialog显示文件内容,
                            // 设置windowType = 2，进入view显示文件内容, 文件内容会挂到设置的layout上。
                            param.putInt("windowType", TbsFileInterfaceImpl.FILE_READER_WINDOW_TYPE_VIEW);
                            TbsFileInterfaceImpl.getInstance().openFileReader(MainActivity.this, param, callback, contentLayout);
                        } else {
                            // dialog to show
                            TbsFileInterfaceImpl.getInstance().openFileReader(MainActivity.this, param, callback, null);
                        }

                        Log.i("FileReaderdemo", "ret1 = " + ret + "file name = " + fileName);
                    }

                    @Override
                    public boolean canBackPress() {
                        if (mReaderView != null) {
                            mReaderView.onStop();
                            if (null != mReaderView.getParent()) {
                                ((FrameLayout) mReaderView.getParent()).removeView(mReaderView);
                            }
                            mReaderView = null;
                            return false;
                        }

                        return true;
                    }
                });

                readerDialog.refreshFileList();
                readerDialog.show();
            }
        });
    }

    TbsReaderView mReaderView = null;

    void loadReaderView(String fileName, FrameLayout rootlayout) {
        String[] items = fileName.split("\\.");
        String extName = "";
        if (items.length > 1) {
            extName = items[items.length - 1].toLowerCase();
            if (!TbsReaderView.isSupportExt(MainActivity.this, extName)) {
                Toast.makeText(MainActivity.this, "ext " + extName + " not supported", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lp.topMargin = 70;
        TbsReaderView readerView = new TbsReaderView(MainActivity.this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer type, Object o, Object o1) {
                Log.d("mainActivity", "type=" + type);
            }
        });

        Bundle param = new Bundle();
        param.putString("fileExt", extName);
        param.putString("filePath", fileName);
        if (readerView.preOpen(extName, false)) {
            readerView.openFile(param);
        }
        mReaderView = readerView;
        rootlayout.addView(readerView, lp);
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings_dialog)
//        {
//            return true;
//        }
        mSettingId = id;
        return super.onOptionsItemSelected(item);
    }

    private static final String SDCARD_TBS_READER_TEMP_DIR = Environment.getExternalStorageDirectory() + File.separator
            + "tencent" + File.separator + "tbs";
    private static final String DIR_READER_TEMP = "TbsReaderTemp";

    public static String getReaderTemp(Context context) {
        File parentDir = null;
        try {
            parentDir = context.getDir("tbs", Context.MODE_PRIVATE);
            if (parentDir != null) {
                if (!parentDir.exists())
                    parentDir.mkdir();
                return createDir(parentDir, DIR_READER_TEMP).getAbsolutePath();
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static File createDir(File parent, String dirName) {
        if (parent == null || dirName == null || dirName.length() == 0)
            return null;

        File childDir = new File(parent, dirName);
        if (!childDir.exists())
            childDir.mkdirs();

        return childDir;
    }
}
