package iVenue.ui;

import iVenue.models.Customer;
import iVenue.models.Venue;
import iVenue.services.VenueAdmin;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public class ExplorePanel extends VBox {

    private final HBox venueCarousel = new HBox(40);
    private int currentIndex = 0;
    private List<Venue> venues;
    private final Customer customer;
    private Consumer<Venue> onVenueSelected;

    private final double CARD_WIDTH = 350;
    private final double CARD_HEIGHT = 250;
    private final double CENTER_SCALE = 1.0;
    private final double SIDE_SCALE = 0.8;

    public ExplorePanel(Customer customer, Consumer<Venue> onVenueSelected) {
        this.customer = customer;
        this.onVenueSelected = onVenueSelected;

        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.CENTER);

        Label title = new Label("Explore Venues");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold;");
        this.getChildren().add(title);

        VenueAdmin venueAdmin = new VenueAdmin();
        venues = venueAdmin.getAll();

        updateCarousel();

        // Carousel arrows
        Button prev = new Button("<");
        Button next = new Button(">");

        prev.setOnAction(e -> slideCarousel(-1));
        next.setOnAction(e -> slideCarousel(1));

        HBox controls = new HBox(20, prev, next);
        controls.setAlignment(Pos.CENTER);
        this.getChildren().addAll(venueCarousel, controls);

        // Auto-slide
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> slideCarousel(1)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void slideCarousel(int direction) {
        if (venues.isEmpty()) return;
        currentIndex = (currentIndex + direction + venues.size()) % venues.size();
        animateCarousel();
    }

    private void animateCarousel() {
        venueCarousel.getChildren().clear();
        for (int i = -1; i <= 1; i++) {
            int index = (currentIndex + i + venues.size()) % venues.size();
            Venue v = venues.get(index);
            boolean isCenter = i == 0;
            VBox card = createVenueCard(v, isCenter);
            venueCarousel.getChildren().add(card);
        }
        venueCarousel.setAlignment(Pos.CENTER);

        for (int i = 0; i < venueCarousel.getChildren().size(); i++) {
            VBox card = (VBox) venueCarousel.getChildren().get(i);
            double targetScale = (i == 1) ? CENTER_SCALE : SIDE_SCALE;
            ScaleTransition st = new ScaleTransition(Duration.millis(400), card);
            st.setToX(targetScale);
            st.setToY(targetScale);
            st.play();
        }
    }

    private VBox createVenueCard(Venue v, boolean isCenter) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setStyle("""
                -fx-border-color: #B0B0B0;
                -fx-border-width: 1;
                -fx-background-color: #F8F8F8;
                -fx-background-radius: 8;
                """);

        ImageView imageView = new ImageView(new Image("https://via.placeholder.com/400x250"));
        imageView.setFitWidth(CARD_WIDTH - 40);
        imageView.setFitHeight(CARD_HEIGHT - 80);

        Label name = new Label(v.getName());
        name.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label price = new Label(v.isFree() ? "FREE" : "₱" + v.getPrice());
        price.setStyle("-fx-font-size:14px;");

        card.getChildren().addAll(imageView, name, price);

        // Call the external callback instead of referencing a local variable
        card.setOnMouseClicked(e -> {
            if (onVenueSelected != null) {
                onVenueSelected.accept(v);
            }
        });

        double initialScale = isCenter ? CENTER_SCALE : SIDE_SCALE;
        card.setScaleX(initialScale);
        card.setScaleY(initialScale);

        return card;
    }

    private void updateCarousel() {
        animateCarousel();
    }
}
