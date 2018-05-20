package fr.gallyoko.app.fastart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.content.Intent;
import android.appwidget.AppWidgetManager;

import java.util.ArrayList;

import fr.gallyoko.app.fastart.bdd.entity.WidgetTypeEntity;
import fr.gallyoko.app.fastart.bdd.repository.WidgetTypeRepository;

public class DialogActivity extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED);
            finish();
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        displayDialog();
    }

    private void displayDialog() {
        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        AlertDialog.Builder builder = new AlertDialog.Builder(getDialogContext());

        builder.setTitle("Choose an type");

        WidgetTypeRepository widgetTypeRepository = new WidgetTypeRepository(getDialogContext());
        widgetTypeRepository.open();
        if (widgetTypeRepository.getByName("button") == null) {
            WidgetTypeEntity widgetTypeEntity1 = new WidgetTypeEntity("button");
            widgetTypeRepository.insert(widgetTypeEntity1);
        }
        if (widgetTypeRepository.getByName("toggle") == null) {
            WidgetTypeEntity widgetTypeEntity2 = new WidgetTypeEntity("toggle");
            widgetTypeRepository.insert(widgetTypeEntity2);
        }
        final ArrayList<WidgetTypeEntity> widgetTypes = widgetTypeRepository.getAll();
        widgetTypeRepository.close();

        String[] items = new String[widgetTypes.size()];
        int index = 0;
        for (WidgetTypeEntity widgetType: widgetTypes) {
            items[index] = widgetType.getName();
            index ++;
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getDialogContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("type_" + mAppWidgetId, widgetTypes.get(which).getName());
                editor.commit();
                setResult(RESULT_OK, resultValue);
                Intent refreshIntent = new Intent(getDialogContext(), AppWidget.class);
                refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                sendBroadcast(refreshIntent);
                dialog.dismiss();
                finish();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED, resultValue);
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Context getDialogContext() {
        final Context context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light);
        } else {
            context = new ContextThemeWrapper(this, android.R.style.Theme_Dialog);
        }

        return context;
    }
}
