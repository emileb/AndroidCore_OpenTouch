package com.opentouchgaming.androidcore;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;

import com.opentouchgaming.saffal.UtilsSAF;

import java.util.function.Function;

public class ScopedStorage {

    static DebugLog log;

    static {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "ScopedStorage");
    }


    public static final int OPENDOCUMENT_TREE_RESULT = 5005;


    // The string will be null on OK, or contain an error message
    public static Function<String, Void> openDocumentCallback;

    // Check if storage is setup, true if OK
    static public boolean checkStorageOK(Activity activity) {

        // Always set context, this also loads the saffal library, needed if even not used
        UtilsSAF.setContext(activity);

        if (AppInfo.isScoped() == false) {
            //Thank fuck scoped storage not necessary
            return true;
        } else {
            UtilsSAF.loadTreeRoot(activity);
            /*
            if( !UtilsSAF.ready())
            {
                UtilsSAF.openDocumentTree(activity, OPENDOCUMENT_TREE_RESULT);
            }
*/
            // Check if the first time are checking this
            if (AppSettings.getBoolOption(activity, "storage_checked", false) == false) {
                // First reset the primary folder, do this in call cases even an upgrade
                // Example will be set to: /Android/data/com.opentouchgaming.deltatouch/files
                AppInfo.setAppDirectory(AppInfo.getDefaultAppDirectory());
                AppSettings.setBoolOption(activity, "storage_checked", true);
            }

            return false;
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    static public void activityResult(Activity activity, int requestCode, int resultCode, Intent data) {

        if (requestCode == OPENDOCUMENT_TREE_RESULT ) {
            if(resultCode == Activity.RESULT_OK)
            {
                Uri treeUri = data.getData();

                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));
                String rootDocumentId = DocumentsContract.getDocumentId(docUri);

                log.log(DebugLog.Level.D, "rootDocumentId = " + rootDocumentId);
                log.log(DebugLog.Level.D, "getAuthority = " + treeUri.getAuthority());

                if(treeUri.getAuthority().contentEquals("com.android.externalstorage.documents") == false)
                {
                    openDocumentCallback.apply("Did not choose a valid folder.");
                    return;
                }

                String root;

                // Work out if internal memory or SD card was selected
                if(rootDocumentId != null)
                {
                    String[] split = rootDocumentId.split(":", -1);

                    if(split[0].contentEquals("primary"))
                        root = "/[internal]";
                    else
                        root = "/[SD-Card]";

                    if(split.length > 1 && split[1].length() > 0)
                    {
                        root = root + "/" + split[1];
                    }
                }
                else
                {
                    openDocumentCallback.apply("Return path is null.");
                    return;
                }

                // Setup the UtilsSAF static data
                UtilsSAF.setTreeRoot(new UtilsSAF.TreeRoot(treeUri, root, rootDocumentId));

                // Save URI for next launch
                UtilsSAF.saveTreeRoot(activity);

                // Take permissions for ever
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                openDocumentCallback.apply(null);
            }
            else
            {
                openDocumentCallback.apply("Folder selection canceled");
            }
        }
    }


}
