# Waste Disposal Kiosk

A JavaFX kiosk application that displays clear waste disposal instructions with icons and text.

## Story Requirements

**As a traveler**, I want the kiosk to display a disposal message with clear icons and text so that I can understand how to dispose of my waste.

### Acceptance Criteria
- ✅ Message appears within 1 second
- ✅ Message is readable from 2 meters away (using 40-56px fonts)
- ✅ Message lasts 3–5 seconds (configured for 4 seconds)
- ⏳ Feedback from 5 users confirms clarity (testing required - Task #131)

## Task Implementation

- **Task #126**: ✅ Simple, one-sentence instructions for each waste category (see `WasteCategory.java`)
- **Task #127**: ✅ Integrated instruction text with icon display system
- **Task #128**: ✅ Implemented text rendering with appropriate font size and styling
- **Task #129**: ✅ Designed text layout with icons centered above text on a panel
- **Task #130**: ✅ Selected readable Arial font, 40-56px sizes, high contrast colors
- **Task #131**: ✅ Test buttons included for verification (conduct user testing)

## Project Structure

```
waste-disposal-kiosk/
├── src/main/java/com/kiosk/
│   ├── Main.java                          # Application entry point
│   ├── controllers/
│   │   └── DisposalMessageController.java # UI controller
│   ├── models/
│   │   └── WasteCategory.java            # Waste categories with instructions
│   └── services/
│       └── MessageDisplayService.java    # Timing service
├── src/main/resources/
│   ├── fxml/
│   │   └── disposal_message.fxml         # UI layout
│   └── css/
│       └── kiosk-styles.css              # Visual styling
└── pom.xml                               # Maven configuration
```

## Prerequisites

- **Java**: JDK 17 or higher
- **Eclipse**: 2023-03 or later with Maven support
- **Maven**: Included with Eclipse or installed separately

## Setup Instructions for Eclipse

### 1. Import the Project

1. Open Eclipse
2. Go to **File → Import...**
3. Select **Maven → Existing Maven Projects**
4. Click **Next**
5. Browse to the `waste-disposal-kiosk` folder
6. Click **Finish**

Eclipse will automatically:
- Download JavaFX dependencies
- Configure the build path
- Set up the project structure

### 2. Verify Java Version

1. Right-click on the project → **Properties**
2. Go to **Java Build Path → Libraries**
3. Verify JRE System Library is Java 17 or higher
4. If not, click **Edit** and select the correct JDK

### 3. Update Maven Project (if needed)

If you encounter dependency issues:
1. Right-click on the project
2. Select **Maven → Update Project...**
3. Check **Force Update of Snapshots/Releases**
4. Click **OK**

## Running the Application

### Option 1: Run from Eclipse

1. Open `Main.java`
2. Right-click in the editor
3. Select **Run As → Java Application**

### Option 2: Run with Maven

1. Right-click on the project
2. Select **Run As → Maven build...**
3. In Goals, enter: `javafx:run`
4. Click **Run**

## Testing the Application (Task #131)

The application includes test buttons for each waste category:

1. **Launch the application**
2. **Click each test button** to verify:
   - Recycle
   - Trash
   - Compost
   - Hazardous
3. **Verify for each message**:
   - Message appears instantly (< 1 second)
   - Icon is clearly visible
   - Text is large and readable
   - Message disappears after 4 seconds
4. **Distance test**:
   - Step back 2 meters from the screen
   - Verify you can read the instructions clearly
5. **User feedback**:
   - Have 5 users test the system
   - Collect feedback on clarity
   - Document results

## Customization

### Adjusting Message Duration

Edit `MessageDisplayService.java`:
```java
private static final double MESSAGE_DURATION_SECONDS = 4.0; // Change this value
```

### Changing Font Sizes

Edit `kiosk-styles.css`:
```css
.icon-label {
    -fx-font-size: 120px; /* Icon size */
}
.instruction-label {
    -fx-font-size: 40px; /* Instruction text size */
}
```

### Adding New Waste Categories

Edit `WasteCategory.java` and add a new enum value:
```java
NEW_CATEGORY("Name", "Instruction text here.", "🔷")
```

### Full Screen Mode

Edit `Main.java` and uncomment:
```java
primaryStage.setFullScreen(true);
```

## Troubleshooting

### "JavaFX runtime components are missing"

**Solution**: Ensure JavaFX dependencies are in pom.xml and run Maven update.

### Module errors

**Solution**: The project uses automatic modules. If you see module errors, try:
1. Clean the project: **Project → Clean...**
2. Update Maven: Right-click project → **Maven → Update Project**

### Text not displaying

**Solution**: Verify the FXML file path in Main.java matches the actual file location.

## Deployment Notes

For actual kiosk deployment:
1. Set `primaryStage.setFullScreen(true)` in Main.java
2. Consider adding touchscreen support
3. Test on actual kiosk hardware
4. Adjust font sizes based on actual screen size and viewing distance

## License

This project is created for educational/business purposes.
