import javax.naming.InsufficientResourcesException;
import java.util.Scanner;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in) ;

        System.out.println("Please enter vector height and width");
        Integer vectorHeight = scanner.nextInt();
        Integer vectorWidth = scanner.nextInt();

        System.out.println("Please enter code book size");
        Integer codebookSize = scanner.nextInt();
        VectorQuantizationCompression v = new VectorQuantizationCompression(vectorHeight, vectorWidth, codebookSize, "C:/Users/PC/Downloads/image.png");
        v.run();
        VectorQuantizationDecompression vectorQuantizationDecompression = new VectorQuantizationDecompression(v.getVectorHeight(),v.getVectorWidth(),6,6,v.getAverages(),v.getIndexTable());
        vectorQuantizationDecompression.decompress();
    }
}