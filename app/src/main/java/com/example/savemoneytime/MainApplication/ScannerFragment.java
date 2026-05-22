package com.example.savemoneytime.MainApplication;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.savemoneytime.MainApplication.ViewModels.BudgetViewModel;
import com.example.savemoneytime.R;
import com.example.savemoneytime.model.ExpenseEntity;
import com.example.savemoneytime.network.GeminiService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private PreviewView     cameraPreview;
    private FrameLayout     btnCapture;
    private TextView        btnCancelScan, btnFlash, tvProcessingStatus;
    private LinearLayout    processingOverlay;
    private View            scanLine;

    private ImageCapture    imageCapture;
    private Camera          camera;
    private boolean         flashEnabled = false;
    private ExecutorService cameraExecutor;
    private BudgetViewModel viewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission required to scan receipts", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        viewModel         = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        cameraPreview     = view.findViewById(R.id.camera_preview);
        btnCapture        = view.findViewById(R.id.btn_capture);
        btnCancelScan     = view.findViewById(R.id.btn_cancel_scan);
        btnFlash          = view.findViewById(R.id.btn_flash);
        processingOverlay = view.findViewById(R.id.processing_overlay);
        tvProcessingStatus = view.findViewById(R.id.tv_processing_status);
        scanLine          = view.findViewById(R.id.scan_line);

        cameraExecutor = Executors.newSingleThreadExecutor();

        setupButtons();
        animateScanLine();
        checkCameraPermission();

        return view;
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Toast.makeText(requireContext(), "Camera init failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void setupButtons() {
        btnCapture.setOnClickListener(v -> captureAndScan());

        // 🔥 ĐÃ CẬP NHẬT: Nhấn Hủy quét sẽ trả sếp về đúng màn hình Chatbot thông qua MainActivity
        btnCancelScan.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).returnToChatFromScanner();
            }
        });

        btnFlash.setOnClickListener(v -> {
            flashEnabled = !flashEnabled;
            if (camera != null) {
                camera.getCameraControl().enableTorch(flashEnabled);
            }
            btnFlash.setText(flashEnabled ? "⚡ On" : "⚡ Flash");
            btnFlash.setTextColor(flashEnabled ? 0xFFD4AF37 : 0xFF9CA3AF);
        });
    }

    private void captureAndScan() {
        if (imageCapture == null) return;

        showProcessing("Capturing receipt...");

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        showProcessing("Reading text from receipt...");
                        processImageWithMLKit(imageProxy);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        hideProcessing();
                        Toast.makeText(requireContext(), "Capture failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void processImageWithMLKit(ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    imageProxy.close();
                    String extractedText = visionText.getText();

                    if (extractedText.trim().isEmpty()) {
                        hideProcessing();
                        Toast.makeText(requireContext(), "No text found. Try better lighting.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showProcessing("AI is analyzing the receipt...");
                    sendToAI(extractedText);
                })
                .addOnFailureListener(e -> {
                    imageProxy.close();
                    hideProcessing();
                    Toast.makeText(requireContext(), "Text recognition failed. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendToAI(String rawText) {
        String prompt = "Receipt scan text: \"" + rawText.replace("\"", "'") + "\"\n\n"
                + "Extract the TOTAL amount from this receipt text.\n"
                + "Return ONLY this JSON: "
                + "{\"amount\": TOTAL_IN_VND, \"category\": \"BEST_CATEGORY\", "
                + "\"note\": \"SHORT_DESCRIPTION\", \"type\": \"EXPENSE\"}\n"
                + "If you cannot find a total amount, return: "
                + "{\"intent\": \"error\", \"message\": \"Could not read receipt total\"}";

        GeminiService.getInstance().parseTransaction(prompt, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    hideProcessing();
                    handleScanResult(response, rawText);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    hideProcessing();
                    Toast.makeText(requireContext(), "AI unavailable. Try manual entry.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void handleScanResult(String jsonResponse, String rawText) {
        try {
            String cleaned = jsonResponse.replace("```json", "").replace("```", "").trim();
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(cleaned).getAsJsonObject();

            if (json.has("intent")) {
                Toast.makeText(requireContext(), "Could not read total. Please enter manually.", Toast.LENGTH_LONG).show();
                return;
            }

            long   amount   = json.get("amount").getAsLong();
            String category = json.get("category").getAsString();
            String note     = json.has("note") ? json.get("note").getAsString() : "Receipt";

            showConfirmDialog(amount, category, note);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Parse error. Amount: try manual entry.", Toast.LENGTH_LONG).show();
        }
    }

    private void showConfirmDialog(long amount, String category, String note) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("📄 Receipt Scanned!")
                .setMessage("Amount: " + String.format("%,d", amount) + " VND"
                        + "\nCategory: " + category
                        + "\nNote: " + note
                        + "\n\nSave this expense?")
                .setPositiveButton("✅ Save", (dialog, which) -> {
                    ExpenseEntity expense = new ExpenseEntity(note, amount, category, Calendar.getInstance().getTimeInMillis(), "Scanned receipt");
                    viewModel.saveExpense(expense);
                    Toast.makeText(requireContext(), "✅ Expense saved from receipt!", Toast.LENGTH_SHORT).show();

                    // 🔥 ĐÃ CẬP NHẬT: Lưu xong sẽ nhảy hẳn về màn hình Home cập nhật dòng tiền tức thì
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).returnToHomeFromScanner();
                    }
                })
                .setNeutralButton("✏️ Edit", (dialog, which) -> {
                    // 🔥 ĐÃ CẬP NHẬT: Nhấn Sửa sẽ đưa sếp về lại màn hình AI Chatbot để bổ sung thông tin
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).returnToChatFromScanner();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void animateScanLine() {
        if (scanLine == null) return;

        scanLine.post(() -> {
            View frame = requireView().findViewById(R.id.scan_frame);
            if (frame == null) return;

            int frameHeight = frame.getHeight();

            ObjectAnimator animator = ObjectAnimator.ofFloat(scanLine, "translationY", 0, frameHeight - 4);
            animator.setDuration(1800);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.start();
        });
    }

    private void showProcessing(String message) {
        tvProcessingStatus.setText(message);
        processingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideProcessing() {
        processingOverlay.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}