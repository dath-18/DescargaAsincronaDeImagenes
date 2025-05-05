public class Imagenes {
    private int identificador;
    private String enlace;
    public Imagenes(String enlace, int identificador) {
        this.enlace = enlace;
        this.identificador = identificador;
    }
    public int getIdentificador() {
        return identificador;
    }
    public String getEnlace() {
        return enlace;
    }
    @Override
    public String toString() {
        return "Imagenes{" + "identificador=" + identificador + ", enlace=" + enlace + '}';
    }
}
