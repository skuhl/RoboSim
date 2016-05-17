/**
 * A dynamic scrollbar object that allows for the scrolling of a text area, whose contents are stored in 2D ArrayList.
 *
 * TODO: add textarea data (x, y, length, height)
 *
 * 12 April 2016
 */
public class Scrollbar {
  // The position of the top-left corner of the scroll bar
  public float pos_x, pos_y,
               // The length and height of the scrolbar
               s_len, s_height;
  // The current position of the slider on the scroll bar
  private float slider_pos_y;
  // If the scroll bar is selected by the mouse
  public boolean focus;
  // The normal coloring of the scroll bar
  private color n_outline, n_fill,
  // The color of the scroll bar when selected by the mouse
                h_outline, h_fill;
  
  /* Creates a scroll bar */
  public Scrollbar() {
    pos_x = 0;
    pos_y = 0;
    slider_pos_y = pos_y;
    s_len = 5;
    s_height = 25;
    
    focus = false;
    // default colors
    n_outline = color(0, 0, 0);
    n_fill = color(255, 255, 255);
    h_outline = color(255, 255, 255);
    h_fill = color(0, 0, 0);
  }
  
  /* Set the position of the scroll bar's top-left corner */
  public void setPosition(float x, float y) {
    pos_x = x;
    pos_y = y;
    // Keep the slider within the bounds of the scroll bar
    slider_pos_y = max( pos_y, min( (pos_y + s_height - 1), slider_pos_y ) );
  }
  
  /* Define the scroll bar's length and height */
  public void setDim(float len, float hgt) {
    s_len = len;
    s_height = hgt;
    // Keep the slider within the bounds of the scroll bar
    slider_pos_y = max( pos_y, min( (pos_y + s_height - 1), slider_pos_y ) );
  }
  
  /**
   * Define the color scheme for the scroll bar's normal coloring and
   * highlighted coloring.
   *
   * @param n_o  the normal outline color of the scroll bar
   * @param n_f  the normal fill color of the scroll bar
   * @param h_o  the highlighted outline color of the scroll bar
   * @param h_F  the highlighted fill color of the scroll bar
   */
  public void setColorScheme(color n_o, color n_f, color h_o, color h_f) {
    // normal
    n_outline = n_o;
    n_fill = n_f;
    // highlighted
    h_outline = h_o;
    h_fill = h_f;
  }
  
  /* Update the current position of the scroll bar's slider. */
  public void update() {
    if (focus) {
      
      slider_pos_y = mouseY;
      // Keep the slider within the bounds of the scroll bar
      slider_pos_y = max( pos_y, min( (pos_y + s_height - 1), slider_pos_y ) );
      
      updateScreen(color(255,0,0), color(0,0,0));
    }
  }
  
  public void checkMousePosition(int mx, int my) {
    focus = mouseX >= pos_x && mouseX <= (pos_x + s_len) && mouseY >= slider_pos_y && mouseY <= (slider_pos_y + s_len);
  }
  
  /* Draw the scrollbar on the screen */
  public void draw() {
    // Draw midline of the scroll bar
    stroke(n_outline);
    line(pos_x + (s_len / 2), pos_y + (s_len / 2), pos_x + (s_len / 2), pos_y + s_height);
    // Draw slider
    stroke( (focus) ? h_outline : n_outline );
    fill( (focus) ? h_fill : n_fill );
    rect(pos_x, slider_pos_y, s_len, s_len);
  }
  
  /* Given the size of the list of contents to display and the maximum
   * number of lines that can appear on the screen at one time, compute
   * the index of the element that should appear at the top of the
   * window.
   * 
   * @param size         the total number of line elements for the
   *                     given window
   * @param display-cap  the maximum number of elements that can
   *                     appear on the screen at one time
   * @return             The index of the first element to display on
   *                     the screen
   */
  public int slider_ratio(int size, int display_cap) {
    if (size < display_cap) {
      // Number of elements display is less than the display cap
      return 0;
    } else {
      return (int)( ((slider_pos_y - pos_y) / (s_height - 1)) * (size - display_cap) );
    }
  }
  
  /* Increment the position of the slider based on a float value: utilized by
   * the mouse's scroll wheel.
   *
   * @param value  the amount to add to the slider's current position on the
   *               scroll bar
   */
  public void increment_slider(float value) {
    slider_pos_y += value;
    // Keep the slider within the bounds of the scroll bar
    slider_pos_y = max( pos_y, min( (pos_y + s_height - 1), slider_pos_y ) );
  }
}