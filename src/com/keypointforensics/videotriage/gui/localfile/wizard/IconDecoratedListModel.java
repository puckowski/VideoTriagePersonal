package com.keypointforensics.videotriage.gui.localfile.wizard;

import javax.swing.DefaultListModel;

public class IconDecoratedListModel<T> extends DefaultListModel<T> {
    public void update(int index) {
        fireContentsChanged(this, index, index);
    }
}