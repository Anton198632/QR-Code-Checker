package com.builderlinebr.qr_codechecker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class QRCodeDecoder implements ImageAnalysis.Analyzer {
    BarcodeScannerOptions options;
    BarcodeScanner scanner;
    Context context;


    public QRCodeDecoder(Context context) {
        this.context = context;
        options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
        Image mediaImage = imageProxy.getImage();
        mediaImage.setCropRect(new Rect());
        if (mediaImage != null) {
            int rotationDeg = imageProxy.getImageInfo().getRotationDegrees();

            InputImage image = InputImage.fromMediaImage(mediaImage, rotationDeg);

            Task<List<Barcode>> result = scanner.process(image);
            result.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(@NonNull List<Barcode> barcodes) {
                    if (barcodes.size() > 0) {

                        Barcode.UrlBookmark urlBookmark = barcodes.get(0).getUrl();
                        String url = null;
                        try {
                            url = urlBookmark.getUrl();
                        } catch (Exception ex) {
                            url = barcodes.get(0).getDisplayValue();
                        }

                        if (!((MainActivity) context).isProcess && url != null) {
                            ((MainActivity) context).isProcess = true;
                            ((MainActivity) context).qRCodeFindEvent(url);
                        }
                    }
                    imageProxy.close();
                }
            });
            result.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    imageProxy.close();
                }
            });
        }
    }
}
