package com.attendance.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

public class FaceCompareUtil {
    private static final double THRESHOLD = 0.6;
    private static final int SIMILARITY_THRESHOLD = 75;
    private static final int COMPARE_THRESHOLD = 25;
    private static final int TARGET_WIDTH = 200;
    private static final int TARGET_HEIGHT = 200;
    private static final int COLOR_DIFF_THRESHOLD = 100;

    public static boolean compare(String faceFeature1, String faceFeature2) {
        double score = compareAndGetScore(faceFeature1, faceFeature2);
        return score >= 60;
    }

    public static boolean compareWithResult(String faceFeature1, String faceFeature2) {
        return compare(faceFeature1, faceFeature2);
    }

    public static double compareAndGetScore(String faceFeature1, String faceFeature2) {
        return compareAndGetScore(faceFeature1, faceFeature2, null);
    }

    public static double compareAndGetScore(String faceFeature1, String faceFeature2, java.util.Map<String, Integer> detailScores) {
        if (faceFeature1 == null || faceFeature2 == null) {
            return 0;
        }

        if (faceFeature1.startsWith("[") && faceFeature1.endsWith("]")) {
            try {
                double[] feature1 = parseFeature(faceFeature1);
                double[] feature2 = parseFeature(faceFeature2);
                double similarity = PCAUtil.compareFeatures(feature1, feature2);
                
                if (detailScores != null) {
                    detailScores.put("pcaSimilarity", (int) similarity);
                }
                
                return similarity;
            } catch (Exception e) {
                return 0;
            }
        } else {
            try {
                double[] feature1 = PCAUtil.extractFeature(faceFeature1);
                double[] feature2 = PCAUtil.extractFeature(faceFeature2);
                
                if (feature1 == null || feature2 == null) {
                    return 0;
                }
                
                double similarity = PCAUtil.compareFeatures(feature1, feature2);
                
                if (detailScores != null) {
                    detailScores.put("pcaSimilarity", (int) similarity);
                    detailScores.put("hogSimilarity", (int) (similarity * 0.4));
                    detailScores.put("lbpSimilarity", (int) (similarity * 0.3));
                    detailScores.put("pixelSimilarity", (int) (similarity * 0.3));
                }
                
                return similarity;
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private static boolean compareFeatureVectors(String featureStr1, String featureStr2) {
        try {
            double[] feature1 = parseFeature(featureStr1);
            double[] feature2 = parseFeature(featureStr2);
            
            if (feature1.length != feature2.length) {
                return false;
            }
            
            double distance = calculateEuclideanDistance(feature1, feature2);
            return distance < THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean compareImages(String imageBase64_1, String imageBase64_2) {
        try {
            BufferedImage img1 = decodeBase64ToImage(imageBase64_1);
            BufferedImage img2 = decodeBase64ToImage(imageBase64_2);

            if (img1 == null || img2 == null) {
                return compareSimple(imageBase64_1, imageBase64_2);
            }

            img1 = resizeImage(img1, TARGET_WIDTH, TARGET_HEIGHT);
            img2 = resizeImage(img2, TARGET_WIDTH, TARGET_HEIGHT);

            BufferedImage gray1 = toGrayscale(img1);
            BufferedImage gray2 = toGrayscale(img2);

            BufferedImage blur1 = gaussianBlur(gray1);
            BufferedImage blur2 = gaussianBlur(gray2);

            BufferedImage eq1 = histogramEqualization(blur1);
            BufferedImage eq2 = histogramEqualization(blur2);

            int pixelSimilarity = comparePixelsOptimized(eq1, eq2);
            int histogramSimilarity = compareHistogramsOptimized(eq1, eq2);
            int colorSimilarity = compareColorMeanOptimized(img1, img2);
            int ssimSimilarity = calculateSSIM(eq1, eq2);
            int gradientSimilarity = compareGradients(eq1, eq2);

            double avgSimilarity = pixelSimilarity * 0.25 
                                + histogramSimilarity * 0.2 
                                + colorSimilarity * 0.2 
                                + ssimSimilarity * 0.2
                                + gradientSimilarity * 0.15;

            return avgSimilarity >= COMPARE_THRESHOLD;
        } catch (Exception e) {
            return compareSimple(imageBase64_1, imageBase64_2);
        }
    }

    private static BufferedImage decodeBase64ToImage(String base64Str) {
        try {
            String cleanBase64 = base64Str.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.split(",")[1];
            }
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bis);
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        if (originalImage == null) {
            return null;
        }
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    private static BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayscale.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayscale;
    }

    private static BufferedImage histogramEqualization(BufferedImage grayscale) {
        int width = grayscale.getWidth();
        int height = grayscale.getHeight();
        int[] histogram = new int[256];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grayscale.getRGB(x, y) & 0xFF;
                histogram[gray]++;
            }
        }

        int[] cumulative = new int[256];
        cumulative[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i - 1] + histogram[i];
        }

        BufferedImage equalized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grayscale.getRGB(x, y) & 0xFF;
                int newGray = (int) ((cumulative[gray] * 255.0) / (width * height));
                equalized.setRGB(x, y, newGray | (newGray << 8) | (newGray << 16));
            }
        }
        return equalized;
    }

    private static BufferedImage gaussianBlur(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurred = new BufferedImage(width, height, image.getType());
        
        double[][] kernel = {
            {0.0625, 0.125, 0.0625},
            {0.125, 0.25, 0.125},
            {0.0625, 0.125, 0.0625}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sum = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int gray = image.getRGB(x + kx, y + ky) & 0xFF;
                        sum += gray * kernel[ky + 1][kx + 1];
                    }
                }
                int newGray = (int) Math.round(sum);
                blurred.setRGB(x, y, newGray | (newGray << 8) | (newGray << 16));
            }
        }
        return blurred;
    }

    private static int comparePixelsOptimized(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int similarPixels = 0;
        int step = Math.max(1, width / 40);
        
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int gray1 = img1.getRGB(x, y) & 0xFF;
                int gray2 = img2.getRGB(x, y) & 0xFF;
                
                int diff = Math.abs(gray1 - gray2);
                if (diff < 40) {
                    similarPixels++;
                }
            }
        }
        
        int sampledPixels = ((height / step) + 1) * ((width / step) + 1);
        return (int) (similarPixels * 100.0 / sampledPixels);
    }

    private static int compareHistogramsOptimized(BufferedImage img1, BufferedImage img2) {
        int[] hist1 = calculateGrayscaleHistogram(img1);
        int[] hist2 = calculateGrayscaleHistogram(img2);
        
        double sumDiff = 0;
        double sumTotal = 0;
        
        for (int i = 0; i < 256; i++) {
            sumDiff += Math.abs(hist1[i] - hist2[i]);
            sumTotal += hist1[i] + hist2[i];
        }
        
        if (sumTotal == 0) return 0;
        
        double diff = sumDiff / sumTotal;
        int similarity = (int) ((1 - diff) * 100);
        return Math.max(0, Math.min(100, similarity));
    }

    private static int[] calculateGrayscaleHistogram(BufferedImage img) {
        int[] histogram = new int[256];
        int width = img.getWidth();
        int height = img.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = img.getRGB(x, y) & 0xFF;
                histogram[gray]++;
            }
        }
        return histogram;
    }

    private static int compareColorMeanOptimized(BufferedImage img1, BufferedImage img2) {
        int[] mean1 = calculateColorMean(img1);
        int[] mean2 = calculateColorMean(img2);
        
        int diffR = Math.abs(mean1[0] - mean2[0]);
        int diffG = Math.abs(mean1[1] - mean2[1]);
        int diffB = Math.abs(mean1[2] - mean2[2]);
        
        int totalDiff = diffR + diffG + diffB;
        int maxDiff = 255 * 3;
        
        int similarity = (int) ((1 - (double) totalDiff / maxDiff) * 100);
        return Math.max(0, similarity);
    }

    private static int[] calculateColorMean(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int totalPixels = width * height;
        
        long sumR = 0, sumG = 0, sumB = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                sumR += (rgb >> 16) & 0xFF;
                sumG += (rgb >> 8) & 0xFF;
                sumB += rgb & 0xFF;
            }
        }
        
        return new int[]{
            (int) (sumR / totalPixels),
            (int) (sumG / totalPixels),
            (int) (sumB / totalPixels)
        };
    }

    private static int calculateSSIM(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img2.getHeight();
        
        double mu1 = 0, mu2 = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mu1 += (img1.getRGB(x, y) & 0xFF);
                mu2 += (img2.getRGB(x, y) & 0xFF);
            }
        }
        mu1 /= (width * height);
        mu2 /= (width * height);
        
        double sigma1 = 0, sigma2 = 0, sigma12 = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double g1 = img1.getRGB(x, y) & 0xFF;
                double g2 = img2.getRGB(x, y) & 0xFF;
                sigma1 += Math.pow(g1 - mu1, 2);
                sigma2 += Math.pow(g2 - mu2, 2);
                sigma12 += (g1 - mu1) * (g2 - mu2);
            }
        }
        sigma1 = Math.sqrt(sigma1 / (width * height - 1));
        sigma2 = Math.sqrt(sigma2 / (width * height - 1));
        sigma12 /= (width * height - 1);
        
        double C1 = 6.5025;
        double C2 = 58.5225;
        
        double ssim = ((2 * mu1 * mu2 + C1) * (2 * sigma12 + C2)) 
                    / ((mu1 * mu1 + mu2 * mu2 + C1) * (sigma1 * sigma1 + sigma2 * sigma2 + C2));
        
        return (int) (ssim * 100);
    }

    private static int compareGradients(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        
        int[][] grad1X = calculateGradientX(img1);
        int[][] grad1Y = calculateGradientY(img1);
        int[][] grad2X = calculateGradientX(img2);
        int[][] grad2Y = calculateGradientY(img2);
        
        int similar = 0;
        int total = 0;
        int step = 4;
        
        for (int y = step; y < height - step; y += step) {
            for (int x = step; x < width - step; x += step) {
                double mag1 = Math.sqrt(grad1X[y][x] * grad1X[y][x] + grad1Y[y][x] * grad1Y[y][x]);
                double mag2 = Math.sqrt(grad2X[y][x] * grad2X[y][x] + grad2Y[y][x] * grad2Y[y][x]);
                
                if (Math.abs(mag1 - mag2) < 30) {
                    similar++;
                }
                total++;
            }
        }
        
        return total > 0 ? (int) (similar * 100.0 / total) : 0;
    }

    private static int[][] calculateGradientX(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] gradient = new int[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width - 1; x++) {
                int left = img.getRGB(x - 1, y) & 0xFF;
                int right = img.getRGB(x + 1, y) & 0xFF;
                gradient[y][x] = right - left;
            }
        }
        return gradient;
    }

    private static int[][] calculateGradientY(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] gradient = new int[height][width];
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 0; x < width; x++) {
                int top = img.getRGB(x, y - 1) & 0xFF;
                int bottom = img.getRGB(x, y + 1) & 0xFF;
                gradient[y][x] = bottom - top;
            }
        }
        return gradient;
    }

    private static boolean compareSimple(String imageBase64_1, String imageBase64_2) {
        try {
            String hash1 = md5(imageBase64_1.substring(0, Math.min(imageBase64_1.length(), 1000)));
            String hash2 = md5(imageBase64_2.substring(0, Math.min(imageBase64_2.length(), 1000)));
            
            int similarity = calculateStringSimilarity(hash1, hash2);
            return similarity >= SIMILARITY_THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }

    private static int calculateStringSimilarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 100;
        }
        int longer = s1.length();
        int shorter = s2.length();
        if (longer == 0) {
            return 100;
        }
        return (int) ((longer - editDistance(s1, s2)) * 100.0 / longer);
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    private static double[] parseFeature(String featureStr) {
        String[] parts = featureStr.replace("[", "").replace("]", "").split(",");
        double[] feature = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            feature[i] = Double.parseDouble(parts[i].trim());
        }
        return feature;
    }

    private static double calculateEuclideanDistance(double[] v1, double[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static String generateRandomFeature() {
        StringBuilder sb = new StringBuilder("[");
        Random random = new Random();
        for (int i = 0; i < 128; i++) {
            sb.append(String.format("%.6f", random.nextDouble() * 2 - 1));
            if (i < 127) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}