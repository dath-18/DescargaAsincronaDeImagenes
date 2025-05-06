import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Principal {
    static MyCollection myCollection = new MyCollection();
    // Núcleos disponibles
    static int nucleos = Runtime.getRuntime().availableProcessors();
    public static void AbrirArchivo(){
        try {
            int iden=0;
            String pathname = "src/main/java/";
            String filename = "urls.txt";
            File file = new File(pathname + filename);
            try (FileReader fileReader = new FileReader(file.getAbsolutePath())) {
                BufferedReader buff = new BufferedReader (fileReader);
                String line;
                while ((line = buff.readLine()) != null) {
                    if (!line.isEmpty())
                        myCollection.add(new Imagenes(line,iden++));
                }
                buff.close();
            }
        } catch (IOException e){}
    }
    public static void descargaImagen(String enlace,int nameImg){
        HttpClient c = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest r = HttpRequest.newBuilder().uri(URI.create(enlace)).GET().build();
        try {
            HttpResponse<byte[]> respuesta = c.send(r, HttpResponse.BodyHandlers.ofByteArray());
            String pathname = "src/main/java/Imagenes/";
            File file = new File(pathname);
            File archivoDestino = new File(file.getAbsolutePath(), "img"+nameImg+".jpg");
            if (respuesta.statusCode() == 200) {
                try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
                    fos.write(respuesta.body());
                }
            } else {
                System.out.println("Error al descargar " + enlace);
            }
        } catch (IOException | InterruptedException e){}
    }
    private static BufferedImage filtroSepia(BufferedImage imagen) {
        int width = imagen.getWidth();
        int height = imagen.getHeight();
        BufferedImage sepia = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = imagen.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int R = (p >> 16) & 0xff;
                int G = (p >> 8) & 0xff;
                int B = p & 0xff;
                int newRed = (int) (0.393 * R + 0.769 * G + 0.189 * B);
                int newGreen = (int) (0.349 * R + 0.686 * G + 0.168 * B);
                int newBlue = (int) (0.272 * R + 0.534 * G + 0.131 * B);
                if (newRed > 255) {
                    R = 255;
                } else {
                    R = newRed;
                }
                if (newGreen > 255) {
                    G = 255;
                } else {
                    G = newGreen;
                }
                if (newBlue > 255) {
                    B = 255;
                } else {
                    B = newBlue;
                }
                // set new RGB value 
                p = (a << 24) | (R << 16) | (G << 8) | B;
                sepia.setRGB(x, y, p);
            }
        }
        return sepia;
    }
    private static BufferedImage filtroSharpen(BufferedImage imagen) {
        float[] matrizSharpen = {
            -1.0f, -1.0f, -1.0f,
            -1.0f,  9.0f, -1.0f,
            -1.0f, -1.0f, -1.0f
        };
        Kernel kernel = new Kernel(3, 3, matrizSharpen);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage sharpen = op.filter(imagen, null);
        return sharpen;
    }
    private static BufferedImage filtroBlackWhite(BufferedImage imagen) {
        int width = imagen.getWidth();
        int height = imagen.getHeight();
        BufferedImage blackWhite = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = new Color(imagen.getRGB(i,j));
                int rc = color.getRed();
                int gc = color.getGreen();
                int bc = color.getBlue();
                int bw = (rc+gc+bc)/3;
                blackWhite.setRGB(i,j,new Color(bw,bw,bw).getRGB());
            }
        }
        return blackWhite;
    }
    public static String extraeNombre(File archivo, String tipo) {
        String ar = archivo.getName();
        int indicePunto = ar.lastIndexOf('.');
        String nFnl = "";
        if (indicePunto > 0) {
            String no = ar.substring(0, indicePunto);
            String ext = ar.substring(indicePunto + 1);
            nFnl = "/" + no + "_" + tipo + "." + ext;
        }
        return nFnl;
    }
    private static void guardarImagen(File dir1,File archivo,String filtro,BufferedImage imgNew) {
        try {
            File salida = new File(dir1 + extraeNombre(archivo,filtro));
            ImageIO.write(imgNew, "jpg", salida);
        } catch (IOException e) {}
    }
    public static void filtros(){
        File file = new File("src/main/java/Imagenes");
        File directorio = new File(file.getAbsolutePath());
        File[] archivos = directorio.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
        File file2 = new File("src/main/java/imagenes_filtradas");
        File directorio2 = new File(file2.getAbsolutePath());
        ExecutorService service = Executors.newFixedThreadPool(nucleos);
        for (File archivo : archivos) {
            Runnable fill_total = () -> {
                try {
                    BufferedImage imagen = ImageIO.read(archivo);
                    guardarImagen(directorio2, archivo, "Sepia", filtroSepia(imagen));
                    guardarImagen(directorio2, archivo, "Sharpen", filtroSharpen(imagen));
                    guardarImagen(directorio2, archivo, "BlackWhite", filtroBlackWhite(imagen));
                } catch (IOException e) {e.printStackTrace();}
            };
            service.submit(fill_total);
        }
        service.shutdown();
        try {
            while (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Aplicando Filtros...");
            }
            System.out.println("Se aplico filtro a todas las imagenes");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public static void main(String[] args) {
        AbrirArchivo();
        ExecutorService service = Executors.newFixedThreadPool(nucleos);
        for (Imagenes i: myCollection){
            int indice = i.getIdentificador();
            String enlace = i.getEnlace();
            Runnable tarea = () -> {
                descargaImagen(enlace, indice);
            };
            service.submit(tarea);
        }
        service.shutdown();
        try {
            while (!service.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println("Descargando...");
            }
            System.out.println("Se han descargado todas las imagenes");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        filtros();
        System.out.println("Aplicando Filtros...");
    }
}
