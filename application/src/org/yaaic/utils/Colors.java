package org.yaaic.utils;


public class Colors {
    /*
     * Colors from the "Classic" theme in mIRC.
     */
    public static final String[] colors = {
        "#FFFFFF",  // White
        "#000000",  // Black
        "#00007F",  // Blue (navy)
        "#009300",  // Green
        "#FC0000",  // Red
        "#7F0000",  // Brown (maroon)
        "#9C009C",  // Purple
        "#FC7F00",  // Orange (olive)
        "#FFFF00",  // Yellow
        "#00FC00",  // Light Green (lime)
        "#008080",  // Teal (a green/blue cyan)
        "#00FFFF",  // Light Cyan (cyan) (aqua)
        "#0000FF",  // Light Blue (royal)
        "#FF00FF",  // Pink (light purple) (fuchsia)
        "#7F7F7F",  // Grey
        "#D2D2D2"   // Light Grey (silver)
    };
    public Colors() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Converts a string with mIRC color codes to a HTML string.
     * 
     * @param text  A string with mIRC color codes.
     * @return      HTML string.
     */
    public static String mircColorParser(String text) {
        return text;
    }
}