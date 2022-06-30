package com.builderlinebr.qr_codechecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.widget.Toast.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final Executor executor = Executors.newSingleThreadExecutor();
    PreviewView mPreviewView;
    ImageView captureImage;
    public boolean isProcess = false;
    ImageCapture imageCapture;
    TextView textViewWords;
    View scanLine;
    FloatingActionButton shareButton;

    private InterstitialAd interstitialAd;
    int counter = 3;
    int DISPLAY_FREQUENCY = 100; //3


    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                int i = 0;

            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadAd();


        mPreviewView = findViewById(R.id.camera);
        captureImage = findViewById(R.id.captureImg);
        textViewWords = findViewById(R.id.textView2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        scanLine = findViewById(R.id.scan_line);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels / 3;

        new TranslaterAnim(scanLine, 800, -height, height);


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        shareButton = findViewById(R.id.to_share_button);
        shareButton.setOnClickListener(this);
    }

    // ------------ Межстраничная реклама -----------

    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        MainActivity.this.interstitialAd = interstitialAd;
                        //Toast.makeText(MainActivity.this, "OnLoad()", LENGTH_SHORT).show();
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                MainActivity.this.interstitialAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                //Toast.makeText(MainActivity.this, "The ad was shown.", LENGTH_SHORT).show();
                                counter = DISPLAY_FREQUENCY;
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                    }
                });


    }

    private void showInterstitial() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
        }  //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (counter == 0) {
            showInterstitial();
            loadAd();
            counter = DISPLAY_FREQUENCY;
        }

        counter--;


    }

    // ------------ Разрешения ----------------------
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                //makeText(this, "Permissions not granted by the user.", LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    // -------- Запуск камеры --------------------------------------------------
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }, ContextCompat.getMainExecutor(this));
    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), new QRCodeDecoder(this));


        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

    }

    public void photoSave() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        // Результат сохраниения изображения
        imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> makeText(MainActivity.this, getString(R.string.message_photo_save), LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }

        });
    }


    public String getBatchDirectoryName() { // получение пути сохранения изображения
        String app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
//        File dir = new File(app_folder_path);
//        if (!dir.exists() && !dir.mkdirs()) {
//
//        }
        return app_folder_path;
    }


    // ---- Ответ найденного QR-кода
    @SuppressLint("NonConstantResourceId")
    public void qRCodeFindEvent(String qrCodeText) {

        if (qrCodeText.indexOf(getString(R.string.url_official_site)) == 0) {

            Uri page = Uri.parse(qrCodeText);
            Intent intent = new Intent(Intent.ACTION_VIEW, page);
            startActivity(intent);

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isProcess = false;
            }).start();


        } else {

            //photoSave();

            final DialogFragment dialogFragment = new AttentionDialogFragment(qrCodeText);
            FragmentManager fragmentManager = getSupportFragmentManager();
            dialogFragment.show(fragmentManager, "Attention dialog");

            fragmentManager.executePendingTransactions();
            Dialog dialog = dialogFragment.getDialog();
            dialog.setOnCancelListener(dialog1 -> {
                if (AttentionDialogFragment.resultDialog != -1) {
                    switch (AttentionDialogFragment.resultDialog) {
                        case R.id.button_save:
                        case R.id.button_close:

                            break;
                        case R.id.button_go_over:
                            goOver(qrCodeText);
                            break;
                    }
                }
                isProcess = false;
            });


        }
    }


    private void goOver(String link) {

        Uri page = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, page);
        try {
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(this, getString(R.string.not_link_message), LENGTH_LONG).show();
        }
    }


//    public static void showToastCustom(final Context context, String text) {
//        final ViewGroup parent = null;
//        LayoutInflater inflate = LayoutInflater.from(context);
//        View view = inflate.inflate(R.layout.toast_custom_a, parent, false);
//        Toast customToast = new Toast(context);
//        customToast.setView(view);
//        TextView textView = view.findViewById(R.id.toast_textView);
//        textView.setText(context.getString(R.string.attention_mess) + "\n\n" + text);
//        customToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
//        customToast.setDuration(Toast.LENGTH_SHORT);
//        customToast.show();
//    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.to_share_button) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_link));
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Поделиться"));
        }

    }
}


