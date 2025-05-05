import java.util.ArrayList;
import java.util.Iterator;
public class MyCollection implements Iterable<Imagenes>{
    private ArrayList<Imagenes> imagenes;
    public MyCollection() {
        imagenes = new ArrayList<> ();
    }
    public void add (Imagenes imagen) {
        imagenes.add(imagen);
    }
    public boolean remove (Imagenes imagen) {
        return imagenes.remove(imagen);
    }
    public Imagenes get (int index) {
        return imagenes.get (index);
    }
    public int size () {
       return imagenes.size();
    }

    @Override
    public Iterator<Imagenes> iterator() {
        return new Iterator<Imagenes> () {
            private int position = -1;
            @Override
            public boolean hasNext () {
                return position < imagenes.size () - 1;
            }
            @Override
            public Imagenes next () {
                position++;
                return imagenes.get (position);
            }
        };
    }
}