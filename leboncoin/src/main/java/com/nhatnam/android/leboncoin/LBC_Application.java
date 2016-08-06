package com.nhatnam.android.leboncoin;

import java.util.ArrayList;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;

import com.nhatnam.android.leboncoin.parsers.CategoryDepartement;
import com.nhatnam.android.leboncoin.parsers.Criteria;
import com.nhatnam.android.leboncoin.utils.LeboncoinConstant;
import com.nhatnam.android.leboncoin.utils.PreviewImageManager;

public class LBC_Application extends Application {
    
	private static LBC_Application                        sInstance;
    
	private PreviewImageManager mImageManager;
    
    private boolean isProcessInBackground;
    
    private String mCurrentRequestOffresUri = "http://mobile.leboncoin.fr/li?th=2";
    private String mCurrentRequestCriteriaUri = "http://mobile.leboncoin.fr/criterias.html?th=2";
	private int mCurrentPageNumber 						= 1;
	private int mCurrentPageTotal						= 1;
	private int mCurrentCategory 						= 0;
	private int mCurrentRegion 							= 0;
	private int mCurrentDepartement 					= 0;
	private String mCurrentSearchKey 					= "";
	//Particuliers AND Professionnel: f = "a" , "p" , "c"
	private String mCurrentParticulierOrProfessionel 	= "a";
	private boolean mCurrentSearchInTitle 				= false;
	private boolean mCurrentSearchUrgent 				= false;
	private String mCurrentSearchZipCode 				= "";
	private Criteria mCurrentCriteria					= null;
	
	private ArrayList<CategoryDepartement> mCategoryList = null, mDepartementList = null;
	
	
    /**
     * Init the unique LBC_Application instance. These are the very first lines of our code that will run.
     */
    public LBC_Application()
    {
        sInstance = this;
    }
    
	@Override
	public void onCreate() {
		
		super.onCreate();
		
	}
	
	public int getCurrentPageNumber() {
		return this.mCurrentPageNumber;
	}
	
	public int getNextPageNumber() {
		return ++this.mCurrentPageNumber;
	}
	
	public void resetCurrentPageNumber() {
		this.mCurrentPageNumber = 1;
	}
	
	public int getCurrentPageTotal() {
		return this.mCurrentPageTotal; 
	}
	
	public void setCurrentPageTotal(final int pageTotal) {
		this.mCurrentPageTotal = pageTotal;
	}
	
	public int getCurrentCategory() {
		return this.mCurrentCategory; 
	}
	
	public void setCurrentCategory(final int category, final boolean saveToPreferences) {
		this.mCurrentCategory = category;
		
		if (saveToPreferences) {
			//Save to sharedPreferences
			SharedPreferences sharedPreferences = this.getSharedPreferences(LeboncoinConstant.PREF_SESSION, Context.MODE_PRIVATE);
	        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
	        prefsEditor.putInt(LeboncoinConstant.PREF_CATEGORY, this.mCurrentCategory);
	        prefsEditor.commit();
		}
	}
	
	public ArrayList<CategoryDepartement> getCategoryList() {
		return mCategoryList;
	}

	public void setCategoryList(ArrayList<CategoryDepartement> categoryList) {
		this.mCategoryList = categoryList;
	}
	
	public ArrayList<CategoryDepartement> getDepartementList() {
		return mDepartementList;
	}

	public void setDepartementList(ArrayList<CategoryDepartement> departementList) {
		this.mDepartementList = departementList;
	}
	
	public void resetDepartementList() {
		this.mDepartementList = null; 
	}

	public int getCurrentDepartement() {
		return this.mCurrentDepartement; 
	}
	
	public void setCurrentDepartement(final int departement, boolean saveToPreferences) {
		this.mCurrentDepartement = departement;
		
		if (saveToPreferences) {
			//Save to sharedPreferences
			SharedPreferences sharedPreferences = this.getSharedPreferences(LeboncoinConstant.PREF_SESSION, Context.MODE_PRIVATE);
	        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
	        prefsEditor.putInt(LeboncoinConstant.PREF_DEPARTEMENT, this.mCurrentDepartement);
	        prefsEditor.commit();
		}
	}
	
	public int getCurrentRegion() {
		return this.mCurrentRegion; 
	}
	
	public void setCurrentRegion(final int region, boolean saveToPreferences) {
		this.mCurrentRegion = region;
		
		if (saveToPreferences) {
			//Save to sharedPreferences
			SharedPreferences sharedPreferences = this.getSharedPreferences(LeboncoinConstant.PREF_SESSION, Context.MODE_PRIVATE);
	        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
	        prefsEditor.putInt(LeboncoinConstant.PREF_REGION, this.mCurrentRegion);
	        prefsEditor.commit();
		}
	}
	
	public String getCurrentSearchKey() {
		return this.mCurrentSearchKey; 
	}
	
	public void setCurrentSearchKey(final String searchKey) {
		this.mCurrentSearchKey = searchKey;
	}
	
	public void setCurrentParticulierOrProfessionel(final String particulierOrProfessionel) {
		this.mCurrentParticulierOrProfessionel = particulierOrProfessionel;
	}
	
	public String getCurrentParticulierOrProfessionel() {
		return this.mCurrentParticulierOrProfessionel; 
	}
	
	public boolean getCurrentSearchInTitle() {
		return this.mCurrentSearchInTitle; 
	}
	
	public void setCurrentSearchInTitle(final boolean searchInTitle) {
		this.mCurrentSearchInTitle = searchInTitle;
	}
	
	public boolean getCurrentSearchUrgent() {
		return this.mCurrentSearchUrgent; 
	}
	
	public void setCurrentSearchUrgent(final boolean searchUrgent) {
		this.mCurrentSearchUrgent = searchUrgent;
	}
	
	public String getCurrentSearchZipCode() {
		return this.mCurrentSearchZipCode; 
	}
	
	public void setCurrentSearchZipCode(final String searchZipCode) {
		this.mCurrentSearchZipCode = searchZipCode;
	}
	
	public Criteria getCurrentCriteria() {
		return this.mCurrentCriteria; 
	}
	
	public void setCurrentCriteria(final Criteria currentCriteria) {
		this.mCurrentCriteria = currentCriteria;
	}
	
	
	
	
	
	public String getCurrentRequestOffresUri() {
		
		String appendSearchZipCode = "";
		if (!TextUtils.isEmpty(this.getCurrentSearchZipCode())) {
			appendSearchZipCode = "&zz=" + this.mCurrentSearchZipCode;
		}
		
		String headerDepartement = "";
		if (this.getCurrentDepartement() > 0)
			headerDepartement = "&w=" + String.valueOf(this.getCurrentDepartement());
		
		return this.mCurrentRequestOffresUri
				/*Region*/   							+ "&ca=" + String.valueOf(this.getCurrentRegion()) + "_s"
				/*Departement*/							+ headerDepartement
				/*Category*/							+ "&c=" + String.valueOf(this.getCurrentCategory())
				/*SearchKey*/							+ "&q=" + this.getCurrentSearchKey().replace(" ", "+")
				/*SearchInTitle*/						+ (this.getCurrentSearchInTitle()?	"&it=1":"")
				/*SearchUrgent*/						+ (this.getCurrentSearchUrgent()?	"&ur=1":"")
				/*SearchZipCode*/						+ appendSearchZipCode
				/*Particulier or Professionnels*/		+ "&f=" + this.getCurrentParticulierOrProfessionel();
	}
	
	public void setCurrentRequestOffresUri(final String requestUri) {
		this.mCurrentRequestOffresUri = requestUri;
	}
	
	
	
public String getCurrentRequestCriteriasUri() {
	
		String headerDepartement = "";
		if (this.getCurrentDepartement() > 0)
			headerDepartement = "&w=" + String.valueOf(this.getCurrentDepartement());
		
		return this.mCurrentRequestCriteriaUri
				/*Region*/   							+ "&ca=" + String.valueOf(this.getCurrentRegion()) + "_s"
				/*Departement*/							+ headerDepartement
				/*Category*/							+ "&c=" + String.valueOf(this.getCurrentCategory())
				/*SearchKey*/							+ "&q="
				/*Particulier or Professionnels*/		+ "&f=a";
	}
	
	 /**
     * @return the isProcessInBackground
     */
    public boolean isProcessInBackground()
    {
        return this.isProcessInBackground;
    }
    
    /**
     * @return the unique LBC_Application instance
     */
    public static LBC_Application getInstance()
    {
        return sInstance;
    }
    
    @Override
    public Object getSystemService(final String name)
    {

        if (PreviewImageManager.PREVIEW_IMAGE_SERVICE.equals(name))
        {
            if (this.mImageManager == null)
            {
                this.mImageManager = PreviewImageManager.createPreviewImageManager(this);
            }
            return this.mImageManager;
        }

        return super.getSystemService(name);
    }
    
    /**
     * @see Context#getResources()
     */
    public static Resources getR()
    {
        return sInstance.getResources();
    }

    /**
     * @see Context#getContentResolver()
     */
    public static ContentResolver getResolver()
    {
        return sInstance.getContentResolver();
    }

    /**
     * @see Context#getString(int)
     */
    public static String getStr(final int resId)
    {
        return sInstance.getString(resId);
    }
}