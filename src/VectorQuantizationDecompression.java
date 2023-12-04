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

    public void decompress() {
        Double [][] image = new Double[imageHeight][imageWidth];
        for (int i = 0; i < imageHeight; i += vectorHeight) {
            for (int j = 0; j < imageWidth; j += vectorWidth) {
                Vector<Vector<Double>> reconstructionValue = lookupTable.get(indexTable.get(j));
                System.out.println(reconstructionValue);
            }
        }
        System.out.println(Arrays.deepToString(image));
    }

}
