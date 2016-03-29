package com.envyserve.githubreference;

import android.content.Context;
import android.support.v4.app.Fragment;

public class NavItem {
	public static int ITEM = 1;
	public static int SECTION = 2;
	public static int EXTRA = 3;
	public static int TOP = 4;
	
    private String mText;
    private int mTextResource;
    private int mDrawableResource;
    private String[] mData;
    private int mType;
    private boolean mRequirePurchase;
    private Class<? extends Fragment> mFragment;

    //Create a new item with a resource string, resource drawable, type, fragment and data
    public NavItem(int text, int drawable, int type, Class<? extends Fragment> fragment, String[] data) {
        this(null, drawable, type, fragment, data, false);
        mTextResource = text;
    }

    //Create a new item with a resource string, resource drawable, type, fragment, data and purchase requirement
    public NavItem(int text, int drawable, int type, Class<? extends Fragment> fragment, String[] data, boolean mRequirePurchase) {
        this(null, drawable, type, fragment, data, mRequirePurchase);
    }

    //Create a new item with a text string, resource drawable, type, fragment and data
    public NavItem(String text, int drawable, int type, Class<? extends Fragment> fragment, String[] data) {
        this(text, drawable, type, fragment, data, false);
    }

    //Create a new item with a text string, resource drawable, type, fragment, data and purchase requirement
    public NavItem(String text, int drawable, int type, Class<? extends Fragment> fragment, String[] data, boolean requiresPurchase) {
        mText = text;
        mDrawableResource = drawable;
        mFragment = fragment;
        mData = data;
        mType = type;
        mRequirePurchase = requiresPurchase;
    }

    //Create a new item with a text string and type; typically a section
    public NavItem(String text, int type) {
    	mText = text;
        mType = type;
        mRequirePurchase = false;
    }

    public String getText(Context c) {
        if (mText != null) {
            return mText;
        } else {
            return c.getResources().getString(mTextResource);
        }
    }

    public int getDrawable() {
        return mDrawableResource;
    }
    
    public Class<? extends Fragment> getFragment() {
    	return mFragment;
    }
    
    public String[] getData() {
    	return mData;
    }
    
    public int getType(){
    	return mType;
    }
    
    public boolean requiresPurchase(){
    	return mRequirePurchase;
    }
}
