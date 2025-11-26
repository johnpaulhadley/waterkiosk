package com.smartbin.yolo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import com.smartbin.DetectionResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper around YOLOv8 ONNX inference and simple webcam streaming.
 */
public class YoloBridge implements AutoCloseable {
    private static final Size INPUT_SIZE = new Size(640, 640);
    private static final Scalar BLOB_MEAN = new Scalar(0, 0, 0);
    private static final double SCALE = 1.0 / 255.0;

    // Static initializer to load OpenCV native library
    static {
        try {
            // First try to load using the OpenPNP loader
            nu.pattern.OpenCV.loadLocally();
            System.out.println("[YoloBridge] OpenCV loaded successfully using OpenPNP loader");
        } catch (Exception e) {
            try {
                // Fallback to standard OpenCV loader
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                System.out.println("[YoloBridge] OpenCV loaded successfully using Core.NATIVE_LIBRARY_NAME");
            } catch (UnsatisfiedLinkError err) {
                System.err.println("[YoloBridge] Failed to load OpenCV native library");
                System.err.println("Error: " + err.getMessage());
                throw new RuntimeException("OpenCV native library not found. Please ensure OpenCV is properly installed.", err);
            }
        }
    }

    private final Net net;
    private final VideoCapture camera;
    private final List<String> labels;

    private double confidenceThreshold = 0.4;
    private final double nmsThreshold = 0.45;
    private boolean loggedOutputShape = false;
    private final boolean debugMode = "1".equals(System.getenv("SMARTBIN_DEBUG"));
    private boolean loggedSample = false;

    public YoloBridge(String modelPath) {
        this(modelPath, 0);
    }

    public YoloBridge(String modelPath, int cameraIndex) {
        File file = new File(modelPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Model file not found at: " + file.getAbsolutePath());
        }
        this.net = Dnn.readNetFromONNX(file.getAbsolutePath());
        this.camera = new VideoCapture(cameraIndex);
        this.camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        this.camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        this.labels = Collections.singletonList("bottle");
    }

    public boolean isCameraOpen() {
        return camera.isOpened();
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = Math.max(0.0, Math.min(1.0, confidenceThreshold));
    }

    public boolean readFrame(Mat frame) {
        return camera.read(frame);
    }

    public List<DetectionResult> detect(Mat frame) {
        if (frame == null || frame.empty()) {
            return Collections.emptyList();
        }

        Mat blob = Dnn.blobFromImage(frame, SCALE, INPUT_SIZE, BLOB_MEAN, true, false);
        net.setInput(blob);
        Mat rawOutput = net.forward();
        blob.release();

        if (!loggedOutputShape) {
            System.out.printf("YOLO output shape: [%d, %d, %d]%n",
                    (int) rawOutput.size(0),
                    (int) rawOutput.size(1),
                    (int) rawOutput.size(2));
            loggedOutputShape = true;
        }

        List<DetectionResult> detections = parseDetections(frame, rawOutput);
        rawOutput.release();
        return detections;
    }

    private List<DetectionResult> parseDetections(Mat frame, Mat output) {
        if (output.empty()) {
            return Collections.emptyList();
        }

        int candidates = (int) output.size(2);
        int channels = (int) output.size(1);

        if (candidates == 0 || channels == 0) {
            candidates = (int) output.size(1);
            channels = (int) output.size(2);
        }

        if (candidates == 0 || channels == 0) {
            return Collections.emptyList();
        }

        float[] data = new float[candidates * channels];
        output.reshape(1, 1).get(0, 0, data);

        List<Rect> boxes = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        List<Integer> classIds = new ArrayList<>();

        double xFactor = frame.cols() / INPUT_SIZE.width;
        double yFactor = frame.rows() / INPUT_SIZE.height;

        int classOffset = 4;
        int classes = Math.max(1, channels - classOffset);

        double bestRawScore = Double.NEGATIVE_INFINITY;
        double bestSigmoidScore = Double.NEGATIVE_INFINITY;

        if (debugMode && !loggedSample) {
            logSampleRows(data, candidates, channels);
            loggedSample = true;
        }

        for (int i = 0; i < candidates; i++) {
            float x = data[i];
            float y = data[i + candidates];
            float width = data[i + 2 * candidates];
            float height = data[i + 3 * candidates];

            int bestClass = 0;
            float bestScore = data[i + classOffset * candidates];

            for (int c = 1; c < classes; c++) {
                float score = data[i + (classOffset + c) * candidates];
                if (score > bestScore) {
                    bestScore = score;
                    bestClass = c;
                }
            }

            float confidence = clamp(bestScore);
            if (confidence < confidenceThreshold) {
                if (!Float.isNaN(bestScore)) {
                    bestRawScore = Math.max(bestRawScore, bestScore);
                }
                if (!Float.isNaN(confidence)) {
                    bestSigmoidScore = Math.max(bestSigmoidScore, confidence);
                }
                continue;
            }

            int left = (int) ((x - width / 2) * xFactor);
            int top = (int) ((y - height / 2) * yFactor);
            int boxWidth = (int) (width * xFactor);
            int boxHeight = (int) (height * yFactor);

            left = Math.max(0, left);
            top = Math.max(0, top);
            boxWidth = Math.min(boxWidth, frame.cols() - left);
            boxHeight = Math.min(boxHeight, frame.rows() - top);

            if (boxWidth <= 0 || boxHeight <= 0) {
                continue;
            }

            boxes.add(new Rect(left, top, boxWidth, boxHeight));
            confidences.add(confidence);
            classIds.add(bestClass);
        }

        if (debugMode && boxes.isEmpty()) {
            System.out.printf(
                    "Debug: no detection cleared threshold %.2f | best raw %.3f | best sigmoid %.3f%n",
                    confidenceThreshold,
                    bestRawScore,
                    bestSigmoidScore);
        }

        if (boxes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Rect2d> boxes2d = new ArrayList<>(boxes.size());
        for (Rect box : boxes) {
            boxes2d.add(new Rect2d(box.x, box.y, box.width, box.height));
        }

        MatOfRect2d boxMat = new MatOfRect2d();
        boxMat.fromList(boxes2d);
        MatOfFloat confidenceMat = new MatOfFloat();
        confidenceMat.fromList(confidences);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxes(boxMat, confidenceMat, (float) confidenceThreshold, (float) nmsThreshold, indices);

        int[] keep = indices.toArray();
        if (keep.length == 0) {
            return Collections.emptyList();
        }

        List<DetectionResult> results = new ArrayList<>(keep.length);
        for (int idx : keep) {
            String label = classIds.get(idx) < labels.size()
                    ? labels.get(classIds.get(idx))
                    : "unknown";
            results.add(new DetectionResult(label, confidences.get(idx), boxes.get(idx)));
        }
        return results;
    }

    private float clamp(float value) {
        if (Float.isNaN(value)) {
            return 0f;
        }
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }

    private void logSampleRows(float[] data, int candidates, int channels) {
        int[] sampleIndices = buildSampleIndices(candidates);
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (float value : data) {
            if (Float.isNaN(value)) {
                continue;
            }
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        System.out.printf("Debug raw output min=%.4f max=%.4f%n", min, max);
        System.out.println("Debug sample of raw model rows (x, y, w, h, score...):");
        for (int index : sampleIndices) {
            if (index >= candidates) {
                break;
            }
            StringBuilder builder = new StringBuilder("  row ")
                    .append(index)
                    .append(": ");
            builder.append(String.format("x=%.4f y=%.4f w=%.4f h=%.4f",
                    data[index],
                    data[index + candidates],
                    data[index + 2 * candidates],
                    data[index + 3 * candidates]));
            if (channels > 4) {
                for (int c = 0; c < Math.min(channels - 4, 3); c++) {
                    builder.append(" class")
                            .append(c)
                            .append('=')
                            .append(String.format("%.4f", data[index + (4 + c) * candidates]));
                }
            }
            System.out.println(builder);
        }
    }

    private int[] buildSampleIndices(int total) {
        if (total <= 0) {
            return new int[0];
        }
        return new int[]{
                0,
                total / 10,
                total / 5,
                total / 2,
                total - 1
        };
    }

    public DetectionResult getBestResult(List<DetectionResult> detections) {
        DetectionResult best = null;
        for (DetectionResult detection : detections) {
            if (!detection.isConfident(confidenceThreshold)) {
                continue;
            }
            if (best == null || detection.getConfidence() > best.getConfidence()) {
                best = detection;
            }
        }
        return best;
    }

    public Mat drawFeedback(Mat frame, DetectionResult bestDetection) {
        Mat display = frame.clone();
        Scalar successColor = new Scalar(0, 160, 0);
        Scalar failureColor = new Scalar(0, 0, 200);
        Scalar textColor = new Scalar(255, 255, 255);

        String message = "No bottle detected.";

        if (bestDetection != null) {
            Rect box = bestDetection.getBoundingBox();
            Imgproc.rectangle(display, box, successColor, 2);
            String label = String.format("%s %.0f%%",
                    bestDetection.getLabel(),
                    bestDetection.getConfidence() * 100);
            Imgproc.putText(display, label,
                    new Point(box.x, Math.max(20, box.y - 10)),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, successColor, 2);
            message = bestDetection.getStatusMessage(confidenceThreshold);
        }

        int padding = 12;
        Point overlayTopLeft = new Point(0, display.rows() - 70);
        Point overlayBottomRight = new Point(display.cols(), display.rows());
        Scalar background = (bestDetection != null) ? successColor : failureColor;
        Imgproc.rectangle(display, overlayTopLeft, overlayBottomRight, background, Imgproc.FILLED);
        Imgproc.putText(display, message,
                new Point(padding, display.rows() - padding),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, textColor, 2);

        return display;
    }

    @Override
    public void close() {
        if (camera.isOpened()) {
            camera.release();
        }
    }
}
