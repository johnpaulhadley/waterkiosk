# Quick Start Guide for Eclipse

## Import Steps (5 minutes)

1. **Open Eclipse**
   - Launch Eclipse IDE

2. **Import Project**
   - File → Import...
   - Select "Maven" → "Existing Maven Projects"
   - Click Next
   - Browse to the `waste-disposal-kiosk` folder
   - Click Finish

3. **Wait for Setup**
   - Eclipse will automatically download JavaFX dependencies
   - This may take 2-3 minutes on first import
   - Watch the progress bar in the bottom right

4. **Run the Application**
   - Open `src/main/java/com/kiosk/Main.java`
   - Right-click in the editor
   - Select "Run As" → "Java Application"

## What You'll See

- A window with four test buttons at the bottom
- Click any button (Recycle, Trash, Compost, Hazardous)
- A message will appear with:
  - Large icon at top
  - Category name
  - Clear instruction text
- Message automatically disappears after 4 seconds

## Testing Checklist (Task #131)

For each waste category, verify:
- [ ] Message appears instantly (< 1 second)
- [ ] Icon is clearly visible and matches category
- [ ] Text is readable from your seat
- [ ] Message lasts approximately 4 seconds
- [ ] Message automatically disappears

**Distance Test:**
- [ ] Stand 2 meters (6.5 feet) from screen
- [ ] Can you still read the instructions clearly?
- [ ] Adjust font sizes in CSS if needed

**User Testing:**
- [ ] Test with 5 different users
- [ ] Ask: "Is the message clear?"
- [ ] Ask: "Do you understand what to do?"
- [ ] Document feedback

## Common Issues

**"JavaFX not found"**
→ Right-click project → Maven → Update Project → Force Update

**"Module errors"**
→ Project → Clean → Clean all projects

**"Can't run Main.java"**
→ Verify JDK 17 is configured in Build Path

## Next Steps

- Review the full README.md for customization options
- Modify text in WasteCategory.java
- Adjust styling in kiosk-styles.css
- Test with actual users for Task #131
