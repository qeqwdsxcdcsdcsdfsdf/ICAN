package com.attendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.imageio.ImageIO;

public class PCAUtil {

    private static final Logger logger = LoggerFactory.getLogger(PCAUtil.class);
    
    private static final int FACE_WIDTH = 150;
    private static final int FACE_HEIGHT = 150;
    private static final int NUM_EIGENFACES = 30;

    public static double[] extractFeature(String base64Image) {
        try {
            String cleanBase64 = base64Image;
            
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.split(",")[1];
            }
            
            cleanBase64 = cleanBase64.replaceAll("\\s", "");
            
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);
            
            if (image == null) {
                logger.error("无法读取图片");
                return null;
            }

            BufferedImage grayImage = toGrayscale(image);
            BufferedImage resizedImage = resizeImage(grayImage, FACE_WIDTH, FACE_HEIGHT);
            BufferedImage normalizedImage = normalizeImage(resizedImage);
            
            double[] pixelVector = imageToVector(normalizedImage);
            double[] hogFeature = extractHOG(normalizedImage);
            double[] lbpFeature = extractLBP(normalizedImage);
            
            double[] combined = new double[pixelVector.length + hogFeature.length + lbpFeature.length];
            System.arraycopy(pixelVector, 0, combined, 0, pixelVector.length);
            System.arraycopy(hogFeature, 0, combined, pixelVector.length, hogFeature.length);
            System.arraycopy(lbpFeature, 0, combined, pixelVector.length + hogFeature.length, lbpFeature.length);
            
            return combined;
            
        } catch (Exception e) {
            logger.error("提取人脸特征失败", e);
            return null;
        }
    }

    public static double compareFeatures(double[] feature1, double[] feature2) {
        if (feature1 == null || feature2 == null) {
            return 0.0;
        }
        
        int minLength = Math.min(feature1.length, feature2.length);
        double sumDiff = 0.0;
        double sum1 = 0.0;
        double sum2 = 0.0;
        
        for (int i = 0; i < minLength; i++) {
            sumDiff += (feature1[i] - feature2[i]) * (feature1[i] - feature2[i]);
            sum1 += feature1[i] * feature1[i];
            sum2 += feature2[i] * feature2[i];
        }
        
        double euclideanDistance = Math.sqrt(sumDiff);
        double norm1 = Math.sqrt(sum1);
        double norm2 = Math.sqrt(sum2);
        
        double cosineSimilarity = 0.0;
        if (norm1 > 0 && norm2 > 0) {
            double dotProduct = 0.0;
            for (int i = 0; i < minLength; i++) {
                dotProduct += feature1[i] * feature2[i];
            }
            cosineSimilarity = dotProduct / (norm1 * norm2);
        }
        
        double maxDistance = Math.sqrt(minLength * 4);
        double normalizedDist = 1.0 - (euclideanDistance / maxDistance);
        
        double similarity = (normalizedDist * 0.4 + cosineSimilarity * 0.6) * 100;
        return Math.max(0, Math.min(100, similarity));
    }

    private static BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int grayValue = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                gray.setRGB(x, y, (grayValue << 16) | (grayValue << 8) | grayValue);
            }
        }
        return gray;
    }

    private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, image.getType());
        java.awt.Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private static BufferedImage normalizeImage(BufferedImage image) {
        double mean = 0.0;
        double variance = 0.0;
        int count = image.getWidth() * image.getHeight();
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                mean += (image.getRGB(x, y) & 0xFF);
            }
        }
        mean /= count;
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double diff = (image.getRGB(x, y) & 0xFF) - mean;
                variance += diff * diff;
            }
        }
        variance = Math.sqrt(variance / count);
        
        BufferedImage normalized = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                double normalizedPixel = ((pixel - mean) / (variance + 1e-6)) * 30 + 127;
                normalizedPixel = Math.max(0, Math.min(255, normalizedPixel));
                normalized.setRGB(x, y, ((int) normalizedPixel << 16) | ((int) normalizedPixel << 8) | (int) normalizedPixel);
            }
        }
        
        return normalized;
    }

    private static double[] imageToVector(BufferedImage image) {
        double[] vector = new double[image.getWidth() * image.getHeight()];
        int idx = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                vector[idx++] = (image.getRGB(x, y) & 0xFF) / 255.0;
            }
        }
        return vector;
    }

    private static double[] extractHOG(BufferedImage image) {
        int cellSize = 15;
        int blockSize = 2;
        int numBins = 9;
        
        int cellsPerRow = image.getWidth() / cellSize;
        int cellsPerCol = image.getHeight() / cellSize;
        
        double[][] gradientsX = new double[image.getHeight()][image.getWidth()];
        double[][] gradientsY = new double[image.getHeight()][image.getWidth()];
        
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                gradientsX[y][x] = ((image.getRGB(x + 1, y) & 0xFF) - (image.getRGB(x - 1, y) & 0xFF)) / 2.0;
                gradientsY[y][x] = ((image.getRGB(x, y + 1) & 0xFF) - (image.getRGB(x, y - 1) & 0xFF)) / 2.0;
            }
        }
        
        double[][] magnitude = new double[image.getHeight()][image.getWidth()];
        double[][] angle = new double[image.getHeight()][image.getWidth()];
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                magnitude[y][x] = Math.sqrt(gradientsX[y][x] * gradientsX[y][x] + gradientsY[y][x] * gradientsY[y][x]);
                angle[y][x] = Math.atan2(gradientsY[y][x], gradientsX[y][x]) * 180 / Math.PI;
                if (angle[y][x] < 0) angle[y][x] += 360;
            }
        }
        
        java.util.ArrayList<Double> hogFeatures = new java.util.ArrayList<>();
        
        for (int blockY = 0; blockY <= cellsPerCol - blockSize; blockY++) {
            for (int blockX = 0; blockX <= cellsPerRow - blockSize; blockX++) {
                double[] blockHist = new double[blockSize * blockSize * numBins];
                int histIdx = 0;
                
                for (int cellY = blockY; cellY < blockY + blockSize; cellY++) {
                    for (int cellX = blockX; cellX < blockX + blockSize; cellX++) {
                        double[] cellHist = new double[numBins];
                        for (int y = cellY * cellSize; y < (cellY + 1) * cellSize; y++) {
                            for (int x = cellX * cellSize; x < (cellX + 1) * cellSize; x++) {
                                int bin = (int) (angle[y][x] / 40);
                                bin = Math.min(bin, numBins - 1);
                                cellHist[bin] += magnitude[y][x];
                            }
                        }
                        
                        for (int i = 0; i < numBins; i++) {
                            blockHist[histIdx++] = cellHist[i];
                        }
                    }
                }
                
                double norm = 0.0;
                for (double val : blockHist) norm += val * val;
                norm = Math.sqrt(norm) + 1e-6;
                
                for (double val : blockHist) {
                    hogFeatures.add(val / norm);
                }
            }
        }
        
        double[] result = new double[hogFeatures.size()];
        for (int i = 0; i < hogFeatures.size(); i++) {
            result[i] = hogFeatures.get(i);
        }
        return result;
    }

    private static double[] extractLBP(BufferedImage image) {
        int cellSize = 30;
        int radius = 1;
        int neighbors = 8;
        
        int cellsPerRow = image.getWidth() / cellSize;
        int cellsPerCol = image.getHeight() / cellSize;
        
        double[] lbpHist = new double[cellsPerRow * cellsPerCol * 256];
        int histIdx = 0;
        
        for (int cellY = 0; cellY < cellsPerCol; cellY++) {
            for (int cellX = 0; cellX < cellsPerRow; cellX++) {
                int[] cellLBP = new int[256];
                
                for (int y = cellY * cellSize + radius; y < (cellY + 1) * cellSize - radius; y++) {
                    for (int x = cellX * cellSize + radius; x < (cellX + 1) * cellSize - radius; x++) {
                        int center = image.getRGB(x, y) & 0xFF;
                        int lbpCode = 0;
                        
                        for (int i = 0; i < neighbors; i++) {
                            double angle = 2.0 * Math.PI * i / neighbors;
                            int nx = x + (int) (radius * Math.cos(angle));
                            int ny = y - (int) (radius * Math.sin(angle));
                            
                            int neighbor = image.getRGB(nx, ny) & 0xFF;
                            lbpCode |= (neighbor >= center) ? (1 << i) : 0;
                        }
                        
                        cellLBP[lbpCode]++;
                    }
                }
                
                int total = 0;
                for (int val : cellLBP) total += val;
                
                for (int i = 0; i < 256; i++) {
                    lbpHist[histIdx++] = total > 0 ? (double) cellLBP[i] / total : 0;
                }
            }
        }
        
        return lbpHist;
    }
}
