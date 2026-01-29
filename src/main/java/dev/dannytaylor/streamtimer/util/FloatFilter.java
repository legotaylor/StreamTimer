/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.util;

import javax.swing.text.*;

public class FloatFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        String newText = fb.getDocument().getText(0, fb.getDocument().getLength());
        newText = newText.substring(0, offset) + string + newText.substring(offset);
        if (isValidFloat(newText)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String newText = fb.getDocument().getText(0, fb.getDocument().getLength());
        newText = newText.substring(0, offset) + text + newText.substring(offset + length);
        if (isValidFloat(newText)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        String newText = fb.getDocument().getText(0, fb.getDocument().getLength());
        newText = newText.substring(0, offset) + newText.substring(offset + length);
        if (isValidFloat(newText)) {
            super.remove(fb, offset, length);
        }
    }

    private boolean isValidFloat(String text) {
        if (text.isEmpty() || text.equals("-") || text.equals(".")) return true; // allow typing in progress
        try {
            Float.parseFloat(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
