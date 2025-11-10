package com.miplata.nlp;

import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier;

import java.io.IOException;
import java.util.List;

public class TransactionClassifier {

    private static final String TAG = "TransactionClassifier";
    private static final String MODEL_NAME = "finances_model_custom.tflite";

    private NLClassifier nlClassifier;

    public static TransactionClassifier create(Context context) {
        try {
            return new TransactionClassifier(context);
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar el TransactionClassifier.", e);
            return null;
        }
    }

    // El constructor ahora declara que puede lanzar una IOException.
    private TransactionClassifier(Context context) throws IOException {
        BaseOptions baseOptions = BaseOptions.builder().setNumThreads(4).build();

        NLClassifier.NLClassifierOptions options = NLClassifier.NLClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .build();

        nlClassifier = NLClassifier.createFromFileAndOptions(context, MODEL_NAME, options);

        Log.d(TAG, "Clasificador de IA creado e inicializado con éxito.");
    }

    public String[] classify(String text) {
        if (nlClassifier == null) {
            return new String[]{"UNKNOWN", "0.0"};
        }

        List<Category> results = nlClassifier.classify(text);

        if (results != null && !results.isEmpty()) {
            Category topResult = results.get(0);
            String rawLabel = topResult.getLabel();

            String finalLabel = "UNKNOWN";
            if (rawLabel.equalsIgnoreCase("gasto")) {
                finalLabel = "DEBIT";
            } else if (rawLabel.equalsIgnoreCase("ingreso")) {
                finalLabel = "CREDIT";
            }

            return new String[]{finalLabel, String.valueOf(topResult.getScore())};
        }

        return new String[]{"UNKNOWN", "0.0"};
    }

    public void close() {
        if (nlClassifier != null) {
            nlClassifier.close();
            Log.d(TAG, "Clasificador de IA cerrado.");
        }
    }
}
