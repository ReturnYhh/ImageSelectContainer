package com.myimageselectcontainer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.myimageselectcontainer.adapter.ImageDirAdapter;
import com.myimageselectcontainer.adapter.MyAdapter;
import com.myimageselectcontainer.adapter.SpacesItemDecoration;
import com.myimageselectcontainer.bean.ImageBean;
import com.myimageselectcontainer.bean.ImageDirBean;
import com.myimageselectcontainer.click.OnChangeListener;
import com.myimageselectcontainer.click.OnImageDirItemListener;
import com.myimageselectcontainer.click.OnItemClickListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


public class ImageSelectActivity extends AppCompatActivity implements OnItemClickListener,
        OnChangeListener, View.OnClickListener, PopupWindow.OnDismissListener, OnImageDirItemListener {
    private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
    private static final int PHOTO_REQUEST_CUT = 2;// 结果
    private ProgressDialog mProgressDialog;
    /**
     * 存储文件夹中的图片数量
     */
    private int mPicsSize;
    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageDirBean> imageDirBeans = new ArrayList<>();
    /**
     * 图片数量
     */
    private int totalCount = 0;
    /**
     * 临时的辅助类，用于防止同一个文件夹的多次扫描
     */
    private HashSet<String> mDirPaths = new HashSet<>();
    /**
     * 所有的图片
     */
    private List<ImageBean> mImages = new ArrayList<>();
    /**
     * 选中的图片集合
     */
    private ArrayList<ImageBean> mSelectImages = new ArrayList<>();
    /**
     * 最大的图片数
     */
    private int maxImageCount = 9;
    /**
     * 屏幕高度
     */
    private int mScreenHeight;
    /**
     * 用来存储选中的文件
     */
    private File mSelectFile;

    /**
     * 用于显示全部文件夹
     */
    private PopupWindow mPopupWindow;
    /**
     * 当PopupWindow显示或者消失时改变背景色
     */
    private WindowManager.LayoutParams lp;
    /**
     * 拿到传过来的值，测试选择图片
     */
    private int select;
    /**
     * 存储拍照和选中的图片
     */
    private File file;
    private MyThread mThread;

    private ImageDirBean imageDirBean;
    private ImageBean imageBean;
    private RecyclerView rcyImageSelect;
    private TextView tvImageCount;
    private TextView tvImageDir;
    private TextView tvConfirm;
    private LinearLayout linearLayout;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mProgressDialog.dismiss();
            if (msg.what == 0x110) {
                //绑定数据
                setData();
            } else {
                if (mThread != null && !mThread.isInterrupted()) {
                    mThread.isInterrupted();
                }
            }
        }
    };

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);


        select = getIntent().getIntExtra("select", 0);
        maxImageCount = select;


        rcyImageSelect = (RecyclerView) findViewById(R.id.rcyView_imageSelect);
        tvImageCount = (TextView) findViewById(R.id.tv_imageCount);
        tvImageDir = (TextView) findViewById(R.id.tv_imageDir);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        mPopupWindow = new PopupWindow(this);
        //获取屏幕高度，设给PopupWindow
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenHeight = outMetrics.heightPixels;
        lp = getWindow().getAttributes();

        rcyImageSelect.setLayoutManager(new GridLayoutManager(this, 3));
        tvConfirm.setText("确定" + mSelectImages.size() + "/" + maxImageCount);

        getImageList();
        setImageDirData();

        tvImageDir.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        mPopupWindow.setOnDismissListener(this);

        file = new File(Environment.getExternalStorageDirectory(), "temp.jpg");

    }

    private void setData() {
        mAdapter = new MyAdapter(this, maxImageCount, mImages, this, this);
        rcyImageSelect.setAdapter(mAdapter);
        rcyImageSelect.addItemDecoration(new SpacesItemDecoration(2));
        tvImageCount.setText(totalCount + "张");
    }

    //图片文件数据
    private void setImageDirData() {
        if (!imageDirBeans.isEmpty()) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.image_dir_list, null);
            RecyclerView rcyViewImageDir = (RecyclerView) contentView.findViewById(R.id.rcyView_imageDir);
            rcyViewImageDir.setLayoutManager(new LinearLayoutManager(this));
            rcyViewImageDir.setAdapter(new ImageDirAdapter(this, imageDirBeans, this));

            mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupWindow.setHeight((int) (mScreenHeight * 0.7f));
            mPopupWindow.setContentView(contentView);

            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setFocusable(true);
            mPopupWindow.showAsDropDown(linearLayout, 0, 0);
            // 设置背景颜色变暗
            lp.alpha = 0.5f;
            getWindow().setAttributes(lp);
        }
    }

    private void getImageList() {
        //判断是否有内存卡
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
        } else {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.show();

            mThread = new MyThread();
            mThread.start();

            //在子线程中读取最多图片的集合
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    //第一张图片
//                    String firstImage = null;
//                    //获取内存卡路径
//                    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                    //通过内容解析器解析出png和jpeg格式的图片
//                    ContentResolver mContentResolver = ImageSelectActivity.this
//                            .getContentResolver();
//                    Cursor mCursor = mContentResolver.query(mImageUri, null,
//                            MediaStore.Images.Media.MIME_TYPE + "=? or "
//                                    + MediaStore.Images.Media.MIME_TYPE + "=?",
//                            new String[]{"image/png", "image/jpeg"},
//                            MediaStore.Images.Media.DATE_MODIFIED);
//                    //判断是否存在图片
//                    if (mCursor.getCount() > 0) {
//                        while (mCursor.moveToNext()) {
//                            // 获取图片的路径
//                            String path = mCursor.getString(mCursor
//                                    .getColumnIndex(MediaStore.Images.Media.DATA));
//                            // 拿到第一张图片的路径
//                            if (firstImage == null)
//                                firstImage = path;
//                            // 获取该图片的文件名
//                            File parentFile = new File(path).getParentFile();
//                            if (parentFile == null)
//                                continue;
//                            //获取到文件地址
//                            String dirPath = parentFile.getAbsolutePath();
//                            imageBean = new ImageBean();
//                            imageBean.setPath(path);
//                            mImages.add(imageBean);
//                            if (mDirPaths.contains(dirPath)) {
//                                continue;
//                            } else {
//                                mDirPaths.add(dirPath);
//                                imageDirBean = new ImageDirBean();
//                                imageDirBean.setDir(dirPath);
//                                imageDirBean.setImagePath(path);
//                            }
//                            int picSize = parentFile.list(new FilenameFilter() {
//                                @Override
//                                public boolean accept(File dir, String filename) {
//                                    if (filename.endsWith(".jpg")
//                                            || filename.endsWith(".png")
//                                            || filename.endsWith(".jpeg"))
//                                        return true;
//                                    return false;
//                                }
//                            }).length;
//                            totalCount += picSize;
//
//                            imageDirBean.setImageCount(picSize);
//                            imageDirBeans.add(imageDirBean);
//                            if (picSize > mPicsSize) {
//                                mPicsSize = picSize;
//                            }
//                        }
//                        mCursor.close();
//                        mDirPaths = null;
//                        // 通知Handler扫描图片完成
//                        mHandler.sendEmptyMessage(0x110);
//                    }
//                }
//            }).start();


        }
    }

    @Override
    public void onItemClickListener(View view, int position) {
        if (position != 0) {
            if (maxImageCount == 1) {
                clipPhoto(Uri.fromFile(new File(mImages.get(position).getPath())), PHOTO_REQUEST_CUT);//开始裁减图片
            } else {
                Toast.makeText(this, position + "", Toast.LENGTH_SHORT).show();
            }
        } else if (select == 1) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // 下面这句指定调用相机拍照后的照片存储的路径
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(cameraIntent, PHOTO_REQUEST_CAMERA);// CAMERA_OK是用作判断返回结果的标识
        }

    }

    @Override
    public void OnChangeListener(int position, boolean isChecked) {
        if (isChecked) {
            mImages.get(position).setSelect(true);
            if (!contains(mSelectImages, mImages.get(position))) {
                mSelectImages.add(mImages.get(position));
                if (mSelectImages.size() == maxImageCount) {
                    mAdapter.notifyData(mSelectImages);
                }
            }
        } else {
            mImages.get(position).setSelect(false);
            if (contains(mSelectImages, mImages.get(position))) {
                mSelectImages.remove(mImages.get(position));
                if (mSelectImages.size() == maxImageCount - 1) {
                    mAdapter.notifyData(mSelectImages);
                }
            }
        }
        tvConfirm.setText("确定" + mSelectImages.size() + "/" + maxImageCount);
    }

    private boolean contains(List<ImageBean> list, ImageBean imageBean) {
        for (ImageBean bean : list) {
            if (bean.getPath().equals(imageBean.getPath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_imageDir) {
            setImageDirData();
        } else if (v.getId() == R.id.tv_confirm) {
            if (mSelectImages.size() != 0) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("selectImages", mSelectImages);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "请选择至少一张图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDismiss() {
        // 设置背景颜色变暗
        lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onImageDirItemListener(View view, int position) {
        mImages.clear();
        mImages.add(null);
        if (mSelectImages.size() > 0) {
            mSelectImages.clear();
        }
        String dir = imageDirBeans.get(position).getDir();
        mSelectFile = new File(dir);
        List<String> imagePath = Arrays.asList(mSelectFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".png")
                        || filename.endsWith(".jpeg"))
                    return true;
                return false;
            }
        }));
        for (int i = 0; i < imagePath.size(); i++) {
            imageBean = new ImageBean();
            imageBean.setPath(dir + "/" + imagePath.get(i));
            imageBean.setSelect(false);
            mImages.add(imageBean);
        }
        tvImageDir.setText(imageDirBeans.get(position).getImageName());
        tvImageCount.setText(imageDirBeans.get(position).getImageCount() + "张");
        mAdapter.notifyDataSetChanged();
        mPopupWindow.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case PHOTO_REQUEST_CAMERA:
                    clipPhoto(Uri.fromFile(file), PHOTO_REQUEST_CAMERA);//开始裁减图片
                    break;
                case PHOTO_REQUEST_CUT:
                    Bitmap bitmap = data.getParcelableExtra("data");
                    Intent intent = new Intent();
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bs);
                    byte[] bitmapByte = bs.toByteArray();
                    intent.putExtra("bitmap", bitmapByte);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
    }

    private void clipPhoto(Uri uri, int type) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setAction("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");// mUri是已经选择的图片Uri
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);// 输出图片大小
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);

        if (type == PHOTO_REQUEST_CAMERA) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            //第一张图片
            String firstImage = null;
            //获取内存卡路径
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            //通过内容解析器解析出png和jpeg格式的图片
            ContentResolver mContentResolver = ImageSelectActivity.this
                    .getContentResolver();
            Cursor mCursor = mContentResolver.query(mImageUri, null,
                    MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/png", "image/jpeg"},
                    MediaStore.Images.Media.DATE_MODIFIED);
            //判断是否存在图片
            if (mCursor.getCount() > 0) {
                mImages.add(null);
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    // 拿到第一张图片的路径
                    if (firstImage == null)
                        firstImage = path;
                    // 获取该图片的文件名
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null)
                        continue;
                    //获取到文件地址
                    String dirPath = parentFile.getAbsolutePath();
                    imageBean = new ImageBean();
                    imageBean.setPath(path);
                    mImages.add(imageBean);
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        imageDirBean = new ImageDirBean();
                        imageDirBean.setDir(dirPath);
                        imageDirBean.setImagePath(path);
                    }
                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if (filename.endsWith(".jpg")
                                    || filename.endsWith(".png")
                                    || filename.endsWith(".jpeg"))
                                return true;
                            return false;
                        }
                    }).length;
                    totalCount += picSize;

                    imageDirBean.setImageCount(picSize);
                    imageDirBeans.add(imageDirBean);
                    if (picSize > mPicsSize) {
                        mPicsSize = picSize;
                    }
                }
                mCursor.close();
                mDirPaths = null;
                // 通知Handler扫描图片完成
                mHandler.sendEmptyMessage(0x110);
            } else {
                mHandler.sendEmptyMessage(0x120);
            }
        }
    }
}
