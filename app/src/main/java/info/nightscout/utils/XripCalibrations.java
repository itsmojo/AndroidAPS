package info.nightscout.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.Services.Intents;
import info.nightscout.client.data.NSProfile;

/**
 * Created by mike on 10.02.2017.
 */

public class XripCalibrations {
    private static Logger log = LoggerFactory.getLogger(XripCalibrations.class);

    public static void confirmAndSendCalibration(final Double bg, Context parentContext) {
        if (parentContext != null) {
            String confirmMessage = String.format(MainApp.sResources.getString(R.string.send_calibration), bg);

            AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
            builder.setTitle(MainApp.sResources.getString(R.string.confirmation));
            builder.setMessage(confirmMessage);
            builder.setPositiveButton(MainApp.sResources.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendIntent(bg);
                }
            });
            builder.setNegativeButton(MainApp.sResources.getString(R.string.cancel), null);
            builder.show();
        }
    }

    private static boolean sendIntent(Double bg) {
        final NSProfile profile = MainApp.getConfigBuilder().getActiveProfile().getProfile();

        Context context = MainApp.instance().getApplicationContext();
        Bundle bundle = new Bundle();
        bundle.putDouble("glucose_number", bg);
        bundle.putString("units", profile.getUnits().equals(Constants.MGDL) ? "mgdl" : "mmol");
        bundle.putLong("timestamp", new Date().getTime());
        Intent intent = new Intent(Intents.ACTION_REMOTE_CALIBRATION);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
        List<ResolveInfo> q = MainApp.instance().getApplicationContext().getPackageManager().queryBroadcastReceivers(intent, 0);
        if (q.size() < 1) {
            ToastUtils.showToastInUiThread(MainApp.instance().getApplicationContext(), MainApp.sResources.getString(R.string.xdripnotinstalled));
            log.debug(MainApp.sResources.getString(R.string.xdripnotinstalled));
            return false;
        } else {
            ToastUtils.showToastInUiThread(MainApp.instance().getApplicationContext(), MainApp.sResources.getString(R.string.calibrationsent));
            return true;
        }
    }
}