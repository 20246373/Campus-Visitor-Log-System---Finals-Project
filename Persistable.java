import java.io.IOException;

interface Persistable {
    void saveToFile() throws IOException;
    void loadFromFile() throws IOException;
}
