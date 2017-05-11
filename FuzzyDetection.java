import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
public class FuzzyDetection {

  int mWidth, mHeight;

  public static void main(String[] args) {
    if(args == null) {
      return ;
    }
    FuzzyDetection fuzzyDetection = new FuzzyDetection();
    String path = args[0];
    File[] files = fuzzyDetection.getFiles(path);
    if (files == null) {
      System.out.println("没有提供图片文件夹路径");
      return ;
    }
    File reulstFile = null;
    double maxScore = 0;
    for (File file : files) {
      try {
        BufferedImage bufferedImage = ImageIO.read(file);
        fuzzyDetection.mWidth = bufferedImage.getWidth();
        fuzzyDetection.mHeight = bufferedImage.getHeight();
        int []gray = fuzzyDetection.grayImage(bufferedImage);
        int []pixes = fuzzyDetection.convolution(gray);
        double nowResult = fuzzyDetection.calcVariance(pixes);
        System.out.print("这个图片名字 >> " + file.getName());
        System.out.println(" 清晰度是 >> " + nowResult);
        if (nowResult >= maxScore) {
          maxScore = nowResult;
          reulstFile = file;
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    System.out.println("这组图片中最优的是 >> " + reulstFile.getName());
    System.out.println("此图片的清晰度是 >> " + maxScore);
  }

  /**
  * 获取指定路径下的所有png图片
  */
  private File[] getFiles(String paths) {
    if (paths == null) {
      return null;
    }
    File file = new File(paths);
    if (file.exists() && file.isDirectory()) {
      File[] files = file.listFiles();
      if (files.length != 0) {
        return files;
      }
    }
    return null;
  }

  /**
  * 将颜色转换成rgb
  */
  private int colorToRGB(int alpha, int red, int green, int blue) {
    int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;
  }

  /**
  * 将图片转换成灰度图数组
  */
  private int[] grayImage(BufferedImage image) {
    int width = mWidth;
    int height = mHeight;
    int [] grey = new int[width * height];
    for (int j = 0; j < height; j++) {
      for (int i = 0; i < width; i++) {
          final int color = image.getRGB(i, j);
          long otColor = color;
          final int r = (color >> 16) & 0xff;
          final int g = (color >> 8) & 0xff;
          final int b = color & 0xff;
          //将颜色值 使用权值方式 生成灰度值 更加准确
          int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
          int newPixel = colorToRGB(255, gray, gray, gray);

          grey[i + j * width] = newPixel;
        }
    }
    return grey;
  }

  /**
  * 通过灰度数组与拉卡拉斯算子计算卷积值
  */
  private int[] convolution(int[] gray) {
      int[] FOUR = new int[] { 0, -1, 0, -1, 4, -1, 0, -1, 0 };
      int total = mWidth * mHeight;
      int[] output = new int[total];

      int offset = 0;
      int k0 = 0, k1 = 0, k2 = 0;
      int k3 = 0, k4 = 0, k5 = 0;
      int k6 = 0, k7 = 0, k8 = 0;
      k0 = FOUR[0];
      k1 = FOUR[1];
      k2 = FOUR[2];
      k3 = FOUR[3];
      k4 = FOUR[4];
      k5 = FOUR[5];
      k6 = FOUR[6];
      k7 = FOUR[7];
      k8 = FOUR[8];

      int sr = 0;
      int r = 0;
      for (int row = 1; row < mHeight - 1; row++) {
        offset = row * mWidth;
        for (int col = 1; col < mWidth - 1; col++) {
          // red
          sr = k0 * ((gray[offset - mWidth + col - 1] >> 16) & 0xff)
              + k1 * (gray[offset - mWidth + col] & 0xff)
              + k2 * (gray[offset - mWidth + col + 1] & 0xff)
              + k3 * (gray[offset + col - 1] & 0xff)
              + k4 * (gray[offset + col] & 0xff)
              + k5 * (gray[offset + col + 1] & 0xff)
              + k6 * (gray[offset + mWidth + col - 1] & 0xff)
              + k7 * (gray[offset + mWidth + col] & 0xff)
              + k8 * (gray[offset + mWidth + col + 1] & 0xff);
          r = sr;

          r = r > 255 ? 255 :( (r < 0) ? 0: r);
          output[offset+col]= r;

          // for next pixel
          sr = 0;
        }
      }
      return output;
  }

  /**
  * 计算方差
  */
  private double calcVariance(int [] values) {
      double variance = 0;//方差
      double average = 0;//平均数
      int i,len = values.length;
      double sum=0, sum2=0;
      for(i = 0; i< len; i ++){
         sum += values[i];
      }

      average = sum/ len;

      for(i = 0; i < len; i++){
         sum2 += ((double)values[i]-average)*((double)values[i]-average);
      }
      variance = sum2/ len;
      return variance;
  }
}
