import javafx.scene.control.Button;
import java.io.Serializable;

public class StandardSeat extends Button implements Serializable {
    public static final int WIDTH = 30;
    public static final int HEIGHT = 25;
    public static final String FREE = "-fx-background-color: #2E58CD";
    public static final String RESERVED = "-fx-background-color: #a1a1a1";

    private static int price = 120;

    private final int column;
    private final int row;

    private boolean isFree;

    public StandardSeat(int column, int row) {
        setMinSize(StandardSeat.WIDTH, StandardSeat.HEIGHT);

        this.column = column;
        this.row = row;

        isFree = true;
        setStyle(FREE);
    }

    public static int getPrice() {
        return price;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public boolean isFree() {
        return isFree;
    }

    public static void setPrice(int price) {
        StandardSeat.price = price;
    }

    public void changeState() {
        isFree = !isFree;

        if (!isFree) {
            setStyle(RESERVED);
        } else {
            setStyle(FREE);
        }
    }
}