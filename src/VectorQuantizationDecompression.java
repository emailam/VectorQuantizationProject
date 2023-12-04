import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

public class VectorQuantizationDecompression {
    private Integer vectorHeight, vectorWidth, imageHeight, imageWidth;
    private Map<Integer, Integer> indexTable;
    private Vector<Vector<Vector<Double>>> lookupTable;

    VectorQuantizationDecompression(Integer vectorHeight, Integer vectorWidth, Integer imageHeight, Integer imageWidth, Vector<Vector<Vector<Double>>> lookupTable, Map<Integer, Integer> indexTable) {
        this.vectorHeight = vectorHeight;
        this.vectorWidth = vectorWidth;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.lookupTable = lookupTable;
        this.indexTable = indexTable;
        System.out.println(lookupTable);
    }

    public Vector<Vector<Double>> decompress() {
        Double[][] image = new Double[imageHeight][imageWidth];
        int cnt = 0;
        Vector<Vector<Double>> reconstructedImage = new Vector<>();
        for (int i = 0; i < imageHeight; i += vectorHeight) {
            for (int j = 0; j < imageWidth; j += vectorWidth) {
                Vector<Vector<Double>> reconstructionBlock = lookupTable.get(indexTable.get(cnt++));

                for (int k = 0; k < vectorHeight; k++) {
                    Vector<Double> row = new Vector<>();

                    for (int l = 0; l < vectorWidth; l++) {
                        row.add(reconstructionBlock.get(k).get(l));
                    }

                    reconstructedImage.add(row);
                }
            }
        }
        return reconstructedImage;
    }
}