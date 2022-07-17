import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TicketBookingApp extends Application {
    private Stage window;
    private Scene scene;
    private BorderPane root;
    private MenuBar menuBar;
    private Menu file;
    private Menu edit;
    private MenuItem save;
    private MenuItem editingMode;
    private BorderPane hall;
    private GridPane information;
    private StackPane placeForScreen;
    private GridPane gridForStandardSeats;
    private GridPane gridForBackRowSeats;
    private StandardSeat[] standardSeats;
    private BackRowSeat[] backRowSeats;
    private List<Button> selectedButtons = new ArrayList<>();

    private int currentSum = 0;
    
    private final Font font = new Font(null, 22);
    private final Color color = Color.web("#FFFFFF");

    @Override
    public void start(Stage window) {
        root = new BorderPane();
        root.setId("root");

        menuBar = new MenuBar();
        root.setTop(menuBar);

        file = new Menu();
        file.setText("File");
        menuBar.getMenus().add(file);

        edit = new Menu();
        edit.setText("Edit");
        menuBar.getMenus().add(edit);

        save = new MenuItem();
        save.setText("Save");
        save.setOnAction(this::saveSeats);
        file.getItems().add(save);

        editingMode = new MenuItem();
        editingMode.setText("Editing mode");
        editingMode.setOnAction(e -> enterEditingMode());
        edit.getItems().add(editingMode);

        information = new GridPane();
        information.setId("information");
        information.setPrefWidth(250);
        information.setAlignment(Pos.CENTER);
        information.setVgap(30);
        root.setLeft(information);

        hall = new BorderPane();
        hall.setPrefWidth(750);
        root.setCenter(hall);

        placeForScreen = new StackPane();
        placeForScreen.setPrefHeight(140);
        placeForScreen.getChildren().add(new Screen());
        hall.setTop(placeForScreen);

        gridForStandardSeats = new GridPane();
        gridForStandardSeats.setAlignment(Pos.CENTER);
        gridForStandardSeats.setHgap(8);
        gridForStandardSeats.setVgap(15);
        hall.setCenter(gridForStandardSeats);

        gridForBackRowSeats = new GridPane();
        gridForBackRowSeats.setAlignment(Pos.CENTER);
        gridForBackRowSeats.setPrefHeight(195);
        gridForBackRowSeats.setHgap(8);
        gridForBackRowSeats.setVgap(15);
        hall.setBottom(gridForBackRowSeats);

        loadSeats();
        loadPrices();
        setDefaultSettingsToSeats();
        displaySeatStates();

        scene = new Scene(root);
        scene.getStylesheets().add("Styles.css");

        this.window = window;
        window.setOnCloseRequest(e -> {
            e.consume();
            close();
        });
        window.setScene(scene);
        window.setTitle("Ticket booking");
        window.getIcons().add(new Image
                (TicketBookingApp.class.getResourceAsStream("TicketBookingAppIcon.png")));
        window.show();
        window.setMinWidth(window.getWidth());
        window.setMinHeight(window.getHeight());
    }

    private void loadSeats() {
        try (FileInputStream fis = new FileInputStream("src/Standard seats save.bin");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            standardSeats = (StandardSeat[]) ois.readObject();

        } catch (FileNotFoundException fnfe) {
            System.out.println("Your file wasn't found.");
        } catch (IOException ioe) {
            System.out.println("I/o problems occurred.");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Class wasn't found.");
        }

        try (FileInputStream fis = new FileInputStream("src/Back row seats save.bin");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            backRowSeats = (BackRowSeat[]) ois.readObject();

        } catch (FileNotFoundException fnfe) {
            System.out.println("Your file wasn't found.");
        } catch (IOException ioe) {
            System.out.println("I/o problems occurred.");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Class wasn't found.");
        }
    }

    private void saveSeats(ActionEvent e) {
        MenuItem source = (MenuItem) e.getSource();
        source.setDisable(true);

        Thread standardSeatsSaver = new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream("src/Standard seats save.bin");
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                oos.writeObject(standardSeats);

            } catch (FileNotFoundException fnfe) {
                System.out.println("Your file wasn't found.");
            } catch (IOException ioe) {
                System.out.println("I/o problems occurred.");
            }
        });

        Thread backRowSeatsSaver = new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream("src/Back row seats save.bin");
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                oos.writeObject(backRowSeats);

            } catch (FileNotFoundException fnfe) {
                System.out.println("Your file wasn't found.");
            } catch (IOException ioe) {
                System.out.println("I/o problems occurred.");
            }
        });

        standardSeatsSaver.start();
        backRowSeatsSaver.start();

        try {
            standardSeatsSaver.join();
            backRowSeatsSaver.join();
        } catch (InterruptedException ie) {
            System.out.println("Interrupted exception occurred.");
        }

        source.setDisable(false);
    }

    private void loadPrices() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Prices.txt"))) {

            int standardSeatPrice = Integer.parseInt(br.readLine());
            int backRowSeatPrice = Integer.parseInt(br.readLine());

            StandardSeat.setPrice(standardSeatPrice);
            BackRowSeat.setPrice(backRowSeatPrice);

        } catch (FileNotFoundException fnfe) {
            System.out.println("Your file wasn't found.");
        } catch (IOException ioe) {
            System.out.println("I/o problems occurred.");
        }
    }

    private void savePrices(String standardSeatPrice, String backRowSeatPrice) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/Prices.txt"))) {

            bw.write(standardSeatPrice);
            bw.write("\n");
            bw.write(backRowSeatPrice);

        } catch (FileNotFoundException fnfe) {
            System.out.println("Your file wasn't found.");
        } catch (IOException ioe) {
            System.out.println("I/o problems occurred.");
        }
    }

    private void close() {
        TextInputDialog passwordInputDialog = new TextInputDialog();
        passwordInputDialog.setTitle("Password check");
        passwordInputDialog.setHeaderText("Enter the password to exit:");

        Optional<String> input = passwordInputDialog.showAndWait();

        input.ifPresent(password -> {
            if (input.get().equals("pukkk")) {
                window.close();
            } else {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Ooops, something went wrong");
                alert.setHeaderText("Incorrect password");
                alert.showAndWait();
            }
        });
    }

    private void displaySeatStates() {
        information.getChildren().removeAll(information.getChildren());

        Rectangle freeSeat = new Rectangle();
        freeSeat.setWidth(30);
        freeSeat.setHeight(25);
        freeSeat.setArcWidth(10);
        freeSeat.setArcHeight(10);
        freeSeat.setStyle("-fx-fill: #2E58CD");

        Rectangle reservedSeat = new Rectangle();
        reservedSeat.setWidth(30);
        reservedSeat.setHeight(25);
        reservedSeat.setArcWidth(10);
        reservedSeat.setArcHeight(10);
        reservedSeat.setStyle("-fx-fill: #A1A1A1");

        Label forFreeSeat = new Label();
        forFreeSeat.setText(" - is free");
        forFreeSeat.setTextFill(color);
        forFreeSeat.setFont(font);

        Label forReservedSeat = new Label();
        forReservedSeat.setText(" - is reserved");
        forReservedSeat.setTextFill(color);
        forReservedSeat.setFont(font);

        information.addRow(0, freeSeat, forFreeSeat);
        information.addRow(1, reservedSeat, forReservedSeat);
    }

    private void setDefaultSettingsToSeats() {
        for (StandardSeat ss : standardSeats) {
            ss.setMinSize(StandardSeat.WIDTH, StandardSeat.HEIGHT);

            ss.setText(String.valueOf(ss.getColumn() + 1));

            ss.setOnAction(event -> {
                displaySeatInfo(ss, true, ss.getRow() + 1,
                        ss.getColumn() + 1, StandardSeat.getPrice());
            });

            if (!ss.isFree()) {
                ss.setStyle(StandardSeat.RESERVED);
                ss.setDisable(true);
            } else {
                ss.setStyle(StandardSeat.FREE);
            }

            ss.getStyleClass().add("seat");

            gridForStandardSeats.add(ss, ss.getColumn(), ss.getRow());
        }

        for (BackRowSeat brs : backRowSeats) {
            brs.setMinSize(BackRowSeat.WIDTH, BackRowSeat.HEIGHT);

            brs.setText(String.valueOf(brs.getColumn() + 1));

            brs.setOnAction(e -> displaySeatInfo(brs, false, brs.getRow() + 7,
                                                 brs.getColumn() + 1, BackRowSeat.getPrice()));

            if (!brs.isFree()) {
                brs.setStyle(BackRowSeat.RESERVED);
                brs.setDisable(true);
            } else {
                brs.setStyle(BackRowSeat.FREE);
            }

            brs.getStyleClass().add("seat");

            gridForBackRowSeats.add(brs, brs.getColumn(), brs.getRow());
        }
    }

    private void displaySeatInfo(Button seat, boolean isStandard, int row, int seatNumber, int price) {
        information.getChildren().removeAll(information.getChildren());

        Label forRow = new Label();
        forRow.setText("Row number: " + row);
        forRow.setTextFill(color);
        forRow.setFont(font);

        Label forSeatNumber = new Label();
        forSeatNumber.setText("Seat number: " + seatNumber);
        forSeatNumber.setTextFill(color);
        forSeatNumber.setFont(font);

        Label forPrice = new Label();
        forPrice.setText("Price: " + price);
        forPrice.setTextFill(color);
        forPrice.setFont(font);

        Button reserve = new Button();
        reserve.setText("Reserve");
        reserve.setTextFill(color);
        reserve.setFont(font);
        reserve.setStyle("-fx-background-color: #2E58CD");
        reserve.setOnAction(e -> {
            if (isStandard) {
                StandardSeat source = (StandardSeat) seat;
                source.changeState();
                source.setDisable(true);
            } else {
                BackRowSeat source = (BackRowSeat) seat;
                source.changeState();
                source.setDisable(true);
            }

            displaySeatStates();
        });

        Button cancel = new Button();
        cancel.setText("Cancel");
        cancel.setTextFill(color);
        cancel.setFont(font);
        cancel.setStyle("-fx-background-color: #2E58CD");
        cancel.setOnAction(e -> displaySeatStates());

        information.addColumn(0, forRow, forSeatNumber, forPrice, reserve, cancel);
    }

    private void enterEditingMode() {
        TextInputDialog passwordInputDialog = new TextInputDialog();
        passwordInputDialog.setTitle("Password check");
        passwordInputDialog.setHeaderText("Enter the password to enable editing mode:");

        Optional<String> input = passwordInputDialog.showAndWait();

        input.ifPresent(password -> {
            if (input.get().equals("pukkk")) {
                for (StandardSeat ss : standardSeats) {
                    ss.setDisable(false);
                    ss.setOnAction(e -> ss.changeState());
                }

                for (BackRowSeat brs : backRowSeats) {
                    brs.setDisable(false);
                    brs.setOnAction(e -> brs.changeState());
                }

                information.getChildren().removeAll(information.getChildren());

                Label forStandardSeatPrice = new Label();
                forStandardSeatPrice.setText("Standard seat price:");
                forStandardSeatPrice.setTextFill(color);
                forStandardSeatPrice.setFont(font);

                TextField standardSeatPrice = new TextField();
                standardSeatPrice.setId("standardSeatPrice");
                standardSeatPrice.setMaxSize(100, 30);
                standardSeatPrice.setText(String.valueOf(StandardSeat.getPrice()));
                standardSeatPrice.setFont(font);

                Label forBackRowSeatPrice = new Label();
                forBackRowSeatPrice.setText("Back row seat price:");
                forBackRowSeatPrice.setTextFill(color);
                forBackRowSeatPrice.setFont(font);

                TextField backRowSeatPrice = new TextField();
                backRowSeatPrice.setId("backRowSeatPrice");
                backRowSeatPrice.setMaxSize(100, 30);
                backRowSeatPrice.setText(String.valueOf(BackRowSeat.getPrice()));
                backRowSeatPrice.setFont(font);

                Button finishEditing = new Button();
                finishEditing.setText("Finish editing!");
                finishEditing.setTextFill(color);
                finishEditing.setFont(font);
                finishEditing.setStyle("-fx-background-color: #2E58CD;");
                finishEditing.setOnAction(e -> {
                    if (!standardSeatPrice.getText().matches("^\\d+$") ||
                        !backRowSeatPrice.getText().matches("^\\d+$")) {

                        Alert alert = new Alert(AlertType.WARNING);
                        alert.setHeaderText("Price must be a number");
                        alert.showAndWait();
                    } else {
                        for (StandardSeat ss : standardSeats) {
                            if (!ss.isFree()) {
                                ss.setDisable(true);
                            }

                            ss.setOnAction(event -> displaySeatInfo(ss, true, ss.getRow() + 1,
                                           ss.getColumn() + 1, StandardSeat.getPrice()));
                        }

                        for (BackRowSeat brs : backRowSeats) {
                            if (!brs.isFree()) {
                                brs.setDisable(true);
                            }

                            brs.setOnAction(event -> displaySeatInfo(brs, false, brs.getRow() + 7,
                                            brs.getColumn() + 1, BackRowSeat.getPrice()));
                        }

                        savePrices(standardSeatPrice.getText(), backRowSeatPrice.getText());
                        loadPrices();
                        displaySeatStates();
                    }
                });

                information.addColumn(0,
                                      forStandardSeatPrice,
                                      standardSeatPrice,
                                      forBackRowSeatPrice,
                                      backRowSeatPrice,
                                      finishEditing);
            } else {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setHeaderText("Incorrect password");
                alert.showAndWait();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}