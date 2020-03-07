package com.tencent.filedemo;

import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileReaderDialog extends Dialog implements ListAdapter, Comparator<File>, OnClickListener
{
	private float			mDensity  = -1 ;
	private String			mRootDir  = null ;
	private File			mCurrDir  = null ;
	private LinearLayout	mRootView = null ;
	
	private LinearLayout	mHeadView = null ;
	private Button			mBackButn = null ;
	
	private ListView		mListView = null ;
	
	private LinearLayout	mFootView = null ;
	private TextView		mPathView = null ;
	
	private DataSetObserver mObserver = null ;
	
	private List<File>		mFileList = new ArrayList<File>();
	private Set<String>		mFileType = new HashSet<String>();
	Context mContext = null;

	private FileReaderDialogCallback mCallback;
	
	public FileReaderDialog(Context context, String rootDir,FileReaderDialogCallback callback)
	{
		super(context, android.R.style.Theme_Light);
		mCallback = callback;
		mContext = context;
		this.mRootDir = rootDir ;
		this.mCurrDir = new File(rootDir) ;
		
		this.mFileType.add("xls") ;
		this.mFileType.add("xlsx") ;
		
		this.mFileType.add("ppt") ;
		this.mFileType.add("pptx") ;
		
		this.mFileType.add("doc") ;
		this.mFileType.add("docx") ;
		
		this.mFileType.add("txt") ;
		this.mFileType.add("log") ;
		
		this.mFileType.add("pdf") ;
		this.mFileType.add("epub") ;
		
		//...
		initDialog(context) ;
	}
	
	private void initDialog(Context context)
	{
		this.setTitle("FileReaderDemo");
		this.mRootView = new LinearLayout(context);
		this.mRootView.setOrientation(LinearLayout.VERTICAL);
		this.setContentView(mRootView);
		
		this.mHeadView = new LinearLayout(context);
		this.mHeadView.setOrientation(LinearLayout.HORIZONTAL);
		this.mHeadView.setBackgroundColor(0x44000000);
		this.mHeadView.setGravity(Gravity.LEFT);
		this.mRootView.addView(this.mHeadView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp2px(50)));
		
		//header
		{
			this.mBackButn = new Button(context) ;
			this.mBackButn.setText("back");
			this.mBackButn.setOnClickListener(this);
			this.mHeadView.addView(this.mBackButn, new LinearLayout.LayoutParams(dp2px(80), LayoutParams.MATCH_PARENT));
		}
		
		this.mListView = new ListView(context);
		this.mListView.setAdapter(this);
		this.mRootView.addView(this.mListView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
		
		this.mFootView = new LinearLayout(context);
		this.mFootView.setOrientation(LinearLayout.HORIZONTAL);
		this.mFootView.setBackgroundColor(0x22000FFF);
		this.mFootView.setGravity(Gravity.LEFT);
		this.mRootView.addView(this.mFootView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp2px(50)));
		
		//footer
		{
			this.mPathView = new TextView(context) ;
			this.mPathView.setText(this.mCurrDir.getAbsolutePath());
			this.mPathView.setTextColor(0xFF000000);
			this.mPathView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
			this.mFootView.addView(this.mPathView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}
	
	
	public void registerDataSetObserver(DataSetObserver observer)
	{
		this.mObserver = observer ;
	}

	public void unregisterDataSetObserver(DataSetObserver observer)
	{}

	public int getCount()
	{
		return this.mFileList.size();
	}

	public Object getItem(int position)
	{
		return this.mFileList.get(position);
	}

	public long getItemId(int position)
	{
		return 0;
	}

	public boolean hasStableIds()
	{
		return false ;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		File fileItem = this.mFileList.get(position) ;
		if(convertView == null)
		{
			FileItemView textView = new FileItemView(parent.getContext()) ;
			textView.setTextColor(0xFF000000);
			textView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
			textView.setPadding(dp2px(5), 0, dp2px(5), 0);
			textView.setLayoutParams(new ListView.LayoutParams(parent.getWidth(), dp2px(40)));
			convertView = textView ;
		}

		FileItemView textView = (FileItemView)convertView ;
		textView.setFileObject(fileItem);
		textView.setText(fileItem.getName());
		textView.setOnClickListener(this);
		
		if(fileItem.isDirectory())
			textView.setBackgroundColor(0x55993300);
		else
			textView.setBackgroundColor(0xFFFFFFFF);
		
		return convertView;
	}

	public int getItemViewType(int position)
	{
		return 0;
	}

	public int getViewTypeCount()
	{
		return 1;
	}

	public boolean isEmpty()
	{
		return this.mFileList.isEmpty();
	}

	public boolean areAllItemsEnabled()
	{
		return false;
	}

	public boolean isEnabled(int position)
	{
		return false;
	}
	
	public void refreshFileList()
	{
		 
		 File[] subFiles = this.mCurrDir.listFiles() ;
		 this.mFileList.clear();
		 for(File subFile : subFiles)
		 {
			 if(!subFile.getName().startsWith(".") && !subFile.isHidden())
				 this.mFileList.add(subFile) ;
		 }
		 Collections.sort(this.mFileList, this);
		 
		 this.mPathView.setText(this.mCurrDir.getAbsolutePath());
		 this.mObserver.onChanged();
	}
	
	public void onClick(View view)
	{
		if(view == this.mBackButn) 
		{
			File parentFile = this.mCurrDir.getParentFile() ;
			if(parentFile.getAbsolutePath().length() >= this.mRootDir.length())
			{
				this.mCurrDir = parentFile ;
				this.refreshFileList() ;
			}
		}
		else if(view instanceof FileItemView) {
			FileItemView fileItem = (FileItemView)view ;
			this.onFileItemClicked(fileItem);
		}
	}
	
	public void onFileItemClicked(FileItemView fileItem) 
	{
		if(fileItem.getFileObject().isDirectory())
		{
			this.mCurrDir = fileItem.getFileObject() ;
			this.refreshFileList() ;
			return ;
		}
		else
		{
			String fileName = fileItem.getFileObject().getName() ;
			if(mCallback != null){
				mCallback.onFileSelect(fileItem.getFileObject().getAbsolutePath(), (FrameLayout)this.getWindow().getDecorView());
			}
		}
	}

	public void onBackPressed()
	{
		boolean ret = true;
		if(mCallback != null)
		{
			ret = mCallback.canBackPress();
		}
		if (ret)
			super.onBackPressed();
	}
	

	public int compare(File lhs, File rhs)
	{
		if(lhs.isDirectory() && !rhs.isDirectory())
			return -1;
		else if(!lhs.isDirectory() && rhs.isDirectory())
			return 1 ;
		else 
			return lhs.getName().compareTo(rhs.getName()) ;
	}

	public int dp2px(float dp) {
		if (mDensity < 0) 
			mDensity = this.getContext().getResources().getDisplayMetrics().density ;
		return (int) (dp * mDensity + 0.5);
	}
	
	
	public static class FileItemView extends TextView 
	{
		public FileItemView(Context context)
		{
			super(context);
		}
		
		public File	 fileObject = null ;

		public File getFileObject()
		{
			return fileObject;
		}

		public void setFileObject(File fileObject)
		{
			this.fileObject = fileObject;
		}
	}

	interface FileReaderDialogCallback{
		void onFileSelect(String fileName, FrameLayout layout);
		boolean canBackPress();
	}
}
