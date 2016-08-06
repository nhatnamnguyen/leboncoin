package com.nhatnam.android.leboncoin.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.nhatnam.android.leboncoin.LBC_Application;
import com.nhatnam.android.leboncoin.R;

public class LeboncoinDialogFragment extends DialogFragment {
	
	/**
	 * Dialogs ids
	 */
	public static final transient int DIALOG_CATEGORY_ID 			= 0;
	public static final transient int DIALOG_DEPARTEMENT_ID 		= 1;
	public static final transient int DIALOG_MY_ANNOUNCES_ID		= 2;
	public static final transient int DIALOG_ABOUT_ID				= 3;
	public static final transient int DIALOG_TAKEORCHOOSEPHOTO_ID	= 4;
	
	
	public static int mCurrentDialogId;
	private static OnClickListener mListener;
	
	public static LeboncoinDialogFragment newInstance(OnClickListener listener, int dialogId) {
		LeboncoinDialogFragment frag = new LeboncoinDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        mCurrentDialogId = dialogId; 
        frag.setArguments(args);
        mListener = listener;
        
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
    	int dialogId = getArguments().getInt("dialogId");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
        
        switch (dialogId) {
			case DIALOG_CATEGORY_ID :
				dialog.setTitle("Select a category");
				
				//dialog.setMessage("Dialog message");
				String[] choiceItems = new String[LBC_Application.getInstance().getCategoryList().size()];
				int chooseItem = 0;
				for (int i = 0; i < LBC_Application.getInstance().getCategoryList().size(); i++) {
					choiceItems[i] = LBC_Application.getInstance().getCategoryList().get(i).getText();
					if (LBC_Application.getInstance().getCategoryList().get(i).getValue()==LBC_Application.getInstance().getCurrentCategory())
						chooseItem = i;
				}
				dialog.setSingleChoiceItems(choiceItems, chooseItem, mListener);
				break;
			case DIALOG_DEPARTEMENT_ID :
				dialog.setTitle("Select a localisation");
				
				//dialog.setMessage("Dialog message");
				String[] localisationItems = new String[LBC_Application.getInstance().getDepartementList().size()];
				int localisationItem = 0;
				for (int i = 0; i < LBC_Application.getInstance().getDepartementList().size(); i++) {
					localisationItems[i] = LBC_Application.getInstance().getDepartementList().get(i).getText();
					if (LBC_Application.getInstance().getDepartementList().get(i).getValue()==LBC_Application.getInstance().getCurrentDepartement())
						localisationItem = i;
				}
				dialog.setSingleChoiceItems(localisationItems, localisationItem, mListener);
				break;
			case DIALOG_MY_ANNOUNCES_ID :
				dialog.setTitle("Mes annonces en ligne");
				
		    	final View view_dialog = LayoutInflater.from(this.getActivity()).inflate(R.layout.my_announces_online_dialog, null);
		    	// TODO final String email = ((EditText) view_dialog.findViewById(R.id.email)).getText().toString();
		    	dialog.setView(view_dialog);
		    	dialog.setPositiveButton("Rechercher", mListener);
		    	dialog.setNegativeButton("Annuler", null);
		    	break;
			case DIALOG_TAKEORCHOOSEPHOTO_ID :
				dialog.setTitle("Insérer une photo");
				dialog.setItems(R.array.take_choose_photo_arrays, mListener);
				break;
			case DIALOG_ABOUT_ID :
				dialog.setTitle("A propos");
				dialog.setMessage("Leboncoin Android.\nnguyennhatnam2010@gmail.com");
				dialog.setPositiveButton("OK", null);
				break;
			default :
				break;
		}
        
        return dialog.create();
    }
}
