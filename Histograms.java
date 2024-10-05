import java.awt.image.BufferedImage;

// calculates color histograms for an image
public class Histograms {

    static final int INTENSITY_NUM_BINS = 25;
    static final int INTENSITY_BIN_SIZE = 10;

    static final int COLORCODE_NUM_BINS = 64;

    // Calculate Intensity Histogram of an image
    public static int[] intensityMethod(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] histogram = new int[INTENSITY_NUM_BINS + 1]; // +1 to store total pixel count
        histogram[0] = width * height; // store total pixels / image size

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                // rgb value of pixel at "[0,0]"
                int rgb = image.getRGB(x, y);
                // Needed to google for this part for syntax to get the r, g, and b values
                // SHIFT r value right (by 16 bits) to get intensity of red pixel
                int r = (rgb >> 16) & 0xff; // 0xFF = only get 8 bits
                // SHIFT g value right (by 8 bits) to get intensity of green pixel
                int g = (rgb >> 8) & 0xff;
                // MASK rgb value to get intensity of blue pixel
                // mask --> isolate just the section that we need
                int b = rgb & 0xff;

                // Calculate the intensity value (I)
                double intensity = 0.299 * r + 0.587 * g + 0.114 * b;

                // put I value in corresponding bin (ensure in bounds)
                int binIndex = Math.min((int) (intensity / INTENSITY_BIN_SIZE), INTENSITY_NUM_BINS);
                histogram[binIndex + 1]++; //  Increment count in the corresponding bin
            }
        }
        // process each pixel and assign to the bins
        return histogram;
    }

    // Calculate color-code histograms of an image
    public static int[] colorCodeMethod(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] histogram = new int[COLORCODE_NUM_BINS + 1]; // 64 bins + index 0 for total pixels
        histogram[0] = width * height; // Store the total number of pixels in the image

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                int rgb = image.getRGB(x, y); // SWAP?
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int colorCode = getColorCode(r, g, b);
                histogram[colorCode + 1]++;

            }
        }
        return histogram;
    }

    public static int getColorCode(int r, int g, int b) {
        // extract 2 leftmost (most important) bits (MIB)
        // had to google the syntax for extraction technique
        int rMIB = (r >> 6) & 0x03; // Shift right by 6 to get the two values
        int gMIB = (g >> 6) & 0x03;
        int bMIB = (b >> 6) & 0x03;

        // combine MIBs to a 6-bit value (concatenate)
        // also had to google syntax for concatenation
        int colorCode = (rMIB << 4) | (gMIB << 2) | bMIB;

        return colorCode;
    }

    public static double manhattanDistance(int[] histo1, int[] histo2, int numBins) {
        double distance = 0.0;
        // for each bin in histogram...
        for (int i = 1; i <= numBins; i++) {
            // calculate distance between bins (absolute value of the distance) then summation
            distance += Math.abs((double) histo1[i] / histo1[0] - (double) histo2[i] / histo2[0]);
        }
        return distance;
    }
}
