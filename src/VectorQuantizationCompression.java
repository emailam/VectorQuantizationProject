import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class VectorQuantizationCompression {
    public class Pair<F, S> {
        private final F first;
        private final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }

    }
    private Integer vectorHeight, vectorWidth, codebook;
    private Vector<Vector<Vector<Double>>> vectorBlocks;
    private Vector<Vector<Vector<Double>>> averages;
    private Map<Integer, Vector<Vector<Vector<Double>>>> groups, lastGroups;
    private Map<Integer, Integer> indexTable;

    public void run() {
        while (true) {
            average();
            groups.clear();
            for (var v : vectorBlocks) {
                findNearest(v);
            }
            if (groups.equals(lastGroups)) {
                average();
                break;
            } else {
                lastGroups.clear();
                lastGroups.putAll(groups);
            }
        }
        buildIndexTable();
    }

    public void buildIndexTable() {
        int cnt = 0;
        for (var vec : vectorBlocks) {
            indexTable.put(cnt++,findNearestIndex(vec));
        }
    }

    public void average() {
        averages.clear();

        for (Vector<Vector<Vector<Double>>> v : groups.values()) {
            Vector<Vector<Double>> avg = new Vector<>();
            for (int i = 0; i < vectorHeight; i++) {
                Vector<Double> row = new Vector<>();
                for (int j = 0; j < vectorWidth; j++) {
                    row.add(0.0);
                }
                avg.add(row);
            }
            for (var v2 : v) {
                for (int i = 0; i < vectorHeight; i++) {
                    for (int j = 0; j < vectorWidth; j++) {
                        avg.get(i).set(j, avg.get(i).get(j) + v2.get(i).get(j));
                    }
                }
            }
            for (int i = 0; i < vectorHeight; i++) {
                for (int j = 0; j < vectorWidth; j++) {
                    avg.get(i).set(j, avg.get(i).get(j) / v.size());
                }
            }
            if (codebook > 0) {
                codebook /= 2;
                averages.add(ceilVector(avg));
                averages.add(floorVector(avg));
            } else {
                averages.add(avg);
            }
        }
    }

    public int findNearestIndex(Vector<Vector<Double>> block) {
        double mnDis = 1e9;
        for (var avg : averages) {
            mnDis = Math.min(mnDis, distance(block, avg));
        }
        for (int i = 0; i < averages.size(); i++) {
            Vector<Vector<Double>> avg = averages.get(i);
            if (mnDis == distance(block, avg)) {
                return i;
            }
        }
        return -1;
    }

    public void findNearest(Vector<Vector<Double>> block) {
        double mnDis = 1e9;
        for (var avg : averages) {
            mnDis = Math.min(mnDis, distance(block, avg));
        }
        for (int i = 0; i < averages.size(); i++) {
            Vector<Vector<Double>> avg = averages.get(i);
            if (mnDis == distance(block, avg)) {
                Vector<Vector<Vector<Double>>> blocks = groups.get(i);
                if (blocks != null) {
                    blocks.add(block);
                    groups.put(i, blocks);
                } else {
                    Vector<Vector<Vector<Double>>> blocksz = new Vector<>();
                    blocksz.add(block);
                    groups.put(i, blocksz);
                }
                return;
            }
        }
    }

    public double distance(Vector<Vector<Double>> v1, Vector<Vector<Double>> v2) {
        double diff = 0;
        for (int i = 0; i < vectorHeight; i++) {
            for (int j = 0; j < vectorWidth; j++) {
                diff += Math.abs(v1.get(i).get(j) - v2.get(i).get(j));
            }
        }
        return diff;
    }

    public VectorQuantizationCompression(Integer vectorHeight, Integer vectorWidth, Integer codebook, String path) {
        groups = new HashMap<>();
        lastGroups = new HashMap<>();
        vectorBlocks = new Vector<>();
        averages = new Vector<>();
        this.vectorHeight = vectorHeight;
        this.vectorWidth = vectorWidth;
        this.codebook = codebook;
        indexTable = new HashMap<>();

        Vector<Vector<Double>> image = imageConverter(path);
        for (int i = 0; i < image.size(); i += vectorHeight) {
            for (int j = 0; j < image.lastElement().size(); j += vectorHeight) {
                Vector<Vector<Double>> tempBlock = new Vector<>();
                for (int k = 0; k < vectorHeight; k++) {
                    Vector<Double> tempVector = new Vector<>();
                    for (int l = 0; l < vectorWidth; l++) {
                        if (i + k < image.size() && j + l < image.lastElement().size()) {
                            tempVector.add(image.get(i + k).get(j + l));
                        } else {
                            tempVector.add(0.0);
                        }
                    }
                    tempBlock.add(tempVector);
                }
                vectorBlocks.add(tempBlock);
            }
        }
        groups.put(0, vectorBlocks);
    }

    public Vector<Vector<Double>> ceilVector(Vector<Vector<Double>> v) {
        Vector<Vector<Double>> result = new Vector<>();

        for (Vector<Double> row : v) {
            Vector<Double> ceilRow = new Vector<>();
            for (Double value : row) {
                ceilRow.add(Math.ceil(value - 1));
            }
            result.add(ceilRow);
        }

        return result;
    }

    public Vector<Vector<Double>> floorVector(Vector<Vector<Double>> v) {
        Vector<Vector<Double>> result = new Vector<>();

        for (Vector<Double> row : v) {
            Vector<Double> floorRow = new Vector<>();
            for (Double value : row) {
                floorRow.add(Math.floor(value + 1));
            }
            result.add(floorRow);
        }

        return result;
    }

    public Vector<Vector<Double>> imageConverter(String path) {
        try {
            File file = new File(path);
            BufferedImage image = ImageIO.read(file);

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            Vector<Vector<Double>> result = new Vector<>();

            for (int i = 0; i < imageHeight; i++) {
                Vector<Double> row = new Vector<>();
                for (int j = 0; j < imageWidth; j++) {
                    int pixel = image.getRGB(j, i);

                    // Extract color channels
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;

                    // Correctly average the channels to get grayscale value
                    double grayscaleValue = (red + green + blue) / 3.0;
                    row.add(grayscaleValue);
                }
                result.add(row);
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer getVectorHeight() {
        return vectorHeight;
    }

    public Integer getVectorWidth() {
        return vectorWidth;
    }

    public Integer getCodebook() {
        return codebook;
    }

    public Vector<Vector<Vector<Double>>> getVectorBlocks() {
        return vectorBlocks;
    }

    public Vector<Vector<Vector<Double>>> getAverages() {
        return averages;
    }

    public Map<Integer, Vector<Vector<Vector<Double>>>> getGroups() {
        return groups;
    }

    public Map<Integer, Vector<Vector<Vector<Double>>>> getLastGroups() {
        return lastGroups;
    }

    public Map<Integer, Integer> getIndexTable() {
        return indexTable;
    }
}