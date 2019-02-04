package com.seu.magiccamera.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import java.util.Arrays;
import java.util.List;

public class RationaleDialogFragment extends DialogFragment {
    public static final String TAG = "RationaleDialogFragment";

    private static final String ARG_POSITIVE_BUTTON = "positiveButton";
    private static final String ARG_NEGATIVE_BUTTON = "negativeButton";
    private static final String ARG_RATIONALE_MESSAGE = "rationaleMsg";
    private static final String ARG_REQUEST_CODE = "requestCode";
    private static final String ARG_PERMISSIONS = "permissions";

    private int positiveButton;
    private int negativeButton;
    private String rationaleMsg;
    private int requestCode;
    private String[] permissions;
    private PermissionCallbacks mPermissionCallbacks;

    public RationaleDialogFragment() {

    }

    public static RationaleDialogFragment newInstance(
            @StringRes int positiveButton, @StringRes int negativeButton,
            @NonNull String rationaleMsg, int requestCode, @NonNull String[] permissions) {

        RationaleDialogFragment fragment = new RationaleDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITIVE_BUTTON, positiveButton);
        args.putInt(ARG_NEGATIVE_BUTTON, negativeButton);
        args.putString(ARG_RATIONALE_MESSAGE, rationaleMsg);
        args.putInt(ARG_REQUEST_CODE, requestCode);
        args.putStringArray(ARG_PERMISSIONS, permissions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        boolean isAtLeastJellyBeanMR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

        if (isAtLeastJellyBeanMR1
                && getParentFragment() != null
                && getParentFragment() instanceof PermissionCallbacks) {
            mPermissionCallbacks = (PermissionCallbacks) getParentFragment();
        } else if (context instanceof PermissionCallbacks) {
            mPermissionCallbacks = (PermissionCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PermissionCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPermissionCallbacks = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            positiveButton = getArguments().getInt(ARG_POSITIVE_BUTTON);
            negativeButton = getArguments().getInt(ARG_NEGATIVE_BUTTON);
            rationaleMsg = getArguments().getString(ARG_RATIONALE_MESSAGE);
            requestCode = getArguments().getInt(ARG_REQUEST_CODE);
            permissions = getArguments().getStringArray(ARG_PERMISSIONS);
        }

        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Object host;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            host = getParentFragment() != null ?
                                    getParentFragment() :
                                    getActivity();
                        } else {
                            host = getActivity();
                        }
                        if (host instanceof Fragment) {
                            ((Fragment) host).requestPermissions(permissions, requestCode);
                        } else if (host instanceof FragmentActivity) {
                            ActivityCompat.requestPermissions(
                                    (FragmentActivity) host, permissions, requestCode);
                        }
                    }
                })
                .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mPermissionCallbacks != null) {
                            mPermissionCallbacks.onPermissionsDenied(requestCode,
                                    Arrays.asList(permissions));
                        }
                    }
                })
                .setMessage(rationaleMsg);
        return builder.create();
    }

    public interface PermissionCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

    }

}
