package com.opentouchgaming.androidcore.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.ScopedStorage;
import com.opentouchgaming.saffal.UtilsSAF;

import java.util.function.Function;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;


public class ScopedStorageDialog {

    static DebugLog log;

    static {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "ScopedStorageDialog");
    }

    Activity activity;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ScopedStorageDialog(final Activity activity, Runnable update) {
        this.activity = activity;

        final Dialog dialog = new Dialog(activity);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_scoped_storage);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);


        Button goButton = dialog.findViewById(R.id.go_button);

        goButton.setOnClickListener(v -> {
            UtilsSAF.openDocumentTree(activity, ScopedStorage.OPENDOCUMENT_TREE_RESULT);
        });

        dialog.setOnDismissListener(dialog1 -> update.run());

        dialog.show();


        ScopedStorage.openDocumentCallback = new Function<String, Void>() {
            @Override
            public Void apply(String error) {
                if(error != null) {
                    log.log(D, "ERROR: " + error);
                    AppInfo.setAppSecDirectory(null);
                }
                else
                {
                    AppInfo.setAppSecDirectory(UtilsSAF.getTreeRoot().rootPath);
                }
                return null;
            }
        };
    }

    public void dismiss() {
        //Override me
    }

}
