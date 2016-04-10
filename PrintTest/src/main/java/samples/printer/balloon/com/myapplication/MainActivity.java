package samples.printer.balloon.com.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sec.android.ngen.common.lib.ssp.CapabilitiesExceededException;
import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributes;
import com.sec.android.ngen.common.lib.ssp.printer.PrintAttributesCaps;
import com.sec.android.ngen.common.lib.ssp.printer.PrinterService;
import com.sec.android.ngen.common.lib.ssp.printer.PrintletAttributes;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PrintTest";
    private static String FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnPrint = (Button) findViewById(R.id.btnPrint);

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executePrint();            }
        });


    }
    private void executePrint() {

        new PrintAsyncTask(getApplicationContext()).execute();
    }
    private static PrintAttributesCaps requestCaps(final Context context) {
        final Result result = new Result();
        final PrintAttributesCaps caps = PrinterService.getCapabilities(context, result);

        if (caps != null) {
            Log.d(TAG, "Received Caps as:" +
                    "AutoFit: " + caps.getAutoFitList() +
                    ", ColorMode: " + caps.getColorModeList() +
                    ", Max Copies: " + caps.getMaxCopies() +
                    ", Duplex: " + caps.getDuplexList());
        }

        return caps;
    }
    private static final class PrintAsyncTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;
        private final SharedPreferences mPrefs;

        PrintAsyncTask(final Context context) {
            mContext = context;
            mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                final PrintAttributesCaps caps = requestCaps(mContext);
                PrintAttributes.ColorMode cm = PrintAttributes.ColorMode.AUTO;
                PrintAttributes.Duplex duplex = PrintAttributes.Duplex.DEFAULT;
                PrintAttributes.AutoFit af = PrintAttributes.AutoFit.TRUE;
                int copies = 1;
                Result result = new Result();

                PrintAttributes attributes;
                PrintletAttributes taskAttribs = new PrintletAttributes.Builder()
                        .setShowSettingsUi(false)
                        .build();
                attributes = new PrintAttributes.PrintFromStorageBuilder(Uri.fromFile(new File(FILE_PATH + "/1.pdf")))
                        .setColorMode(cm)
                        .setDuplex(duplex)
                        .setAutoFit(af)
                        .setCopies(copies)
                        .build(caps);
                PrinterService.submit(mContext, attributes, taskAttribs);
            } catch (CapabilitiesExceededException e) {
                e.printStackTrace();
            }


            return null;
        }
    }
}
